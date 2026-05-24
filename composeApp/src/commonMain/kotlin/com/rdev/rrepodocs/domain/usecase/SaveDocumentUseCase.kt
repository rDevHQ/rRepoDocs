package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class SaveDocumentUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        updatedContent: String,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        return repositoryService.saveMarkdownDocument(
            session = session,
            repository = repository,
            document = document,
            updatedContent = updatedContent,
            commitMessage = commitMessage,
        )
    }
}
