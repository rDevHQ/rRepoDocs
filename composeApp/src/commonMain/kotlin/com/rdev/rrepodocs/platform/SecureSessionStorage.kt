package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession

interface SecureSessionStorage {
    fun load(): UserSession?
    fun save(session: UserSession)
    fun loadAccounts(): List<UserSession>
    fun setActiveAccount(userId: String): UserSession?
    fun removeAccount(userId: String)
    fun clear()
}

expect fun provideSecureSessionStorage(): SecureSessionStorage
