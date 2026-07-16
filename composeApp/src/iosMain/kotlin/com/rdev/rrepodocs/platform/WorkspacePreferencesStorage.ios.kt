package com.rdev.rrepodocs.platform

import platform.Foundation.NSUserDefaults

private const val LastRepositoryFullNameKey = "rrepodocs.last_repository_full_name"

private class IosWorkspacePreferencesStorage : WorkspacePreferencesStorage {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun loadLastRepositoryFullName(userId: String): String? {
        val accountKey = accountKey(userId)
        defaults.stringForKey(accountKey)?.let { return it }
        val legacyValue = defaults.stringForKey(LastRepositoryFullNameKey) ?: return null
        defaults.setObject(legacyValue, forKey = accountKey)
        defaults.removeObjectForKey(LastRepositoryFullNameKey)
        return legacyValue
    }

    override fun saveLastRepositoryFullName(userId: String, fullName: String) {
        defaults.setObject(fullName, forKey = accountKey(userId))
    }

    override fun clearLastRepositoryFullName(userId: String) {
        defaults.removeObjectForKey(accountKey(userId))
    }

    override fun clearAllLastRepositoryFullNames() { defaults.removeObjectForKey(LastRepositoryFullNameKey) }

    private fun accountKey(userId: String) = "$LastRepositoryFullNameKey.$userId"
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    return IosWorkspacePreferencesStorage()
}
