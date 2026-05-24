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

    override fun load(): UserSession? {
        val serialized = sessionPreferences.getString(SessionKey, null) ?: return null
        return decodeUserSession(serialized)
    }

    override fun save(session: UserSession) {
        sessionPreferences.edit()
            .putString(SessionKey, encodeUserSession(session))
            .apply()
    }

    override fun clear() {
        sessionPreferences.edit()
            .remove(SessionKey)
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
