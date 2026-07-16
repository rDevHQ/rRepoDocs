package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.RepositorySource
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual suspend fun pickLocalMarkdownFolder(): RepositoryRef? {
    val previousDirectoryMode = System.getProperty("apple.awt.fileDialogForDirectories")
    System.setProperty("apple.awt.fileDialogForDirectories", "true")
    val chooser = FileDialog(null as Frame?, "Open local Markdown folder", FileDialog.LOAD).apply {
        directory = System.getProperty("user.home")
    }
    try {
        chooser.isVisible = true
    } finally {
        if (previousDirectoryMode == null) System.clearProperty("apple.awt.fileDialogForDirectories")
        else System.setProperty("apple.awt.fileDialogForDirectories", previousDirectoryMode)
    }
    val selected = chooser.file ?: return null
    val folder = File(chooser.directory, selected).takeIf { it.isDirectory } ?: return null
    val root = folder.canonicalFile
    return RepositoryRef(
        id = -root.path.hashCode().toLong(),
        name = root.name.ifBlank { root.path },
        fullName = root.path,
        ownerLogin = "Local folder",
        ownerAvatarUrl = null,
        isPrivate = true,
        defaultBranch = "Local",
        source = RepositorySource.LocalFolder,
        localRootPath = root.path,
    )
}
