package com.rdev.rrepodocs.domain.model

sealed interface MarkdownPreviewBlock {
    data class Heading(
        val level: Int,
        val text: String,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class Paragraph(
        val text: String,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class UnorderedList(
        val items: List<String>,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class OrderedList(
        val items: List<String>,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class CodeFence(
        val language: String?,
        val code: String,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class BlockQuote(
        val text: String,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
        val sourceOffset: Int = 0,
    ) : MarkdownPreviewBlock

    data object HorizontalRule : MarkdownPreviewBlock
}
