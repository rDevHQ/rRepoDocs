package com.rdev.rrepodocs.platform

actual fun provideGitHubOAuthClientId(): String? {
    val configured = GitHubAuthConfig.defaultClientId.trim()
    if (configured.isNotEmpty()) return configured

    return System.getenv("RREPODOCS_GITHUB_CLIENT_ID")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
