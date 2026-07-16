package com.rdev.rrepodocs.presentation.editor

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GitHubFileLinkTest {
    @Test
    fun createsACompleteLinkForTheFileOnTheDefaultBranch() {
        assertEquals(
            "https://github.com/rdev/rRepoDocs/blob/main/recipes/pasta/Citron%20och%20chili.md",
            githubFileUrl(
                repositoryFullName = "rdev/rRepoDocs",
                defaultBranch = "main",
                path = "recipes/pasta/Citron och chili.md",
            ),
        )
    }

    @Test
    fun returnsNullForAnInvalidRepositoryOrFilePath() {
        assertNull(githubFileUrl("rRepoDocs", "main", "notes/todo.md"))
        assertNull(githubFileUrl("rdev/rRepoDocs", "main", ""))
    }
}
