package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkdownEditHistoryTest {
    @Test
    fun undoRestoresPreviousTextAndSelection() {
        val initial = TextFieldValue("Hello", TextRange(5))
        val updated = TextFieldValue("Hello world", TextRange(11))
        val history = MarkdownEditHistory()

        history.recordChange(initial, updated)

        assertEquals(initial, history.undo(updated))
        assertFalse(history.canUndo)
        assertTrue(history.canRedo)
    }

    @Test
    fun redoRestoresUndoneChange() {
        val initial = TextFieldValue("Hello", TextRange(5))
        val updated = TextFieldValue("Hello world", TextRange(11))
        val history = MarkdownEditHistory()
        history.recordChange(initial, updated)
        history.undo(updated)

        assertEquals(updated, history.redo(initial))
        assertTrue(history.canUndo)
        assertFalse(history.canRedo)
    }

    @Test
    fun newChangeClearsRedoHistory() {
        val initial = TextFieldValue("Hello", TextRange(5))
        val updated = TextFieldValue("Hello world", TextRange(11))
        val replacement = TextFieldValue("Hello Codex", TextRange(11))
        val history = MarkdownEditHistory()
        history.recordChange(initial, updated)
        history.undo(updated)

        history.recordChange(initial, replacement)

        assertFalse(history.canRedo)
        assertEquals(null, history.redo(replacement))
    }

    @Test
    fun selectionOnlyChangesDoNotCreateHistoryEntries() {
        val initial = TextFieldValue("Hello", TextRange(5))
        val movedSelection = TextFieldValue("Hello", TextRange(0))
        val history = MarkdownEditHistory()

        history.recordChange(initial, movedSelection)

        assertFalse(history.canUndo)
    }
}
