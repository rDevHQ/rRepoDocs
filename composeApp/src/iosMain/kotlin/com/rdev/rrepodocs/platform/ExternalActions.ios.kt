package com.rdev.rrepodocs.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

actual fun copyTextToClipboard(text: String): Boolean {
    return runCatching {
        UIPasteboard.generalPasteboard.string = text
    }.isSuccess
}

actual fun openExternalUrl(url: String): Boolean {
    val nsUrl = NSURL.URLWithString(url) ?: return false
    return runCatching {
        UIApplication.sharedApplication.openURL(nsUrl)
    }.isSuccess
}
