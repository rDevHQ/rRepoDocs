package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private val userSessionJson = Json {
    ignoreUnknownKeys = true
}

data class StoredAccounts(
    val accounts: List<UserSession>,
    val activeUserId: String?,
)

fun encodeUserSession(session: UserSession): String {
    return buildJsonObject {
        put("user_id", session.userId)
        put("username", session.username)
        session.avatarUrl?.let { put("avatar_url", it) }
        put("access_token", session.accessToken)
    }.toString()
}

fun decodeUserSession(raw: String): UserSession? {
    return runCatching {
        val jsonObject = userSessionJson.parseToJsonElement(raw).jsonObject
        UserSession(
            userId = jsonObject["user_id"]?.jsonPrimitive?.content ?: return null,
            username = jsonObject["username"]?.jsonPrimitive?.content ?: return null,
            avatarUrl = jsonObject["avatar_url"]?.jsonPrimitive?.contentOrNull,
            accessToken = jsonObject["access_token"]?.jsonPrimitive?.content ?: return null,
        )
    }.getOrNull()
}

fun encodeStoredAccounts(accounts: List<UserSession>, activeUserId: String?): String {
    return buildJsonObject {
        put("active_user_id", activeUserId.orEmpty())
        put("accounts", buildJsonArray {
            accounts.forEach { account ->
                add(userSessionJson.parseToJsonElement(encodeUserSession(account)))
            }
        })
    }.toString()
}

fun decodeStoredAccounts(raw: String): StoredAccounts? {
    return runCatching {
        val jsonObject = userSessionJson.parseToJsonElement(raw).jsonObject
        val accountValues = jsonObject["accounts"]?.jsonArray ?: return null
        val accounts = accountValues.mapNotNull { account -> decodeUserSession(account.toString()) }
        if (accounts.isEmpty()) return null
        val activeUserId = jsonObject["active_user_id"]?.jsonPrimitive?.contentOrNull
            ?.takeIf { activeId -> accounts.any { it.userId == activeId } }
            ?: accounts.first().userId
        StoredAccounts(accounts = accounts, activeUserId = activeUserId)
    }.getOrNull()
}
