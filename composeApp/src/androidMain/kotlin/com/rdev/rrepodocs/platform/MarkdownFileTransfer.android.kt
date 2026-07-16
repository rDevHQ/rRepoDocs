package com.rdev.rrepodocs.platform

actual fun pickMarkdownFileForImport(): ImportedMarkdownFile? = null

actual fun exportMarkdownFile(
    defaultFileName: String,
    content: String,
): String? = null

actual fun exportPdfFile(
    defaultFileName: String,
    title: String,
    content: String,
): String? = null

actual fun printMarkdownPreview(
    title: String,
    content: String,
): Boolean = false
