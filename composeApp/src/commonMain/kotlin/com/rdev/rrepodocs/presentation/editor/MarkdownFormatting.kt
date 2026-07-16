package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

internal enum class MarkdownFormat {
    Heading,
    Bold,
    Italic,
    BulletList,
    Link,
    InlineCode,
    HardLineBreak,
}

internal fun applyMarkdownFormat(
    value: TextFieldValue,
    format: MarkdownFormat,
): TextFieldValue = when (format) {
    MarkdownFormat.Heading -> prefixSelectedLines(value, "# ")
    MarkdownFormat.BulletList -> prefixSelectedLines(value, "- ")
    MarkdownFormat.Bold -> wrapSelection(value, "**", "**", "bold text")
    MarkdownFormat.Italic -> wrapSelection(value, "*", "*", "italic text")
    MarkdownFormat.InlineCode -> wrapSelection(value, "`", "`", "code")
    MarkdownFormat.Link -> wrapSelection(value, "[", "](url)", "link text")
    MarkdownFormat.HardLineBreak -> replaceSelection(value, "  \n")
}

internal fun formattingValue(
    currentValue: TextFieldValue,
    rememberedSelection: TextRange?,
): TextFieldValue = rememberedSelection?.let { selection ->
    currentValue.copy(selection = selection)
} ?: currentValue

private fun wrapSelection(
    value: TextFieldValue,
    prefix: String,
    suffix: String,
    placeholder: String,
): TextFieldValue {
    val selection = normalizedSelection(value.selection)
    val selectedText = value.text.substring(selection.start, selection.end).ifEmpty { placeholder }
    val replacement = "$prefix$selectedText$suffix"
    val newText = value.text.replaceRange(selection.start, selection.end, replacement)
    val editableRangeStart = selection.start + prefix.length

    return value.copy(
        text = newText,
        selection = TextRange(editableRangeStart, editableRangeStart + selectedText.length),
    )
}

private fun replaceSelection(
    value: TextFieldValue,
    replacement: String,
): TextFieldValue {
    val selection = normalizedSelection(value.selection)
    val newText = value.text.replaceRange(selection.start, selection.end, replacement)
    val cursor = selection.start + replacement.length

    return value.copy(
        text = newText,
        selection = TextRange(cursor),
    )
}

private fun prefixSelectedLines(
    value: TextFieldValue,
    prefix: String,
): TextFieldValue {
    val selection = normalizedSelection(value.selection)
    val firstLineStart = value.text.lastIndexOf('\n', selection.start - 1).let { index -> index + 1 }
    val lastLineEnd = value.text.indexOf('\n', selection.end).let { index -> if (index == -1) value.text.length else index }
    val selectedLines = value.text.substring(firstLineStart, lastLineEnd)
    val replacement = selectedLines.lineSequence().joinToString("\n") { line ->
        if (line.startsWith(prefix)) line else "$prefix$line"
    }
    val insertedPrefixLength = replacement.length - selectedLines.length
    val newText = value.text.replaceRange(firstLineStart, lastLineEnd, replacement)

    return value.copy(
        text = newText,
        selection = TextRange(
            start = selection.start + if (selection.start == firstLineStart) prefix.length else 0,
            end = selection.end + insertedPrefixLength,
        ),
    )
}

private fun normalizedSelection(selection: TextRange): TextRange = TextRange(
    start = minOf(selection.start, selection.end),
    end = maxOf(selection.start, selection.end),
)
