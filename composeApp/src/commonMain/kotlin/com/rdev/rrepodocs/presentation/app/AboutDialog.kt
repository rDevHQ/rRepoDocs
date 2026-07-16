package com.rdev.rrepodocs.presentation.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.platform.openExternalUrl
import com.rdev.rrepodocs.resources.Res
import com.rdev.rrepodocs.resources.app_icon
import org.jetbrains.compose.resources.painterResource

private const val ApplicationVersion = "1.0.0"
private const val LastUpdatedDate = "July 16, 2026"
private const val ApplicationWebsite = "https://rdevhq.github.io/#rrepodocs"

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = "rRepoDocs logo",
                modifier = Modifier.size(64.dp),
            )
        },
        title = {
            Text(
                text = "rRepoDocs",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "A GitHub-first Markdown editor.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Version $ApplicationVersion · Updated $LastUpdatedDate",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Developed by Robert Gustavsson at rDEV.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = { openExternalUrl(ApplicationWebsite) }) {
                Text("Visit website")
            }
        },
    )
}
