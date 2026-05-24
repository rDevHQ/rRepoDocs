package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession

class InMemorySecureSessionStorage : SecureSessionStorage {
    private var cachedSession: UserSession? = null

    override fun load(): UserSession? = cachedSession

    override fun save(session: UserSession) {
        cachedSession = session
    }

    override fun clear() {
        cachedSession = null
    }
}
