package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecureSessionStorageTest {
    @Test
    fun keepsMultipleAccountsAndSwitchesTheActiveAccount() {
        val storage = InMemorySecureSessionStorage()
        val first = session(userId = "1", username = "octo-one")
        val second = session(userId = "2", username = "octo-two")

        storage.save(first)
        storage.save(second)

        assertEquals(listOf(first, second), storage.loadAccounts())
        assertEquals(second, storage.load())
        assertEquals(first, storage.setActiveAccount(first.userId))
        assertEquals(first, storage.load())
    }

    @Test
    fun removingTheActiveAccountSelectsTheRemainingAccount() {
        val storage = InMemorySecureSessionStorage()
        val first = session(userId = "1", username = "octo-one")
        val second = session(userId = "2", username = "octo-two")
        storage.save(first)
        storage.save(second)

        storage.removeAccount(second.userId)

        assertEquals(listOf(first), storage.loadAccounts())
        assertEquals(first, storage.load())
        storage.clear()
        assertNull(storage.load())
    }

    @Test
    fun accountCodecRoundTripsTheActiveAccount() {
        val first = session(userId = "1", username = "octo-one")
        val second = session(userId = "2", username = "octo-two")

        assertEquals(
            StoredAccounts(accounts = listOf(first, second), activeUserId = second.userId),
            decodeStoredAccounts(encodeStoredAccounts(listOf(first, second), second.userId)),
        )
    }

    private fun session(userId: String, username: String) = UserSession(
        userId = userId,
        username = username,
        avatarUrl = null,
        accessToken = "token-$userId",
    )
}
