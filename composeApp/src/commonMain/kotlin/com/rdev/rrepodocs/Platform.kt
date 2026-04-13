package com.rdev.rrepodocs

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform