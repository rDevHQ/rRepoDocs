package com.rdev.rrepodocs.domain.model

data class UserSession(
    val userId: String,
    val username: String,
    val avatarUrl: String? = null,
    val accessToken: String,
)
