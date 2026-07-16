package com.rdev.rrepodocs.domain.model

data class RepositoryRef(
    val id: Long,
    val name: String,
    val fullName: String,
    val ownerLogin: String,
    val ownerAvatarUrl: String?,
    val isPrivate: Boolean,
    val defaultBranch: String = "HEAD",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val pushedAt: String? = null,
    val source: RepositorySource = RepositorySource.GitHub,
    /** Absolute root path, used only by the desktop local-folder source. */
    val localRootPath: String? = null,
)

enum class RepositorySource {
    GitHub,
    LocalFolder,
}
