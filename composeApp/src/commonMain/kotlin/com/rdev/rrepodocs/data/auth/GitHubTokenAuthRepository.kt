package com.rdev.rrepodocs.data.auth

import com.rdev.rrepodocs.data.github.GitHubApiClient
import com.rdev.rrepodocs.data.github.GitHubApiException
import com.rdev.rrepodocs.domain.model.UserSession
import com.rdev.rrepodocs.platform.SecureSessionStorage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GitHubTokenAuthRepository(
    private val secureSessionStorage: SecureSessionStorage,
    private val gitHubApiClient: GitHubApiClient,
) : AuthRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun signIn(accessToken: String): Result<UserSession> {
        val trimmedToken = accessToken.trim()
        if (trimmedToken.isBlank()) {
            return Result.failure(GitHubApiException("Enter a GitHub access token."))
        }

        return gitHubApiClient.getCurrentUser(trimmedToken).fold(
            onSuccess = { response ->
                runCatching {
                    val payload = json.parseToJsonElement(response).jsonObject
                    val userId = payload["id"]?.jsonPrimitive?.content?.toLongOrNull()?.toString()
                        ?: throw GitHubApiException("GitHub user id missing in response.")
                    val username = payload["login"]?.jsonPrimitive?.content
                        ?: throw GitHubApiException("GitHub username missing in response.")
                    val avatarUrl = payload["avatar_url"]?.jsonPrimitive?.contentOrNull
                    UserSession(
                        userId = userId,
                        username = username,
                        avatarUrl = avatarUrl,
                        accessToken = trimmedToken,
                    ).also(secureSessionStorage::save)
                }.fold(
                    onSuccess = Result.Companion::success,
                    onFailure = { error ->
                        Result.failure(
                            if (error is GitHubApiException) {
                                error
                            } else {
                                GitHubApiException("Could not parse GitHub user profile.", error)
                            }
                        )
                    },
                )
            },
            onFailure = { error ->
                Result.failure(error)
            },
        )
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
