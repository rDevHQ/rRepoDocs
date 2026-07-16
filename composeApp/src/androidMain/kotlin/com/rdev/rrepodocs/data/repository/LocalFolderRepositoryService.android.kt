package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.domain.model.*

private class UnsupportedLocalFolderRepositoryService : LocalFolderRepositoryService {
    private fun <T> unsupported(): Result<T> = Result.failure(UnsupportedOperationException("Local folders are currently available on Desktop only."))
    override suspend fun loadTree(repository: RepositoryRef) = unsupported<List<RepoTreeNode>>()
    override suspend fun loadDocument(repository: RepositoryRef, path: DocumentPath) = unsupported<MarkdownDocument>()
    override suspend fun saveDocument(repository: RepositoryRef, document: MarkdownDocument, content: String) = unsupported<MarkdownDocument>()
    override suspend fun createDocument(repository: RepositoryRef, path: DocumentPath, content: String) = unsupported<MarkdownDocument>()
    override suspend fun createFolder(repository: RepositoryRef, folderPath: String) = unsupported<Unit>()
    override suspend fun deleteItems(repository: RepositoryRef, paths: List<String>) = unsupported<Unit>()
    override suspend fun moveDocument(repository: RepositoryRef, document: MarkdownDocument, destinationPath: DocumentPath) = unsupported<MarkdownDocument>()
    override suspend fun loadHistory(repository: RepositoryRef, path: DocumentPath) = unsupported<List<DocumentHistoryEntry>>()
}
actual fun provideLocalFolderRepositoryService(): LocalFolderRepositoryService = UnsupportedLocalFolderRepositoryService()
