package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import java.util.prefs.Preferences

private const val SessionNode = "com/rdev/rrepodocs/session"
private const val SessionKey = "session_json"

private class DesktopSessionStorage : SecureSessionStorage {
    private val preferences = Preferences.userRoot().node(SessionNode)
    private val serialized = preferences.get(SessionKey, null)
    private val storedAccounts = serialized?.let(::decodeStoredAccounts)
    private var accounts = (storedAccounts?.accounts ?: serialized?.let(::decodeUserSession)?.let(::listOf).orEmpty())
        .toMutableList()
    private var activeUserId = storedAccounts?.activeUserId ?: accounts.firstOrNull()?.userId

    init {
        if (serialized != null && storedAccounts == null && accounts.isNotEmpty()) persist()
    }

    override fun load(): UserSession? = accounts.firstOrNull { it.userId == activeUserId }

    override fun save(session: UserSession) {
        accounts.removeAll { it.userId == session.userId }
        accounts.add(session)
        activeUserId = session.userId
        persist()
    }

    override fun loadAccounts(): List<UserSession> = accounts.toList()

    override fun setActiveAccount(userId: String): UserSession? {
        val account = accounts.firstOrNull { it.userId == userId } ?: return null
        activeUserId = userId
        persist()
        return account
    }

    override fun removeAccount(userId: String) {
        accounts.removeAll { it.userId == userId }
        if (activeUserId == userId) activeUserId = accounts.firstOrNull()?.userId
        persist()
    }

    override fun clear() {
        preferences.remove(SessionKey)
        accounts.clear()
        activeUserId = null
    }

    private fun persist() {
        if (accounts.isEmpty()) {
            preferences.remove(SessionKey)
        } else {
            preferences.put(SessionKey, encodeStoredAccounts(accounts, activeUserId))
        }
    }
}

actual fun provideSecureSessionStorage(): SecureSessionStorage {
    return DesktopSessionStorage()
}
