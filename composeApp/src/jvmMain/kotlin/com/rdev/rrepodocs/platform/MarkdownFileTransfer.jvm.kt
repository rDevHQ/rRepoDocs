package com.rdev.rrepodocs.platform

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

actual fun pickMarkdownFileForImport(): ImportedMarkdownFile? {
    val dialog = FileDialog(null as Frame?, "Import Markdown File", FileDialog.LOAD).apply {
        file = "*.md;*.markdown"
        filenameFilter = java.io.FilenameFilter { _, name -> name.isMarkdownFileName() }
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val fileName = dialog.file ?: return null
    val file = File(directory, fileName)
    return ImportedMarkdownFile(
        fileName = file.name,
        content = file.readText(),
    )
}

actual fun exportMarkdownFile(
    defaultFileName: String,
    content: String,
): String? {
    val dialog = FileDialog(null as Frame?, "Export Markdown File", FileDialog.SAVE).apply {
        file = defaultFileName.ensureMarkdownFileName()
        isVisible = true
    }
    val directory = dialog.directory ?: return null
    val selectedFileName = dialog.file ?: return null
    val file = File(directory, selectedFileName.ensureMarkdownFileName())
    file.writeText(content)
    return file.absolutePath
}

private fun String.isMarkdownFileName(): Boolean {
    return endsWith(".md", ignoreCase = true) || endsWith(".markdown", ignoreCase = true)
}

private fun String.ensureMarkdownFileName(): String {
    if (isMarkdownFileName()) return this
    return "$this.md"
}
