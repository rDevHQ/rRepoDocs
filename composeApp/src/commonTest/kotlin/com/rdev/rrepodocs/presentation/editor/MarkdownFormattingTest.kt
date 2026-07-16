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
}
