package com.rdev.rrepodocs.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rdev.rrepodocs.domain.model.UserSession

object DesktopMenuBridge {
    var canCopyFile by mutableStateOf(false)
    var canPasteFile by mutableStateOf(false)
    var canImportFile by mutableStateOf(false)
    var canExportFile by mutableStateOf(false)
    var canExportPdf by mutableStateOf(false)
    var canPrintPreview by mutableStateOf(false)
    var canShareDocument by mutableStateOf(false)
    var isSignedIn by mutableStateOf(false)
    var githubProfileUrl by mutableStateOf<String?>(null)
    var accounts by mutableStateOf<List<UserSession>>(emptyList())
    var activeAccountId by mutableStateOf<String?>(null)
    var showNonMarkdownFiles by mutableStateOf(false)
    var inWorkspace by mutableStateOf(false)

    var onCopyFile: (() -> Unit)? = null
    var onPasteFile: (() -> Unit)? = null
    var onImportFile: (() -> Unit)? = null
    var onExportFile: (() -> Unit)? = null
    var onExportPdf: (() -> Unit)? = null
    var onPrintPreview: (() -> Unit)? = null
    var onShareDocument: (() -> Unit)? = null
    var onShowSharedLinks: (() -> Unit)? = null
    var onSwitchRepository: (() -> Unit)? = null
    var onSwitchAccount: ((String) -> Unit)? = null
    var onAddAccount: (() -> Unit)? = null
    var onOpenGitHubProfile: (() -> Unit)? = null
    var onSignOut: (() -> Unit)? = null
    var onToggleShowNonMarkdownFiles: (() -> Unit)? = null
    var onShowAbout: (() -> Unit)? = null
}
