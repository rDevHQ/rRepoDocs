package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import java.util.prefs.Preferences

private const val SessionNode = "com/rdev/rrepodocs/session"
private const val SessionKey = "session_json"

private class DesktopSessionStorage : SecureSessionStorage {
    private val preferences = Preferences.userRoot().node(SessionNode)

    override fun load(): UserSession? {
        val serialized = preferences.get(SessionKey, null) ?: return null
        return decodeUserSession(serialized)
    }

    override fun save(session: UserSession) {
        preferences.put(SessionKey, encodeUserSession(session))
    }

    override fun clear() {
        preferences.remove(SessionKey)
    }
}

actual fun provideSecureSessionStorage(): SecureSessionStorage {
    return DesktopSessionStorage()
}
