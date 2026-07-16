package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.data.github.GitHubApiClient
import com.rdev.rrepodocs.data.github.GitHubApiException
import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepoTreeNodeKind
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

class GitHubRepositoryServiceImpl(
    private val gitHubApiClient: GitHubApiClient,
) : GitHubRepositoryService {
    private val mockMarkdownContentOverrides = mutableMapOf<String, String>()
    private val mockRemovedPaths = mutableSetOf<String>()

    override suspend fun loadAccessibleRepositories(session: UserSession): Result<List<RepositoryRef>> {
        if (session.accessToken.startsWith("mock-")) {
            return Result.success(mockRepositories())
        }

        val rawResponse = gitHubApiClient.getAccessibleRepositories(session.accessToken)
        return rawResponse.fold(
            onSuccess = { body -> parseRepositories(body) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun loadRepositoryTree(
        session: UserSession,
        repository: RepositoryRef,
    ): Result<List<RepoTreeNode>> {
        if (session.accessToken.startsWith("mock-")) {
            return Result.success(mockTree())
        }

        return loadDefaultBranch(session, repository).fold(
            onSuccess = { defaultBranch ->
                gitHubApiClient.getRepositoryTree(
                    accessToken = session.accessToken,
                    ownerLogin = repository.ownerLogin,
                    repositoryName = repository.name,
                    reference = defaultBranch,
                    recursive = true,
                ).fold(
                    onSuccess = ::parseRepositoryTree,
                    onFailure = { error -> Result.failure(error) },
                )
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun loadMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
    ): Result<MarkdownDocument> {
        if (session.accessToken.startsWith("mock-")) {
            val content = mockMarkdownContentOverrides[path.value] ?: mockDocumentContent(path.value)
            return Result.success(
                MarkdownDocument(
                    path = path,
                    content = content,
                )
            )
        }

        return loadDefaultBranch(session, repository).fold(
            onSuccess = { defaultBranch ->
                gitHubApiClient.getRepositoryContent(
                    accessToken = session.accessToken,
                    ownerLogin = repository.ownerLogin,
                    repositoryName = repository.name,
                    path = path.value,
                    reference = defaultBranch,
                ).fold(
                    onSuccess = { body -> parseMarkdownDocument(body, path) },
                    onFailure = { error -> Result.failure(error) },
                )
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun loadDocumentHistory(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
    ): Result<List<DocumentHistoryEntry>> {
        if (session.accessToken.startsWith("mock-")) {
            return Result.success(mockDocumentHistory(path.value))
        }

        return gitHubApiClient.getRepositoryCommits(
            accessToken = session.accessToken,
            ownerLogin = repository.ownerLogin,
            repositoryName = repository.name,
            path = path.value,
        ).fold(
            onSuccess = ::parseDocumentHistory,
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun saveMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        updatedContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        val normalizedContent = updatedContent.replace("\r\n", "\n")
        if (session.accessToken.startsWith("mock-")) {
            mockMarkdownContentOverrides[document.path.value] = normalizedContent
            return Result.success(
                document.copy(
                    content = normalizedContent,
                    sha = "mock-sha-${normalizedContent.hashCode()}",
                )
            )
        }

        val payload = buildJsonObject {
            put("message", commitMessage)
            put("content", encodeBase64(normalizedContent))
            document.sha?.let { currentSha ->
                put("sha", currentSha)
            }
        }.toString()

        return gitHubApiClient.updateRepositoryContent(
            accessToken = session.accessToken,
            ownerLogin = repository.ownerLogin,
            repositoryName = repository.name,
            path = document.path.value,
            payload = payload,
        ).fold(
            onSuccess = { body ->
                parseSavedDocument(
                    rawJson = body,
                    document = document,
                    normalizedContent = normalizedContent,
                )
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun createMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
        initialContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        val normalizedContent = initialContent.replace("\r\n", "\n")
        if (session.accessToken.startsWith("mock-")) {
            mockRemovedPaths -= path.value
            mockMarkdownContentOverrides[path.value] = normalizedContent
            return Result.success(
                MarkdownDocument(
                    path = path,
                    content = normalizedContent,
                    sha = "mock-sha-${path.value.hashCode()}-${normalizedContent.hashCode()}",
                )
            )
        }

        val payload = buildJsonObject {
            put("message", commitMessage)
            put("content", encodeBase64(normalizedContent))
        }.toString()

        return gitHubApiClient.updateRepositoryContent(
            accessToken = session.accessToken,
            ownerLogin = repository.ownerLogin,
            repositoryName = repository.name,
            path = path.value,
            payload = payload,
        ).fold(
            onSuccess = { body ->
                parseCreatedDocument(
                    rawJson = body,
                    path = path,
                    normalizedContent = normalizedContent,
                )
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun createFolder(
        session: UserSession,
        repository: RepositoryRef,
        folderPath: String,
        commitMessage: String,
    ): Result<Unit> {
        val normalizedFolder = folderPath.trim().trim('/')
        if (normalizedFolder.isBlank()) {
            return Result.failure(GitHubApiException("Folder name is required."))
        }

        val placeholderPath = "$normalizedFolder/.gitkeep"
        if (session.accessToken.startsWith("mock-")) {
            mockRemovedPaths -= placeholderPath
            mockMarkdownContentOverrides[placeholderPath] = ""
            return Result.success(Unit)
        }

        val payload = buildJsonObject {
            put("message", commitMessage)
            put("content", encodeBase64(""))
        }.toString()

        return gitHubApiClient.updateRepositoryContent(
            accessToken = session.accessToken,
            ownerLogin = repository.ownerLogin,
            repositoryName = repository.name,
            path = placeholderPath,
            payload = payload,
        ).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun deleteRepositoryItems(
        session: UserSession,
        repository: RepositoryRef,
        paths: List<String>,
        commitMessage: String,
    ): Result<Unit> {
        val normalizedPaths = paths
            .map { it.trim().trim('/') }
            .filter { it.isNotBlank() }
            .distinct()
        if (normalizedPaths.isEmpty()) {
            return Result.failure(GitHubApiException("Select at least one item to delete."))
        }

        if (session.accessToken.startsWith("mock-")) {
            normalizedPaths.forEach { path ->
                mockRemovedPaths += path
                mockMarkdownContentOverrides.remove(path)
            }
            return Result.success(Unit)
        }

        val defaultBranch = loadDefaultBranch(session, repository).getOrElse { error ->
            return Result.failure(error)
        }

        normalizedPaths.forEach { path ->
            val sha = gitHubApiClient.getRepositoryContent(
                accessToken = session.accessToken,
                ownerLogin = repository.ownerLogin,
                repositoryName = repository.name,
                path = path,
                reference = defaultBranch,
            ).fold(
                onSuccess = { body -> parseContentSha(body).getOrElse { return Result.failure(it) } },
                onFailure = { error -> return Result.failure(error) },
            )

            val payload = buildJsonObject {
                put("message", commitMessage)
                put("sha", sha)
            }.toString()

            gitHubApiClient.deleteRepositoryContent(
                accessToken = session.accessToken,
                ownerLogin = repository.ownerLogin,
                repositoryName = repository.name,
                path = path,
                payload = payload,
            ).onFailure { error ->
                return Result.failure(error)
            }
        }

        return Result.success(Unit)
    }

    override suspend fun renameMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        newPath: DocumentPath,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        if (document.path.value == newPath.value) {
            return Result.failure(
                GitHubApiException("Rename target matches current path.")
            )
        }

        val normalizedContent = document.content.replace("\r\n", "\n")
        if (session.accessToken.startsWith("mock-")) {
            mockRemovedPaths += document.path.value
            mockRemovedPaths -= newPath.value
            mockMarkdownContentOverrides.remove(document.path.value)
            mockMarkdownContentOverrides[newPath.value] = normalizedContent
            return Result.success(
                MarkdownDocument(
                    path = newPath,
                    content = normalizedContent,
                    sha = "mock-sha-${newPath.value.hashCode()}-${normalizedContent.hashCode()}",
                )
            )
        }

        val sourceSha = document.sha
            ?: return Result.failure(
                GitHubApiException("Cannot rename ${document.path.value}: missing file SHA.")
            )

        val createPayload = buildJsonObject {
            put("message", commitMessage)
            put("content", encodeBase64(normalizedContent))
        }.toString()

        return gitHubApiClient.updateRepositoryContent(
            accessToken = session.accessToken,
            ownerLogin = repository.ownerLogin,
            repositoryName = repository.name,
            path = newPath.value,
            payload = createPayload,
        ).fold(
            onSuccess = { createBody ->
                parseCreatedDocument(
                    rawJson = createBody,
                    path = newPath,
                    normalizedContent = normalizedContent,
                ).fold(
                    onSuccess = { renamedDocument ->
                        val deletePayload = buildJsonObject {
                            put("message", commitMessage)
                            put("sha", sourceSha)
                        }.toString()
                        gitHubApiClient.deleteRepositoryContent(
                            accessToken = session.accessToken,
                            ownerLogin = repository.ownerLogin,
                            repositoryName = repository.name,
                            path = document.path.value,
                            payload = deletePayload,
                        ).fold(
                            onSuccess = { Result.success(renamedDocument) },
                            onFailure = { error ->
                                Result.failure(
                                    GitHubApiException(
                                        message = "Renamed to ${newPath.value}, but failed deleting ${document.path.value}.",
                                        cause = error,
                                    )
                                )
                            },
                        )
                    },
                    onFailure = { error -> Result.failure(error) },
                )
            },
            onFailure = { error -> Result.failure(error) },
        )
    }

    override suspend fun moveMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        destinationPath: DocumentPath,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        return renameMarkdownDocument(
            session = session,
            repository = repository,
            document = document,
            newPath = destinationPath,
            commitMessage = commitMessage,
        )
    }

    private suspend fun loadDefaultBranch(
        session: UserSession,
        repository: RepositoryRef,
    ): Result<String> {
        return gitHubApiClient
            .getRepositoryDetails(
                accessToken = session.accessToken,
                ownerLogin = repository.ownerLogin,
                repositoryName = repository.name,
            )
            .fold(
                onSuccess = { body -> parseDefaultBranch(body) },
                onFailure = { error -> Result.failure(error) },
            )
    }

    private fun parseRepositories(rawJson: String): Result<List<RepositoryRef>> {
        return try {
            val repositories = Json.parseToJsonElement(rawJson)
                .jsonArray
                .mapNotNull(::toRepositoryRef)
                .sortedBy { it.fullName.lowercase() }
            Result.success(repositories)
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse repository list from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseDefaultBranch(rawJson: String): Result<String> {
        return try {
            val defaultBranch = Json.parseToJsonElement(rawJson)
                .jsonObject
                .string("default_branch")
                ?: "main"
            Result.success(defaultBranch)
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse repository details from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseRepositoryTree(rawJson: String): Result<List<RepoTreeNode>> {
        return try {
            val entries = Json.parseToJsonElement(rawJson)
                .jsonObject
                .array("tree")
                .orEmpty()
            Result.success(buildTreeFromEntries(entries))
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse repository tree from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseMarkdownDocument(
        rawJson: String,
        path: DocumentPath,
    ): Result<MarkdownDocument> {
        return try {
            val contentObject = Json.parseToJsonElement(rawJson).jsonObject
            val type = contentObject.string("type")
            if (type != null && type != "file") {
                return Result.failure(
                    GitHubApiException("Path does not point to a file: ${path.value}")
                )
            }

            val encodedContent = contentObject.string("content")
                ?: return Result.failure(
                    GitHubApiException("GitHub did not return file content for ${path.value}")
                )
            val encoding = contentObject.string("encoding")
            val decodedContent = when (encoding?.lowercase()) {
                "base64" -> decodeBase64(encodedContent)
                null -> encodedContent
                else -> {
                    return Result.failure(
                        GitHubApiException("Unsupported GitHub content encoding: $encoding")
                    )
                }
            }

            Result.success(
                MarkdownDocument(
                    path = path,
                    content = decodedContent.replace("\r\n", "\n"),
                    sha = contentObject.string("sha"),
                )
            )
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse markdown document from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseContentSha(rawJson: String): Result<String> {
        return try {
            val contentObject = Json.parseToJsonElement(rawJson).jsonObject
            val sha = contentObject.string("sha")
                ?: return Result.failure(GitHubApiException("GitHub did not return a file SHA."))
            Result.success(sha)
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse file SHA from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseDocumentHistory(rawJson: String): Result<List<DocumentHistoryEntry>> {
        return try {
            val entries = Json.parseToJsonElement(rawJson)
                .jsonArray
                .mapNotNull { item ->
                    val obj = item as? JsonObject ?: return@mapNotNull null
                    val hash = obj.string("sha") ?: return@mapNotNull null
                    val commitObject = obj.objectField("commit") ?: return@mapNotNull null
                    val message = commitObject
                        .string("message")
                        ?.lineSequence()
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    val authorObject = commitObject.objectField("author")
                    val committerObject = commitObject.objectField("committer")
                    val authorUserObject = obj.objectField("author")
                    val authorAvatarUrl = authorUserObject?.string("avatar_url")
                    val authorName = authorObject?.string("name")
                        ?: committerObject?.string("name")
                        ?: "Unknown"
                    val authoredDate = authorObject?.string("date")
                        ?: committerObject?.string("date")
                        ?: ""
                    DocumentHistoryEntry(
                        hash = hash.take(7),
                        message = if (message.isBlank()) "(No commit message)" else message,
                        authorName = authorName,
                        authoredDate = authoredDate,
                        authorAvatarUrl = authorAvatarUrl,
                    )
                }
            Result.success(entries)
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse document history from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseSavedDocument(
        rawJson: String,
        document: MarkdownDocument,
        normalizedContent: String,
    ): Result<MarkdownDocument> {
        return try {
            val response = Json.parseToJsonElement(rawJson).jsonObject
            val contentObject = response.objectField("content")
            val returnedPath = contentObject?.string("path")
            val returnedSha = contentObject?.string("sha")

            if (returnedPath != null && returnedPath != document.path.value) {
                return Result.failure(
                    GitHubApiException(
                        message = "Saved path mismatch. Expected ${document.path.value}, got $returnedPath",
                    )
                )
            }

            Result.success(
                document.copy(
                    content = normalizedContent,
                    sha = returnedSha ?: document.sha,
                )
            )
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse save response from GitHub",
                    cause = error,
                )
            )
        }
    }

    private fun parseCreatedDocument(
        rawJson: String,
        path: DocumentPath,
        normalizedContent: String,
    ): Result<MarkdownDocument> {
        return try {
            val response = Json.parseToJsonElement(rawJson).jsonObject
            val contentObject = response.objectField("content")
            val returnedPath = contentObject?.string("path")
            val returnedSha = contentObject?.string("sha")

            if (returnedPath != null && returnedPath != path.value) {
                return Result.failure(
                    GitHubApiException(
                        message = "Created path mismatch. Expected ${path.value}, got $returnedPath",
                    )
                )
            }

            Result.success(
                MarkdownDocument(
                    path = path,
                    content = normalizedContent,
                    sha = returnedSha,
                )
            )
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not parse create response from GitHub",
                    cause = error,
                )
            )
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBase64(raw: String): String {
        val sanitized = raw.replace("\n", "")
        return Base64.Default.decode(sanitized).decodeToString()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeBase64(content: String): String {
        return Base64.Default.encode(content.encodeToByteArray())
    }

    private fun buildTreeFromEntries(entries: List<JsonElement>): List<RepoTreeNode> {
        val folders = entries
            .filter { it.jsonObject.string("type") == "tree" }
            .mapNotNull { it.jsonObject.string("path") }
            .sortedBy { pathDepth(it) }
        val files = entries
            .filter { it.jsonObject.string("type") == "blob" }
            .mapNotNull { it.jsonObject.string("path") }
            .sorted()

        val rootNodes = mutableListOf<MutableTreeNode>()
        val foldersByPath = mutableMapOf<String, MutableTreeNode>()

        fun ensureFolder(path: String): MutableTreeNode {
            foldersByPath[path]?.let { return it }

            val folderNode = MutableTreeNode(
                path = path,
                name = path.substringAfterLast('/'),
                kind = RepoTreeNodeKind.Folder,
            )
            foldersByPath[path] = folderNode

            val parentPath = path.substringBeforeLast('/', "")
            if (parentPath.isEmpty()) {
                rootNodes += folderNode
            } else {
                ensureFolder(parentPath).children += folderNode
            }

            return folderNode
        }

        folders.forEach(::ensureFolder)

        files.forEach { filePath ->
            val fileNode = MutableTreeNode(
                path = filePath,
                name = filePath.substringAfterLast('/'),
                kind = RepoTreeNodeKind.File,
                isMarkdownFile = isMarkdownPath(filePath),
            )
            val parentPath = filePath.substringBeforeLast('/', "")
            if (parentPath.isEmpty()) {
                rootNodes += fileNode
            } else {
                ensureFolder(parentPath).children += fileNode
            }
        }

        return rootNodes
            .sortedWith(treeNodeComparator)
            .map { node -> node.toImmutable() }
    }

    private fun MutableTreeNode.toImmutable(): RepoTreeNode {
        val sortedChildren = children
            .sortedWith(treeNodeComparator)
            .map { child -> child.toImmutable() }
        return RepoTreeNode(
            path = path,
            name = name,
            kind = kind,
            isMarkdownFile = isMarkdownFile,
            children = sortedChildren,
        )
    }

    private fun toRepositoryRef(item: JsonElement): RepositoryRef? {
        val obj = item as? JsonObject ?: return null

        val id = obj.long("id") ?: return null
        val name = obj.string("name") ?: return null
        val fullName = obj.string("full_name") ?: return null
        val privateRepo = obj.boolean("private") ?: false
        val owner = obj.objectField("owner")
        val ownerLogin = owner?.string("login") ?: "unknown"
        val ownerAvatarUrl = owner?.string("avatar_url")
        val defaultBranch = obj.string("default_branch") ?: "HEAD"

        return RepositoryRef(
            id = id,
            name = name,
            fullName = fullName,
            ownerLogin = ownerLogin,
            ownerAvatarUrl = ownerAvatarUrl,
            isPrivate = privateRepo,
            defaultBranch = defaultBranch,
            createdAt = obj.string("created_at"),
            updatedAt = obj.string("updated_at"),
            pushedAt = obj.string("pushed_at"),
        )
    }

    private fun mockRepositories(): List<RepositoryRef> {
        return listOf(
            RepositoryRef(
                id = 1,
                name = "rRepoDocs",
                fullName = "demo-user/rRepoDocs",
                ownerLogin = "demo-user",
                ownerAvatarUrl = "https://github.com/demo-user.png",
                isPrivate = true,
                defaultBranch = "main",
                createdAt = "2025-01-10T09:00:00Z",
                updatedAt = "2026-07-15T12:30:00Z",
                pushedAt = "2026-07-15T12:30:00Z",
            ),
            RepositoryRef(
                id = 2,
                name = "personal-notes",
                fullName = "demo-user/personal-notes",
                ownerLogin = "demo-user",
                ownerAvatarUrl = "https://github.com/demo-user.png",
                isPrivate = true,
                defaultBranch = "main",
                createdAt = "2023-09-02T10:15:00Z",
                updatedAt = "2026-07-12T09:45:00Z",
                pushedAt = "2026-07-12T09:45:00Z",
            ),
            RepositoryRef(
                id = 3,
                name = "kitchen-recipes",
                fullName = "demo-user/kitchen-recipes",
                ownerLogin = "demo-user",
                ownerAvatarUrl = "https://github.com/demo-user.png",
                isPrivate = false,
                defaultBranch = "main",
                createdAt = "2024-04-17T08:00:00Z",
                updatedAt = "2026-06-28T16:20:00Z",
                pushedAt = "2026-06-28T16:20:00Z",
            ),
        )
    }

    private fun mockTree(): List<RepoTreeNode> {
        val baseFiles = listOf(
            "docs/README.md",
            "docs/architecture.md",
            "notes/todo.md",
            "notes/diagram.png",
            "CHANGELOG.md",
            "build.gradle.kts",
        )
        val allFiles = (baseFiles + mockMarkdownContentOverrides.keys)
            .filterNot { path -> path in mockRemovedPaths }
            .distinct()
            .sorted()

        val folderPaths = mutableSetOf<String>()
        allFiles.forEach { filePath ->
            var folderPath = filePath.substringBeforeLast('/', "")
            while (folderPath.isNotEmpty()) {
                folderPaths += folderPath
                folderPath = folderPath.substringBeforeLast('/', "")
            }
        }

        val entries = mutableListOf<JsonElement>()
        folderPaths
            .sortedBy(::pathDepth)
            .forEach { folderPath ->
                entries += buildJsonObject {
                    put("type", "tree")
                    put("path", folderPath)
                }
            }
        allFiles.forEach { filePath ->
            entries += buildJsonObject {
                put("type", "blob")
                put("path", filePath)
            }
        }

        return buildTreeFromEntries(entries)
    }

    private fun mockDocumentContent(path: String): String {
        return when (path) {
            "docs/README.md" -> {
                """
                # Welcome

                This is mock content for `$path`.

                - Edit this text in the Markdown editor.
                - Dirty state should update while typing.
                """.trimIndent()
            }

            "docs/architecture.md" -> {
                """
                # Architecture Notes

                The app is split into:

                - presentation
                - domain
                - data
                - platform
                """.trimIndent()
            }

            "notes/todo.md" -> {
                """
                # TODO

                - [ ] Ship Phase 6
                - [ ] Start Phase 7 preview rendering
                """.trimIndent()
            }

            "CHANGELOG.md" -> {
                """
                # Changelog

                ## Unreleased

                - Added repository tree loading.
                - Added markdown document open/edit flow.
                """.trimIndent()
            }

            else -> {
                """
                # $path

                Mock markdown content for this file.
                """.trimIndent()
            }
        }
    }

    private fun mockDocumentHistory(path: String): List<DocumentHistoryEntry> {
        val fileName = path.substringAfterLast('/').ifBlank { path }
        return listOf(
            DocumentHistoryEntry(
                hash = "a13f2b0",
                message = "Refine $fileName",
                authorName = "demo-user",
                authoredDate = "2026-04-16T11:32:00Z",
                authorAvatarUrl = null,
            ),
            DocumentHistoryEntry(
                hash = "7de44b1",
                message = "Adjust headings and links",
                authorName = "demo-user",
                authoredDate = "2026-04-15T08:14:00Z",
                authorAvatarUrl = null,
            ),
            DocumentHistoryEntry(
                hash = "04d8ec8",
                message = "Create $fileName",
                authorName = "demo-user",
                authoredDate = "2026-04-12T18:05:00Z",
                authorAvatarUrl = null,
            ),
        )
    }

    private fun isMarkdownPath(path: String): Boolean {
        return path.endsWith(".md", ignoreCase = true) ||
            path.endsWith(".markdown", ignoreCase = true)
    }

    private fun pathDepth(path: String): Int {
        return path.count { it == '/' }
    }

    private fun JsonObject.array(key: String): List<JsonElement>? {
        return (this[key] as? JsonArray)?.toList()
    }

    private fun JsonObject.string(key: String): String? {
        return (this[key] as? JsonPrimitive)?.let { primitive ->
            runCatching { primitive.content }.getOrNull()
        }
    }

    private fun JsonObject.long(key: String): Long? {
        return string(key)?.toLongOrNull()
    }

    private fun JsonObject.boolean(key: String): Boolean? {
        return string(key)?.toBooleanStrictOrNull()
    }

    private fun JsonObject.objectField(key: String): JsonObject? {
        return this[key] as? JsonObject
    }

    private data class MutableTreeNode(
        val path: String,
        val name: String,
        val kind: RepoTreeNodeKind,
        val isMarkdownFile: Boolean = false,
        val children: MutableList<MutableTreeNode> = mutableListOf(),
    )

    private companion object {
        val treeNodeComparator = compareBy<MutableTreeNode>(
            { it.kind != RepoTreeNodeKind.Folder },
            { it.name.lowercase() },
        )
    }
}
