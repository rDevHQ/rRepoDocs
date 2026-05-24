package com.rdev.rrepodocs.domain.model

data class MarkdownDocument(
    val path: DocumentPath,
    val content: String,
    val sha: String? = null,
)
