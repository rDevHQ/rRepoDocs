package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun MarkdownEditorPanel(
    repositoryName: String,
    activeDocumentPath: String?,
    content: String,
    isDirty: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    pendingDocumentPath: String?,
    commitMessageDraft: String,
    saveInProgress: Boolean,
    saveError: String?,
    saveSuccess: String?,
    onContentChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onSaveDocument: () -> Unit,
    onRevertChanges: () -> Unit,
    onRetryOpenDocument: () -> Unit,
    onDiscardUnsavedAndOpenPending: () -> Unit,
    onKeepEditingCurrent: () -> Unit,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
    onEditorFocusChanged: (Boolean) -> Unit = {},
    onScrollProgressChanged: (Float) -> Unit = {},
    sourceNavigation: EditorSourceNavigation? = null,
    contentHorizontalPadding: Dp = 56.dp,
    contentVerticalPadding: Dp = 48.dp,
) {
    val canEdit = activeDocumentPath != null && !isLoading && !saveInProgress
    val canSave = canEdit && isDirty
    val repoDisplayName = repositoryName.substringAfterLast('/').ifBlank { repositoryName }
    val breadcrumbSegments = when (activeDocumentPath) {
        null -> listOf("No file selected")
        else -> listOf(repoDisplayName) + activeDocumentPath.split('/').filter { it.isNotBlank() }
    }
    val editorScroll = rememberScrollState()
    val editorFocusRequester = remember { FocusRequester() }
    val editorState = remember(activeDocumentPath) { TextFieldState(content) }
    val editHistory = remember(activeDocumentPath) { MarkdownEditHistory() }
    val historyValue = remember(activeDocumentPath) { TextFieldValueMemory(editorState.asTextFieldValue()) }
    val latestTextSelection = remember(activeDocumentPath) { TextSelectionMemory() }
    var editorHasFocus by remember(activeDocumentPath) { mutableStateOf(false) }

    LaunchedEffect(content) {
        if (content != editorState.text.toString()) {
            editorState.setTextAndPlaceCursorAtEnd(content)
            editHistory.reset()
            historyValue.value = editorState.asTextFieldValue()
            latestTextSelection.value = null
        }
    }

    LaunchedEffect(editorState) {
        snapshotFlow { editorState.text.toString() }.collect(onContentChanged)
    }

    LaunchedEffect(editorState) {
        snapshotFlow { editorState.selection }.collect { selection ->
            if (selection.start != selection.end) {
                latestTextSelection.value = selection
            } else if (editorHasFocus) {
                latestTextSelection.value = null
            }
        }
    }

    LaunchedEffect(editorState) {
        snapshotFlow { editorState.asTextFieldValue() }.collect { updatedValue ->
            if (updatedValue.text != historyValue.value.text) {
                editHistory.recordChange(historyValue.value, updatedValue)
            }
            historyValue.value = updatedValue
        }
    }

    LaunchedEffect(editorScroll) {
        snapshotFlow {
            if (editorScroll.maxValue == 0) 0f else editorScroll.value.toFloat() / editorScroll.maxValue
        }.collect(onScrollProgressChanged)
    }

    LaunchedEffect(sourceNavigation?.requestId) {
        sourceNavigation ?: return@LaunchedEffect
        editorState.edit {
            selection = TextRange(sourceNavigation.offset.coerceIn(0, length))
        }
        editorFocusRequester.requestFocus()
    }

    Surface(
        modifier = modifier,
        color = AppThemeTokens.colors.editorSurface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (showChrome) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppThemeTokens.colors.editorSurface)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        breadcrumbSegments.forEachIndexed { index, segment ->
                            val isLast = index == breadcrumbSegments.lastIndex
                            Text(
                                text = segment,
                                style = if (isLast && activeDocumentPath != null) {
                                    MaterialTheme.typography.titleLarge
                                } else {
                                    MaterialTheme.typography.titleMedium
                                },
                                color = if (isLast && activeDocumentPath != null) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (!isLast) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (activeDocumentPath != null && isDirty) {
                            Text(
                                text = "•",
                                color = AppThemeTokens.colors.statusWarning,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    val statusLabel = when {
                        saveInProgress -> "Saving"
                        saveError != null -> "Save failed"
                        isDirty -> "Unsaved changes"
                        saveSuccess != null -> "Saved"
                        else -> null
                    }
                    if (statusLabel != null) {
                        Surface(
                            color = when {
                                saveError != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
                                isDirty || saveInProgress -> AppThemeTokens.colors.statusWarning.copy(alpha = 0.22f)
                                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(7.dp),
                        ) {
                            Text(
                                text = statusLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    saveError != null -> MaterialTheme.colorScheme.error
                                    isDirty || saveInProgress -> AppThemeTokens.colors.statusWarning
                                    else -> MaterialTheme.colorScheme.primary
                                },
                            )
                        }
                    }

                    Button(
                        enabled = canSave,
                        onClick = onRevertChanges,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Undo,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(text = "Revert Changes")
                    }

                    Button(
                        enabled = canSave,
                        onClick = onSaveDocument,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canSave) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            contentColor = if (canSave) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                        Text(text = "Commit")
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppThemeTokens.colors.borderSubtle.copy(alpha = 0.5f)),
                )
            }

            if (errorMessage != null || saveError != null) {
                Column(
                    modifier = Modifier.padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (saveError != null) {
                        Text(
                            text = saveError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    if (pendingDocumentPath != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = onDiscardUnsavedAndOpenPending) {
                                Text("Discard & Open")
                            }
                            TextButton(onClick = onKeepEditingCurrent) {
                                Text("Keep Editing")
                            }
                        }
                    } else if (errorMessage != null) {
                        TextButton(onClick = onRetryOpenDocument) {
                            Text("Try Again")
                        }
                    }
                }
            }

            MarkdownFormattingToolbar(
                enabled = canEdit,
                canUndo = editHistory.canUndo,
                canRedo = editHistory.canRedo,
                onUndo = {
                    editHistory.undo(editorState.asTextFieldValue())?.let { previousValue ->
                        historyValue.value = previousValue
                        editorState.updateFrom(previousValue)
                        latestTextSelection.value = null
                        onContentChanged(previousValue.text)
                        editorFocusRequester.requestFocus()
                    }
                },
                onRedo = {
                    editHistory.redo(editorState.asTextFieldValue())?.let { nextValue ->
                        historyValue.value = nextValue
                        editorState.updateFrom(nextValue)
                        latestTextSelection.value = null
                        onContentChanged(nextValue.text)
                        editorFocusRequester.requestFocus()
                    }
                },
                onFormat = { format ->
                    val currentValue = editorState.asTextFieldValue()
                    val valueToFormat = formattingValue(currentValue, latestTextSelection.value)
                    val formattedValue = applyMarkdownFormat(valueToFormat, format)
                    editHistory.recordChange(currentValue, formattedValue)
                    historyValue.value = formattedValue
                    editorState.updateFrom(formattedValue)
                    latestTextSelection.value = null
                    onContentChanged(formattedValue.text)
                    editorFocusRequester.requestFocus()
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = contentHorizontalPadding, vertical = contentVerticalPadding),
            ) {
                val textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = if (canEdit) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                if (content.isBlank() && activeDocumentPath == null) {
                    Text(
                        text = "Select a Markdown file from the repository tree.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                BasicTextField(
                    state = editorState,
                    enabled = canEdit,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    scrollState = editorScroll,
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(editorFocusRequester)
                        .onFocusChanged {
                            editorHasFocus = it.isFocused
                            onEditorFocusChanged(it.isFocused)
                        },
                )
            }
        }
    }
}

data class EditorSourceNavigation(
    val offset: Int,
    val requestId: Long,
)

private fun TextFieldState.asTextFieldValue(): TextFieldValue = TextFieldValue(
    text = text.toString(),
    selection = selection,
)

private fun TextFieldState.updateFrom(value: TextFieldValue) {
    edit {
        replace(0, length, value.text)
        selection = value.selection
    }
}

private class TextFieldValueMemory(
    var value: TextFieldValue,
)

private class TextSelectionMemory(
    var value: TextRange? = null,
)

@Composable
private fun MarkdownFormattingToolbar(
    enabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: (MarkdownFormat) -> Unit,
) {
    val toolbarScroll = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppThemeTokens.colors.editorSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(toolbarScroll)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarkdownHistoryButton("Undo", enabled && canUndo, onUndo) {
                Icon(Icons.AutoMirrored.Outlined.Undo, contentDescription = null)
            }
            MarkdownHistoryButton("Redo", enabled && canRedo, onRedo) {
                Icon(Icons.AutoMirrored.Outlined.Redo, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.Heading1, "Heading 1", enabled, onFormat) {
                ToolbarFormatLabel("H1")
            }
            MarkdownFormatButton(MarkdownFormat.Heading2, "Heading 2", enabled, onFormat) {
                ToolbarFormatLabel("H2")
            }
            MarkdownFormatButton(MarkdownFormat.Heading3, "Heading 3", enabled, onFormat) {
                ToolbarFormatLabel("H3")
            }
            MarkdownFormatButton(MarkdownFormat.Bold, "Bold", enabled, onFormat) {
                Icon(Icons.Outlined.FormatBold, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.Italic, "Italic", enabled, onFormat) {
                Icon(Icons.Outlined.FormatItalic, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.BoldItalic, "Bold italic", enabled, onFormat) {
                ToolbarFormatLabel("B+I")
            }
            MarkdownFormatButton(MarkdownFormat.Strikethrough, "Strikethrough", enabled, onFormat) {
                ToolbarFormatLabel("S̶")
            }
            MarkdownFormatButton(MarkdownFormat.BulletList, "Bullet list", enabled, onFormat) {
                Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.OrderedList, "Ordered list", enabled, onFormat) {
                ToolbarFormatLabel("1.")
            }
            MarkdownFormatButton(MarkdownFormat.TaskList, "Task list", enabled, onFormat) {
                ToolbarFormatLabel("[ ]")
            }
            MarkdownFormatButton(MarkdownFormat.Blockquote, "Blockquote", enabled, onFormat) {
                ToolbarFormatLabel(">")
            }
            MarkdownFormatButton(MarkdownFormat.Link, "Link", enabled, onFormat) {
                Icon(Icons.Outlined.Link, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.Image, "Image", enabled, onFormat) {
                ToolbarFormatLabel("Img")
            }
            MarkdownFormatButton(MarkdownFormat.InlineCode, "Inline code", enabled, onFormat) {
                Icon(Icons.Outlined.Code, contentDescription = null)
            }
            MarkdownFormatButton(MarkdownFormat.CodeBlock, "Code block", enabled, onFormat) {
                ToolbarFormatLabel("```")
            }
            MarkdownFormatButton(MarkdownFormat.Table, "Table", enabled, onFormat) {
                ToolbarFormatLabel("Tbl")
            }
            MarkdownFormatButton(MarkdownFormat.HorizontalRule, "Horizontal rule", enabled, onFormat) {
                ToolbarFormatLabel("—")
            }
            MarkdownFormatButton(MarkdownFormat.HardLineBreak, "Radbrytning", enabled, onFormat) {
                Text("↵", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ToolbarFormatLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MarkdownHistoryButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    MarkdownToolbarTooltip(label) {
        IconButton(
            modifier = Modifier.semantics { contentDescription = label },
            enabled = enabled,
            onClick = onClick,
        ) {
            icon()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MarkdownFormatButton(
    format: MarkdownFormat,
    label: String,
    enabled: Boolean,
    onFormat: (MarkdownFormat) -> Unit,
    icon: @Composable () -> Unit,
) {
    MarkdownToolbarTooltip(label) {
        IconButton(
            modifier = Modifier.semantics { contentDescription = label },
            enabled = enabled,
            onClick = { onFormat(format) },
        ) {
            icon()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MarkdownToolbarTooltip(
    label: String,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = {
            PlainTooltip {
                Text(label)
            }
        },
        state = rememberTooltipState(),
        content = content,
    )
}
