package com.rdev.rrepodocs.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object DesktopMenuBridge {
    var canCopyFile by mutableStateOf(false)
    var canPasteFile by mutableStateOf(false)
    var canImportFile by mutableStateOf(false)
    var canExportFile by mutableStateOf(false)
    var showNonMarkdownFiles by mutableStateOf(false)
    var inWorkspace by mutableStateOf(false)

    var onCopyFile: (() -> Unit)? = null
    var onPasteFile: (() -> Unit)? = null
    var onImportFile: (() -> Unit)? = null
    var onExportFile: (() -> Unit)? = null
    var onToggleShowNonMarkdownFiles: (() -> Unit)? = null
}
