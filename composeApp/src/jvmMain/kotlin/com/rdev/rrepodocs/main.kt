package com.rdev.rrepodocs

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.rdev.rrepodocs.resources.Res
import com.rdev.rrepodocs.resources.app_icon
import com.rdev.rrepodocs.presentation.app.DesktopMenuBridge
import org.jetbrains.compose.resources.painterResource
import java.util.prefs.Preferences
import kotlin.math.roundToInt

private const val WindowPrefsNode = "com.rdev.rrepodocs.window"
private const val WindowWidthKey = "window_width_dp"
private const val WindowHeightKey = "window_height_dp"
private const val WindowXKey = "window_x_dp"
private const val WindowYKey = "window_y_dp"
private const val WindowMaximizedKey = "window_maximized"
private const val MissingCoord = Int.MIN_VALUE
private val FirstRunWindowSize = DpSize(width = 1440.dp, height = 920.dp)

fun main() {
    configureMacOsApplication()

    application {
        val windowPrefs = remember { Preferences.userRoot().node(WindowPrefsNode) }
        val savedWidth = remember { windowPrefs.getInt(WindowWidthKey, -1) }
        val savedHeight = remember { windowPrefs.getInt(WindowHeightKey, -1) }
        val savedX = remember { windowPrefs.getInt(WindowXKey, MissingCoord) }
        val savedY = remember { windowPrefs.getInt(WindowYKey, MissingCoord) }
        val savedMaximized = remember { windowPrefs.getBoolean(WindowMaximizedKey, false) }

        val initialSize = if (savedWidth > 0 && savedHeight > 0) {
            DpSize(savedWidth.dp, savedHeight.dp)
        } else {
            FirstRunWindowSize
        }
        val initialPosition = if (savedX != MissingCoord && savedY != MissingCoord) {
            WindowPosition(savedX.dp, savedY.dp)
        } else {
            WindowPosition.Aligned(Alignment.Center)
        }
        val initialPlacement = if (savedMaximized) WindowPlacement.Maximized else WindowPlacement.Floating

        val windowState = rememberWindowState(
            size = initialSize,
            position = initialPosition,
            placement = initialPlacement,
        )

        Window(
            state = windowState,
            onCloseRequest = {
                persistWindowState(windowPrefs, windowState)
                exitApplication()
            },
            title = "rRepoDocs",
            icon = painterResource(Res.drawable.app_icon),
        ) {
            MenuBar {
                Menu("File") {
                    Item(
                        text = "Import Markdown...",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canImportFile,
                        onClick = { DesktopMenuBridge.onImportFile?.invoke() },
                    )
                    Item(
                        text = "Export Markdown...",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canExportFile,
                        onClick = { DesktopMenuBridge.onExportFile?.invoke() },
                    )
                    Separator()
                    Item(
                        text = "Copy File",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canCopyFile,
                        onClick = { DesktopMenuBridge.onCopyFile?.invoke() },
                    )
                    Item(
                        text = "Paste File",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canPasteFile,
                        onClick = { DesktopMenuBridge.onPasteFile?.invoke() },
                    )
                }
                Menu("Edit") {
                    Item(
                        text = "Copy File",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canCopyFile,
                        onClick = { DesktopMenuBridge.onCopyFile?.invoke() },
                    )
                    Item(
                        text = "Paste File",
                        enabled = DesktopMenuBridge.inWorkspace && DesktopMenuBridge.canPasteFile,
                        onClick = { DesktopMenuBridge.onPasteFile?.invoke() },
                    )
                }
                Menu("View") {
                    CheckboxItem(
                        text = "Show non-markdown files",
                        checked = DesktopMenuBridge.showNonMarkdownFiles,
                        enabled = DesktopMenuBridge.inWorkspace,
                        onCheckedChange = { DesktopMenuBridge.onToggleShowNonMarkdownFiles?.invoke() },
                    )
                }
                Menu("Window") {}
                Menu("Help") {
                    Item(
                        text = "rRepoDocs",
                        enabled = false,
                        onClick = {},
                    )
                }
            }
            App()
        }
    }
}

private fun configureMacOsApplication() {
    if (!System.getProperty("os.name").contains("Mac", ignoreCase = true)) return
    System.setProperty("apple.awt.application.name", "rRepoDocs")
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "rRepoDocs")
    System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua")
}

private fun persistWindowState(
    preferences: Preferences,
    windowState: WindowState,
) {
    preferences.putBoolean(WindowMaximizedKey, windowState.placement == WindowPlacement.Maximized)
    if (windowState.placement != WindowPlacement.Floating) return

    preferences.putInt(WindowWidthKey, windowState.size.width.value.roundToInt())
    preferences.putInt(WindowHeightKey, windowState.size.height.value.roundToInt())

    when (val position = windowState.position) {
        is WindowPosition.Absolute -> {
            preferences.putInt(WindowXKey, position.x.value.roundToInt())
            preferences.putInt(WindowYKey, position.y.value.roundToInt())
        }
        else -> {
            preferences.remove(WindowXKey)
            preferences.remove(WindowYKey)
        }
    }
}
