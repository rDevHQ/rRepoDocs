package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class DeleteRepositoryItemsUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        paths: List<String>,
        commitMessage: String,
    ): Result<Unit> {
        return repositoryService.deleteRepositoryItems(
            session = session,
            repository = repository,
            paths = paths,
            commitMessage = commitMessage,
        )
    }
}
