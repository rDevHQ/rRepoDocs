package com.rdev.rrepodocs.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri

actual fun copyTextToClipboard(text: String): Boolean {
    val context = getRegisteredAndroidAppContext() ?: return false
    return runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("rRepoDocs share link", text))
    }.isSuccess
}

actual fun openExternalUrl(url: String): Boolean {
    val context = getRegisteredAndroidAppContext() ?: return false
    return runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }.isSuccess
}
