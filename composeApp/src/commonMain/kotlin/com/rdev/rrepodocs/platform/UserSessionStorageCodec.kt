package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.UserSession
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

private val userSessionJson = Json {
    ignoreUnknownKeys = true
}

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
