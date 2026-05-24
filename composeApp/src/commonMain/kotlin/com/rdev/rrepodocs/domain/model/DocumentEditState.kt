package com.rdev.rrepodocs.domain.model

data class DocumentEditState(
    val originalContent: String,
    val currentContent: String,
) {
    val isDirty: Boolean
        get() = originalContent != currentContent
}
