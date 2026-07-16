package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession

class InMemorySecureSessionStorage : SecureSessionStorage {
    private val accounts = mutableListOf<UserSession>()
    private var activeUserId: String? = null

    override fun load(): UserSession? = accounts.firstOrNull { it.userId == activeUserId }

    override fun save(session: UserSession) {
        accounts.removeAll { it.userId == session.userId }
        accounts.add(session)
        activeUserId = session.userId
    }

    override fun loadAccounts(): List<UserSession> = accounts.toList()

    override fun setActiveAccount(userId: String): UserSession? {
        val account = accounts.firstOrNull { it.userId == userId } ?: return null
        activeUserId = userId
        return account
    }

    override fun removeAccount(userId: String) {
        accounts.removeAll { it.userId == userId }
        if (activeUserId == userId) activeUserId = accounts.firstOrNull()?.userId
    }

    override fun clear() {
        accounts.clear()
        activeUserId = null
    }
}
