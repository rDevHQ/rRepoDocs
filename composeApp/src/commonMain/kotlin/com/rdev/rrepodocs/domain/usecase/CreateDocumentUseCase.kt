package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class CreateDocumentUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
        initialContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        return repositoryService.createMarkdownDocument(
            session = session,
            repository = repository,
            path = path,
            initialContent = initialContent,
            commitMessage = commitMessage,
        )
    }
}
