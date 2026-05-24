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

    override fun loadLastRepositoryFullName(): String? {
        return preferences.getString(LastRepositoryFullNameKey, null)
    }

    override fun saveLastRepositoryFullName(fullName: String) {
        preferences.edit()
            .putString(LastRepositoryFullNameKey, fullName)
            .apply()
    }

    override fun clearLastRepositoryFullName() {
        preferences.edit()
            .remove(LastRepositoryFullNameKey)
            .apply()
    }
}

actual fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage {
    val context = getRegisteredAndroidAppContext()
    return if (context != null) {
        AndroidWorkspacePreferencesStorage(context)
    } else {
        object : WorkspacePreferencesStorage {
            private var lastRepositoryFullName: String? = null

            override fun loadLastRepositoryFullName(): String? = lastRepositoryFullName

            override fun saveLastRepositoryFullName(fullName: String) {
                lastRepositoryFullName = fullName
            }

            override fun clearLastRepositoryFullName() {
                lastRepositoryFullName = null
            }
        }
    }
}
