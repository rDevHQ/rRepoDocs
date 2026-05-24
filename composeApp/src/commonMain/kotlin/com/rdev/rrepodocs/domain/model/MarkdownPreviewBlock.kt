package com.rdev.rrepodocs.domain.model

sealed interface MarkdownPreviewBlock {
    data class Heading(
        val level: Int,
        val text: String,
    ) : MarkdownPreviewBlock

    data class Paragraph(
        val text: String,
    ) : MarkdownPreviewBlock

    data class UnorderedList(
        val items: List<String>,
    ) : MarkdownPreviewBlock

    data class OrderedList(
        val items: List<String>,
    ) : MarkdownPreviewBlock

    data class CodeFence(
        val language: String?,
        val code: String,
    ) : MarkdownPreviewBlock

    data class BlockQuote(
        val text: String,
    ) : MarkdownPreviewBlock

    data class Table(
        val headers: List<String>,
        val rows: List<List<String>>,
    ) : MarkdownPreviewBlock

    data object HorizontalRule : MarkdownPreviewBlock
}
