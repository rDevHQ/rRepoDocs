package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val KeychainService = "com.rdev.rrepodocs.accounts"
private const val KeychainAccount = "saved-github-accounts"

private class IosSessionStorage : SecureSessionStorage {
    private val storedAccounts = readStoredAccounts()
    private var accounts = storedAccounts.accounts.toMutableList()
    private var activeUserId = storedAccounts.activeUserId

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
        accounts.clear()
        activeUserId = null
        persist()
    }

    private fun persist() {
        deleteKeychainValue()
        if (accounts.isNotEmpty()) {
            writeKeychainValue(encodeStoredAccounts(accounts, activeUserId))
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readStoredAccounts(): StoredAccounts {
    val raw = memScoped {
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(keychainQuery(returnData = true), result.ptr)
        if (status != errSecSuccess) return@memScoped null
        val data = result.value as? NSData ?: return@memScoped null
        data.toByteArray().decodeToString()
    }
    return raw?.let(::decodeStoredAccounts)
        ?: raw?.let(::decodeUserSession)?.let { session ->
            StoredAccounts(accounts = listOf(session), activeUserId = session.userId)
        }
        ?: StoredAccounts(emptyList(), null)
}

@OptIn(ExperimentalForeignApi::class)
private fun writeKeychainValue(value: String) {
    SecItemAdd(
        keychainQuery(value = value.toByteArray().toNSData()),
        null,
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun deleteKeychainValue() {
    SecItemDelete(keychainQuery())
}

@OptIn(ExperimentalForeignApi::class)
private fun keychainQuery(
    returnData: Boolean = false,
    value: NSData? = null,
): CFDictionaryRef {
    return buildMap<Any?, Any?> {
        put(kSecClass, kSecClassGenericPassword)
        put(kSecAttrService, KeychainService)
        put(kSecAttrAccount, KeychainAccount)
        if (returnData) {
            put(kSecReturnData, true)
            put(kSecMatchLimit, kSecMatchLimitOne)
        }
        value?.let { put(kSecValueData, it) }
    } as CFDictionaryRef
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { bytes ->
    bytes.usePinned { pinned ->
        platform.posix.memcpy(pinned.addressOf(0), this.bytes, length)
    }
}

actual fun provideSecureSessionStorage(): SecureSessionStorage = IosSessionStorage()
