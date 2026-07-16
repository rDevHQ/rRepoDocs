package com.rdev.rrepodocs.data.auth

import com.rdev.rrepodocs.domain.model.UserSession
import com.rdev.rrepodocs.platform.SecureSessionStorage

class MockAuthRepository(
    private val secureSessionStorage: SecureSessionStorage,
) : AuthRepository {

    override suspend fun signIn(accessToken: String): Result<UserSession> {
        val session = UserSession(
            userId = "mock-user-id",
            username = "demo-user",
            avatarUrl = null,
            accessToken = "mock-github-access-token",
        )
        secureSessionStorage.save(session)
        return Result.success(session)
    }

    override fun restoreSession(): UserSession? = secureSessionStorage.load()

    override fun loadAccounts(): List<UserSession> = secureSessionStorage.loadAccounts()

    override fun switchAccount(userId: String): UserSession? = secureSessionStorage.setActiveAccount(userId)

    override fun removeAccount(userId: String) {
        secureSessionStorage.removeAccount(userId)
    }

    override fun signOut() {
        secureSessionStorage.clear()
    }
}
