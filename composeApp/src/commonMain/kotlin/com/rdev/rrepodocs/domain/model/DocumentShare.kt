package com.rdev.rrepodocs.domain.model

data class DocumentShare(
    val id: String,
    val shareUrl: String,
    val expiresAt: String?,
    val createdAt: String,
    val revokedAt: String? = null,
    val repoFullName: String? = null,
    val documentPath: String? = null,
    val title: String? = null,
    val sourceSha: String? = null,
)
