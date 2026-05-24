package com.rdev.rrepodocs.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rdev.rrepodocs.data.auth.AuthRepository
import com.rdev.rrepodocs.data.auth.GitHubDeviceFlowService
import com.rdev.rrepodocs.domain.model.AuthState
import com.rdev.rrepodocs.domain.model.UserSession

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val gitHubDeviceFlowService: GitHubDeviceFlowService,
    private val configuredClientId: String?,
) {
    var authState by mutableStateOf<AuthState>(AuthState.SignedOut)
        private set
    var verificationUri by mutableStateOf<String?>(null)
        private set
    var verificationUriComplete by mutableStateOf<String?>(null)
        private set
    var userCode by mutableStateOf<String?>(null)
        private set
    var isAwaitingAuthorization by mutableStateOf(false)
        private set

    private var pendingDeviceCode: String? = null
    private var pendingDeviceExpirySeconds: Int = 900
    private var pendingDeviceIntervalSeconds: Int = 5

    fun restoreSession(): UserSession? {
        val restoredSession = authRepository.restoreSession()
        authState = if (restoredSession == null) {
            AuthState.SignedOut
        } else {
            AuthState.SignedIn(restoredSession)
        }
        return restoredSession
    }

    suspend fun startDeviceSignIn(): Boolean {
        val clientId = configuredClientId?.trim().orEmpty()
        if (clientId.isBlank()) {
            authState = AuthState.Error(
                "GitHub OAuth is not configured. Set GitHubAuthConfig.defaultClientId and restart."
            )
            return false
        }
        authState = AuthState.Loading
        val result = gitHubDeviceFlowService.requestDeviceCode(clientId)
        return result.fold(
            onSuccess = { deviceCode ->
                pendingDeviceCode = deviceCode.deviceCode
                pendingDeviceExpirySeconds = deviceCode.expiresInSeconds
                pendingDeviceIntervalSeconds = deviceCode.intervalSeconds
                verificationUri = deviceCode.verificationUri
                verificationUriComplete = deviceCode.verificationUriComplete
                userCode = deviceCode.userCode
                isAwaitingAuthorization = true
                authState = AuthState.SignedOut
                true
            },
            onFailure = { throwable ->
                authState = AuthState.Error(throwable.message ?: "Could not start GitHub sign-in.")
                false
            },
        )
    }

    suspend fun finishDeviceSignIn(): UserSession? {
        val deviceCode = pendingDeviceCode
        if (deviceCode.isNullOrBlank()) {
            authState = AuthState.Error("Start GitHub sign-in first.")
            return null
        }

        authState = AuthState.Loading
        val tokenResult = gitHubDeviceFlowService.pollAccessToken(
            clientId = configuredClientId.orEmpty(),
            deviceCode = deviceCode,
            intervalSeconds = pendingDeviceIntervalSeconds,
            expiresInSeconds = pendingDeviceExpirySeconds,
        )
        val result = tokenResult.fold(
            onSuccess = { token -> authRepository.signIn(token) },
            onFailure = { error -> Result.failure(error) },
        )
        return result.fold(
            onSuccess = { session ->
                authState = AuthState.SignedIn(session)
                clearPendingDeviceFlow()
                session
            },
            onFailure = { throwable ->
                authState = AuthState.Error(throwable.message ?: "Sign-in failed")
                null
            },
        )
    }

    fun signOut() {
        authRepository.signOut()
        clearPendingDeviceFlow()
        authState = AuthState.SignedOut
    }

    private fun clearPendingDeviceFlow() {
        pendingDeviceCode = null
        pendingDeviceExpirySeconds = 900
        pendingDeviceIntervalSeconds = 5
        verificationUri = null
        verificationUriComplete = null
        userCode = null
        isAwaitingAuthorization = false
    }
}
