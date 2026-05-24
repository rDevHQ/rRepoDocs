package com.rdev.rrepodocs.platform

import java.util.prefs.Preferences

private const val WorkspaceNode = "com/rdev/rrepodocs/workspace"
private const val LastRepositoryFullNameKey = "last_repository_full_name"

private class DesktopWorkspacePreferencesStorage : WorkspacePreferencesStorage {
    private val preferences = Preferences.userRoot().node(WorkspaceNode)

    override fun loadLastRepositoryFullName(): String? {
        return preferences.get(LastRepositoryFullNameKey, null)
    }

    override fun saveLastRepositoryFullName(fullName: String) {
        preferences.put(LastRepositoryFullNameKey, fullName)
    }

    override fun clearLastRepositoryFullName() {
        preferences.remove(LastRepositoryFullNameKey)
    }
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    return DesktopWorkspacePreferencesStorage()
}
