package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepoTreeNodeKind
import com.rdev.rrepodocs.domain.model.RepositoryRef
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

private class DesktopLocalFolderRepositoryService : LocalFolderRepositoryService {
    override suspend fun loadTree(repository: RepositoryRef): Result<List<RepoTreeNode>> = runCatching {
        val root = rootFor(repository)
        Files.walk(root).use { paths ->
            paths.filter { it != root && !Files.isSymbolicLink(it) }
                .sorted()
                .map { path ->
                    val relative = root.relativize(path).toString().replace('\\', '/')
                    RepoTreeNode(
                        path = relative,
                        name = path.name,
                        kind = if (path.isDirectory()) RepoTreeNodeKind.Folder else RepoTreeNodeKind.File,
                        isMarkdownFile = path.isRegularFile() && isMarkdownPath(relative),
                    )
                }
                .toList()
                .toTree()
        }
    }

    override suspend fun loadDocument(repository: RepositoryRef, path: DocumentPath): Result<MarkdownDocument> = runCatching {
        val file = resolve(repository, path.value)
        require(file.isRegularFile()) { "File does not exist: ${path.value}" }
        MarkdownDocument(path, Files.readString(file, StandardCharsets.UTF_8).replace("\r\n", "\n"))
    }

    override suspend fun saveDocument(repository: RepositoryRef, document: MarkdownDocument, content: String): Result<MarkdownDocument> = runCatching {
        val file = resolve(repository, document.path.value)
        require(file.isRegularFile()) { "File does not exist: ${document.path.value}" }
        val normalized = content.replace("\r\n", "\n")
        Files.writeString(file, normalized, StandardCharsets.UTF_8)
        document.copy(content = normalized)
    }

    override suspend fun createDocument(repository: RepositoryRef, path: DocumentPath, content: String): Result<MarkdownDocument> = runCatching {
        val file = resolve(repository, path.value)
        require(!file.exists()) { "File already exists: ${path.value}" }
        Files.createDirectories(requireNotNull(file.parent))
        val normalized = content.replace("\r\n", "\n")
        Files.writeString(file, normalized, StandardCharsets.UTF_8)
        MarkdownDocument(path, normalized)
    }

    override suspend fun createFolder(repository: RepositoryRef, folderPath: String): Result<Unit> = runCatching {
        Files.createDirectories(resolve(repository, folderPath))
    }

    override suspend fun deleteItems(repository: RepositoryRef, paths: List<String>): Result<Unit> = runCatching {
        paths.distinct().sortedByDescending { it.length }.forEach { relative ->
            val target = resolve(repository, relative)
            if (target.isDirectory()) {
                Files.walk(target).use { entries -> entries.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
            } else {
                Files.deleteIfExists(target)
            }
        }
    }

    override suspend fun moveDocument(repository: RepositoryRef, document: MarkdownDocument, destinationPath: DocumentPath): Result<MarkdownDocument> = runCatching {
        val source = resolve(repository, document.path.value)
        val target = resolve(repository, destinationPath.value)
        require(!target.exists()) { "File already exists: ${destinationPath.value}" }
        Files.createDirectories(requireNotNull(target.parent))
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
        document.copy(path = destinationPath)
    }

    override suspend fun loadHistory(repository: RepositoryRef, path: DocumentPath): Result<List<DocumentHistoryEntry>> =
        Result.success(emptyList())

    private fun rootFor(repository: RepositoryRef): Path {
        val root = repository.localRootPath?.let(Path::of)?.normalize()
            ?: error("Local folder path is missing.")
        require(root.isDirectory()) { "Local folder is no longer available." }
        return root
    }

    private fun resolve(repository: RepositoryRef, relativePath: String): Path {
        require(relativePath.isNotBlank() && !relativePath.startsWith('/')) { "A relative path is required." }
        val root = rootFor(repository)
        val target = root.resolve(relativePath).normalize()
        require(target.startsWith(root)) { "Path must stay inside the selected folder." }
        return target
    }
}

private fun List<RepoTreeNode>.toTree(): List<RepoTreeNode> {
    val folders = mutableMapOf<String, MutableList<RepoTreeNode>>()
    val roots = mutableListOf<RepoTreeNode>()
    forEach { node ->
        val parent = node.path.substringBeforeLast('/', "")
        if (parent.isEmpty()) roots += node else folders.getOrPut(parent) { mutableListOf() } += node
    }
    fun nest(node: RepoTreeNode): RepoTreeNode = node.copy(children = folders[node.path].orEmpty().sortedWith(compareBy<RepoTreeNode> { it.kind != RepoTreeNodeKind.Folder }.thenBy { it.name.lowercase() }).map(::nest))
    return roots.sortedWith(compareBy<RepoTreeNode> { it.kind != RepoTreeNodeKind.Folder }.thenBy { it.name.lowercase() }).map(::nest)
}

private fun isMarkdownPath(path: String) = path.endsWith(".md", true) || path.endsWith(".markdown", true)

actual fun provideLocalFolderRepositoryService(): LocalFolderRepositoryService = DesktopLocalFolderRepositoryService()
