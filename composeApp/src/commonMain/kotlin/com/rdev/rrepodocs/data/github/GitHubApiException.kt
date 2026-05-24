package com.rdev.rrepodocs.data.github

class GitHubApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
