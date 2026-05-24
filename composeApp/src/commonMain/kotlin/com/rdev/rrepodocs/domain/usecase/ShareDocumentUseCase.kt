package com.rdev.rrepodocs.domain.usecase

import com.rdev.rrepodocs.data.share.ShareService
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import com.rdev.rrepodocs.domain.model.UserSession

class ShareDocumentUseCase(
    private val shareService: ShareService,
) {
    suspend operator fun invoke(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        expires: ShareExpiryOption,
    ): Result<DocumentShare> {
        return shareService.createDocumentShare(
            session = session,
            repository = repository,
            document = document,
            expires = expires,
        )
    }
}
