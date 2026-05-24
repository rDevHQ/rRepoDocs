package com.rdev.rrepodocs.platform

data class ImportedMarkdownFile(
    val fileName: String,
    val content: String,
)

expect fun pickMarkdownFileForImport(): ImportedMarkdownFile?

expect fun exportMarkdownFile(
    defaultFileName: String,
    content: String,
): String?
