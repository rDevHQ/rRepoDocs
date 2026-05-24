package com.rdev.rrepodocs.platform

import platform.UIKit.UIApplication

actual fun setPreviewScreenAwake(enabled: Boolean) {
    UIApplication.sharedApplication.idleTimerDisabled = enabled
}
