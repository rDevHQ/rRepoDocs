package com.rdev.rrepodocs.platform

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.rdev.rrepodocs.domain.model.UserSession

private const val SessionPrefsFile = "rrepodocs.session.secure"
private const val SessionKey = "session_json"

internal object AndroidAppContextRegistry {
    var context: Context? = null
}

fun registerAndroidAppContext(context: Context) {
    AndroidAppContextRegistry.context = context.applicationContext
}

internal fun getRegisteredAndroidAppContext(): Context? {
    return AndroidAppContextRegistry.context
}

private class AndroidSecureSessionStorage(
    context: Context,
) : SecureSessionStorage {

    private val sessionPreferences: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            SessionPrefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }.getOrElse {
        // Fallback keeps persistence available if encrypted preferences cannot initialize.
        context.getSharedPreferences(SessionPrefsFile, Context.MODE_PRIVATE)
    }
    private val serialized = sessionPreferences.getString(SessionKey, null)
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
        accounts.clear()
        activeUserId = null
        persist()
    }

    private fun persist() {
        sessionPreferences.edit()
            .putString(SessionKey, encodeStoredAccounts(accounts, activeUserId))
            .apply()
    }
}

actual fun provideSecureSessionStorage(): SecureSessionStorage {
    val context = getRegisteredAndroidAppContext()
    return if (context != null) {
        AndroidSecureSessionStorage(context)
    } else {
        InMemorySecureSessionStorage()
    }
}
