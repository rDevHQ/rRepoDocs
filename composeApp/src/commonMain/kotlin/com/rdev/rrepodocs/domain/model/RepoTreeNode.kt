package com.rdev.rrepodocs.domain.model

enum class RepoTreeNodeKind {
    Folder,
    File,
}

data class RepoTreeNode(
    val path: String,
    val name: String,
    val kind: RepoTreeNodeKind,
    val isMarkdownFile: Boolean = false,
    val children: List<RepoTreeNode> = emptyList(),
)
