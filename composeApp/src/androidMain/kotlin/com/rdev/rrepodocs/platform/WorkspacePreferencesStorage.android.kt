package com.rdev.rrepodocs.platform

import android.content.Context
import android.content.SharedPreferences

private const val WorkspacePrefsFile = "rrepodocs.workspace"
private const val LastRepositoryFullNameKey = "last_repository_full_name"

private class AndroidWorkspacePreferencesStorage(
    context: Context,
) : WorkspacePreferencesStorage {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        WorkspacePrefsFile,
        Context.MODE_PRIVATE,
    )

    override fun loadLastRepositoryFullName(userId: String): String? {
        val accountKey = accountKey(userId)
        preferences.getString(accountKey, null)?.let { return it }
        val legacyValue = preferences.getString(LastRepositoryFullNameKey, null) ?: return null
        preferences.edit().putString(accountKey, legacyValue).remove(LastRepositoryFullNameKey).apply()
        return legacyValue
    }

    override fun saveLastRepositoryFullName(userId: String, fullName: String) {
        preferences.edit()
            .putString(accountKey(userId), fullName)
            .apply()
    }

    override fun clearLastRepositoryFullName(userId: String) {
        preferences.edit()
            .remove(accountKey(userId))
            .apply()
    }

    override fun clearAllLastRepositoryFullNames() {
        preferences.edit().clear().apply()
    }

    private fun accountKey(userId: String) = "$LastRepositoryFullNameKey.$userId"
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    val context = getRegisteredAndroidAppContext()
    return if (context != null) {
        AndroidWorkspacePreferencesStorage(context)
    } else {
        object : WorkspacePreferencesStorage {
            private var lastRepositoryFullName: String? = null

            override fun loadLastRepositoryFullName(userId: String): String? = lastRepositoryFullName

            override fun saveLastRepositoryFullName(userId: String, fullName: String) {
                lastRepositoryFullName = fullName
            }

            override fun clearLastRepositoryFullName(userId: String) {
                lastRepositoryFullName = null
            }

            override fun clearAllLastRepositoryFullNames() { lastRepositoryFullName = null }
        }
    }
}
