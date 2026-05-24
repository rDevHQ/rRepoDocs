package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isCtrlPressed
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onDesktopContextPress(
    enabled: Boolean,
    onPress: (Offset) -> Unit,
): Modifier {
    if (!enabled) return this

    return onPointerEvent(PointerEventType.Press) { event ->
        val contextPress = event.buttons.isSecondaryPressed ||
            (event.buttons.isPrimaryPressed && event.keyboardModifiers.isCtrlPressed)
        if (contextPress) {
            event.changes.firstOrNull()?.position?.let(onPress)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onDesktopPointerHover(
    onEnter: () -> Unit,
    onExit: () -> Unit,
): Modifier {
    return onPointerEvent(PointerEventType.Enter) { onEnter() }
        .onPointerEvent(PointerEventType.Exit) { onExit() }
}

actual fun Modifier.onDesktopFileDrag(
    enabled: Boolean,
    onDrag: (pointerPosition: Offset, dragAmount: Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
): Modifier {
    if (!enabled) return this

    return pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                onDrag(change.position, dragAmount)
            },
            onDragEnd = onDragEnd,
            onDragCancel = onDragCancel,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun Modifier.onExplorerPrimaryClick(
    enabled: Boolean,
    onClick: (additiveSelection: Boolean) -> Unit,
): Modifier {
    if (!enabled) return this

    return onPointerEvent(PointerEventType.Press) { event ->
        if (event.buttons.isPrimaryPressed) {
            onClick(event.keyboardModifiers.isMetaPressed || event.keyboardModifiers.isCtrlPressed)
        }
    }
}
