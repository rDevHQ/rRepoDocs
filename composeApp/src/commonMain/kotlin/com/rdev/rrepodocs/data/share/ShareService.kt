package com.rdev.rrepodocs.data.share

import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import com.rdev.rrepodocs.domain.model.UserSession

interface ShareService {
    suspend fun loadDocumentShares(
        session: UserSession,
    ): Result<List<DocumentShare>>

    suspend fun createDocumentShare(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        expires: ShareExpiryOption,
    ): Result<DocumentShare>

    suspend fun revokeDocumentShare(
        session: UserSession,
        share: DocumentShare,
    ): Result<DocumentShare>
}
