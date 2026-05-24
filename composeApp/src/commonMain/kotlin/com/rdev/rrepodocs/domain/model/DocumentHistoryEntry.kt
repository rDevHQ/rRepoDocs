package com.rdev.rrepodocs.domain.model

data class DocumentHistoryEntry(
    val hash: String,
    val message: String,
    val authorName: String,
    val authoredDate: String,
    val authorAvatarUrl: String? = null,
)
