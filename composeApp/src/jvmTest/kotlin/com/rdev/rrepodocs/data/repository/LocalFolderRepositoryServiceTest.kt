package com.rdev.rrepodocs.data.repository

import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.RepositorySource
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class LocalFolderRepositoryServiceTest {
    @Test
    fun `writes and moves files only inside the selected folder`() = runBlocking {
        val root = Files.createTempDirectory("rrepodocs-local-test")
        try {
            val repository = RepositoryRef(
                id = -1,
                name = "notes",
                fullName = root.toString(),
                ownerLogin = "Local folder",
                ownerAvatarUrl = null,
                isPrivate = true,
                source = RepositorySource.LocalFolder,
                localRootPath = root.toString(),
            )
            val service = provideLocalFolderRepositoryService()

            val created = service.createDocument(repository, DocumentPath("inbox/idea.md"), "# Idea").getOrThrow()
            assertEquals("# Idea", service.loadDocument(repository, created.path).getOrThrow().content)

            val moved = service.moveDocument(repository, created, DocumentPath("archive/idea.md")).getOrThrow()
            assertTrue(Files.exists(root.resolve("archive/idea.md")))
            assertFalse(Files.exists(root.resolve("inbox/idea.md")))
            assertEquals("archive/idea.md", moved.path.value)

            assertTrue(service.createDocument(repository, DocumentPath("../outside.md"), "no").isFailure)
        } finally {
            Files.walk(root).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::deleteIfExists) }
        }
    }
}
