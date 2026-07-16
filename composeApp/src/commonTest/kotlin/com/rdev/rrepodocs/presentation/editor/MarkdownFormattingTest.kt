package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownFormattingTest {
    @Test
    fun wrapsSelectedTextInBoldMarkdown() {
        val result = applyMarkdownFormat(
            TextFieldValue("A clear document", TextRange(2, 7)),
            MarkdownFormat.Bold,
        )

        assertEquals("A **clear** document", result.text)
        assertEquals(TextRange(4, 9), result.selection)
    }

    @Test
    fun insertsEditableLinkPlaceholderAtCursor() {
        val result = applyMarkdownFormat(
            TextFieldValue("Read ", TextRange(5)),
            MarkdownFormat.Link,
        )

        assertEquals("Read [link text](url)", result.text)
        assertEquals(TextRange(6, 15), result.selection)
    }

    @Test
    fun prefixesEverySelectedLineWithBulletMarker() {
        val result = applyMarkdownFormat(
            TextFieldValue("One\nTwo\nThree", TextRange(0, 7)),
            MarkdownFormat.BulletList,
        )

        assertEquals("- One\n- Two\nThree", result.text)
        assertEquals(TextRange(2, 11), result.selection)
    }

    @Test
    fun formatsRememberedSelectionAfterToolbarFocusMoves() {
        val currentValue = TextFieldValue("HEJ", TextRange(3))
        val selectionBeforeToolbarClick = TextRange(0, 3)

        val result = applyMarkdownFormat(
            formattingValue(currentValue, selectionBeforeToolbarClick),
            MarkdownFormat.Bold,
        )

        assertEquals("**HEJ**", result.text)
        assertEquals(TextRange(2, 5), result.selection)
    }

    @Test
    fun insertsHardLineBreakAtCursor() {
        val result = applyMarkdownFormat(
            TextFieldValue("First lineSecond line", TextRange(10)),
            MarkdownFormat.HardLineBreak,
        )

        assertEquals("First line  \nSecond line", result.text)
        assertEquals(TextRange(13), result.selection)
    }

    @Test
    fun supportsAllHeadingLevelsAndBlockPrefixes() {
        assertEquals(
            "# Title",
            applyMarkdownFormat(TextFieldValue("Title", TextRange(0)), MarkdownFormat.Heading1).text,
        )
        assertEquals(
            "## Title",
            applyMarkdownFormat(TextFieldValue("Title", TextRange(0)), MarkdownFormat.Heading2).text,
        )
        assertEquals(
            "### Title",
            applyMarkdownFormat(TextFieldValue("Title", TextRange(0)), MarkdownFormat.Heading3).text,
        )
        assertEquals(
            "> Quote",
            applyMarkdownFormat(TextFieldValue("Quote", TextRange(0)), MarkdownFormat.Blockquote).text,
        )
        assertEquals(
            "- [ ] Task",
            applyMarkdownFormat(TextFieldValue("Task", TextRange(0)), MarkdownFormat.TaskList).text,
        )
    }

    @Test
    fun supportsOrderedListsAndCombinedInlineFormats() {
        assertEquals(
            "1. First\n1. Second",
            applyMarkdownFormat(TextFieldValue("First\nSecond", TextRange(0, 12)), MarkdownFormat.OrderedList).text,
        )
        assertEquals(
            "***text***",
            applyMarkdownFormat(TextFieldValue("text", TextRange(0, 4)), MarkdownFormat.BoldItalic).text,
        )
        assertEquals(
            "~~text~~",
            applyMarkdownFormat(TextFieldValue("text", TextRange(0, 4)), MarkdownFormat.Strikethrough).text,
        )
    }

    @Test
    fun supportsImageAndCodeBlockPlaceholders() {
        assertEquals(
            "![alt text](url)",
            applyMarkdownFormat(TextFieldValue("", TextRange(0)), MarkdownFormat.Image).text,
        )
        assertEquals(
            "```\ncode\n```",
            applyMarkdownFormat(TextFieldValue("", TextRange(0)), MarkdownFormat.CodeBlock).text,
        )
    }

    @Test
    fun insertsTableAndHorizontalRule() {
        val table = applyMarkdownFormat(TextFieldValue("", TextRange(0)), MarkdownFormat.Table)

        assertEquals(
            "| Column 1 | Column 2 |\n| --- | --- |\n| Value 1 | Value 2 |",
            table.text,
        )
        assertEquals(TextRange(2, 10), table.selection)
        assertEquals(
            "---",
            applyMarkdownFormat(TextFieldValue("", TextRange(0)), MarkdownFormat.HorizontalRule).text,
        )
    }
}
