package com.rdev.rrepodocs.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
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
                modifier = Modifier.padding(top = AppThemeTokens.spacing.xl),
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
                enabled = verificationUri != null || verificationUriComplete != null,
                onClick = {
                    uriHandler.openUri(verificationUriComplete ?: verificationUri.orEmpty())
                },
                modifier = Modifier.padding(top = AppThemeTokens.spacing.md),
            ) {
                Text("Open GitHub Authorization")
            }
            Button(
                enabled = !isLoading,
                onClick = onFinishSignIn,
                modifier = Modifier.padding(top = AppThemeTokens.spacing.md),
            ) {
                Text(if (isLoading) "Checking..." else "I Authorized, Continue")
            }
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
