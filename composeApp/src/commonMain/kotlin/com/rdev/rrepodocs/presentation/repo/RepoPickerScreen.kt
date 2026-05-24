package com.rdev.rrepodocs.presentation.repo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun RepoPickerScreen(
    repositories: List<RepositoryRef>,
    isLoading: Boolean,
    errorMessage: String?,
    onRepositorySelected: (RepositoryRef) -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppThemeTokens.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.lg),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.xs),
            ) {
                Text(
                    text = "Select GitHub Repository",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Choose one active GitHub repository for this workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }

        when {
            isLoading && repositories.isEmpty() -> {
                Text(
                    text = "Loading repositories...",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            errorMessage != null -> {
                Column(verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm)) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    TextButton(onClick = onRetry) {
                        Text("Try again")
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.md),
                ) {
                    items(repositories) { repository ->
                        ElevatedCard(
                            onClick = { onRepositorySelected(repository) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(AppThemeTokens.spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.xs),
                            ) {
                                Text(
                                    text = repository.name,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Text(
                                    text = repository.fullName,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
