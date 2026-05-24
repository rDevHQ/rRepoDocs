package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

interface GitHubRepositoryService {
    suspend fun loadAccessibleRepositories(session: UserSession): Result<List<RepositoryRef>>
    suspend fun loadRepositoryTree(
        session: UserSession,
        repository: RepositoryRef,
    ): Result<List<RepoTreeNode>>
    suspend fun loadMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
    ): Result<MarkdownDocument>
    suspend fun loadDocumentHistory(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
    ): Result<List<DocumentHistoryEntry>>
    suspend fun saveMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        updatedContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument>
    suspend fun createMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
        initialContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument>
    suspend fun createFolder(
        session: UserSession,
        repository: RepositoryRef,
        folderPath: String,
        commitMessage: String,
    ): Result<Unit>
    suspend fun deleteRepositoryItems(
        session: UserSession,
        repository: RepositoryRef,
        paths: List<String>,
        commitMessage: String,
    ): Result<Unit>
    suspend fun renameMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        newPath: DocumentPath,
        commitMessage: String,
    ): Result<MarkdownDocument>
    suspend fun moveMarkdownDocument(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        destinationPath: DocumentPath,
        commitMessage: String,
    ): Result<MarkdownDocument>
}
