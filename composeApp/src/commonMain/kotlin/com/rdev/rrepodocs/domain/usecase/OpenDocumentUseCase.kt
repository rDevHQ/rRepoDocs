package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class OpenDocumentUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        path: DocumentPath,
    ): Result<MarkdownDocument> {
        return repositoryService.loadMarkdownDocument(
            session = session,
            repository = repository,
            path = path,
        )
    }
}
