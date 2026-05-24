package com.rdev.rrepodocs.platform

import platform.Foundation.NSProcessInfo

actual fun provideGitHubOAuthClientId(): String? {
    val configured = GitHubAuthConfig.defaultClientId.trim()
    if (configured.isNotEmpty()) return configured

    val environment = NSProcessInfo.processInfo.environment
    val raw = environment["RREPODOCS_GITHUB_CLIENT_ID"] as? String ?: return null
    return raw.trim().takeIf { it.isNotEmpty() }
}
