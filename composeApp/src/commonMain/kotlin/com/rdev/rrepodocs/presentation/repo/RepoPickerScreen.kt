package com.rdev.rrepodocs.presentation.repo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun RepoPickerScreen(
    repositories: List<RepositoryRef>,
    isLoading: Boolean,
    errorMessage: String?,
    onRepositorySelected: (RepositoryRef) -> Unit,
    onOpenLocalFolder: () -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredRepositories = remember(repositories, searchQuery) {
        val normalizedQuery = searchQuery.trim()
        if (normalizedQuery.isEmpty()) {
            repositories
        } else {
            repositories.filter { repository ->
                repository.name.contains(normalizedQuery, ignoreCase = true) ||
                    repository.fullName.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppThemeTokens.spacing.xl, vertical = AppThemeTokens.spacing.lg),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 960.dp),
            verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.xs),
                ) {
                    Text(
                        text = "Open a repository",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Choose the GitHub repository you want to work in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onOpenLocalFolder) {
                        Text("Open local folder")
                    }
                }
                TextButton(onClick = onSignOut) {
                    Text("Sign out")
                }
            }

            when {
                isLoading && repositories.isEmpty() -> {
                    LoadingRepositories()
                }

                errorMessage != null -> {
                    RepositoryLoadError(errorMessage, onRetry)
                }

                else -> {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Filter repositories") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null)
                        },
                    )

                    Text(
                        text = when {
                            filteredRepositories.isEmpty() -> "No repositories found"
                            searchQuery.isBlank() -> "${repositories.size} repositories"
                            else -> "${filteredRepositories.size} matching repositories"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (filteredRepositories.isEmpty()) {
                        EmptySearchResult(searchQuery)
                    } else {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            ),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    MaterialTheme.colorScheme.outline,
                                ),
                            ),
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(
                                    items = filteredRepositories,
                                    key = { repository -> repository.fullName },
                                ) { repository ->
                                    RepositoryRow(
                                        repository = repository,
                                        onClick = { onRepositorySelected(repository) },
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepositoryRow(repository: RepositoryRef, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppThemeTokens.spacing.md, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.md),
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = repository.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                RepositoryMetadata(repository)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Open ${repository.fullName}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RepositoryMetadata(repository: RepositoryRef) {
    val metadata = buildList {
        add(if (repository.isPrivate) "Private" else "Public")
        repository.createdAt?.let { add("Created ${formatRepositoryDate(it)}") }
        (repository.pushedAt ?: repository.updatedAt)?.let { add("Last push ${formatRepositoryDate(it)}") }
    }
    Text(
        text = metadata.joinToString("  ·  "),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun formatRepositoryDate(value: String): String {
    val date = value.substringBefore('T')
    val parts = date.split('-')
    if (parts.size != 3) return date

    val month = when (parts[1]) {
        "01" -> "Jan"
        "02" -> "Feb"
        "03" -> "Mar"
        "04" -> "Apr"
        "05" -> "May"
        "06" -> "Jun"
        "07" -> "Jul"
        "08" -> "Aug"
        "09" -> "Sep"
        "10" -> "Oct"
        "11" -> "Nov"
        "12" -> "Dec"
        else -> return date
    }
    return "${parts[2].toIntOrNull() ?: return date} $month ${parts[0]}"
}

@Composable
private fun LoadingRepositories() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppThemeTokens.spacing.xxl),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Spacer(Modifier.size(AppThemeTokens.spacing.sm))
        Text("Loading repositories…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RepositoryLoadError(errorMessage: String, onRetry: () -> Unit) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(AppThemeTokens.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = errorMessage,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            TextButton(onClick = onRetry) { Text("Try again") }
        }
    }
}

@Composable
private fun EmptySearchResult(searchQuery: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Text(
            text = "No repository matches “${searchQuery.trim()}”.",
            modifier = Modifier.padding(AppThemeTokens.spacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
