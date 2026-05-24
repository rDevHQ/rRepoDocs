package com.rdev.rrepodocs.platform

import android.app.Activity
import android.view.WindowManager

private object AndroidActivityRegistry {
    var activity: Activity? = null
}

fun registerAndroidActivity(activity: Activity) {
    AndroidActivityRegistry.activity = activity
}

fun unregisterAndroidActivity(activity: Activity) {
    if (AndroidActivityRegistry.activity === activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AndroidActivityRegistry.activity = null
    }
}

actual fun setPreviewScreenAwake(enabled: Boolean) {
    val window = AndroidActivityRegistry.activity?.window ?: return
    if (enabled) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
