package com.rdev.rrepodocs.presentation.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rdev.rrepodocs.domain.model.DocumentEditState
import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import com.rdev.rrepodocs.domain.model.MarkdownDocument
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepoTreeNodeKind
import com.rdev.rrepodocs.domain.model.RepositoryRef
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import com.rdev.rrepodocs.domain.model.UserSession

data class MarkdownExportSnapshot(
    val fileName: String,
    val content: String,
)

class AppViewModel {
    var uiState by mutableStateOf(
        AppUiState(
            mode = AppMode.SignedOut,
        )
    )
        private set

    fun onSignedIn(session: UserSession) {
        uiState = uiState.copy(
            mode = AppMode.RepositorySelection,
            session = session,
            repositoryOptions = emptyList(),
            selectedRepository = null,
            showNonMarkdownFiles = false,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            selectedMarkdownPath = null,
            activeDocument = null,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = null,
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            documentLoadNonce = uiState.documentLoadNonce + 1,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = null,
            saveRequestNonce = uiState.saveRequestNonce + 1,
            createDialogVisible = false,
            createTargetFolderDraft = "",
            createFileNameDraft = DEFAULT_NEW_FILE_NAME,
            createCommitMessageDraft = defaultCreateCommitMessage(DEFAULT_NEW_FILE_NAME),
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createInProgress = false,
            createError = null,
            createRequestNonce = uiState.createRequestNonce + 1,
            renameDialogVisible = false,
            renameFileNameDraft = "",
            renameCommitMessageDraft = "",
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameInProgress = false,
            renameError = null,
            renameRequestNonce = uiState.renameRequestNonce + 1,
            moveDialogVisible = false,
            moveDestinationFolderDraft = "",
            moveCommitMessageDraft = "",
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            moveRequestNonce = uiState.moveRequestNonce + 1,
            copiedMarkdownPath = null,
            pasteRequestSourcePath = null,
            pasteRequestDestinationPath = null,
            pasteRequestCommitMessage = "",
            pasteInProgress = false,
            pasteError = null,
            pasteRequestNonce = uiState.pasteRequestNonce + 1,
            shareDialogVisible = false,
            shareExpiryOption = ShareExpiryOption.ThirtyDays,
            activeShare = null,
            shareInProgress = false,
            shareError = null,
            shareRequestNonce = uiState.shareRequestNonce + 1,
            pendingShareRevokeId = null,
            shareRevokeInProgress = false,
            shareRevokeError = null,
            shareRevokeRequestNonce = uiState.shareRevokeRequestNonce + 1,
            sharedLinksDialogVisible = false,
            sharedLinks = emptyList(),
            sharedLinksLoading = false,
            sharedLinksError = null,
            sharedLinksLoadNonce = uiState.sharedLinksLoadNonce + 1,
            repoTreeLoading = false,
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
            repositoriesLoading = false,
            repositoriesError = null,
            repositoryLoadNonce = uiState.repositoryLoadNonce + 1,
        )
    }

    fun signOut() {
        uiState = uiState.copy(
            mode = AppMode.SignedOut,
            session = null,
            repositoryOptions = emptyList(),
            selectedRepository = null,
            showNonMarkdownFiles = false,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            selectedMarkdownPath = null,
            activeDocument = null,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentEditState = null,
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = null,
            createDialogVisible = false,
            createTargetFolderDraft = "",
            createFileNameDraft = DEFAULT_NEW_FILE_NAME,
            createCommitMessageDraft = defaultCreateCommitMessage(DEFAULT_NEW_FILE_NAME),
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createInProgress = false,
            createError = null,
            renameDialogVisible = false,
            renameFileNameDraft = "",
            renameCommitMessageDraft = "",
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameInProgress = false,
            renameError = null,
            moveDialogVisible = false,
            moveDestinationFolderDraft = "",
            moveCommitMessageDraft = "",
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            copiedMarkdownPath = null,
            pasteRequestSourcePath = null,
            pasteRequestDestinationPath = null,
            pasteRequestCommitMessage = "",
            pasteInProgress = false,
            pasteError = null,
            shareDialogVisible = false,
            shareExpiryOption = ShareExpiryOption.ThirtyDays,
            activeShare = null,
            shareInProgress = false,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeInProgress = false,
            shareRevokeError = null,
            sharedLinksDialogVisible = false,
            sharedLinks = emptyList(),
            sharedLinksLoading = false,
            sharedLinksError = null,
            repoTreeLoading = false,
            repoTreeError = null,
            repositoriesLoading = false,
            repositoriesError = null,
        )
    }

    fun beginRepositoryLoad() {
        uiState = uiState.copy(
            repositoriesLoading = true,
            repositoriesError = null,
        )
    }

    fun onRepositoriesLoaded(repositories: List<RepositoryRef>) {
        uiState = uiState.copy(
            repositoryOptions = repositories,
            repositoriesLoading = false,
            repositoriesError = null,
        )
    }

    fun onRepositoriesLoadFailed(message: String) {
        uiState = uiState.copy(
            repositoriesLoading = false,
            repositoriesError = message,
        )
    }

    fun retryRepositoryLoad() {
        uiState = uiState.copy(
            repositoryOptions = emptyList(),
            repositoriesError = null,
            repositoryLoadNonce = uiState.repositoryLoadNonce + 1,
        )
    }

    fun selectRepository(repository: RepositoryRef) {
        uiState = uiState.copy(
            mode = AppMode.Workspace,
            selectedRepository = repository,
            showNonMarkdownFiles = false,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            selectedMarkdownPath = null,
            activeDocument = null,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = null,
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            documentLoadNonce = uiState.documentLoadNonce + 1,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = null,
            saveRequestNonce = uiState.saveRequestNonce + 1,
            createDialogVisible = false,
            createTargetFolderDraft = "",
            createFileNameDraft = DEFAULT_NEW_FILE_NAME,
            createCommitMessageDraft = defaultCreateCommitMessage(DEFAULT_NEW_FILE_NAME),
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createInProgress = false,
            createError = null,
            createRequestNonce = uiState.createRequestNonce + 1,
            renameDialogVisible = false,
            renameFileNameDraft = "",
            renameCommitMessageDraft = "",
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameInProgress = false,
            renameError = null,
            renameRequestNonce = uiState.renameRequestNonce + 1,
            moveDialogVisible = false,
            moveDestinationFolderDraft = "",
            moveCommitMessageDraft = "",
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            moveRequestNonce = uiState.moveRequestNonce + 1,
            copiedMarkdownPath = null,
            pasteRequestSourcePath = null,
            pasteRequestDestinationPath = null,
            pasteRequestCommitMessage = "",
            pasteInProgress = false,
            pasteError = null,
            pasteRequestNonce = uiState.pasteRequestNonce + 1,
            shareDialogVisible = false,
            shareExpiryOption = ShareExpiryOption.ThirtyDays,
            activeShare = null,
            shareInProgress = false,
            shareError = null,
            shareRequestNonce = uiState.shareRequestNonce + 1,
            pendingShareRevokeId = null,
            shareRevokeInProgress = false,
            shareRevokeError = null,
            shareRevokeRequestNonce = uiState.shareRevokeRequestNonce + 1,
            sharedLinksDialogVisible = false,
            sharedLinks = emptyList(),
            sharedLinksLoading = false,
            sharedLinksError = null,
            sharedLinksLoadNonce = uiState.sharedLinksLoadNonce + 1,
            repoTreeLoading = false,
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun openRepositoryPicker() {
        uiState = uiState.copy(
            mode = AppMode.RepositorySelection,
            selectedMarkdownPath = null,
            activeDocument = null,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentEditState = null,
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = null,
            createDialogVisible = false,
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createInProgress = false,
            createError = null,
            renameDialogVisible = false,
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameInProgress = false,
            renameError = null,
            moveDialogVisible = false,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            pasteInProgress = false,
            pasteError = null,
            shareDialogVisible = false,
            activeShare = null,
            shareInProgress = false,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeInProgress = false,
            shareRevokeError = null,
        )
    }

    fun beginRepoTreeLoad() {
        uiState = uiState.copy(
            repoTreeLoading = true,
            repoTreeError = null,
        )
    }

    fun onRepoTreeLoaded(rootNodes: List<RepoTreeNode>) {
        val availableFolderPaths = expandedFolderPathsAfterLoad(rootNodes)
        val activePath = uiState.activeDocument?.path?.value ?: uiState.selectedMarkdownPath
        val expandedFolderPaths = if (uiState.expandedFolderPaths.isEmpty()) {
            availableFolderPaths
        } else {
            val activeFolderPaths = activePath.orEmpty()
                .takeIf { it.isNotBlank() }
                ?.let(::expandedFoldersForPath)
                .orEmpty()
            (uiState.expandedFolderPaths intersect availableFolderPaths) +
                (activeFolderPaths intersect availableFolderPaths)
        }
        uiState = uiState.copy(
            repoTreeRoots = rootNodes,
            repoTreeLoading = false,
            repoTreeError = null,
            expandedFolderPaths = expandedFolderPaths,
        )
    }

    fun onRepoTreeLoadFailed(message: String) {
        uiState = uiState.copy(
            repoTreeLoading = false,
            repoTreeError = message,
        )
    }

    fun retryRepoTreeLoad() {
        uiState = uiState.copy(
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun markRepoTreeDirty() {
        retryRepoTreeLoad()
    }

    fun toggleFolderExpansion(path: String) {
        val expanded = uiState.expandedFolderPaths
        uiState = uiState.copy(
            expandedFolderPaths = if (path in expanded) {
                expanded - path
            } else {
                expanded + path
            },
        )
    }

    fun selectExplorerPath(path: String, additiveSelection: Boolean) {
        uiState = uiState.copy(
            selectedExplorerPaths = if (additiveSelection) {
                if (path in uiState.selectedExplorerPaths) {
                    uiState.selectedExplorerPaths - path
                } else {
                    uiState.selectedExplorerPaths + path
                }
            } else {
                setOf(path)
            },
            deleteError = null,
        )
    }

    fun toggleShowNonMarkdownFiles() {
        uiState = uiState.copy(
            showNonMarkdownFiles = !uiState.showNonMarkdownFiles,
        )
    }

    fun requestOpenMarkdownFile(path: String) {
        if (uiState.createInProgress || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }

        val currentPath = uiState.activeDocument?.path?.value
        val hasUnsavedChanges = uiState.documentEditState?.isDirty == true
        if (hasUnsavedChanges && currentPath != null && currentPath != path) {
            uiState = uiState.copy(
                pendingDocumentPath = path,
                documentError = "Unsaved changes in $currentPath. Discard changes to open $path.",
            )
            return
        }

        val shouldReload = currentPath != path || uiState.activeDocument == null || uiState.documentError != null
        uiState = uiState.copy(
            selectedExplorerPaths = setOf(path),
            selectedMarkdownPath = path,
            pendingDocumentPath = null,
            documentError = null,
            saveSuccess = null,
            saveError = null,
            renameError = null,
            moveError = null,
            documentLoadNonce = if (shouldReload) uiState.documentLoadNonce + 1 else uiState.documentLoadNonce,
        )
    }

    fun beginDocumentLoad() {
        uiState = uiState.copy(
            documentLoading = true,
            documentError = null,
        )
    }

    fun onDocumentLoaded(document: MarkdownDocument) {
        uiState = uiState.copy(
            selectedMarkdownPath = document.path.value,
            activeDocument = document,
            loadedDocumentLoadNonce = uiState.documentLoadNonce,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = document.content,
                currentContent = document.content,
            ),
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = null,
            renameError = null,
            moveError = null,
            activeShare = null,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeError = null,
        )
    }

    fun onDocumentLoadFailed(message: String) {
        uiState = uiState.copy(
            documentLoading = false,
            documentError = message,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
        )
    }

    fun retryOpenDocument() {
        if (uiState.selectedMarkdownPath == null) return
        uiState = uiState.copy(
            documentError = null,
            documentLoadNonce = uiState.documentLoadNonce + 1,
        )
    }

    fun requestRepositorySync() {
        if (
            uiState.repoTreeLoading ||
            uiState.documentLoading ||
            uiState.createInProgress ||
            uiState.createFolderInProgress ||
            uiState.renameInProgress ||
            uiState.moveInProgress ||
            uiState.pasteInProgress ||
            uiState.deleteInProgress ||
            uiState.saveInProgress
        ) {
            return
        }

        val activePath = uiState.activeDocument?.path?.value ?: uiState.selectedMarkdownPath
        val reloadActiveDocument = activePath != null && uiState.documentEditState?.isDirty != true
        uiState = uiState.copy(
            repoTreeRoots = emptyList(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
            selectedMarkdownPath = activePath ?: uiState.selectedMarkdownPath,
            documentError = if (reloadActiveDocument) null else uiState.documentError,
            documentLoadNonce = if (reloadActiveDocument) {
                uiState.documentLoadNonce + 1
            } else {
                uiState.documentLoadNonce
            },
        )
    }

    fun beginDocumentHistoryLoad() {
        uiState = uiState.copy(
            documentHistoryLoading = true,
            documentHistoryError = null,
        )
    }

    fun onDocumentHistoryLoaded(path: String, history: List<DocumentHistoryEntry>) {
        uiState = uiState.copy(
            documentHistoryPath = path,
            documentHistory = history,
            documentHistoryLoading = false,
            documentHistoryError = null,
        )
    }

    fun onDocumentHistoryLoadFailed(path: String, message: String) {
        uiState = uiState.copy(
            documentHistoryPath = path,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = message,
        )
    }

    fun retryDocumentHistory() {
        val activePath = uiState.activeDocument?.path?.value ?: return
        uiState = uiState.copy(
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            selectedMarkdownPath = activePath,
        )
    }

    fun updateDocumentContent(content: String) {
        val editState = uiState.documentEditState ?: return
        val updatedEditState = editState.copy(currentContent = content)
        val commitMessageDraft = when {
            !updatedEditState.isDirty -> ""
            !editState.isDirty && updatedEditState.isDirty -> ""
            else -> uiState.commitMessageDraft
        }
        uiState = uiState.copy(
            documentEditState = updatedEditState,
            commitMessageDraft = commitMessageDraft,
            saveError = null,
            saveSuccess = null,
        )
    }

    fun updateCommitMessageDraft(value: String) {
        uiState = uiState.copy(commitMessageDraft = value)
    }

    fun requestSaveDocument() {
        if (uiState.saveInProgress || uiState.documentLoading || uiState.createInProgress || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }
        val document = uiState.activeDocument
        val editState = uiState.documentEditState
        if (document == null || editState == null) {
            uiState = uiState.copy(saveError = "No active document to save.", saveSuccess = null)
            return
        }
        if (!editState.isDirty) {
            uiState = uiState.copy(saveSuccess = "No changes to save.", saveError = null)
            return
        }

        val commitMessage = uiState.commitMessageDraft
            .trim()
            .ifBlank { defaultUpdateCommitMessage(document.path.value) }

        uiState = uiState.copy(
            commitMessageDraft = commitMessage,
            saveError = null,
            saveSuccess = null,
            saveRequestNonce = uiState.saveRequestNonce + 1,
        )
    }

    fun beginDocumentSave() {
        uiState = uiState.copy(
            saveInProgress = true,
            saveError = null,
            saveSuccess = null,
        )
    }

    fun onDocumentSaved(savedDocument: MarkdownDocument) {
        uiState = uiState.copy(
            activeDocument = savedDocument,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = savedDocument.content,
                currentContent = savedDocument.content,
            ),
            saveInProgress = false,
            saveError = null,
            saveSuccess = "Saved ${savedDocument.path.value}",
        )
    }

    fun onDocumentSaveFailed(message: String) {
        uiState = uiState.copy(
            saveInProgress = false,
            saveError = message,
            saveSuccess = null,
        )
    }

    fun requestShowCreateDocumentDialog() {
        if (uiState.createInProgress || uiState.createFolderInProgress || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }
        val selectedPath = uiState.selectedMarkdownPath ?: uiState.activeDocument?.path?.value
        val suggestedFolder = normalizeFolder(selectedPath?.substringBeforeLast('/', ""))
        val defaultCommitMessage = defaultCreateCommitMessage(DEFAULT_NEW_FILE_NAME)
        uiState = uiState.copy(
            createDialogVisible = true,
            createTargetFolderDraft = suggestedFolder,
            createFileNameDraft = DEFAULT_NEW_FILE_NAME,
            createCommitMessageDraft = defaultCommitMessage,
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createError = null,
        )
    }

    fun requestShowCreateFolderDialog(parentFolder: String? = null) {
        if (uiState.createInProgress || uiState.createFolderInProgress || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }

        val selectedPath = uiState.selectedMarkdownPath ?: uiState.activeDocument?.path?.value
        val suggestedParent = normalizeFolder(parentFolder ?: selectedPath?.substringBeforeLast('/', ""))
        val defaultFolderName = DEFAULT_NEW_FOLDER_NAME
        uiState = uiState.copy(
            createFolderDialogVisible = true,
            createFolderParentDraft = suggestedParent,
            createFolderNameDraft = defaultFolderName,
            createFolderCommitMessageDraft = defaultCreateFolderCommitMessage(defaultFolderName),
            createFolderRequestPath = null,
            createFolderRequestCommitMessage = "",
            createFolderError = null,
        )
    }

    fun dismissCreateFolderDialog() {
        if (uiState.createFolderInProgress) {
            return
        }
        uiState = uiState.copy(
            createFolderDialogVisible = false,
            createFolderError = null,
        )
    }

    fun updateCreateFolderParentDraft(value: String) {
        uiState = uiState.copy(
            createFolderParentDraft = value,
            createFolderError = null,
        )
    }

    fun updateCreateFolderNameDraft(value: String) {
        uiState = uiState.copy(
            createFolderNameDraft = value,
            createFolderError = null,
        )
    }

    fun updateCreateFolderCommitMessageDraft(value: String) {
        uiState = uiState.copy(
            createFolderCommitMessageDraft = value,
            createFolderError = null,
        )
    }

    fun requestCreateFolder() {
        if (uiState.createInProgress || uiState.createFolderInProgress || uiState.saveInProgress || uiState.documentLoading || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }

        val rawFolderName = uiState.createFolderNameDraft.trim()
        if (rawFolderName.isBlank()) {
            uiState = uiState.copy(createFolderError = "Folder name is required.")
            return
        }
        if (rawFolderName.contains("/") || rawFolderName.contains("\\")) {
            uiState = uiState.copy(createFolderError = "Folder name cannot contain path separators.")
            return
        }

        val parentFolder = normalizeFolder(uiState.createFolderParentDraft)
        if (parentFolder.contains("\\") || parentFolder.contains("//")) {
            uiState = uiState.copy(createFolderError = "Parent folder contains invalid path separators.")
            return
        }

        val folderPath = if (parentFolder.isBlank()) rawFolderName else "$parentFolder/$rawFolderName"
        if (folderPath in existingTreePaths(uiState.repoTreeRoots)) {
            uiState = uiState.copy(createFolderError = "A folder already exists at $folderPath.")
            return
        }

        val commitMessage = uiState.createFolderCommitMessageDraft
            .trim()
            .ifBlank { defaultCreateFolderCommitMessage(folderPath) }
        uiState = uiState.copy(
            createFolderParentDraft = parentFolder,
            createFolderNameDraft = rawFolderName,
            createFolderCommitMessageDraft = commitMessage,
            createFolderRequestPath = folderPath,
            createFolderRequestCommitMessage = commitMessage,
            createFolderError = null,
            createFolderRequestNonce = uiState.createFolderRequestNonce + 1,
        )
    }

    fun beginFolderCreate() {
        uiState = uiState.copy(
            createFolderInProgress = true,
            createFolderError = null,
        )
    }

    fun onFolderCreated(folderPath: String) {
        uiState = uiState.copy(
            saveError = null,
            saveSuccess = "Created folder $folderPath",
            createFolderDialogVisible = false,
            createFolderParentDraft = folderPath.substringBeforeLast('/', ""),
            createFolderNameDraft = DEFAULT_NEW_FOLDER_NAME,
            createFolderCommitMessageDraft = defaultCreateFolderCommitMessage(DEFAULT_NEW_FOLDER_NAME),
            createFolderRequestPath = null,
            createFolderRequestCommitMessage = "",
            createFolderInProgress = false,
            createFolderError = null,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = expandedFoldersForPath(folderPath),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onFolderCreateFailed(message: String) {
        uiState = uiState.copy(
            createFolderInProgress = false,
            createFolderError = message,
        )
    }

    fun dismissCreateDocumentDialog() {
        if (uiState.createInProgress) {
            return
        }
        uiState = uiState.copy(
            createDialogVisible = false,
            createError = null,
        )
    }

    fun updateCreateTargetFolderDraft(value: String) {
        uiState = uiState.copy(
            createTargetFolderDraft = value,
            createError = null,
        )
    }

    fun updateCreateFileNameDraft(value: String) {
        uiState = uiState.copy(
            createFileNameDraft = value,
            createError = null,
        )
    }

    fun updateCreateCommitMessageDraft(value: String) {
        uiState = uiState.copy(
            createCommitMessageDraft = value,
            createError = null,
        )
    }

    fun requestCreateDocument() {
        if (uiState.createInProgress || uiState.createFolderInProgress || uiState.saveInProgress || uiState.documentLoading || uiState.renameInProgress || uiState.moveInProgress) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                createError = "Save or discard current edits before creating a new file.",
            )
            return
        }

        val rawFileName = uiState.createFileNameDraft.trim()
        if (rawFileName.isBlank()) {
            uiState = uiState.copy(createError = "File name is required.")
            return
        }
        if (rawFileName.contains("/") || rawFileName.contains("\\")) {
            uiState = uiState.copy(createError = "File name cannot contain path separators.")
            return
        }

        val normalizedFileName = ensureMarkdownFileName(rawFileName)
        val normalizedFolder = normalizeFolder(uiState.createTargetFolderDraft)
        val path = if (normalizedFolder.isEmpty()) {
            normalizedFileName
        } else {
            "$normalizedFolder/$normalizedFileName"
        }

        if (path in existingTreePaths(uiState.repoTreeRoots)) {
            uiState = uiState.copy(createError = "A file already exists at $path.")
            return
        }

        val commitMessage = uiState.createCommitMessageDraft
            .trim()
            .ifBlank { defaultCreateCommitMessage(normalizedFileName) }
        val initialContent = defaultInitialContent(path)
        uiState = uiState.copy(
            createTargetFolderDraft = normalizedFolder,
            createFileNameDraft = normalizedFileName,
            createCommitMessageDraft = commitMessage,
            createRequestPath = path,
            createRequestContent = initialContent,
            createRequestCommitMessage = commitMessage,
            createError = null,
            createRequestNonce = uiState.createRequestNonce + 1,
        )
    }

    fun beginDocumentCreate() {
        uiState = uiState.copy(
            createInProgress = true,
            createError = null,
        )
    }

    fun onDocumentCreated(document: MarkdownDocument) {
        uiState = uiState.copy(
            selectedMarkdownPath = document.path.value,
            activeDocument = document,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = document.content,
                currentContent = document.content,
            ),
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = "Created ${document.path.value}",
            createDialogVisible = false,
            createTargetFolderDraft = document.path.value.substringBeforeLast('/', ""),
            createFileNameDraft = DEFAULT_NEW_FILE_NAME,
            createCommitMessageDraft = defaultCreateCommitMessage(DEFAULT_NEW_FILE_NAME),
            createRequestPath = null,
            createRequestContent = "",
            createRequestCommitMessage = "",
            createInProgress = false,
            createError = null,
            moveDialogVisible = false,
            moveDestinationFolderDraft = "",
            moveCommitMessageDraft = "",
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onDocumentCreateFailed(message: String) {
        uiState = uiState.copy(
            createInProgress = false,
            createError = message,
            saveError = if (uiState.createDialogVisible) uiState.saveError else message,
            saveSuccess = if (uiState.createDialogVisible) uiState.saveSuccess else null,
        )
    }

    fun requestShowRenameDocumentDialog() {
        if (uiState.renameInProgress || uiState.createInProgress || uiState.saveInProgress || uiState.documentLoading || uiState.moveInProgress) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                renameError = "Save or discard current edits before renaming.",
            )
            return
        }

        val activeDocument = uiState.activeDocument
        if (activeDocument == null) {
            uiState = uiState.copy(
                renameError = "Open a markdown file before renaming.",
            )
            return
        }

        val currentFileName = activeDocument.path.value.substringAfterLast('/')
        uiState = uiState.copy(
            renameDialogVisible = true,
            renameFileNameDraft = currentFileName,
            renameCommitMessageDraft = defaultRenameCommitMessage(
                oldFileName = currentFileName,
                newFileName = currentFileName,
            ),
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameError = null,
        )
    }

    fun dismissRenameDocumentDialog() {
        if (uiState.renameInProgress) {
            return
        }
        uiState = uiState.copy(
            renameDialogVisible = false,
            renameError = null,
        )
    }

    fun updateRenameFileNameDraft(value: String) {
        uiState = uiState.copy(
            renameFileNameDraft = value,
            renameError = null,
        )
    }

    fun updateRenameCommitMessageDraft(value: String) {
        uiState = uiState.copy(
            renameCommitMessageDraft = value,
            renameError = null,
        )
    }

    fun requestRenameDocument() {
        if (uiState.renameInProgress || uiState.createInProgress || uiState.saveInProgress || uiState.documentLoading || uiState.moveInProgress) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                renameError = "Save or discard current edits before renaming.",
            )
            return
        }

        val activeDocument = uiState.activeDocument
        if (activeDocument == null) {
            uiState = uiState.copy(renameError = "No active document to rename.")
            return
        }

        val rawFileName = uiState.renameFileNameDraft.trim()
        if (rawFileName.isBlank()) {
            uiState = uiState.copy(renameError = "File name is required.")
            return
        }
        if (rawFileName.contains("/") || rawFileName.contains("\\")) {
            uiState = uiState.copy(renameError = "File name cannot contain path separators.")
            return
        }

        val normalizedFileName = ensureMarkdownFileName(rawFileName)
        val currentPath = activeDocument.path.value
        val folder = currentPath.substringBeforeLast('/', "")
        val newPath = if (folder.isEmpty()) {
            normalizedFileName
        } else {
            "$folder/$normalizedFileName"
        }
        if (newPath == currentPath) {
            uiState = uiState.copy(renameError = "New name matches current file name.")
            return
        }
        if (newPath in existingTreePaths(uiState.repoTreeRoots)) {
            uiState = uiState.copy(renameError = "A file already exists at $newPath.")
            return
        }

        val oldFileName = currentPath.substringAfterLast('/')
        val commitMessage = uiState.renameCommitMessageDraft
            .trim()
            .ifBlank {
                defaultRenameCommitMessage(
                    oldFileName = oldFileName,
                    newFileName = normalizedFileName,
                )
            }
        uiState = uiState.copy(
            renameFileNameDraft = normalizedFileName,
            renameCommitMessageDraft = commitMessage,
            renameRequestPath = newPath,
            renameRequestCommitMessage = commitMessage,
            renameError = null,
            renameRequestNonce = uiState.renameRequestNonce + 1,
        )
    }

    fun beginDocumentRename() {
        uiState = uiState.copy(
            renameInProgress = true,
            renameError = null,
        )
    }

    fun onDocumentRenamed(renamedDocument: MarkdownDocument) {
        uiState = uiState.copy(
            selectedMarkdownPath = renamedDocument.path.value,
            activeDocument = renamedDocument,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = renamedDocument.content,
                currentContent = renamedDocument.content,
            ),
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = "Renamed to ${renamedDocument.path.value}",
            renameDialogVisible = false,
            renameRequestPath = null,
            renameRequestCommitMessage = "",
            renameInProgress = false,
            renameError = null,
            moveDialogVisible = false,
            moveDestinationFolderDraft = "",
            moveCommitMessageDraft = "",
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onDocumentRenameFailed(message: String) {
        uiState = uiState.copy(
            renameInProgress = false,
            renameError = message,
        )
    }

    fun requestShowMoveDocumentDialog() {
        if (uiState.moveInProgress || uiState.renameInProgress || uiState.createInProgress || uiState.createFolderInProgress || uiState.saveInProgress || uiState.documentLoading) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                moveError = "Save or discard current edits before moving.",
            )
            return
        }

        val activeDocument = uiState.activeDocument
        if (activeDocument == null) {
            uiState = uiState.copy(
                moveError = "Open a markdown file before moving.",
            )
            return
        }

        val fileName = activeDocument.path.value.substringAfterLast('/')
        val currentFolder = normalizeFolder(activeDocument.path.value.substringBeforeLast('/', ""))
        uiState = uiState.copy(
            moveDialogVisible = true,
            moveDestinationFolderDraft = currentFolder,
            moveCommitMessageDraft = defaultMoveCommitMessage(fileName, currentFolder),
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestCommitMessage = "",
            moveError = null,
        )
    }

    fun dismissMoveDocumentDialog() {
        if (uiState.moveInProgress) {
            return
        }
        uiState = uiState.copy(
            moveDialogVisible = false,
            moveError = null,
        )
    }

    fun updateMoveDestinationFolderDraft(value: String) {
        uiState = uiState.copy(
            moveDestinationFolderDraft = value,
            moveError = null,
        )
    }

    fun updateMoveCommitMessageDraft(value: String) {
        uiState = uiState.copy(
            moveCommitMessageDraft = value,
            moveError = null,
        )
    }

    fun requestMoveDocument() {
        if (uiState.moveInProgress || uiState.renameInProgress || uiState.createInProgress || uiState.createFolderInProgress || uiState.saveInProgress || uiState.documentLoading) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                moveError = "Save or discard current edits before moving.",
            )
            return
        }

        val activeDocument = uiState.activeDocument
        if (activeDocument == null) {
            uiState = uiState.copy(moveError = "No active document to move.")
            return
        }

        val rawDestinationFolder = uiState.moveDestinationFolderDraft.trim()
        if (rawDestinationFolder.contains("\\")) {
            uiState = uiState.copy(moveError = "Destination folder cannot contain backslashes.")
            return
        }

        val fileName = activeDocument.path.value.substringAfterLast('/')
        val destinationFolder = normalizeFolder(rawDestinationFolder)
        if (destinationFolder.contains("//")) {
            uiState = uiState.copy(moveError = "Destination folder contains empty path segments.")
            return
        }
        val destinationPath = if (destinationFolder.isEmpty()) {
            fileName
        } else {
            "$destinationFolder/$fileName"
        }
        if (destinationPath == activeDocument.path.value) {
            uiState = uiState.copy(moveError = "Destination matches current location.")
            return
        }
        if (destinationPath in existingTreePaths(uiState.repoTreeRoots)) {
            uiState = uiState.copy(moveError = "A file already exists at $destinationPath.")
            return
        }

        val commitMessage = uiState.moveCommitMessageDraft
            .trim()
            .ifBlank { defaultMoveCommitMessage(fileName, destinationFolder) }
        uiState = uiState.copy(
            moveDestinationFolderDraft = destinationFolder,
            moveCommitMessageDraft = commitMessage,
            moveRequestSourcePath = activeDocument.path.value,
            moveRequestPath = destinationPath,
            moveRequestPaths = listOf(activeDocument.path.value to destinationPath),
            moveRequestCommitMessage = commitMessage,
            moveError = null,
            moveRequestNonce = uiState.moveRequestNonce + 1,
        )
    }

    fun requestMoveMarkdownFileToFolder(sourcePath: String, destinationFolder: String) {
        if (uiState.moveInProgress || uiState.renameInProgress || uiState.createInProgress || uiState.createFolderInProgress || uiState.saveInProgress || uiState.documentLoading) {
            return
        }
        if (uiState.documentEditState?.isDirty == true && uiState.activeDocument?.path?.value == sourcePath) {
            uiState = uiState.copy(moveError = "Save or discard current edits before moving.")
            return
        }
        if (!sourcePath.isMarkdownFileName()) {
            uiState = uiState.copy(moveError = "Only markdown files can be moved.")
            return
        }

        val normalizedDestinationFolder = normalizeFolder(destinationFolder)
        if (normalizedDestinationFolder.contains("\\") || normalizedDestinationFolder.contains("//")) {
            uiState = uiState.copy(moveError = "Destination folder contains invalid path separators.")
            return
        }

        val fileName = sourcePath.substringAfterLast('/')
        val destinationPath = if (normalizedDestinationFolder.isBlank()) {
            fileName
        } else {
            "$normalizedDestinationFolder/$fileName"
        }
        if (destinationPath == sourcePath) {
            return
        }
        if (destinationPath in existingTreePaths(uiState.repoTreeRoots)) {
            uiState = uiState.copy(moveError = "A file already exists at $destinationPath.")
            return
        }

        val commitMessage = defaultMoveCommitMessage(fileName, normalizedDestinationFolder)
        uiState = uiState.copy(
            selectedMarkdownPath = sourcePath,
            moveDialogVisible = false,
            moveDestinationFolderDraft = normalizedDestinationFolder,
            moveCommitMessageDraft = commitMessage,
            moveRequestSourcePath = sourcePath,
            moveRequestPath = destinationPath,
            moveRequestPaths = listOf(sourcePath to destinationPath),
            moveRequestCommitMessage = commitMessage,
            moveError = null,
            saveError = null,
            saveSuccess = null,
            moveRequestNonce = uiState.moveRequestNonce + 1,
        )
    }

    fun requestMoveMarkdownFilesToFolder(sourcePaths: List<String>, destinationFolder: String) {
        val markdownPaths = sourcePaths
            .map { it.trim() }
            .filter { it.isMarkdownFileName() }
            .distinct()
        if (markdownPaths.isEmpty()) {
            uiState = uiState.copy(moveError = "Select at least one markdown file to move.")
            return
        }
        val normalizedDestinationFolder = normalizeFolder(destinationFolder)
        val movePairs = markdownPaths.mapNotNull { sourcePath ->
            val fileName = sourcePath.substringAfterLast('/')
            val destinationPath = if (normalizedDestinationFolder.isBlank()) fileName else "$normalizedDestinationFolder/$fileName"
            if (destinationPath == sourcePath) null else sourcePath to destinationPath
        }
        if (movePairs.isEmpty()) {
            return
        }

        uiState = uiState.copy(
            selectedExplorerPaths = markdownPaths.toSet(),
            moveDialogVisible = false,
            moveDestinationFolderDraft = normalizedDestinationFolder,
            moveCommitMessageDraft = defaultMoveCommitMessage("${movePairs.size} files", normalizedDestinationFolder),
            moveRequestSourcePath = movePairs.first().first,
            moveRequestPath = movePairs.first().second,
            moveRequestPaths = movePairs,
            moveRequestCommitMessage = defaultMoveCommitMessage("${movePairs.size} files", normalizedDestinationFolder),
            moveError = null,
            saveError = null,
            saveSuccess = null,
            moveRequestNonce = uiState.moveRequestNonce + 1,
        )
    }

    fun requestDeleteExplorerPaths(paths: List<String>) {
        if (uiState.deleteInProgress || uiState.createInProgress || uiState.createFolderInProgress || uiState.renameInProgress || uiState.moveInProgress || uiState.saveInProgress || uiState.documentLoading) {
            return
        }

        val filePaths = paths
            .flatMap { path -> filePathsForExplorerPath(path) }
            .distinct()
        if (filePaths.isEmpty()) {
            uiState = uiState.copy(deleteError = "Select files or folders to delete.")
            return
        }
        if (uiState.documentEditState?.isDirty == true && uiState.activeDocument?.path?.value in filePaths) {
            uiState = uiState.copy(deleteError = "Save or discard current edits before deleting.")
            return
        }

        val commitMessage = defaultDeleteCommitMessage(filePaths)
        uiState = uiState.copy(
            deleteRequestPaths = filePaths,
            deleteRequestCommitMessage = commitMessage,
            deleteError = null,
            saveError = null,
            saveSuccess = null,
            deleteRequestNonce = uiState.deleteRequestNonce + 1,
        )
    }

    fun beginDeleteRepositoryItems() {
        uiState = uiState.copy(
            deleteInProgress = true,
            deleteError = null,
        )
    }

    fun onRepositoryItemsDeleted(paths: List<String>) {
        val activePath = uiState.activeDocument?.path?.value
        val deletedActiveDocument = activePath != null && activePath in paths
        uiState = uiState.copy(
            selectedExplorerPaths = emptySet(),
            selectedMarkdownPath = if (deletedActiveDocument) null else uiState.selectedMarkdownPath,
            activeDocument = if (deletedActiveDocument) null else uiState.activeDocument,
            documentEditState = if (deletedActiveDocument) null else uiState.documentEditState,
            documentHistoryPath = if (deletedActiveDocument) null else uiState.documentHistoryPath,
            documentHistory = if (deletedActiveDocument) emptyList() else uiState.documentHistory,
            saveError = null,
            saveSuccess = "Deleted ${paths.size} item${if (paths.size == 1) "" else "s"}",
            deleteRequestPaths = emptyList(),
            deleteRequestCommitMessage = "",
            deleteInProgress = false,
            deleteError = null,
            repoTreeRoots = emptyList(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onRepositoryItemsDeleteFailed(message: String) {
        uiState = uiState.copy(
            deleteInProgress = false,
            deleteError = message,
            saveError = message,
            saveSuccess = null,
        )
    }

    fun beginDocumentMove() {
        uiState = uiState.copy(
            moveInProgress = true,
            moveError = null,
        )
    }

    fun onDocumentMoved(movedDocument: MarkdownDocument) {
        uiState = uiState.copy(
            selectedMarkdownPath = movedDocument.path.value,
            activeDocument = movedDocument,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = movedDocument.content,
                currentContent = movedDocument.content,
            ),
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = "Moved to ${movedDocument.path.value}",
            moveDialogVisible = false,
            moveRequestSourcePath = null,
            moveRequestPath = null,
            moveRequestPaths = emptyList(),
            moveRequestCommitMessage = "",
            moveInProgress = false,
            moveError = null,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = expandedFoldersForPath(movedDocument.path.value),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onDocumentMoveFailed(message: String) {
        uiState = uiState.copy(
            moveInProgress = false,
            moveError = message,
        )
    }

    fun requestCopyMarkdownFile(path: String? = null) {
        val explicitPath = path?.trim().orEmpty()
        val candidatePath = when {
            explicitPath.isNotBlank() -> explicitPath
            !uiState.selectedMarkdownPath.isNullOrBlank() -> uiState.selectedMarkdownPath
            else -> uiState.activeDocument?.path?.value
        } ?: run {
            uiState = uiState.copy(pasteError = "Select a markdown file to copy.")
            return
        }

        if (!candidatePath.endsWith(".md", ignoreCase = true) && !candidatePath.endsWith(".markdown", ignoreCase = true)) {
            uiState = uiState.copy(pasteError = "Only markdown files can be copied.")
            return
        }

        uiState = uiState.copy(
            copiedMarkdownPath = candidatePath,
            pasteError = null,
            saveSuccess = "Copied ${candidatePath.substringAfterLast('/')}",
        )
    }

    fun requestImportMarkdownFile(fileName: String, content: String) {
        if (
            uiState.createInProgress ||
            uiState.saveInProgress ||
            uiState.documentLoading ||
            uiState.renameInProgress ||
            uiState.moveInProgress ||
            uiState.pasteInProgress
        ) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(
                saveError = "Save or discard current edits before importing a file.",
                saveSuccess = null,
            )
            return
        }

        val rawFileName = fileName.trim()
        if (rawFileName.isBlank()) {
            uiState = uiState.copy(saveError = "Imported file name is required.", saveSuccess = null)
            return
        }
        if (rawFileName.contains("/") || rawFileName.contains("\\")) {
            uiState = uiState.copy(saveError = "Imported file name cannot contain path separators.", saveSuccess = null)
            return
        }
        if (!rawFileName.isMarkdownFileName()) {
            uiState = uiState.copy(saveError = "Only .md and .markdown files can be imported.", saveSuccess = null)
            return
        }

        val destinationFolder = normalizeFolder(
            when {
                !uiState.selectedMarkdownPath.isNullOrBlank() -> uiState.selectedMarkdownPath?.substringBeforeLast('/', "")
                else -> uiState.activeDocument?.path?.value?.substringBeforeLast('/', "")
            },
        )
        val destinationPath = buildImportedPath(
            fileName = rawFileName,
            destinationFolder = destinationFolder,
            existingPaths = existingTreePaths(uiState.repoTreeRoots),
        )
        val commitMessage = defaultImportCommitMessage(rawFileName, destinationFolder)

        uiState = uiState.copy(
            createDialogVisible = false,
            createTargetFolderDraft = destinationFolder,
            createFileNameDraft = destinationPath.substringAfterLast('/'),
            createCommitMessageDraft = commitMessage,
            createRequestPath = destinationPath,
            createRequestContent = content,
            createRequestCommitMessage = commitMessage,
            createError = null,
            saveError = null,
            saveSuccess = null,
            createRequestNonce = uiState.createRequestNonce + 1,
        )
    }

    fun exportMarkdownSnapshot(): MarkdownExportSnapshot? {
        val document = uiState.activeDocument
        val editState = uiState.documentEditState
        if (document == null || editState == null) {
            uiState = uiState.copy(saveError = "Open a markdown file before exporting.", saveSuccess = null)
            return null
        }
        return MarkdownExportSnapshot(
            fileName = document.path.value.substringAfterLast('/').ifBlank { "document.md" },
            content = editState.currentContent,
        )
    }

    fun onMarkdownFileExported(path: String) {
        uiState = uiState.copy(
            saveError = null,
            saveSuccess = "Exported ${path.substringAfterLast('/')}",
        )
    }

    fun onMarkdownFileExportFailed(message: String) {
        uiState = uiState.copy(
            saveError = message,
            saveSuccess = null,
        )
    }

    fun onMarkdownFileImportFailed(message: String) {
        uiState = uiState.copy(
            saveError = message,
            saveSuccess = null,
        )
    }

    fun requestPasteMarkdownFile(destinationFolderOverride: String? = null) {
        if (
            uiState.pasteInProgress ||
            uiState.moveInProgress ||
            uiState.renameInProgress ||
            uiState.createInProgress ||
            uiState.saveInProgress ||
            uiState.documentLoading
        ) {
            return
        }

        val sourcePath = uiState.copiedMarkdownPath
        if (sourcePath.isNullOrBlank()) {
            uiState = uiState.copy(pasteError = "Copy a markdown file first.")
            return
        }

        val sourceFileName = sourcePath.substringAfterLast('/')
        val destinationFolder = normalizeFolder(
            when {
                !destinationFolderOverride.isNullOrBlank() -> destinationFolderOverride
                !uiState.selectedMarkdownPath.isNullOrBlank() -> uiState.selectedMarkdownPath?.substringBeforeLast('/', "")
                else -> uiState.activeDocument?.path?.value?.substringBeforeLast('/', "")
            },
        )
        val existingPaths = existingTreePaths(uiState.repoTreeRoots)
        val destinationPath = buildPastedPath(
            sourceFileName = sourceFileName,
            destinationFolder = destinationFolder,
            existingPaths = existingPaths,
        )
        if (destinationPath == sourcePath) {
            uiState = uiState.copy(pasteError = "Paste target matches source file path.")
            return
        }

        val commitMessage = defaultCopyCommitMessage(sourceFileName, destinationFolder)
        uiState = uiState.copy(
            pasteRequestSourcePath = sourcePath,
            pasteRequestDestinationPath = destinationPath,
            pasteRequestCommitMessage = commitMessage,
            pasteError = null,
            saveSuccess = null,
            pasteRequestNonce = uiState.pasteRequestNonce + 1,
        )
    }

    fun beginDocumentPaste() {
        uiState = uiState.copy(
            pasteInProgress = true,
            pasteError = null,
        )
    }

    fun onDocumentPasted(pastedDocument: MarkdownDocument) {
        uiState = uiState.copy(
            selectedMarkdownPath = pastedDocument.path.value,
            activeDocument = pastedDocument,
            documentHistoryPath = null,
            documentHistory = emptyList(),
            documentHistoryLoading = false,
            documentHistoryError = null,
            documentHistoryLoadNonce = uiState.documentHistoryLoadNonce + 1,
            documentEditState = DocumentEditState(
                originalContent = pastedDocument.content,
                currentContent = pastedDocument.content,
            ),
            pendingDocumentPath = null,
            documentLoading = false,
            documentError = null,
            commitMessageDraft = "",
            saveInProgress = false,
            saveError = null,
            saveSuccess = "Pasted ${pastedDocument.path.value}",
            pasteRequestSourcePath = null,
            pasteRequestDestinationPath = null,
            pasteRequestCommitMessage = "",
            pasteInProgress = false,
            pasteError = null,
            repoTreeRoots = emptyList(),
            expandedFolderPaths = emptySet(),
            repoTreeError = null,
            repoTreeLoadNonce = uiState.repoTreeLoadNonce + 1,
        )
    }

    fun onDocumentPasteFailed(message: String) {
        uiState = uiState.copy(
            pasteInProgress = false,
            pasteError = message,
        )
    }

    fun requestShowShareDialog() {
        if (
            uiState.shareInProgress ||
            uiState.shareRevokeInProgress ||
            uiState.createInProgress ||
            uiState.renameInProgress ||
            uiState.moveInProgress ||
            uiState.saveInProgress ||
            uiState.documentLoading
        ) {
            return
        }

        val activeDocument = uiState.activeDocument
        if (activeDocument == null) {
            uiState = uiState.copy(shareError = "Open a markdown file before sharing.")
            return
        }

        uiState = uiState.copy(
            shareDialogVisible = true,
            activeShare = null,
            shareError = if (uiState.documentEditState?.isDirty == true) {
                "Save current edits before creating a public share."
            } else {
                null
            },
            pendingShareRevokeId = null,
            shareRevokeError = null,
        )
    }

    fun dismissShareDialog() {
        if (uiState.shareInProgress || uiState.shareRevokeInProgress) {
            return
        }
        uiState = uiState.copy(
            shareDialogVisible = false,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeError = null,
        )
    }

    fun updateShareExpiryOption(option: ShareExpiryOption) {
        uiState = uiState.copy(
            shareExpiryOption = option,
            shareError = null,
        )
    }

    fun requestCreateShare() {
        if (
            uiState.shareInProgress ||
            uiState.shareRevokeInProgress ||
            uiState.createInProgress ||
            uiState.renameInProgress ||
            uiState.moveInProgress ||
            uiState.saveInProgress ||
            uiState.documentLoading
        ) {
            return
        }
        if (uiState.documentEditState?.isDirty == true) {
            uiState = uiState.copy(shareError = "Save current edits before creating a public share.")
            return
        }
        if (uiState.activeDocument == null) {
            uiState = uiState.copy(shareError = "No active document to share.")
            return
        }

        uiState = uiState.copy(
            activeShare = null,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeError = null,
            shareRequestNonce = uiState.shareRequestNonce + 1,
        )
    }

    fun beginShareCreate() {
        uiState = uiState.copy(
            shareInProgress = true,
            shareError = null,
            shareRevokeError = null,
        )
    }

    fun onShareCreated(share: DocumentShare) {
        uiState = uiState.copy(
            activeShare = share,
            sharedLinks = (listOf(share) + uiState.sharedLinks.filterNot { it.id == share.id }),
            shareInProgress = false,
            shareError = null,
            pendingShareRevokeId = null,
            shareRevokeError = null,
        )
    }

    fun onShareCreateFailed(message: String) {
        uiState = uiState.copy(
            shareInProgress = false,
            shareError = message,
        )
    }

    fun requestRevokeShare() {
        if (uiState.shareRevokeInProgress || uiState.shareInProgress) {
            return
        }
        val activeShare = uiState.activeShare
        if (activeShare == null) {
            uiState = uiState.copy(shareRevokeError = "Create a share before revoking it.")
            return
        }
        uiState = uiState.copy(
            pendingShareRevokeId = activeShare.id,
            shareRevokeError = null,
            shareRevokeRequestNonce = uiState.shareRevokeRequestNonce + 1,
        )
    }

    fun beginShareRevoke() {
        uiState = uiState.copy(
            shareRevokeInProgress = true,
            shareRevokeError = null,
        )
    }

    fun onShareRevoked(share: DocumentShare) {
        uiState = uiState.copy(
            activeShare = share,
            sharedLinks = uiState.sharedLinks.filterNot { it.id == share.id },
            pendingShareRevokeId = null,
            shareRevokeInProgress = false,
            shareRevokeError = null,
        )
    }

    fun onShareRevokeFailed(message: String) {
        uiState = uiState.copy(
            shareRevokeInProgress = false,
            pendingShareRevokeId = null,
            shareRevokeError = message,
        )
    }

    fun requestShowSharedLinksDialog() {
        if (uiState.shareInProgress || uiState.shareRevokeInProgress) {
            return
        }
        uiState = uiState.copy(
            sharedLinksDialogVisible = true,
            sharedLinksError = null,
            sharedLinksLoadNonce = uiState.sharedLinksLoadNonce + 1,
        )
    }

    fun dismissSharedLinksDialog() {
        if (uiState.shareRevokeInProgress) {
            return
        }
        uiState = uiState.copy(
            sharedLinksDialogVisible = false,
            sharedLinksError = null,
        )
    }

    fun beginSharedLinksLoad() {
        uiState = uiState.copy(
            sharedLinksLoading = true,
            sharedLinksError = null,
        )
    }

    fun onSharedLinksLoaded(shares: List<DocumentShare>) {
        uiState = uiState.copy(
            sharedLinks = shares,
            sharedLinksLoading = false,
            sharedLinksError = null,
        )
    }

    fun onSharedLinksLoadFailed(message: String) {
        uiState = uiState.copy(
            sharedLinksLoading = false,
            sharedLinksError = message,
        )
    }

    fun retrySharedLinksLoad() {
        uiState = uiState.copy(
            sharedLinksError = null,
            sharedLinksLoadNonce = uiState.sharedLinksLoadNonce + 1,
        )
    }

    fun requestRevokeSharedLink(share: DocumentShare) {
        if (uiState.shareRevokeInProgress || uiState.shareInProgress || share.revokedAt != null) {
            return
        }
        uiState = uiState.copy(
            activeShare = if (uiState.activeShare?.id == share.id) uiState.activeShare else share,
            pendingShareRevokeId = share.id,
            shareRevokeError = null,
            sharedLinksError = null,
            shareRevokeRequestNonce = uiState.shareRevokeRequestNonce + 1,
        )
    }

    fun discardUnsavedAndOpenPendingDocument() {
        val pendingPath = uiState.pendingDocumentPath ?: return
        uiState = uiState.copy(
            selectedMarkdownPath = pendingPath,
            pendingDocumentPath = null,
            documentError = null,
            saveError = null,
            saveSuccess = null,
            documentLoadNonce = uiState.documentLoadNonce + 1,
        )
    }

    fun keepEditingCurrentDocument() {
        uiState = uiState.copy(
            pendingDocumentPath = null,
            documentError = null,
        )
    }

    private fun expandedFolderPathsAfterLoad(nodes: List<RepoTreeNode>): Set<String> {
        val output = mutableSetOf<String>()

        fun walk(currentNodes: List<RepoTreeNode>) {
            currentNodes.forEach { node ->
                if (node.kind == RepoTreeNodeKind.Folder) {
                    output += node.path
                    walk(node.children)
                }
            }
        }

        walk(nodes)
        return output
    }

    private fun defaultCreateCommitMessage(fileName: String): String {
        return "Create $fileName"
    }

    private fun defaultCreateFolderCommitMessage(folderPath: String): String {
        return "Create $folderPath"
    }

    private fun defaultRenameCommitMessage(oldFileName: String, newFileName: String): String {
        return "Rename $oldFileName to $newFileName"
    }

    private fun defaultMoveCommitMessage(fileName: String, destinationFolder: String): String {
        val destination = if (destinationFolder.isBlank()) {
            "repository root"
        } else {
            destinationFolder
        }
        return "Move $fileName to $destination"
    }

    private fun defaultCopyCommitMessage(fileName: String, destinationFolder: String): String {
        val destination = if (destinationFolder.isBlank()) {
            "repository root"
        } else {
            destinationFolder
        }
        return "Copy $fileName to $destination"
    }

    private fun defaultImportCommitMessage(fileName: String, destinationFolder: String): String {
        val destination = if (destinationFolder.isBlank()) {
            "repository root"
        } else {
            destinationFolder
        }
        return "Import $fileName to $destination"
    }

    private fun defaultDeleteCommitMessage(paths: List<String>): String {
        return if (paths.size == 1) {
            "Delete ${paths.first().substringAfterLast('/')}"
        } else {
            "Delete ${paths.size} files"
        }
    }

    private fun defaultUpdateCommitMessage(path: String): String {
        val fileName = path.substringAfterLast('/').ifBlank { "document.md" }
        return "Update $fileName"
    }

    private fun normalizeFolder(folder: String?): String {
        return folder
            .orEmpty()
            .trim()
            .trim('/')
    }

    private fun ensureMarkdownFileName(fileName: String): String {
        if (fileName.endsWith(".md", ignoreCase = true)) {
            return fileName
        }
        if (fileName.endsWith(".markdown", ignoreCase = true)) {
            return fileName
        }
        return "$fileName.md"
    }

    private fun String.isMarkdownFileName(): Boolean {
        return endsWith(".md", ignoreCase = true) || endsWith(".markdown", ignoreCase = true)
    }

    private fun existingTreePaths(roots: List<RepoTreeNode>): Set<String> {
        val output = mutableSetOf<String>()

        fun walk(nodes: List<RepoTreeNode>) {
            nodes.forEach { node ->
                output += node.path
                if (node.children.isNotEmpty()) {
                    walk(node.children)
                }
            }
        }

        walk(roots)
        return output
    }

    private fun filePathsForExplorerPath(path: String): List<String> {
        val node = findTreeNode(path, uiState.repoTreeRoots) ?: return emptyList()
        val output = mutableListOf<String>()

        fun walk(current: RepoTreeNode) {
            when (current.kind) {
                RepoTreeNodeKind.File -> output += current.path
                RepoTreeNodeKind.Folder -> current.children.forEach(::walk)
            }
        }

        walk(node)
        return output
    }

    private fun findTreeNode(path: String, nodes: List<RepoTreeNode>): RepoTreeNode? {
        nodes.forEach { node ->
            if (node.path == path) return node
            findTreeNode(path, node.children)?.let { return it }
        }
        return null
    }

    private fun expandedFoldersForPath(path: String): Set<String> {
        val output = mutableSetOf<String>()
        var folderPath = path.substringBeforeLast('/', "")
        while (folderPath.isNotBlank()) {
            output += folderPath
            folderPath = folderPath.substringBeforeLast('/', "")
        }
        if (!path.substringAfterLast('/').contains(".")) {
            output += path
        }
        return output
    }

    private fun buildPastedPath(
        sourceFileName: String,
        destinationFolder: String,
        existingPaths: Set<String>,
    ): String {
        val extension = sourceFileName.substringAfterLast('.', "")
        val stem = if (extension.isBlank()) sourceFileName else sourceFileName.removeSuffix(".$extension")
        var index = 0
        while (true) {
            val candidateName = when (index) {
                0 -> "$stem-copy"
                else -> "$stem-copy-$index"
            } + if (extension.isBlank()) "" else ".$extension"
            val candidatePath = if (destinationFolder.isBlank()) {
                candidateName
            } else {
                "$destinationFolder/$candidateName"
            }
            if (candidatePath !in existingPaths) {
                return candidatePath
            }
            index++
        }
    }

    private fun buildImportedPath(
        fileName: String,
        destinationFolder: String,
        existingPaths: Set<String>,
    ): String {
        val extension = fileName.substringAfterLast('.', "")
        val stem = if (extension.isBlank()) fileName else fileName.removeSuffix(".$extension")
        var index = 0
        while (true) {
            val candidateName = when (index) {
                0 -> fileName
                else -> "$stem-$index.$extension"
            }
            val candidatePath = if (destinationFolder.isBlank()) {
                candidateName
            } else {
                "$destinationFolder/$candidateName"
            }
            if (candidatePath !in existingPaths) {
                return candidatePath
            }
            index++
        }
    }

    private fun defaultInitialContent(path: String): String {
        val fileName = path.substringAfterLast('/').substringBeforeLast('.')
        val heading = fileName
            .replace('-', ' ')
            .replace('_', ' ')
            .split(' ')
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { first -> first.uppercase() }
            }
            .ifBlank { "New Document" }
        return "# $heading\n"
    }

    private companion object {
        const val DEFAULT_NEW_FILE_NAME = "new-file.md"
        const val DEFAULT_NEW_FOLDER_NAME = "new-folder"
    }
}
