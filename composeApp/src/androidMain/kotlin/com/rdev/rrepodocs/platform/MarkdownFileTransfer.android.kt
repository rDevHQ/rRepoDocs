package com.rdev.rrepodocs.platform

actual fun pickMarkdownFileForImport(): ImportedMarkdownFile? = null

actual fun exportMarkdownFile(
    defaultFileName: String,
    content: String,
): String? = null
