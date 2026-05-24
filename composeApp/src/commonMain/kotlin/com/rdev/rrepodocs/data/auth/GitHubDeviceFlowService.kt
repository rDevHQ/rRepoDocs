package com.rdev.rrepodocs.data.auth

import com.rdev.rrepodocs.data.github.GitHubApiException
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class GitHubDeviceCode(
    val deviceCode: String,
    val userCode: String,
    val verificationUri: String,
    val verificationUriComplete: String?,
    val expiresInSeconds: Int,
    val intervalSeconds: Int,
)

class GitHubDeviceFlowService(
    private val httpClient: HttpClient = HttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun requestDeviceCode(clientId: String): Result<GitHubDeviceCode> {
        val trimmedClientId = clientId.trim()
        if (trimmedClientId.isBlank()) {
            return Result.failure(GitHubApiException("Enter a GitHub OAuth client ID."))
        }

        return runCatching {
            val response = httpClient.post("https://github.com/login/device/code") {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, "rRepoDocs")
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("client_id", trimmedClientId)
                            append("scope", "read:user user:email repo")
                        }
                    )
                )
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw GitHubApiException("Could not start GitHub sign-in (${response.status.value}).")
            }

            val payload = json.parseToJsonElement(body).jsonObject
            GitHubDeviceCode(
                deviceCode = payload["device_code"]?.jsonPrimitive?.content
                    ?: throw GitHubApiException("GitHub did not return device_code."),
                userCode = payload["user_code"]?.jsonPrimitive?.content
                    ?: throw GitHubApiException("GitHub did not return user_code."),
                verificationUri = payload["verification_uri"]?.jsonPrimitive?.content
                    ?: throw GitHubApiException("GitHub did not return verification_uri."),
                verificationUriComplete = payload["verification_uri_complete"]?.jsonPrimitive?.content,
                expiresInSeconds = payload["expires_in"]?.jsonPrimitive?.content?.toIntOrNull() ?: 900,
                intervalSeconds = payload["interval"]?.jsonPrimitive?.content?.toIntOrNull() ?: 5,
            )
        }.fold(
            onSuccess = Result.Companion::success,
            onFailure = { error ->
                Result.failure(
                    if (error is GitHubApiException) error else GitHubApiException(
                        "Could not start GitHub device sign-in.",
                        error
                    )
                )
            },
        )
    }

    suspend fun pollAccessToken(
        clientId: String,
        deviceCode: String,
        intervalSeconds: Int,
        expiresInSeconds: Int,
    ): Result<String> {
        val trimmedClientId = clientId.trim()
        if (trimmedClientId.isBlank()) {
            return Result.failure(GitHubApiException("Enter a GitHub OAuth client ID."))
        }
        if (deviceCode.isBlank()) {
            return Result.failure(GitHubApiException("Device code is missing. Start sign-in again."))
        }

        var pollingDelaySeconds = intervalSeconds.coerceAtLeast(1)
        val maxAttempts = (expiresInSeconds.coerceAtLeast(30) / pollingDelaySeconds).coerceAtLeast(1)

        repeat(maxAttempts) {
            val tokenResult = requestAccessTokenOnce(
                clientId = trimmedClientId,
                deviceCode = deviceCode,
            )
            tokenResult.fold(
                onSuccess = { token ->
                    return Result.success(token)
                },
                onFailure = { error ->
                    val normalized = error.message.orEmpty().lowercase()
                    when {
                        "authorization_pending" in normalized -> Unit
                        "slow_down" in normalized -> pollingDelaySeconds += 5
                        "expired_token" in normalized -> {
                            return Result.failure(
                                GitHubApiException("GitHub sign-in code expired. Start sign-in again.")
                            )
                        }
                        else -> {
                            return Result.failure(error)
                        }
                    }
                },
            )
            delay(pollingDelaySeconds * 1000L)
        }

        return Result.failure(
            GitHubApiException("GitHub sign-in timed out. Start sign-in again.")
        )
    }

    private suspend fun requestAccessTokenOnce(
        clientId: String,
        deviceCode: String,
    ): Result<String> {
        return runCatching {
            val response = httpClient.post("https://github.com/login/oauth/access_token") {
                header(HttpHeaders.Accept, "application/json")
                header(HttpHeaders.UserAgent, "rRepoDocs")
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("client_id", clientId)
                            append("device_code", deviceCode)
                            append("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
                        }
                    )
                )
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw GitHubApiException("GitHub token exchange failed (${response.status.value}).")
            }

            val payload = json.parseToJsonElement(body).jsonObject
            payload["access_token"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                ?: throw GitHubApiException(
                    payload["error"]?.jsonPrimitive?.content
                        ?: "GitHub did not return an access token."
                )
        }.fold(
            onSuccess = Result.Companion::success,
            onFailure = { error ->
                Result.failure(
                    if (error is GitHubApiException) error else GitHubApiException(
                        "GitHub token exchange failed.",
                        error
                    )
                )
            },
        )
    }
}
