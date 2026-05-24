package com.rdev.rrepodocs.data.github

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess

class GitHubApiClient(
    private val httpClient: HttpClient = HttpClient(),
    private val baseUrl: String = "https://api.github.com",
) {
    suspend fun getCurrentUser(accessToken: String): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/user",
        )
    }

    suspend fun getAccessibleRepositories(accessToken: String): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/user/repos",
        ) {
            parameter("per_page", 100)
            parameter("sort", "updated")
            parameter("affiliation", "owner,collaborator,organization_member")
        }
    }

    suspend fun getRepositoryDetails(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
    ): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}",
        )
    }

    suspend fun getRepositoryTree(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
        reference: String,
        recursive: Boolean = true,
    ): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}/git/trees/${reference.urlPathPart()}",
        ) {
            if (recursive) {
                parameter("recursive", 1)
            }
        }
    }

    suspend fun getRepositoryContent(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
        path: String,
        reference: String,
    ): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}/contents/${path.urlPathPreservingSlashes()}",
        ) {
            parameter("ref", reference)
        }
    }

    suspend fun getRepositoryCommits(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
        path: String,
    ): Result<String> {
        return get(
            accessToken = accessToken,
            path = "/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}/commits",
        ) {
            parameter("path", path)
            parameter("per_page", 30)
        }
    }

    suspend fun updateRepositoryContent(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
        path: String,
        payload: String,
    ): Result<String> {
        return try {
            val response = httpClient.put(
                "$baseUrl/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}/contents/${path.urlPathPreservingSlashes()}"
            ) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.UserAgent, "rRepoDocs")
                header(HttpHeaders.ContentType, "application/json")
                setBody(payload)
            }

            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                Result.failure(
                    GitHubApiException("GitHub request failed (${response.status.value})")
                )
            } else {
                Result.success(body)
            }
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not contact GitHub API",
                    cause = error,
                )
            )
        }
    }

    suspend fun deleteRepositoryContent(
        accessToken: String,
        ownerLogin: String,
        repositoryName: String,
        path: String,
        payload: String,
    ): Result<String> {
        return try {
            val response = httpClient.delete(
                "$baseUrl/repos/${ownerLogin.urlPathPart()}/${repositoryName.urlPathPart()}/contents/${path.urlPathPreservingSlashes()}"
            ) {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.UserAgent, "rRepoDocs")
                header(HttpHeaders.ContentType, "application/json")
                setBody(payload)
            }

            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                Result.failure(
                    GitHubApiException("GitHub request failed (${response.status.value})")
                )
            } else {
                Result.success(body)
            }
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not contact GitHub API",
                    cause = error,
                )
            )
        }
    }

    private suspend fun get(
        accessToken: String,
        path: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null,
    ): Result<String> {
        return try {
            val response = httpClient.get("$baseUrl$path") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                header(HttpHeaders.Accept, "application/vnd.github+json")
                header("X-GitHub-Api-Version", "2022-11-28")
                header(HttpHeaders.UserAgent, "rRepoDocs")
                configure?.invoke(this)
            }

            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                Result.failure(
                    GitHubApiException("GitHub request failed (${response.status.value})")
                )
            } else {
                Result.success(body)
            }
        } catch (error: Throwable) {
            Result.failure(
                GitHubApiException(
                    message = "Could not contact GitHub API",
                    cause = error,
                )
            )
        }
    }
}

private fun String.urlPathPart(): String = encodeURLPathPart()

private fun String.urlPathPreservingSlashes(): String =
    trimStart('/')
        .split('/')
        .joinToString("/") { it.urlPathPart() }
