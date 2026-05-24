package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.presentation.app.AppThemeTokens
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url

@Composable
fun MarkdownHistoryPanel(
    activeDocumentPath: String?,
    historyEntries: List<DocumentHistoryEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(6.dp),
    ) {
        Column(
            modifier = Modifier.padding(AppThemeTokens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.lg),
        ) {
            if (showTitle) {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (activeDocumentPath == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.56f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Open a Markdown file to see its Git history.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            } else if (historyEntries.isEmpty() && !isLoading && errorMessage == null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.56f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "No commits found for this file yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    )
                }
            } else if (isLoading) {
                Text(
                    text = "Loading commit history...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    itemsIndexed(historyEntries, key = { index, entry -> "${entry.hash}-$index" }) { index, entry ->
                        HistoryTimelineEntry(
                            entry = entry,
                            isActive = index == 0,
                            showTopConnector = index > 0,
                            showBottomConnector = index < historyEntries.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTimelineEntry(
    entry: DocumentHistoryEntry,
    isActive: Boolean,
    showTopConnector: Boolean,
    showBottomConnector: Boolean,
) {
    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 116.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        TimelineNode(
            isActive = isActive,
            showTopConnector = showTopConnector,
            showBottomConnector = showBottomConnector,
        )
        Column(
            modifier = Modifier.padding(top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (isActive) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = "Latest",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = entry.message.lineSequence().firstOrNull().orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HistoryAuthorAvatar(
                    avatarUrl = entry.authorAvatarUrl,
                    authorName = entry.authorName,
                )
                Text(
                    text = entry.authorName,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = formatHistoryDate(entry.authoredDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    color = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = entry.hash.take(9),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Text(
                    text = "commit",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimelineNode(
    isActive: Boolean,
    showTopConnector: Boolean,
    showBottomConnector: Boolean,
) {
    Column(
        modifier = Modifier.width(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showTopConnector) {
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 18.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        } else {
            Spacer(modifier = Modifier.size(width = 2.dp, height = 18.dp))
        }
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = CircleShape,
                ),
        )
        if (showBottomConnector) {
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 82.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            )
        }
    }
}

@Composable
private fun HistoryAuthorAvatar(
    avatarUrl: String?,
    authorName: String,
) {
    if (avatarUrl.isNullOrBlank()) {
        Surface(
            modifier = Modifier.size(34.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = authorName.take(1).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }
    val avatarResource = asyncPainterResource(data = Url(avatarUrl))
    KamelImage(
        resource = avatarResource,
        contentDescription = "History author avatar",
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape),
    )
}

private fun formatHistoryDate(raw: String): String {
    if (raw.isBlank()) return "Unknown date"
    return raw
        .replace('T', ' ')
        .removeSuffix("Z")
        .take(16)
}
