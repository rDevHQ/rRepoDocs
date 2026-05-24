package com.rdev.rrepodocs.presentation.editor

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset

expect fun Modifier.onDesktopContextPress(
    enabled: Boolean,
    onPress: (Offset) -> Unit,
): Modifier

expect fun Modifier.onDesktopPointerHover(
    onEnter: () -> Unit,
    onExit: () -> Unit,
): Modifier

expect fun Modifier.onDesktopFileDrag(
    enabled: Boolean,
    onDrag: (pointerPosition: Offset, dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
): Modifier

expect fun Modifier.onExplorerPrimaryClick(
    enabled: Boolean,
    onClick: (additiveSelection: Boolean) -> Unit,
): Modifier
