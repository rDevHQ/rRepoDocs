package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.clickable

actual fun Modifier.onDesktopContextPress(
    enabled: Boolean,
    onPress: (Offset) -> Unit,
): Modifier = this

actual fun Modifier.onDesktopPointerHover(
    onEnter: () -> Unit,
    onExit: () -> Unit,
): Modifier = this

actual fun Modifier.onDesktopFileDrag(
    enabled: Boolean,
    onDrag: (pointerPosition: Offset, dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
): Modifier = this

actual fun Modifier.onExplorerPrimaryClick(
    enabled: Boolean,
    onClick: (additiveSelection: Boolean) -> Unit,
): Modifier = if (enabled) clickable { onClick(false) } else this
