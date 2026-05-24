package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class MoveDocumentUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        destinationPath: DocumentPath,
        commitMessage: String,
    ): Result<MarkdownDocument> {
        return repositoryService.moveMarkdownDocument(
            session = session,
            repository = repository,
            document = document,
            destinationPath = destinationPath,
            commitMessage = commitMessage,
        )
    }
}
