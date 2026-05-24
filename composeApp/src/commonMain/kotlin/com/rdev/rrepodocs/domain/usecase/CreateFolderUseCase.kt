package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.repository.GitHubRepositoryService
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.UserSession

class CreateFolderUseCase(
    private val repositoryService: GitHubRepositoryService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        folderPath: String,
        commitMessage: String,
    ): Result<Unit> {
        return repositoryService.createFolder(
            session = session,
            repository = repository,
            folderPath = folderPath,
            commitMessage = commitMessage,
        )
    }
}
