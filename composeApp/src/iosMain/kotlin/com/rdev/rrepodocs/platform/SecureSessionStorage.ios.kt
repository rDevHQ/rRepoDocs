package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import platform.Foundation.NSUserDefaults

private const val SessionStorageKey = "rrepodocs.session"

private class IosSessionStorage : SecureSessionStorage {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun load(): UserSession? {
        val raw = defaults.stringForKey(SessionStorageKey) ?: return null
        return decodeUserSession(raw)
    }

    override fun save(session: UserSession) {
        defaults.setObject(encodeUserSession(session), forKey = SessionStorageKey)
    }

    override fun clear() {
        defaults.removeObjectForKey(SessionStorageKey)
    }
}

actual fun provideSecureSessionStorage(): SecureSessionStorage {
    return IosSessionStorage()
}
