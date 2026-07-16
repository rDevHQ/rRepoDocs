package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.platform.copyTextToClipboard
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun MarkdownFileInfoPanel(
    repositoryName: String,
    repositoryDefaultBranch: String,
    activeDocumentPath: String?,
    markdown: String,
    isDirty: Boolean,
    latestHistoryEntry: DocumentHistoryEntry?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(AppThemeTokens.spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.lg),
        ) {
            Text(
                text = "File info",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (activeDocumentPath == null) {
                InfoEmptyState()
            } else {
                InfoSection("File") {
                    InfoPathRow(
                        path = activeDocumentPath,
                        githubUrl = githubFileUrl(
                            repositoryFullName = repositoryName,
                            defaultBranch = repositoryDefaultBranch,
                            path = activeDocumentPath,
                        ),
                    )
                    InfoRow("Type", "Markdown")
                    InfoRow("Size", formatFileSize(markdown.encodeToByteArray().size))
                    InfoRow("Lines", markdown.lineSequence().count().toString())
                    InfoRow("Status", if (isDirty) "Unsaved changes" else "Saved")
                }

                InfoSection("Latest commit") {
                    if (latestHistoryEntry == null) {
                        InfoRow("Status", "No commit found")
                    } else {
                        InfoRow("Message", latestHistoryEntry.message.lineSequence().firstOrNull().orEmpty())
                        InfoRow("Author", latestHistoryEntry.authorName)
                        InfoRow("Changed", formatInfoDate(latestHistoryEntry.authoredDate))
                        InfoRow("Commit", latestHistoryEntry.hash.take(7), monospace = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPathRow(path: String, githubUrl: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Path",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = path,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { copyTextToClipboard(path) },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy file path",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        githubUrl?.let { url ->
            Text(
                text = "GitHub link",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = url,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { copyTextToClipboard(url) },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy GitHub link",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoEmptyState() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.56f),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = "Open a Markdown file to see its details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    monospace: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (monospace) FontFamily.Monospace else null,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun formatFileSize(bytes: Int): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_024 * 1_024 -> "%.1f KB".format(bytes / 1_024.0)
    else -> "%.1f MB".format(bytes / (1_024.0 * 1_024.0))
}

private fun formatInfoDate(raw: String): String = raw
    .replace('T', ' ')
    .removeSuffix("Z")
    .take(16)
    .ifBlank { "Unknown date" }
