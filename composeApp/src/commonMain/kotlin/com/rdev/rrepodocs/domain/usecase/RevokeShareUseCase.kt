package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.share.ShareService
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.UserSession

class RevokeShareUseCase(
    private val shareService: ShareService,
) {
    suspend operator fun invoke(
        session: UserSession,
        share: DocumentShare,
    ): Result<DocumentShare> {
        return shareService.revokeDocumentShare(
            session = session,
            share = share,
        )
    }
}
