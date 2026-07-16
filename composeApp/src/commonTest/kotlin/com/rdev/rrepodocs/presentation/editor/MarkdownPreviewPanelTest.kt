package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownPreviewPanelTest {
    @Test
    fun rendersCombinedBoldItalicMarkdownWithoutMarkers() {
        val result = buildMarkdownAnnotatedString(
            text = "A ***bold italic*** example",
            linkColor = Color.Blue,
            inlineCodeBackground = Color.Gray,
        )

        assertEquals("A bold italic example", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(2, result.spanStyles.single().start)
        assertEquals(13, result.spanStyles.single().end)
        assertEquals(FontWeight.SemiBold, result.spanStyles.single().item.fontWeight)
        assertEquals(FontStyle.Italic, result.spanStyles.single().item.fontStyle)
    }
}
