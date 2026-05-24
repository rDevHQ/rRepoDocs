package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.share.ShareService
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.UserSession

class LoadDocumentSharesUseCase(
    private val shareService: ShareService,
) {
    suspend operator fun invoke(
        session: UserSession,
    ): Result<List<DocumentShare>> {
        return shareService.loadDocumentShares(session)
    }
}
