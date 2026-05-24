package com.rdev.rrepodocs.data.auth

import com.rdev.rrepodocs.domain.model.UserSession

interface AuthRepository {
    suspend fun signIn(accessToken: String): Result<UserSession>
    fun restoreSession(): UserSession?
    fun signOut()
}
