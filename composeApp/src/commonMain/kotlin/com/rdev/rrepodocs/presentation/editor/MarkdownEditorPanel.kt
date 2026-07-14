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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
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
    onRetryOpenDocument: () -> Unit,
    onDiscardUnsavedAndOpenPending: () -> Unit,
    onKeepEditingCurrent: () -> Unit,
    modifier: Modifier = Modifier,
    showChrome: Boolean = true,
    onEditorFocusChanged: (Boolean) -> Unit = {},
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(editorScroll)
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
                    value = content,
                    onValueChange = onContentChanged,
                    enabled = canEdit,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onEditorFocusChanged(it.isFocused) },
                )
            }
        }
    }
}
