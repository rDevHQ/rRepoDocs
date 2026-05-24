package com.rdev.rrepodocs.platform

import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

actual fun copyTextToClipboard(text: String): Boolean {
    return runCatching {
        Toolkit.getDefaultToolkit()
            .systemClipboard
            .setContents(StringSelection(text), null)
    }.isSuccess
}

actual fun openExternalUrl(url: String): Boolean {
    return runCatching {
        if (!Desktop.isDesktopSupported()) return false
        Desktop.getDesktop().browse(URI(url))
    }.isSuccess
}
