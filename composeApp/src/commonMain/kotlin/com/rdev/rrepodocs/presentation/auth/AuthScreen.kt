package com.rdev.rrepodocs.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.presentation.app.AppThemeTokens

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    isAwaitingAuthorization: Boolean,
    verificationUri: String?,
    verificationUriComplete: String?,
    userCode: String?,
    onStartSignIn: () -> Unit,
    onFinishSignIn: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppThemeTokens.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "rRepoDocs",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "A minimal GitHub-first Markdown workspace",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = AppThemeTokens.spacing.md),
        )
        if (!isAwaitingAuthorization) {
            Button(
                enabled = !isLoading,
                onClick = onStartSignIn,
                modifier = Modifier
                    .padding(top = AppThemeTokens.spacing.xl)
                    .heightIn(min = 48.dp),
            ) {
                Text(if (isLoading) "Preparing..." else "Connect with GitHub")
            }
        } else {
            Text(
                text = "1) Open GitHub and authorize this app",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = AppThemeTokens.spacing.xl),
            )
            userCode?.let { code ->
                SelectionContainer(
                    modifier = Modifier.padding(top = AppThemeTokens.spacing.sm),
                ) {
                    Text(
                        text = "Code: $code",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Button(
                enabled = !isLoading && (verificationUri != null || verificationUriComplete != null),
                onClick = {
                    uriHandler.openUri(verificationUriComplete ?: verificationUri.orEmpty())
                    onFinishSignIn()
                },
                modifier = Modifier
                    .padding(top = AppThemeTokens.spacing.md)
                    .heightIn(min = 48.dp),
            ) {
                Text(if (isLoading) "Waiting for GitHub..." else "Open GitHub Authorization")
            }
            Text(
                text = if (isLoading) {
                    "Waiting for approval in GitHub. This finishes automatically."
                } else {
                    "After authorizing in GitHub, return here and the app will continue automatically."
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = AppThemeTokens.spacing.sm),
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = AppThemeTokens.spacing.md),
            )
        }
    }
}
