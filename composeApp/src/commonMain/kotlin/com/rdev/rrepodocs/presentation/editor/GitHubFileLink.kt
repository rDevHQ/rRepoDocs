package com.rdev.rrepodocs.presentation.editor

import io.ktor.http.encodeURLPathPart

fun githubFileUrl(
    repositoryFullName: String,
    defaultBranch: String,
    path: String,
): String? {
    val repositoryParts = repositoryFullName.trim().trim('/').split('/')
    val normalizedPath = path.trim().trim('/')
    if (repositoryParts.size != 2 || repositoryParts.any { it.isBlank() } || normalizedPath.isBlank()) {
        return null
    }

    val repository = repositoryParts.joinToString("/") { it.encodeURLPathPart() }
    val branch = defaultBranch.trim().ifBlank { "HEAD" }.encodeURLPathPart()
    val encodedPath = normalizedPath.split('/').joinToString("/") { it.encodeURLPathPart() }
    return "https://github.com/$repository/blob/$branch/$encodedPath"
}
