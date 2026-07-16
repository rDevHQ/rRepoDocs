package com.rdev.rrepodocs.platform

interface WorkspacePreferencesStorage {
    fun loadLastRepositoryFullName(userId: String): String?
    fun saveLastRepositoryFullName(userId: String, fullName: String)
    fun clearLastRepositoryFullName(userId: String)
    fun clearAllLastRepositoryFullNames()
}

expect fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage
