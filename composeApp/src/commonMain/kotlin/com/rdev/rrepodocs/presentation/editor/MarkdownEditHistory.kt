package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.text.input.TextFieldValue

internal class MarkdownEditHistory {
    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()

    var canUndo = false
        private set
    var canRedo = false
        private set

    fun recordChange(previousValue: TextFieldValue, updatedValue: TextFieldValue) {
        if (previousValue.text == updatedValue.text) return

        undoStack += previousValue
        redoStack.clear()
        updateAvailability()
    }

    fun undo(currentValue: TextFieldValue): TextFieldValue? {
        val previousValue = undoStack.removeLastOrNull() ?: return null
        redoStack += currentValue
        updateAvailability()
        return previousValue
    }

    fun redo(currentValue: TextFieldValue): TextFieldValue? {
        val nextValue = redoStack.removeLastOrNull() ?: return null
        undoStack += currentValue
        updateAvailability()
        return nextValue
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        updateAvailability()
    }

    private fun updateAvailability() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }
}
