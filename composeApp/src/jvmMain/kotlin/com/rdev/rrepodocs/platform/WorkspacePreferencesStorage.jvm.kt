package com.rdev.rrepodocs.platform

import java.util.prefs.Preferences

private const val WorkspaceNode = "com/rdev/rrepodocs/workspace"
private const val LastRepositoryFullNameKey = "last_repository_full_name"

private class DesktopWorkspacePreferencesStorage : WorkspacePreferencesStorage {
    private val preferences = Preferences.userRoot().node(WorkspaceNode)

    override fun loadLastRepositoryFullName(userId: String): String? {
        val accountKey = accountKey(userId)
        preferences.get(accountKey, null)?.let { return it }
        val legacyValue = preferences.get(LastRepositoryFullNameKey, null) ?: return null
        preferences.put(accountKey, legacyValue)
        preferences.remove(LastRepositoryFullNameKey)
        return legacyValue
    }

    override fun saveLastRepositoryFullName(userId: String, fullName: String) {
        preferences.put(accountKey(userId), fullName)
    }

    override fun clearLastRepositoryFullName(userId: String) {
        preferences.remove(accountKey(userId))
    }

    override fun clearAllLastRepositoryFullNames() {
        preferences.clear()
    }

    private fun accountKey(userId: String) = "$LastRepositoryFullNameKey.$userId"
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    return DesktopWorkspacePreferencesStorage()
}
