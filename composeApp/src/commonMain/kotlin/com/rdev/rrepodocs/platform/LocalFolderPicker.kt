package com.rdev.rrepodocs.platform

import com.rdev.rrepodocs.domain.model.RepositoryRef

expect suspend fun pickLocalMarkdownFolder(): RepositoryRef?
