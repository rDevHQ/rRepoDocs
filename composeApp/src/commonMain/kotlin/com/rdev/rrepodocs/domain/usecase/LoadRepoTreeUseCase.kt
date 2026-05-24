package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class LoadRepoTreeUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
    ): Result<List<RepoTreeNode>> {
        return repositoryService.loadRepositoryTree(
            session = session,
            repository = repository,
        )
    }
}
