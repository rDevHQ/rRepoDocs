package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepositoryRef

interface LocalFolderRepositoryService {
    suspend fun loadTree(repository: RepositoryRef): Result<List<RepoTreeNode>>
    suspend fun loadDocument(repository: RepositoryRef, path: DocumentPath): Result<MarkdownDocument>
    suspend fun saveDocument(repository: RepositoryRef, document: MarkdownDocument, content: String): Result<MarkdownDocument>
    suspend fun createDocument(repository: RepositoryRef, path: DocumentPath, content: String): Result<MarkdownDocument>
    suspend fun createFolder(repository: RepositoryRef, folderPath: String): Result<Unit>
    suspend fun deleteItems(repository: RepositoryRef, paths: List<String>): Result<Unit>
    suspend fun moveDocument(repository: RepositoryRef, document: MarkdownDocument, destinationPath: DocumentPath): Result<MarkdownDocument>
    suspend fun loadHistory(repository: RepositoryRef, path: DocumentPath): Result<List<DocumentHistoryEntry>>
}

expect fun provideLocalFolderRepositoryService(): LocalFolderRepositoryService
