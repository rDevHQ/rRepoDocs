package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class LoadRepositoriesUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(session: UserSession): Result<List<RepositoryRef>> {
        return repositoryService.loadAccessibleRepositories(session)
    }
}
