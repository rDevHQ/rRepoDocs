package com.rdev.rrepodocs.presentation.app

import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppViewModelShareTest {
    @Test
    fun shareDialogRequiresActiveDocument() {
        val viewModel = AppViewModel()

        viewModel.requestShowShareDialog()

        assertFalse(viewModel.uiState.shareDialogVisible)
        assertEquals("Open a markdown file before sharing.", viewModel.uiState.shareError)
    }

    @Test
    fun dirtyDocumentBlocksShareCreation() {
        val viewModel = AppViewModel()
        viewModel.onDocumentLoaded(testDocument())
        viewModel.updateDocumentContent("# Changed")

        viewModel.requestShowShareDialog()
        viewModel.requestCreateShare()

        assertTrue(viewModel.uiState.shareDialogVisible)
        assertEquals("Save current edits before creating a public share.", viewModel.uiState.shareError)
        assertEquals(0, viewModel.uiState.shareRequestNonce)
    }

    @Test
    fun shareCreationUpdatesState() {
        val viewModel = AppViewModel()
        viewModel.onDocumentLoaded(testDocument())
        viewModel.requestShowShareDialog()
        viewModel.updateShareExpiryOption(ShareExpiryOption.SevenDays)

        viewModel.requestCreateShare()
        viewModel.beginShareCreate()
        viewModel.onShareCreated(testShare())

        assertEquals(ShareExpiryOption.SevenDays, viewModel.uiState.shareExpiryOption)
        assertFalse(viewModel.uiState.shareInProgress)
        assertEquals("https://share.test/s/share-1", viewModel.uiState.activeShare?.shareUrl)
        assertEquals(null, viewModel.uiState.shareError)
    }

    @Test
    fun createdShareDoesNotRequestImmediateRevoke() {
        val viewModel = AppViewModel()
        viewModel.onDocumentLoaded(testDocument())
        viewModel.requestShowShareDialog()
        viewModel.requestCreateShare()

        viewModel.beginShareCreate()
        viewModel.onShareCreated(testShare())

        assertEquals(null, viewModel.uiState.pendingShareRevokeId)
        assertFalse(viewModel.uiState.shareRevokeInProgress)
        assertEquals(null, viewModel.uiState.activeShare?.revokedAt)
    }

    @Test
    fun shareRevokeUpdatesActiveShare() {
        val viewModel = AppViewModel()
        viewModel.onDocumentLoaded(testDocument())
        viewModel.onShareCreated(testShare())

        viewModel.requestRevokeShare()
        assertEquals("share-1", viewModel.uiState.pendingShareRevokeId)
        viewModel.beginShareRevoke()
        viewModel.onShareRevoked(testShare().copy(revokedAt = "2026-04-26T10:00:00Z"))

        assertFalse(viewModel.uiState.shareRevokeInProgress)
        assertEquals(null, viewModel.uiState.pendingShareRevokeId)
        assertNotNull(viewModel.uiState.activeShare?.revokedAt)
        assertEquals(null, viewModel.uiState.shareRevokeError)
    }

    @Test
    fun sharedLinksDialogLoadsAndRevokesListedShare() {
        val viewModel = AppViewModel()
        val share = testShare().copy(documentPath = "docs/readme.md", title = "Readme")

        viewModel.requestShowSharedLinksDialog()
        viewModel.beginSharedLinksLoad()
        viewModel.onSharedLinksLoaded(listOf(share))

        assertTrue(viewModel.uiState.sharedLinksDialogVisible)
        assertFalse(viewModel.uiState.sharedLinksLoading)
        assertEquals(listOf(share), viewModel.uiState.sharedLinks)

        viewModel.requestRevokeSharedLink(share)
        assertEquals("share-1", viewModel.uiState.pendingShareRevokeId)
        assertEquals(share, viewModel.uiState.activeShare)

        viewModel.beginShareRevoke()
        viewModel.onShareRevoked(share.copy(revokedAt = "2026-04-26T10:00:00Z"))

        assertEquals(emptyList(), viewModel.uiState.sharedLinks)
        assertEquals(null, viewModel.uiState.pendingShareRevokeId)
    }

    private fun testDocument(): MarkdownDocument {
        return MarkdownDocument(
            path = DocumentPath("docs/readme.md"),
            content = "# Readme",
            sha = "abc123",
        )
    }

    private fun testShare(): DocumentShare {
        return DocumentShare(
            id = "share-1",
            shareUrl = "https://share.test/s/share-1",
            expiresAt = "2026-05-26T10:00:00Z",
            createdAt = "2026-04-26T10:00:00Z",
        )
    }
}
