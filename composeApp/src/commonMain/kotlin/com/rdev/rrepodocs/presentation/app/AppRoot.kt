package com.rdev.rrepodocs.presentation.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.rdev.rrepodocs.data.auth.GitHubDeviceFlowService
import com.rdev.rrepodocs.data.auth.GitHubTokenAuthRepository
import com.rdev.rrepodocs.data.github.GitHubApiClient
import com.rdev.rrepodocs.data.repository.GitHubRepositoryServiceImpl
import com.rdev.rrepodocs.data.share.ShareWorkerService
import com.rdev.rrepodocs.domain.model.AuthState
import com.rdev.rrepodocs.domain.model.DocumentPath
import com.rdev.rrepodocs.domain.usecase.CreateDocumentUseCase
import com.rdev.rrepodocs.domain.usecase.CreateFolderUseCase
import com.rdev.rrepodocs.domain.usecase.DeleteRepositoryItemsUseCase
import com.rdev.rrepodocs.domain.usecase.LoadDocumentHistoryUseCase
import com.rdev.rrepodocs.domain.usecase.LoadDocumentSharesUseCase
import com.rdev.rrepodocs.domain.usecase.LoadRepoTreeUseCase
import com.rdev.rrepodocs.domain.usecase.LoadRepositoriesUseCase
import com.rdev.rrepodocs.domain.usecase.MoveDocumentUseCase
import com.rdev.rrepodocs.domain.usecase.OpenDocumentUseCase
import com.rdev.rrepodocs.domain.usecase.RenameDocumentUseCase
import com.rdev.rrepodocs.domain.usecase.RevokeShareUseCase
import com.rdev.rrepodocs.domain.usecase.SaveDocumentUseCase
import com.rdev.rrepodocs.domain.usecase.ShareDocumentUseCase
import com.rdev.rrepodocs.presentation.auth.AuthScreen
import com.rdev.rrepodocs.presentation.auth.AuthViewModel
import com.rdev.rrepodocs.presentation.editor.WorkspaceScreen
import com.rdev.rrepodocs.presentation.repo.RepoPickerScreen
import com.rdev.rrepodocs.platform.provideGitHubOAuthClientId
import com.rdev.rrepodocs.platform.provideSecureSessionStorage
import com.rdev.rrepodocs.platform.provideWorkspacePreferencesStorage
import com.rdev.rrepodocs.platform.exportPdfFile
import com.rdev.rrepodocs.platform.exportMarkdownFile
import com.rdev.rrepodocs.platform.printMarkdownPreview
import com.rdev.rrepodocs.platform.pickMarkdownFileForImport
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val sessionStorage = remember { provideSecureSessionStorage() }
    val workspacePreferencesStorage = remember { provideWorkspacePreferencesStorage() }
    val gitHubOAuthClientId = remember { provideGitHubOAuthClientId() }
    val gitHubApiClient = remember { GitHubApiClient() }
    val gitHubDeviceFlowService = remember { GitHubDeviceFlowService() }
    val authRepository = remember { GitHubTokenAuthRepository(sessionStorage, gitHubApiClient) }
    val gitHubRepositoryService = remember { GitHubRepositoryServiceImpl(gitHubApiClient) }
    val shareService = remember { ShareWorkerService() }
    val loadRepositoriesUseCase = remember { LoadRepositoriesUseCase(gitHubRepositoryService) }
    val loadRepoTreeUseCase = remember { LoadRepoTreeUseCase(gitHubRepositoryService) }
    val openDocumentUseCase = remember { OpenDocumentUseCase(gitHubRepositoryService) }
    val loadDocumentHistoryUseCase = remember { LoadDocumentHistoryUseCase(gitHubRepositoryService) }
    val saveDocumentUseCase = remember { SaveDocumentUseCase(gitHubRepositoryService) }
    val createDocumentUseCase = remember { CreateDocumentUseCase(gitHubRepositoryService) }
    val createFolderUseCase = remember { CreateFolderUseCase(gitHubRepositoryService) }
    val deleteRepositoryItemsUseCase = remember { DeleteRepositoryItemsUseCase(gitHubRepositoryService) }
    val renameDocumentUseCase = remember { RenameDocumentUseCase(gitHubRepositoryService) }
    val moveDocumentUseCase = remember { MoveDocumentUseCase(gitHubRepositoryService) }
    val shareDocumentUseCase = remember { ShareDocumentUseCase(shareService) }
    val revokeShareUseCase = remember { RevokeShareUseCase(shareService) }
    val loadDocumentSharesUseCase = remember { LoadDocumentSharesUseCase(shareService) }
    val authViewModel = remember {
        AuthViewModel(
            authRepository = authRepository,
            gitHubDeviceFlowService = gitHubDeviceFlowService,
            configuredClientId = gitHubOAuthClientId,
        )
    }
    val appViewModel = remember { AppViewModel() }
    val coroutineScope = rememberCoroutineScope()
    var restoredRepositoryForSession by remember { mutableStateOf<String?>(null) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val restoredSession = authViewModel.restoreSession()
        if (restoredSession != null) {
            appViewModel.onSignedIn(restoredSession)
        }
    }

    val appState = appViewModel.uiState
    val authState = authViewModel.authState

    DisposableEffect(Unit) {
        DesktopMenuBridge.onCopyFile = { appViewModel.requestCopyMarkdownFile(null) }
        DesktopMenuBridge.onPasteFile = { appViewModel.requestPasteMarkdownFile(null) }
        DesktopMenuBridge.onImportFile = {
            coroutineScope.launch {
                runCatching {
                    pickMarkdownFileForImport()?.let { importedFile ->
                        appViewModel.requestImportMarkdownFile(
                            fileName = importedFile.fileName,
                            content = importedFile.content,
                        )
                    }
                }.onFailure { error ->
                    appViewModel.onMarkdownFileImportFailed(
                        userFacingErrorMessage(
                            error = error,
                            fallback = "Unable to import the Markdown file.",
                        )
                    )
                }
            }
        }
        DesktopMenuBridge.onExportFile = {
            coroutineScope.launch {
                val snapshot = appViewModel.exportMarkdownSnapshot() ?: return@launch
                runCatching {
                    exportMarkdownFile(
                        defaultFileName = snapshot.fileName,
                        content = snapshot.content,
                    )
                }.fold(
                    onSuccess = { exportedPath ->
                        if (exportedPath != null) {
                            appViewModel.onMarkdownFileExported(exportedPath)
                        }
                    },
                    onFailure = { error ->
                        appViewModel.onMarkdownFileExportFailed(
                            userFacingErrorMessage(
                                error = error,
                                fallback = "Unable to export the Markdown file.",
                            )
                        )
                    },
                )
            }
        }
        DesktopMenuBridge.onExportPdf = {
            coroutineScope.launch {
                val snapshot = appViewModel.exportMarkdownSnapshot() ?: return@launch
                runCatching {
                    exportPdfFile(
                        defaultFileName = snapshot.fileName,
                        title = snapshot.fileName.substringBeforeLast('.'),
                        content = snapshot.content,
                    )
                }.fold(
                    onSuccess = { exportedPath ->
                        if (exportedPath != null) {
                            appViewModel.onPdfFileExported(exportedPath)
                        }
                    },
                    onFailure = { error ->
                        appViewModel.onMarkdownFileExportFailed(
                            userFacingErrorMessage(
                                error = error,
                                fallback = "Unable to download the PDF.",
                            )
                        )
                    },
                )
            }
        }
        DesktopMenuBridge.onPrintPreview = {
            coroutineScope.launch {
                val snapshot = appViewModel.exportMarkdownSnapshot() ?: return@launch
                runCatching {
                    printMarkdownPreview(
                        title = snapshot.fileName.substringBeforeLast('.'),
                        content = snapshot.content,
                    )
                }.onSuccess { submitted ->
                    if (submitted) {
                        appViewModel.onPreviewPrintSubmitted()
                    }
                }.onFailure { error ->
                    appViewModel.onMarkdownFileExportFailed(
                        userFacingErrorMessage(
                            error = error,
                            fallback = "Unable to print the preview.",
                        )
                    )
                }
            }
        }
        DesktopMenuBridge.onShareDocument = { appViewModel.requestShowShareDialog() }
        DesktopMenuBridge.onShowSharedLinks = { appViewModel.requestShowSharedLinksDialog() }
        DesktopMenuBridge.onSwitchRepository = { appViewModel.openRepositoryPicker() }
        DesktopMenuBridge.onSignOut = {
            workspacePreferencesStorage.clearLastRepositoryFullName()
            authViewModel.signOut()
            appViewModel.signOut()
        }
        DesktopMenuBridge.onToggleShowNonMarkdownFiles = { appViewModel.toggleShowNonMarkdownFiles() }
        DesktopMenuBridge.onShowAbout = { showAboutDialog = true }
        onDispose {
            DesktopMenuBridge.onCopyFile = null
            DesktopMenuBridge.onPasteFile = null
            DesktopMenuBridge.onImportFile = null
            DesktopMenuBridge.onExportFile = null
            DesktopMenuBridge.onExportPdf = null
            DesktopMenuBridge.onPrintPreview = null
            DesktopMenuBridge.onShareDocument = null
            DesktopMenuBridge.onShowSharedLinks = null
            DesktopMenuBridge.onSwitchRepository = null
            DesktopMenuBridge.onSignOut = null
            DesktopMenuBridge.onToggleShowNonMarkdownFiles = null
            DesktopMenuBridge.onShowAbout = null
        }
    }

    LaunchedEffect(
        appState.mode,
        appState.selectedMarkdownPath,
        appState.activeDocument?.path?.value,
        appState.copiedMarkdownPath,
        appState.showNonMarkdownFiles,
        appState.createInProgress,
        appState.saveInProgress,
        appState.documentLoading,
        appState.renameInProgress,
        appState.moveInProgress,
        appState.pasteInProgress,
        appState.session,
    ) {
        DesktopMenuBridge.inWorkspace = appState.mode == AppMode.Workspace
        DesktopMenuBridge.canCopyFile = appState.mode == AppMode.Workspace &&
            (appState.selectedMarkdownPath != null || appState.activeDocument != null)
        DesktopMenuBridge.canPasteFile = appState.mode == AppMode.Workspace &&
            !appState.copiedMarkdownPath.isNullOrBlank() &&
            !appState.pasteInProgress
        DesktopMenuBridge.canImportFile = appState.mode == AppMode.Workspace &&
            !appState.createInProgress &&
            !appState.saveInProgress &&
            !appState.documentLoading &&
            !appState.renameInProgress &&
            !appState.moveInProgress &&
            !appState.pasteInProgress
        DesktopMenuBridge.canExportFile = appState.mode == AppMode.Workspace &&
            appState.activeDocument != null &&
            !appState.documentLoading
        DesktopMenuBridge.canExportPdf = DesktopMenuBridge.canExportFile
        DesktopMenuBridge.canPrintPreview = DesktopMenuBridge.canExportFile
        DesktopMenuBridge.canShareDocument = appState.mode == AppMode.Workspace &&
            appState.activeDocument != null &&
            !appState.documentLoading &&
            !appState.shareInProgress
        DesktopMenuBridge.isSignedIn = appState.session != null
        DesktopMenuBridge.showNonMarkdownFiles = appState.showNonMarkdownFiles
    }

    LaunchedEffect(appState.mode, appState.session?.accessToken, appState.repositoryLoadNonce) {
        val session = appState.session
        if (appState.mode != AppMode.RepositorySelection || session == null) {
            return@LaunchedEffect
        }
        if (appState.repositoriesLoading || appState.repositoryOptions.isNotEmpty() || appState.repositoriesError != null) {
            return@LaunchedEffect
        }

        appViewModel.beginRepositoryLoad()
        val repositoriesResult = loadRepositoriesUseCase(session)
        repositoriesResult.fold(
            onSuccess = appViewModel::onRepositoriesLoaded,
            onFailure = { error ->
                appViewModel.onRepositoriesLoadFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to load repositories right now.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.repositoryOptions,
        appState.selectedRepository?.fullName,
    ) {
        val session = appState.session
        if (appState.mode != AppMode.RepositorySelection || session == null) {
            return@LaunchedEffect
        }
        if (appState.repositoryOptions.isEmpty() || appState.selectedRepository != null) {
            return@LaunchedEffect
        }
        if (restoredRepositoryForSession == session.accessToken) {
            return@LaunchedEffect
        }
        restoredRepositoryForSession = session.accessToken

        val lastFullName = workspacePreferencesStorage.loadLastRepositoryFullName() ?: return@LaunchedEffect
        val matchingRepository = appState.repositoryOptions.firstOrNull { repo ->
            repo.fullName == lastFullName
        } ?: return@LaunchedEffect
        appViewModel.selectRepository(matchingRepository)
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.repoTreeLoadNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        if (appState.mode != AppMode.Workspace || session == null || repository == null) {
            return@LaunchedEffect
        }
        if (appState.repoTreeLoading || appState.repoTreeRoots.isNotEmpty() || appState.repoTreeError != null) {
            return@LaunchedEffect
        }

        appViewModel.beginRepoTreeLoad()
        val treeResult = loadRepoTreeUseCase(session, repository)
        treeResult.fold(
            onSuccess = appViewModel::onRepoTreeLoaded,
            onFailure = { error ->
                appViewModel.onRepoTreeLoadFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to load repository files.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.createRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val requestPath = appState.createRequestPath
        val requestContent = appState.createRequestContent
        val commitMessage = appState.createRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            requestPath == null
        ) {
            return@LaunchedEffect
        }
        if (appState.createInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentCreate()
        val createResult = createDocumentUseCase(
            session = session,
            repository = repository,
            path = DocumentPath(requestPath),
            initialContent = requestContent,
            commitMessage = commitMessage,
        )
        createResult.fold(
            onSuccess = appViewModel::onDocumentCreated,
            onFailure = { error ->
                appViewModel.onDocumentCreateFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to create the Markdown file.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.createFolderRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val folderPath = appState.createFolderRequestPath
        val commitMessage = appState.createFolderRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            folderPath == null
        ) {
            return@LaunchedEffect
        }
        if (appState.createFolderInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginFolderCreate()
        val createResult = createFolderUseCase(
            session = session,
            repository = repository,
            folderPath = folderPath,
            commitMessage = commitMessage,
        )
        createResult.fold(
            onSuccess = { appViewModel.onFolderCreated(folderPath) },
            onFailure = { error ->
                appViewModel.onFolderCreateFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to create the folder.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.renameRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val activeDocument = appState.activeDocument
        val requestPath = appState.renameRequestPath
        val commitMessage = appState.renameRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            activeDocument == null ||
            requestPath == null
        ) {
            return@LaunchedEffect
        }
        if (appState.renameInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentRename()
        val renameResult = renameDocumentUseCase(
            session = session,
            repository = repository,
            document = activeDocument,
            newPath = DocumentPath(requestPath),
            commitMessage = commitMessage,
        )
        renameResult.fold(
            onSuccess = appViewModel::onDocumentRenamed,
            onFailure = { error ->
                appViewModel.onDocumentRenameFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to rename the Markdown file.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.moveRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val movePairs = appState.moveRequestPaths
        val activeDocument = appState.activeDocument
        val commitMessage = appState.moveRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            movePairs.isEmpty()
        ) {
            return@LaunchedEffect
        }
        if (appState.moveInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentMove()
        var lastMovedDocument: com.rdev.rrepodocs.domain.model.MarkdownDocument? = null
        movePairs.forEach { (sourcePath, destinationPath) ->
            val documentToMove = if (activeDocument?.path?.value == sourcePath) {
                activeDocument
            } else {
                openDocumentUseCase(
                    session = session,
                    repository = repository,
                    path = DocumentPath(sourcePath),
                ).getOrElse { error ->
                    appViewModel.onDocumentMoveFailed(
                        userFacingErrorMessage(
                            error = error,
                            fallback = "Unable to load the Markdown file before moving.",
                        )
                    )
                    return@LaunchedEffect
                }
            }
            moveDocumentUseCase(
                session = session,
                repository = repository,
                document = documentToMove,
                destinationPath = DocumentPath(destinationPath),
                commitMessage = commitMessage,
            ).fold(
                onSuccess = { movedDocument -> lastMovedDocument = movedDocument },
                onFailure = { error ->
                    appViewModel.onDocumentMoveFailed(
                        userFacingErrorMessage(
                            error = error,
                            fallback = "Unable to move the Markdown file.",
                        )
                    )
                    return@LaunchedEffect
                },
            )
        }
        lastMovedDocument?.let(appViewModel::onDocumentMoved)
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.deleteRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val requestPaths = appState.deleteRequestPaths
        val commitMessage = appState.deleteRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            requestPaths.isEmpty()
        ) {
            return@LaunchedEffect
        }
        if (appState.deleteInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginDeleteRepositoryItems()
        deleteRepositoryItemsUseCase(
            session = session,
            repository = repository,
            paths = requestPaths,
            commitMessage = commitMessage,
        ).fold(
            onSuccess = { appViewModel.onRepositoryItemsDeleted(requestPaths) },
            onFailure = { error ->
                appViewModel.onRepositoryItemsDeleteFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to delete the selected item.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.pasteRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val sourcePath = appState.pasteRequestSourcePath
        val destinationPath = appState.pasteRequestDestinationPath
        val commitMessage = appState.pasteRequestCommitMessage
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            sourcePath == null ||
            destinationPath == null
        ) {
            return@LaunchedEffect
        }
        if (appState.pasteInProgress) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentPaste()
        val openSourceResult = openDocumentUseCase(
            session = session,
            repository = repository,
            path = DocumentPath(sourcePath),
        )
        val sourceDocument = openSourceResult.getOrElse { error ->
            appViewModel.onDocumentPasteFailed(
                userFacingErrorMessage(
                    error = error,
                    fallback = "Unable to load copied markdown file.",
                )
            )
            return@LaunchedEffect
        }

        val createResult = createDocumentUseCase(
            session = session,
            repository = repository,
            path = DocumentPath(destinationPath),
            initialContent = sourceDocument.content,
            commitMessage = commitMessage,
        )
        createResult.fold(
            onSuccess = appViewModel::onDocumentPasted,
            onFailure = { error ->
                appViewModel.onDocumentPasteFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to paste markdown file.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.activeDocument?.path?.value,
        appState.documentHistoryLoadNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val activeDocument = appState.activeDocument
        val activePath = activeDocument?.path?.value
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            activePath == null
        ) {
            return@LaunchedEffect
        }
        if (appState.documentHistoryLoading) {
            return@LaunchedEffect
        }
        if (appState.documentHistoryPath == activePath && appState.documentHistoryError == null) {
            return@LaunchedEffect
        }
        if (appState.documentHistoryPath == activePath && appState.documentHistoryError != null) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentHistoryLoad()
        val historyResult = loadDocumentHistoryUseCase(
            session = session,
            repository = repository,
            path = activeDocument.path,
        )
        historyResult.fold(
            onSuccess = { history ->
                appViewModel.onDocumentHistoryLoaded(activePath, history)
            },
            onFailure = { error ->
                appViewModel.onDocumentHistoryLoadFailed(
                    path = activePath,
                    message = userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to load commit history for this file.",
                    ),
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.saveRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val activeDocument = appState.activeDocument
        val editState = appState.documentEditState
        val commitMessage = appState.commitMessageDraft.trim()
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            activeDocument == null ||
            editState == null
        ) {
            return@LaunchedEffect
        }
        if (!editState.isDirty || appState.saveInProgress || commitMessage.isBlank()) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentSave()
        val saveResult = saveDocumentUseCase(
            session = session,
            repository = repository,
            document = activeDocument,
            updatedContent = editState.currentContent,
            commitMessage = commitMessage,
        )
        saveResult.fold(
            onSuccess = appViewModel::onDocumentSaved,
            onFailure = { error ->
                appViewModel.onDocumentSaveFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to save changes to GitHub.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.activeDocument?.path?.value,
        appState.shareExpiryOption,
        appState.shareRequestNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val activeDocument = appState.activeDocument
        if (
            appState.mode != AppMode.Workspace ||
            session == null ||
            repository == null ||
            activeDocument == null
        ) {
            return@LaunchedEffect
        }
        if (!appState.shareDialogVisible || appState.shareRequestNonce == 0 || appState.shareInProgress || appState.documentEditState?.isDirty == true) {
            return@LaunchedEffect
        }

        appViewModel.beginShareCreate()
        val shareResult = shareDocumentUseCase(
            session = session,
            repository = repository,
            document = activeDocument,
            expires = appState.shareExpiryOption,
        )
        shareResult.fold(
            onSuccess = appViewModel::onShareCreated,
            onFailure = { error ->
                appViewModel.onShareCreateFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to create a public share.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.sharedLinksDialogVisible,
        appState.sharedLinksLoadNonce,
    ) {
        val session = appState.session
        if (appState.mode != AppMode.Workspace || session == null || !appState.sharedLinksDialogVisible) {
            return@LaunchedEffect
        }
        if (appState.sharedLinksLoading) {
            return@LaunchedEffect
        }

        appViewModel.beginSharedLinksLoad()
        val sharesResult = loadDocumentSharesUseCase(session)
        sharesResult.fold(
            onSuccess = appViewModel::onSharedLinksLoaded,
            onFailure = { error ->
                appViewModel.onSharedLinksLoadFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to load shared links.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.activeShare?.id,
        appState.pendingShareRevokeId,
        appState.shareRevokeRequestNonce,
    ) {
        val session = appState.session
        val share = appState.activeShare
        if (appState.mode != AppMode.Workspace || session == null || share == null) {
            return@LaunchedEffect
        }
        if (
            appState.pendingShareRevokeId != share.id ||
            appState.shareRevokeRequestNonce == 0 ||
            appState.shareRevokeInProgress ||
            share.revokedAt != null
        ) {
            return@LaunchedEffect
        }

        appViewModel.beginShareRevoke()
        val revokeResult = revokeShareUseCase(session, share)
        revokeResult.fold(
            onSuccess = appViewModel::onShareRevoked,
            onFailure = { error ->
                appViewModel.onShareRevokeFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to revoke this public share.",
                    )
                )
            },
        )
    }

    LaunchedEffect(
        appState.mode,
        appState.session?.accessToken,
        appState.selectedRepository?.fullName,
        appState.selectedMarkdownPath,
        appState.documentLoadNonce,
    ) {
        val session = appState.session
        val repository = appState.selectedRepository
        val selectedPath = appState.selectedMarkdownPath
        if (appState.mode != AppMode.Workspace || session == null || repository == null || selectedPath == null) {
            return@LaunchedEffect
        }
        if (appState.documentLoading) {
            return@LaunchedEffect
        }
        if (
            appState.activeDocument?.path?.value == selectedPath &&
            appState.documentError == null &&
            appState.loadedDocumentLoadNonce == appState.documentLoadNonce
        ) {
            return@LaunchedEffect
        }

        appViewModel.beginDocumentLoad()
        val openResult = openDocumentUseCase(
            session = session,
            repository = repository,
            path = DocumentPath(selectedPath),
        )
        openResult.fold(
            onSuccess = appViewModel::onDocumentLoaded,
            onFailure = { error ->
                appViewModel.onDocumentLoadFailed(
                    userFacingErrorMessage(
                        error = error,
                        fallback = "Unable to open this Markdown file.",
                    )
                )
            },
        )
    }

    LaunchedEffect(appState.selectedRepository?.fullName) {
        val selectedFullName = appState.selectedRepository?.fullName ?: return@LaunchedEffect
        workspacePreferencesStorage.saveLastRepositoryFullName(selectedFullName)
    }

    RRepoDocsTheme {
        when (appState.mode) {
            AppMode.SignedOut -> {
                AuthScreen(
                    isLoading = authState is AuthState.Loading,
                    errorMessage = (authState as? AuthState.Error)?.message,
                    isAwaitingAuthorization = authViewModel.isAwaitingAuthorization,
                    verificationUri = authViewModel.verificationUri,
                    verificationUriComplete = authViewModel.verificationUriComplete,
                    userCode = authViewModel.userCode,
                    onStartSignIn = {
                        coroutineScope.launch {
                            authViewModel.startDeviceSignIn()
                        }
                    },
                    onFinishSignIn = {
                        coroutineScope.launch {
                            val session = authViewModel.finishDeviceSignIn()
                            if (session != null) {
                                appViewModel.onSignedIn(session)
                            }
                        }
                    },
                )
            }

            AppMode.RepositorySelection -> {
                RepoPickerScreen(
                    repositories = appState.repositoryOptions,
                    isLoading = appState.repositoriesLoading,
                    errorMessage = appState.repositoriesError,
                    onRepositorySelected = appViewModel::selectRepository,
                    onRetry = appViewModel::retryRepositoryLoad,
                    onSignOut = {
                        workspacePreferencesStorage.clearLastRepositoryFullName()
                        authViewModel.signOut()
                        appViewModel.signOut()
                    },
                )
            }

            AppMode.Workspace -> {
                WorkspaceScreen(
                    repositoryName = appState.selectedRepository?.fullName ?: "Repository",
                    repositoryOwnerLogin = appState.selectedRepository?.ownerLogin ?: "GitHub",
                    repositoryOwnerAvatarUrl = appState.selectedRepository?.ownerAvatarUrl,
                    viewerUsername = appState.session?.username,
                    viewerUserId = appState.session?.userId,
                    viewerAvatarUrl = appState.session?.avatarUrl,
                    showNonMarkdownFiles = appState.showNonMarkdownFiles,
                    treeRoots = appState.repoTreeRoots,
                    expandedFolderPaths = appState.expandedFolderPaths,
                    selectedExplorerPaths = appState.selectedExplorerPaths,
                    selectedMarkdownPath = appState.selectedMarkdownPath,
                    treeLoading = appState.repoTreeLoading,
                    treeError = appState.repoTreeError,
                    onToggleFolder = appViewModel::toggleFolderExpansion,
                    onCollapseFolders = appViewModel::collapseFolderExpansions,
                    onExpandFolders = appViewModel::expandFolderExpansions,
                    onSelectExplorerPath = appViewModel::selectExplorerPath,
                    onToggleShowNonMarkdownFiles = appViewModel::toggleShowNonMarkdownFiles,
                    onSelectMarkdownFile = appViewModel::requestOpenMarkdownFile,
                    onRefreshTree = appViewModel::requestRepositorySync,
                    activeDocumentPath = appState.activeDocument?.path?.value,
                    documentHistoryEntries = appState.documentHistory,
                    documentHistoryLoading = appState.documentHistoryLoading,
                    documentHistoryError = appState.documentHistoryError,
                    editorContent = appState.documentEditState?.currentContent.orEmpty(),
                    documentIsDirty = appState.documentEditState?.isDirty == true,
                    documentLoading = appState.documentLoading,
                    documentError = appState.documentError,
                    pendingDocumentPath = appState.pendingDocumentPath,
                    commitMessageDraft = appState.commitMessageDraft,
                    saveInProgress = appState.saveInProgress,
                    saveError = appState.saveError,
                    saveSuccess = appState.saveSuccess,
                    createDialogVisible = appState.createDialogVisible,
                    createTargetFolderDraft = appState.createTargetFolderDraft,
                    createFileNameDraft = appState.createFileNameDraft,
                    createCommitMessageDraft = appState.createCommitMessageDraft,
                    createInProgress = appState.createInProgress,
                    createFolderDialogVisible = appState.createFolderDialogVisible,
                    createFolderParentDraft = appState.createFolderParentDraft,
                    createFolderNameDraft = appState.createFolderNameDraft,
                    createFolderCommitMessageDraft = appState.createFolderCommitMessageDraft,
                    createFolderInProgress = appState.createFolderInProgress,
                    createFolderError = appState.createFolderError,
                    createError = appState.createError,
                    renameDialogVisible = appState.renameDialogVisible,
                    renameFileNameDraft = appState.renameFileNameDraft,
                    renameCommitMessageDraft = appState.renameCommitMessageDraft,
                    renameInProgress = appState.renameInProgress,
                    renameError = appState.renameError,
                    moveDialogVisible = appState.moveDialogVisible,
                    moveDestinationFolderDraft = appState.moveDestinationFolderDraft,
                    moveCommitMessageDraft = appState.moveCommitMessageDraft,
                    moveInProgress = appState.moveInProgress,
                    moveError = appState.moveError,
                    copiedMarkdownPath = appState.copiedMarkdownPath,
                    pasteInProgress = appState.pasteInProgress,
                    pasteError = appState.pasteError,
                    shareDialogVisible = appState.shareDialogVisible,
                    shareExpiryOption = appState.shareExpiryOption,
                    activeShare = appState.activeShare,
                    shareInProgress = appState.shareInProgress,
                    shareError = appState.shareError,
                    pendingShareRevokeId = appState.pendingShareRevokeId,
                    shareRevokeInProgress = appState.shareRevokeInProgress,
                    shareRevokeError = appState.shareRevokeError,
                    sharedLinksDialogVisible = appState.sharedLinksDialogVisible,
                    sharedLinks = appState.sharedLinks,
                    sharedLinksLoading = appState.sharedLinksLoading,
                    sharedLinksError = appState.sharedLinksError,
                    onEditorContentChanged = appViewModel::updateDocumentContent,
                    onCommitMessageChanged = appViewModel::updateCommitMessageDraft,
                    onSaveDocument = appViewModel::requestSaveDocument,
                    onStartCreateDocument = appViewModel::requestShowCreateDocumentDialog,
                    onDismissCreateDocumentDialog = appViewModel::dismissCreateDocumentDialog,
                    onCreateTargetFolderChanged = appViewModel::updateCreateTargetFolderDraft,
                    onCreateFileNameChanged = appViewModel::updateCreateFileNameDraft,
                    onCreateCommitMessageChanged = appViewModel::updateCreateCommitMessageDraft,
                    onConfirmCreateDocument = appViewModel::requestCreateDocument,
                    onStartCreateFolder = appViewModel::requestShowCreateFolderDialog,
                    onDismissCreateFolderDialog = appViewModel::dismissCreateFolderDialog,
                    onCreateFolderParentChanged = appViewModel::updateCreateFolderParentDraft,
                    onCreateFolderNameChanged = appViewModel::updateCreateFolderNameDraft,
                    onCreateFolderCommitMessageChanged = appViewModel::updateCreateFolderCommitMessageDraft,
                    onConfirmCreateFolder = appViewModel::requestCreateFolder,
                    onStartRenameDocument = appViewModel::requestShowRenameDocumentDialog,
                    onDismissRenameDocumentDialog = appViewModel::dismissRenameDocumentDialog,
                    onRenameFileNameChanged = appViewModel::updateRenameFileNameDraft,
                    onRenameCommitMessageChanged = appViewModel::updateRenameCommitMessageDraft,
                    onConfirmRenameDocument = appViewModel::requestRenameDocument,
                    onStartMoveDocument = appViewModel::requestShowMoveDocumentDialog,
                    onDismissMoveDocumentDialog = appViewModel::dismissMoveDocumentDialog,
                    onMoveDestinationFolderChanged = appViewModel::updateMoveDestinationFolderDraft,
                    onMoveCommitMessageChanged = appViewModel::updateMoveCommitMessageDraft,
                    onConfirmMoveDocument = appViewModel::requestMoveDocument,
                    onMoveMarkdownFileToFolder = appViewModel::requestMoveMarkdownFileToFolder,
                    onMoveMarkdownFilesToFolder = appViewModel::requestMoveMarkdownFilesToFolder,
                    onDeleteExplorerPaths = appViewModel::requestDeleteExplorerPaths,
                    onCopyMarkdownFile = appViewModel::requestCopyMarkdownFile,
                    onPasteMarkdownFile = appViewModel::requestPasteMarkdownFile,
                    onStartShareDocument = appViewModel::requestShowShareDialog,
                    onDismissShareDialog = appViewModel::dismissShareDialog,
                    onShareExpiryChanged = appViewModel::updateShareExpiryOption,
                    onConfirmShareDocument = appViewModel::requestCreateShare,
                    onRevokeShare = appViewModel::requestRevokeShare,
                    onStartSharedLinks = appViewModel::requestShowSharedLinksDialog,
                    onDismissSharedLinksDialog = appViewModel::dismissSharedLinksDialog,
                    onRetrySharedLinks = appViewModel::retrySharedLinksLoad,
                    onRevokeSharedLink = appViewModel::requestRevokeSharedLink,
                    onRetryDocumentOpen = appViewModel::retryOpenDocument,
                    onRetryDocumentHistory = appViewModel::retryDocumentHistory,
                    onDiscardUnsavedAndOpenPending = appViewModel::discardUnsavedAndOpenPendingDocument,
                    onKeepEditingCurrent = appViewModel::keepEditingCurrentDocument,
                    onBackToRepositories = appViewModel::openRepositoryPicker,
                    onSignOut = {
                        workspacePreferencesStorage.clearLastRepositoryFullName()
                        authViewModel.signOut()
                        appViewModel.signOut()
                    },
                )
            }
        }
        if (showAboutDialog) {
            AboutDialog(onDismissRequest = { showAboutDialog = false })
        }
    }
}

private fun userFacingErrorMessage(
    error: Throwable,
    fallback: String,
): String {
    val message = error.message?.trim().orEmpty()
    if (message.isBlank()) return fallback

    val normalized = message.lowercase()
    return when {
        "401" in normalized || "bad credentials" in normalized || "authentication" in normalized -> {
            "GitHub authentication expired. Sign in again and retry."
        }
        "403" in normalized || "denied" in normalized || "forbidden" in normalized -> {
            "GitHub denied this operation. Check repository permissions."
        }
        "404" in normalized || "not found" in normalized -> {
            "The requested repository item was not found."
        }
        "timeout" in normalized || "could not contact github api" in normalized || "network" in normalized -> {
            "Could not reach GitHub. Check your connection and try again."
        }
        "gitHub request failed".lowercase() in normalized -> {
            fallback
        }
        else -> message
    }
}
