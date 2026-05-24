package com.rdev.rrepodocs.platform

import platform.Foundation.NSUserDefaults

private const val LastRepositoryFullNameKey = "rrepodocs.last_repository_full_name"

private class IosWorkspacePreferencesStorage : WorkspacePreferencesStorage {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun loadLastRepositoryFullName(): String? {
        return defaults.stringForKey(LastRepositoryFullNameKey)
    }

    override fun saveLastRepositoryFullName(fullName: String) {
        defaults.setObject(fullName, forKey = LastRepositoryFullNameKey)
    }

    override fun clearLastRepositoryFullName() {
        defaults.removeObjectForKey(LastRepositoryFullNameKey)
    }
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    return IosWorkspacePreferencesStorage()
}
