package com.rdev.rrepodocs.platform

interface WorkspacePreferencesStorage {
    fun loadLastRepositoryFullName(): String?
    fun saveLastRepositoryFullName(fullName: String)
    fun clearLastRepositoryFullName()
}

expect fun provideWorkspacePreferencesStorage(): WorkspacePreferencesStorage
