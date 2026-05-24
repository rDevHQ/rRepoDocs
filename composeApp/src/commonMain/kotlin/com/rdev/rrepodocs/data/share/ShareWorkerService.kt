package com.rdev.rrepodocs.data.share

import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import com.rdev.rrepodocs.domain.model.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class ShareWorkerService(
    private val httpClient: HttpClient = HttpClient(),
    private val baseUrl: String = ShareWorkerConfig.defaultBaseUrl,
) : ShareService {
    override suspend fun loadDocumentShares(
        session: UserSession,
    ): Result<List<DocumentShare>> {
        return runCatching {
            val response = httpClient.get("${baseUrl.trimEnd('/')}/api/shares") {
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                header(HttpHeaders.Accept, "application/json")
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ShareWorkerException(errorMessageFromJson(body) ?: "Shared links request failed (${response.status.value}).")
            }
            parseDocumentShares(body)
        }
    }

    override suspend fun createDocumentShare(
        session: UserSession,
        repository: RepositoryRef,
        document: MarkdownDocument,
        expires: ShareExpiryOption,
    ): Result<DocumentShare> {
        val payload = buildJsonObject {
            put("repoFullName", repository.fullName)
            put("documentPath", document.path.value)
            document.sha?.let { put("sourceSha", it) }
            put("title", document.path.value.substringAfterLast('/').ifBlank { document.path.value })
            put("markdown", document.content)
            expires.days?.let { put("expiresInDays", it) } ?: put("expiresInDays", null as String?)
        }.toString()

        return runCatching {
            val response = httpClient.post("${baseUrl.trimEnd('/')}/api/shares") {
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                header(HttpHeaders.Accept, "application/json")
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ShareWorkerException(errorMessageFromJson(body) ?: "Share request failed (${response.status.value}).")
            }
            parseDocumentShare(body)
        }
    }

    override suspend fun revokeDocumentShare(
        session: UserSession,
        share: DocumentShare,
    ): Result<DocumentShare> {
        return runCatching {
            val response = httpClient.delete("${baseUrl.trimEnd('/')}/api/shares/${share.id.encodeURLPathPart()}") {
                header(HttpHeaders.Authorization, "Bearer ${session.accessToken}")
                header(HttpHeaders.Accept, "application/json")
            }
            val body = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ShareWorkerException(errorMessageFromJson(body) ?: "Revoke request failed (${response.status.value}).")
            }
            val revokedAt = Json.parseToJsonElement(body)
                .jsonObject["revokedAt"]
                ?.jsonPrimitive
                ?.contentOrNull
            share.copy(revokedAt = revokedAt)
        }
    }

    private fun parseDocumentShare(body: String): DocumentShare {
        val obj = Json.parseToJsonElement(body).jsonObject
        return parseDocumentShareObject(obj)
    }

    private fun parseDocumentShares(body: String): List<DocumentShare> {
        val obj = Json.parseToJsonElement(body).jsonObject
        val shares = obj["shares"] ?: return emptyList()
        return shares.jsonArray.mapNotNull { element ->
            runCatching { parseDocumentShareObject(element.jsonObject) }.getOrNull()
        }
    }

    private fun parseDocumentShareObject(obj: JsonObject): DocumentShare {
        return DocumentShare(
            id = obj["id"]?.jsonPrimitive?.content ?: throw ShareWorkerException("Share response was missing id."),
            shareUrl = obj["shareUrl"]?.jsonPrimitive?.content ?: throw ShareWorkerException("Share response was missing URL."),
            expiresAt = obj["expiresAt"]?.jsonPrimitive?.contentOrNull,
            createdAt = obj["createdAt"]?.jsonPrimitive?.content ?: "",
            revokedAt = obj["revokedAt"]?.jsonPrimitive?.contentOrNull,
            repoFullName = obj["repoFullName"]?.jsonPrimitive?.contentOrNull,
            documentPath = obj["documentPath"]?.jsonPrimitive?.contentOrNull,
            title = obj["title"]?.jsonPrimitive?.contentOrNull,
            sourceSha = obj["sourceSha"]?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun errorMessageFromJson(body: String): String? {
        return runCatching {
            val obj = Json.parseToJsonElement(body).jsonObject
            obj["message"]?.jsonPrimitive?.contentOrNull
        }.getOrNull()
    }
}

class ShareWorkerException(message: String) : RuntimeException(message)
