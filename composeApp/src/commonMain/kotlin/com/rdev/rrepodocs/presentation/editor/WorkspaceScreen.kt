package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Commit
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.rdev.rrepodocs.domain.model.DocumentHistoryEntry
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdev.rrepodocs.domain.model.DocumentShare
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepoTreeNodeKind
import com.rdev.rrepodocs.domain.model.ShareExpiryOption
import com.rdev.rrepodocs.platform.copyTextToClipboard
import com.rdev.rrepodocs.platform.openExternalUrl
import com.rdev.rrepodocs.platform.setPreviewScreenAwake
import com.rdev.rrepodocs.presentation.app.AppThemeTokens
import com.rdev.rrepodocs.resources.Res
import com.rdev.rrepodocs.resources.app_icon
import com.rdev.rrepodocs.resources.logo
import com.rdev.rrepodocs.resources.rdev_logo_dark
import org.jetbrains.compose.resources.painterResource

@Composable
fun WorkspaceScreen(
    repositoryName: String,
    repositoryOwnerLogin: String,
    repositoryOwnerAvatarUrl: String?,
    viewerUsername: String?,
    viewerUserId: String?,
    viewerAvatarUrl: String?,
    showNonMarkdownFiles: Boolean,
    treeRoots: List<RepoTreeNode>,
    expandedFolderPaths: Set<String>,
    selectedExplorerPaths: Set<String>,
    selectedMarkdownPath: String?,
    treeLoading: Boolean,
    treeError: String?,
    onToggleFolder: (String) -> Unit,
    onSelectExplorerPath: (String, Boolean) -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onSelectMarkdownFile: (String) -> Unit,
    onRefreshTree: () -> Unit,
    activeDocumentPath: String?,
    documentHistoryEntries: List<DocumentHistoryEntry>,
    documentHistoryLoading: Boolean,
    documentHistoryError: String?,
    editorContent: String,
    documentIsDirty: Boolean,
    documentLoading: Boolean,
    documentError: String?,
    pendingDocumentPath: String?,
    commitMessageDraft: String,
    saveInProgress: Boolean,
    saveError: String?,
    saveSuccess: String?,
    createDialogVisible: Boolean,
    createTargetFolderDraft: String,
    createFileNameDraft: String,
    createCommitMessageDraft: String,
    createInProgress: Boolean,
    createError: String?,
    createFolderDialogVisible: Boolean,
    createFolderParentDraft: String,
    createFolderNameDraft: String,
    createFolderCommitMessageDraft: String,
    createFolderInProgress: Boolean,
    createFolderError: String?,
    renameDialogVisible: Boolean,
    renameFileNameDraft: String,
    renameCommitMessageDraft: String,
    renameInProgress: Boolean,
    renameError: String?,
    moveDialogVisible: Boolean,
    moveDestinationFolderDraft: String,
    moveCommitMessageDraft: String,
    moveInProgress: Boolean,
    moveError: String?,
    copiedMarkdownPath: String?,
    pasteInProgress: Boolean,
    pasteError: String?,
    shareDialogVisible: Boolean,
    shareExpiryOption: ShareExpiryOption,
    activeShare: DocumentShare?,
    shareInProgress: Boolean,
    shareError: String?,
    pendingShareRevokeId: String?,
    shareRevokeInProgress: Boolean,
    shareRevokeError: String?,
    sharedLinksDialogVisible: Boolean,
    sharedLinks: List<DocumentShare>,
    sharedLinksLoading: Boolean,
    sharedLinksError: String?,
    onEditorContentChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onSaveDocument: () -> Unit,
    onStartCreateDocument: () -> Unit,
    onDismissCreateDocumentDialog: () -> Unit,
    onCreateTargetFolderChanged: (String) -> Unit,
    onCreateFileNameChanged: (String) -> Unit,
    onCreateCommitMessageChanged: (String) -> Unit,
    onConfirmCreateDocument: () -> Unit,
    onStartCreateFolder: (String?) -> Unit,
    onDismissCreateFolderDialog: () -> Unit,
    onCreateFolderParentChanged: (String) -> Unit,
    onCreateFolderNameChanged: (String) -> Unit,
    onCreateFolderCommitMessageChanged: (String) -> Unit,
    onConfirmCreateFolder: () -> Unit,
    onStartRenameDocument: () -> Unit,
    onDismissRenameDocumentDialog: () -> Unit,
    onRenameFileNameChanged: (String) -> Unit,
    onRenameCommitMessageChanged: (String) -> Unit,
    onConfirmRenameDocument: () -> Unit,
    onStartMoveDocument: () -> Unit,
    onDismissMoveDocumentDialog: () -> Unit,
    onMoveDestinationFolderChanged: (String) -> Unit,
    onMoveCommitMessageChanged: (String) -> Unit,
    onConfirmMoveDocument: () -> Unit,
    onMoveMarkdownFileToFolder: (String, String) -> Unit,
    onMoveMarkdownFilesToFolder: (List<String>, String) -> Unit,
    onDeleteExplorerPaths: (List<String>) -> Unit,
    onCopyMarkdownFile: (String?) -> Unit,
    onPasteMarkdownFile: (String?) -> Unit,
    onStartShareDocument: () -> Unit,
    onDismissShareDialog: () -> Unit,
    onShareExpiryChanged: (ShareExpiryOption) -> Unit,
    onConfirmShareDocument: () -> Unit,
    onRevokeShare: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onDismissSharedLinksDialog: () -> Unit,
    onRetrySharedLinks: () -> Unit,
    onRevokeSharedLink: (DocumentShare) -> Unit,
    onRetryDocumentOpen: () -> Unit,
    onRetryDocumentHistory: () -> Unit,
    onDiscardUnsavedAndOpenPending: () -> Unit,
    onKeepEditingCurrent: () -> Unit,
    onBackToRepositories: () -> Unit,
    onSignOut: () -> Unit,
) {
    var sourceNavigation by remember { mutableStateOf<EditorSourceNavigation?>(null) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        val desktopLayout = maxWidth >= 900.dp
        if (desktopLayout) {
            DesktopWorkspaceLayout(
                repositoryName = repositoryName,
                repositoryOwnerLogin = repositoryOwnerLogin,
                repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl,
                viewerUsername = viewerUsername,
                viewerUserId = viewerUserId,
                viewerAvatarUrl = viewerAvatarUrl,
                showNonMarkdownFiles = showNonMarkdownFiles,
                treeRoots = treeRoots,
                expandedFolderPaths = expandedFolderPaths,
                selectedExplorerPaths = selectedExplorerPaths,
                selectedMarkdownPath = selectedMarkdownPath,
                treeLoading = treeLoading,
                treeError = treeError,
                onToggleFolder = onToggleFolder,
                onSelectExplorerPath = onSelectExplorerPath,
                onToggleShowNonMarkdownFiles = onToggleShowNonMarkdownFiles,
                onSelectMarkdownFile = onSelectMarkdownFile,
                onRefreshTree = onRefreshTree,
                activeDocumentPath = activeDocumentPath,
                documentHistoryEntries = documentHistoryEntries,
                documentHistoryLoading = documentHistoryLoading,
                documentHistoryError = documentHistoryError,
                editorContent = editorContent,
                documentIsDirty = documentIsDirty,
                documentLoading = documentLoading,
                documentError = documentError,
                pendingDocumentPath = pendingDocumentPath,
                commitMessageDraft = commitMessageDraft,
                saveInProgress = saveInProgress,
                saveError = saveError,
                saveSuccess = saveSuccess,
                createInProgress = createInProgress,
                createFolderInProgress = createFolderInProgress,
                renameInProgress = renameInProgress,
                moveInProgress = moveInProgress,
                copiedMarkdownPath = copiedMarkdownPath,
                pasteInProgress = pasteInProgress,
                pasteError = pasteError,
                shareInProgress = shareInProgress,
                onStartShareDocument = onStartShareDocument,
                onStartSharedLinks = onStartSharedLinks,
                onEditorContentChanged = onEditorContentChanged,
                onCommitMessageChanged = onCommitMessageChanged,
                onSaveDocument = onSaveDocument,
                onStartCreateDocument = onStartCreateDocument,
                onStartCreateFolder = onStartCreateFolder,
                onStartRenameDocument = onStartRenameDocument,
                onStartMoveDocument = onStartMoveDocument,
                onMoveDestinationFolderChanged = onMoveDestinationFolderChanged,
                onMoveMarkdownFileToFolder = onMoveMarkdownFileToFolder,
                onMoveMarkdownFilesToFolder = onMoveMarkdownFilesToFolder,
                onDeleteExplorerPaths = onDeleteExplorerPaths,
                onCopyMarkdownFile = onCopyMarkdownFile,
                onPasteMarkdownFile = onPasteMarkdownFile,
                onRetryDocumentOpen = onRetryDocumentOpen,
                onRetryDocumentHistory = onRetryDocumentHistory,
                onDiscardUnsavedAndOpenPending = onDiscardUnsavedAndOpenPending,
                onKeepEditingCurrent = onKeepEditingCurrent,
                sourceNavigation = sourceNavigation,
                onPreviewSourceSelected = { offset ->
                    sourceNavigation = EditorSourceNavigation(offset, (sourceNavigation?.requestId ?: 0) + 1)
                },
            )
        } else {
            MobileWorkspaceLayout(
                repositoryName = repositoryName,
                repositoryOwnerLogin = repositoryOwnerLogin,
                repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl,
                viewerUsername = viewerUsername,
                viewerUserId = viewerUserId,
                viewerAvatarUrl = viewerAvatarUrl,
                showNonMarkdownFiles = showNonMarkdownFiles,
                treeRoots = treeRoots,
                expandedFolderPaths = expandedFolderPaths,
                selectedExplorerPaths = selectedExplorerPaths,
                selectedMarkdownPath = selectedMarkdownPath,
                treeLoading = treeLoading,
                treeError = treeError,
                onToggleFolder = onToggleFolder,
                onSelectExplorerPath = onSelectExplorerPath,
                onToggleShowNonMarkdownFiles = onToggleShowNonMarkdownFiles,
                onSelectMarkdownFile = onSelectMarkdownFile,
                onRefreshTree = onRefreshTree,
                activeDocumentPath = activeDocumentPath,
                documentHistoryEntries = documentHistoryEntries,
                documentHistoryLoading = documentHistoryLoading,
                documentHistoryError = documentHistoryError,
                editorContent = editorContent,
                documentIsDirty = documentIsDirty,
                documentLoading = documentLoading,
                documentError = documentError,
                pendingDocumentPath = pendingDocumentPath,
                commitMessageDraft = commitMessageDraft,
                saveInProgress = saveInProgress,
                saveError = saveError,
                saveSuccess = saveSuccess,
                createInProgress = createInProgress,
                createFolderInProgress = createFolderInProgress,
                renameInProgress = renameInProgress,
                moveInProgress = moveInProgress,
                copiedMarkdownPath = copiedMarkdownPath,
                pasteInProgress = pasteInProgress,
                pasteError = pasteError,
                shareInProgress = shareInProgress,
                onStartShareDocument = onStartShareDocument,
                onStartSharedLinks = onStartSharedLinks,
                onEditorContentChanged = onEditorContentChanged,
                onCommitMessageChanged = onCommitMessageChanged,
                onSaveDocument = onSaveDocument,
                onStartCreateDocument = onStartCreateDocument,
                onStartCreateFolder = onStartCreateFolder,
                onStartRenameDocument = onStartRenameDocument,
                onStartMoveDocument = onStartMoveDocument,
                onMoveMarkdownFilesToFolder = onMoveMarkdownFilesToFolder,
                onDeleteExplorerPaths = onDeleteExplorerPaths,
                onCopyMarkdownFile = onCopyMarkdownFile,
                onPasteMarkdownFile = onPasteMarkdownFile,
                onRetryDocumentOpen = onRetryDocumentOpen,
                onRetryDocumentHistory = onRetryDocumentHistory,
                onDiscardUnsavedAndOpenPending = onDiscardUnsavedAndOpenPending,
                onKeepEditingCurrent = onKeepEditingCurrent,
                onBackToRepositories = onBackToRepositories,
                onSignOut = onSignOut,
                sourceNavigation = sourceNavigation,
                onPreviewSourceSelected = { offset ->
                    sourceNavigation = EditorSourceNavigation(offset, (sourceNavigation?.requestId ?: 0) + 1)
                },
            )
        }

        if (createDialogVisible) {
            CreateDocumentDialog(
                targetFolderDraft = createTargetFolderDraft,
                targetFolderOptions = remember(treeRoots) { repositoryFolderPaths(treeRoots) },
                fileNameDraft = createFileNameDraft,
                commitMessageDraft = createCommitMessageDraft,
                isCreating = createInProgress,
                errorMessage = createError,
                onTargetFolderChanged = onCreateTargetFolderChanged,
                onFileNameChanged = onCreateFileNameChanged,
                onCommitMessageChanged = onCreateCommitMessageChanged,
                onConfirm = onConfirmCreateDocument,
                onDismiss = onDismissCreateDocumentDialog,
            )
        }

        if (createFolderDialogVisible) {
            CreateFolderDialog(
                parentFolderDraft = createFolderParentDraft,
                parentFolderOptions = remember(treeRoots) { repositoryFolderPaths(treeRoots) },
                folderNameDraft = createFolderNameDraft,
                commitMessageDraft = createFolderCommitMessageDraft,
                isCreating = createFolderInProgress,
                errorMessage = createFolderError,
                onParentFolderChanged = onCreateFolderParentChanged,
                onFolderNameChanged = onCreateFolderNameChanged,
                onCommitMessageChanged = onCreateFolderCommitMessageChanged,
                onConfirm = onConfirmCreateFolder,
                onDismiss = onDismissCreateFolderDialog,
            )
        }

        if (renameDialogVisible) {
            RenameDocumentDialog(
                fileNameDraft = renameFileNameDraft,
                commitMessageDraft = renameCommitMessageDraft,
                isRenaming = renameInProgress,
                errorMessage = renameError,
                onFileNameChanged = onRenameFileNameChanged,
                onCommitMessageChanged = onRenameCommitMessageChanged,
                onConfirm = onConfirmRenameDocument,
                onDismiss = onDismissRenameDocumentDialog,
            )
        }

        if (moveDialogVisible) {
            MoveDocumentDialog(
                destinationFolderDraft = moveDestinationFolderDraft,
                commitMessageDraft = moveCommitMessageDraft,
                isMoving = moveInProgress,
                errorMessage = moveError,
                onDestinationFolderChanged = onMoveDestinationFolderChanged,
                onCommitMessageChanged = onMoveCommitMessageChanged,
                onConfirm = onConfirmMoveDocument,
                onDismiss = onDismissMoveDocumentDialog,
            )
        }

        if (shareDialogVisible) {
            ShareDocumentDialog(
                activeDocumentPath = activeDocumentPath,
                expiryOption = shareExpiryOption,
                activeShare = activeShare,
                isCreating = shareInProgress,
                createError = shareError,
                isRevoking = shareRevokeInProgress,
                revokeError = shareRevokeError,
                onExpiryChanged = onShareExpiryChanged,
                onConfirm = onConfirmShareDocument,
                onRevoke = onRevokeShare,
                onDismiss = onDismissShareDialog,
            )
        }

        if (sharedLinksDialogVisible) {
            SharedLinksDialog(
                shares = sharedLinks,
                isLoading = sharedLinksLoading,
                errorMessage = sharedLinksError ?: shareRevokeError,
                revokingShareId = pendingRevokingShareId(
                    pendingShareRevokeId = pendingShareRevokeId,
                    isRevoking = shareRevokeInProgress,
                ),
                onRetry = onRetrySharedLinks,
                onRevoke = onRevokeSharedLink,
                onDismiss = onDismissSharedLinksDialog,
            )
        }
    }
}

@Composable
private fun DesktopWorkspaceLayout(
    repositoryName: String,
    repositoryOwnerLogin: String,
    repositoryOwnerAvatarUrl: String?,
    viewerUsername: String?,
    viewerUserId: String?,
    viewerAvatarUrl: String?,
    showNonMarkdownFiles: Boolean,
    treeRoots: List<RepoTreeNode>,
    expandedFolderPaths: Set<String>,
    selectedExplorerPaths: Set<String>,
    selectedMarkdownPath: String?,
    treeLoading: Boolean,
    treeError: String?,
    onToggleFolder: (String) -> Unit,
    onSelectExplorerPath: (String, Boolean) -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onSelectMarkdownFile: (String) -> Unit,
    onRefreshTree: () -> Unit,
    activeDocumentPath: String?,
    documentHistoryEntries: List<DocumentHistoryEntry>,
    documentHistoryLoading: Boolean,
    documentHistoryError: String?,
    editorContent: String,
    documentIsDirty: Boolean,
    documentLoading: Boolean,
    documentError: String?,
    pendingDocumentPath: String?,
    commitMessageDraft: String,
    saveInProgress: Boolean,
    saveError: String?,
    saveSuccess: String?,
    createInProgress: Boolean,
    createFolderInProgress: Boolean,
    renameInProgress: Boolean,
    moveInProgress: Boolean,
    copiedMarkdownPath: String?,
    pasteInProgress: Boolean,
    pasteError: String?,
    shareInProgress: Boolean,
    onEditorContentChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onSaveDocument: () -> Unit,
    onStartCreateDocument: () -> Unit,
    onStartCreateFolder: (String?) -> Unit,
    onStartRenameDocument: () -> Unit,
    onStartMoveDocument: () -> Unit,
    onMoveDestinationFolderChanged: (String) -> Unit,
    onMoveMarkdownFileToFolder: (String, String) -> Unit,
    onMoveMarkdownFilesToFolder: (List<String>, String) -> Unit,
    onDeleteExplorerPaths: (List<String>) -> Unit,
    onCopyMarkdownFile: (String?) -> Unit,
    onPasteMarkdownFile: (String?) -> Unit,
    onStartShareDocument: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onRetryDocumentOpen: () -> Unit,
    onRetryDocumentHistory: () -> Unit,
    onDiscardUnsavedAndOpenPending: () -> Unit,
    onKeepEditingCurrent: () -> Unit,
    sourceNavigation: EditorSourceNavigation?,
    onPreviewSourceSelected: (Int) -> Unit,
) {
    var rightPaneMode by rememberSaveable { mutableStateOf(RightPaneMode.Preview) }
    var sidebarVisible by rememberSaveable { mutableStateOf(true) }
    var rightPaneVisible by rememberSaveable { mutableStateOf(true) }
    var leftPaneRatio by rememberSaveable { mutableStateOf(0.15f) }
    var rightPaneRatio by rememberSaveable { mutableStateOf(0.19f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyUp) return@onPreviewKeyEvent false
                val commandPressed = event.isMetaPressed || event.isCtrlPressed
                if (!commandPressed) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.S -> {
                        if (!event.isShiftPressed && !saveInProgress && !documentLoading && documentIsDirty) {
                            onSaveDocument()
                            true
                        } else {
                            false
                        }
                    }
                    Key.C -> {
                        if (event.isShiftPressed) {
                            onCopyMarkdownFile(null)
                            true
                        } else {
                            false
                        }
                    }
                    Key.V -> {
                        if (event.isShiftPressed) {
                            onPasteMarkdownFile(null)
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }
            },
    ) {
        WorkspaceHeader(
            repositoryName = repositoryName,
            repositoryOwnerLogin = viewerUsername ?: repositoryOwnerLogin,
            sidebarVisible = sidebarVisible,
            rightPaneVisible = rightPaneVisible,
            onToggleSidebar = { sidebarVisible = !sidebarVisible },
            onToggleRightPane = { rightPaneVisible = !rightPaneVisible },
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.appBg),
        ) {
            val density = LocalDensity.current
            val splitterWidthDp = 18.dp
            val splitterWidthPx = with(density) { splitterWidthDp.toPx() }
            val minLeft = 0.11f
            val minCenter = 0.30f
            val minRight = 0.14f
            val splitterCount = (if (sidebarVisible) 1 else 0) + (if (rightPaneVisible) 1 else 0)
            val contentWidthPx = (with(density) { maxWidth.toPx() } - (splitterWidthPx * splitterCount)).coerceAtLeast(1f)

            if (sidebarVisible) {
                val maxLeft = 1f - (if (rightPaneVisible) rightPaneRatio else 0f) - minCenter
                leftPaneRatio = leftPaneRatio.coerceIn(minLeft, maxLeft)
            }
            if (rightPaneVisible) {
                val maxRight = if (sidebarVisible) 1f - leftPaneRatio - minCenter else 1f - minCenter
                rightPaneRatio = rightPaneRatio.coerceIn(minRight, maxRight)
            }

            val leftWidthDp = with(density) { (contentWidthPx * leftPaneRatio).toDp() }
            val rightWidthDp = with(density) { (contentWidthPx * rightPaneRatio).toDp() }
            val centerRatio = 1f - (if (sidebarVisible) leftPaneRatio else 0f) - (if (rightPaneVisible) rightPaneRatio else 0f)
            val centerWidthDp = with(density) { (contentWidthPx * centerRatio).toDp() }

            Row(modifier = Modifier.fillMaxSize()) {
            if (sidebarVisible) {
            RepoTreePanel(
                repositoryName = repositoryName,
                repositoryOwnerLogin = viewerUsername ?: repositoryOwnerLogin,
                repositoryOwnerAvatarUrl = viewerIdentityAvatarUrl(
                    viewerAvatarUrl = viewerAvatarUrl,
                    viewerUserId = viewerUserId,
                    viewerUsername = viewerUsername,
                    repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl,
                ),
                activeDocumentPath = activeDocumentPath,
                showNonMarkdownFiles = showNonMarkdownFiles,
                treeRoots = treeRoots,
                expandedFolderPaths = expandedFolderPaths,
                selectedExplorerPaths = selectedExplorerPaths,
                selectedMarkdownPath = selectedMarkdownPath,
                isLoading = treeLoading,
                errorMessage = treeError,
                onToggleFolder = onToggleFolder,
                onSelectExplorerPath = onSelectExplorerPath,
                onSelectMarkdownFile = onSelectMarkdownFile,
                onStartCreateDocument = onStartCreateDocument,
                onStartCreateFolder = onStartCreateFolder,
                createInProgress = createInProgress,
                createFolderInProgress = createFolderInProgress,
                onStartRenameDocument = onStartRenameDocument,
                renameInProgress = renameInProgress,
                onStartMoveDocument = onStartMoveDocument,
                moveInProgress = moveInProgress,
                copiedMarkdownPath = copiedMarkdownPath,
                pasteInProgress = pasteInProgress,
                onRequestMoveDocumentToFolder = { sourcePath, destinationFolder ->
                    onMoveMarkdownFileToFolder(sourcePath, destinationFolder)
                },
                onRequestMoveDocumentsToFolder = onMoveMarkdownFilesToFolder,
                onRequestDeleteExplorerPaths = onDeleteExplorerPaths,
                onCopyMarkdownFile = onCopyMarkdownFile,
                onPasteMarkdownFile = onPasteMarkdownFile,
                onRefresh = onRefreshTree,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(leftWidthDp),
            )
            PaneSplitter(
                width = splitterWidthDp,
                onDragDeltaX = { deltaX ->
                    val deltaRatio = deltaX / contentWidthPx
                    val maxLeft = 1f - rightPaneRatio - minCenter
                    leftPaneRatio = (leftPaneRatio + deltaRatio).coerceIn(minLeft, maxLeft)
                },
            )
            }
            MarkdownEditorPanel(
                modifier = Modifier
                    .width(centerWidthDp)
                    .fillMaxHeight(),
                repositoryName = repositoryName,
                activeDocumentPath = activeDocumentPath,
                content = editorContent,
                isDirty = documentIsDirty,
                isLoading = documentLoading,
                errorMessage = documentError,
                pendingDocumentPath = pendingDocumentPath,
                commitMessageDraft = commitMessageDraft,
                saveInProgress = saveInProgress,
                saveError = pasteError ?: saveError,
                saveSuccess = saveSuccess,
                onContentChanged = onEditorContentChanged,
                onCommitMessageChanged = onCommitMessageChanged,
                onSaveDocument = onSaveDocument,
                onRetryOpenDocument = onRetryDocumentOpen,
                onDiscardUnsavedAndOpenPending = onDiscardUnsavedAndOpenPending,
                onKeepEditingCurrent = onKeepEditingCurrent,
                sourceNavigation = sourceNavigation,
            )
            if (rightPaneVisible) {
                PaneSplitter(
                    width = splitterWidthDp,
                    onDragDeltaX = { deltaX ->
                        val deltaRatio = deltaX / contentWidthPx
                        val maxRight = if (sidebarVisible) 1f - leftPaneRatio - minCenter else 1f - minCenter
                        rightPaneRatio = (rightPaneRatio - deltaRatio).coerceIn(minRight, maxRight)
                    },
                )
                RightContextPane(
                    mode = rightPaneMode,
                    onModeChanged = { rightPaneMode = it },
                    activeDocumentPath = activeDocumentPath,
                    markdown = editorContent,
                    historyEntries = documentHistoryEntries,
                    historyLoading = documentHistoryLoading,
                    historyError = documentHistoryError,
                    onRetryHistory = onRetryDocumentHistory,
                    onPreviewSourceSelected = onPreviewSourceSelected,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(rightWidthDp),
                )
            }
        }
        }
    }
}

@Composable
private fun PaneSplitter(
    width: androidx.compose.ui.unit.Dp,
    onDragDeltaX: (Float) -> Unit,
) {
    val railColor = Color(0xFF858D97)
    val railBackground = Color(0xFF22282B)
    val gripColor = Color(0xFFC2C8D0)
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width)
            .background(railBackground)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDragDeltaX(dragAmount.x)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxHeight(0.84f)
                .width(2.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.05f to railColor.copy(alpha = 0.22f),
                            0.11f to railColor,
                            0.89f to railColor,
                            0.95f to railColor.copy(alpha = 0.22f),
                            1.00f to Color.Transparent,
                        ),
                    ),
                    shape = RoundedCornerShape(999.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(2.dp)
                            .background(gripColor, shape = RoundedCornerShape(999.dp)),
                        )
                }
            }
        }
    }
}

private enum class MobileWorkspaceTab {
    Explorer,
    Editor,
    Preview,
    History,
    Account,
}

private enum class RightPaneMode {
    Preview,
    History,
}

private fun viewerIdentityAvatarUrl(
    viewerAvatarUrl: String?,
    viewerUserId: String?,
    viewerUsername: String?,
    repositoryOwnerAvatarUrl: String?,
): String? {
    return when {
        !viewerAvatarUrl.isNullOrBlank() -> viewerAvatarUrl
        !viewerUserId.isNullOrBlank() -> "https://avatars.githubusercontent.com/u/$viewerUserId?s=96&v=4"
        !viewerUsername.isNullOrBlank() -> "https://avatars.githubusercontent.com/$viewerUsername?size=96"
        else -> repositoryOwnerAvatarUrl
    }
}

@Composable
private fun MobileWorkspaceLayout(
    repositoryName: String,
    repositoryOwnerLogin: String,
    repositoryOwnerAvatarUrl: String?,
    viewerUsername: String?,
    viewerUserId: String?,
    viewerAvatarUrl: String?,
    showNonMarkdownFiles: Boolean,
    treeRoots: List<RepoTreeNode>,
    expandedFolderPaths: Set<String>,
    selectedExplorerPaths: Set<String>,
    selectedMarkdownPath: String?,
    treeLoading: Boolean,
    treeError: String?,
    onToggleFolder: (String) -> Unit,
    onSelectExplorerPath: (String, Boolean) -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onSelectMarkdownFile: (String) -> Unit,
    onRefreshTree: () -> Unit,
    activeDocumentPath: String?,
    documentHistoryEntries: List<DocumentHistoryEntry>,
    documentHistoryLoading: Boolean,
    documentHistoryError: String?,
    editorContent: String,
    documentIsDirty: Boolean,
    documentLoading: Boolean,
    documentError: String?,
    pendingDocumentPath: String?,
    commitMessageDraft: String,
    saveInProgress: Boolean,
    saveError: String?,
    saveSuccess: String?,
    createInProgress: Boolean,
    createFolderInProgress: Boolean,
    renameInProgress: Boolean,
    moveInProgress: Boolean,
    copiedMarkdownPath: String?,
    pasteInProgress: Boolean,
    pasteError: String?,
    shareInProgress: Boolean,
    onEditorContentChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onSaveDocument: () -> Unit,
    onStartCreateDocument: () -> Unit,
    onStartCreateFolder: (String?) -> Unit,
    onStartRenameDocument: () -> Unit,
    onStartMoveDocument: () -> Unit,
    onMoveMarkdownFilesToFolder: (List<String>, String) -> Unit,
    onDeleteExplorerPaths: (List<String>) -> Unit,
    onCopyMarkdownFile: (String?) -> Unit,
    onPasteMarkdownFile: (String?) -> Unit,
    onStartShareDocument: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onRetryDocumentOpen: () -> Unit,
    onRetryDocumentHistory: () -> Unit,
    onDiscardUnsavedAndOpenPending: () -> Unit,
    onKeepEditingCurrent: () -> Unit,
    onBackToRepositories: () -> Unit,
    onSignOut: () -> Unit,
    sourceNavigation: EditorSourceNavigation?,
    onPreviewSourceSelected: (Int) -> Unit,
) {
    var activeTab by rememberSaveable { mutableStateOf(MobileWorkspaceTab.Explorer) }
    var editorHasFocus by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val displayRepositoryName = repositoryName.substringAfterLast('/').ifBlank { repositoryName }
    val canSave = activeDocumentPath != null && documentIsDirty && !documentLoading && !saveInProgress
    val identityLogin = viewerUsername ?: repositoryOwnerLogin
    val identityAvatarUrl = viewerIdentityAvatarUrl(
        viewerAvatarUrl = viewerAvatarUrl,
        viewerUserId = viewerUserId,
        viewerUsername = viewerUsername,
        repositoryOwnerAvatarUrl = repositoryOwnerAvatarUrl,
    )

    DisposableEffect(activeTab) {
        val keepAwake = activeTab == MobileWorkspaceTab.Preview
        setPreviewScreenAwake(keepAwake)
        onDispose {
            if (keepAwake) {
                setPreviewScreenAwake(false)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(top = mobileExtraTopSafeAreaPadding()),
    ) {
        MobileBrandHeader(
            activeTab = activeTab,
            activeDocumentPath = activeDocumentPath,
            showCommitAction = activeTab == MobileWorkspaceTab.Editor,
            showDoneAction = activeTab == MobileWorkspaceTab.Editor && editorHasFocus,
            canSave = canSave,
            onSaveDocument = onSaveDocument,
            onDoneEditing = { focusManager.clearFocus(force = true) },
            showNonMarkdownFiles = showNonMarkdownFiles,
            canShareDocument = activeDocumentPath != null && !documentLoading && !shareInProgress,
            onStartShareDocument = onStartShareDocument,
            onStartSharedLinks = onStartSharedLinks,
            onToggleShowNonMarkdownFiles = onToggleShowNonMarkdownFiles,
            onBackToRepositories = onBackToRepositories,
            onSignOut = onSignOut,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppThemeTokens.colors.borderSubtle),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (activeTab) {
                MobileWorkspaceTab.Explorer -> {
                    RepoTreePanel(
                        repositoryName = repositoryName,
                        repositoryOwnerLogin = identityLogin,
                        repositoryOwnerAvatarUrl = identityAvatarUrl,
                        activeDocumentPath = activeDocumentPath,
                        showNonMarkdownFiles = showNonMarkdownFiles,
                        treeRoots = treeRoots,
                        expandedFolderPaths = expandedFolderPaths,
                        selectedExplorerPaths = selectedExplorerPaths,
                        selectedMarkdownPath = selectedMarkdownPath,
                        isLoading = treeLoading,
                        errorMessage = treeError,
                        onToggleFolder = onToggleFolder,
                        onSelectExplorerPath = onSelectExplorerPath,
                        onSelectMarkdownFile = { path ->
                            onSelectMarkdownFile(path)
                            activeTab = MobileWorkspaceTab.Preview
                        },
                        onStartCreateDocument = onStartCreateDocument,
                        onStartCreateFolder = onStartCreateFolder,
                        createInProgress = createInProgress,
                        createFolderInProgress = createFolderInProgress,
                        onStartRenameDocument = onStartRenameDocument,
                        renameInProgress = renameInProgress,
                        onStartMoveDocument = onStartMoveDocument,
                        moveInProgress = moveInProgress,
                        copiedMarkdownPath = copiedMarkdownPath,
                        pasteInProgress = pasteInProgress,
                        onRequestMoveDocumentToFolder = { _, _ -> },
                        onRequestMoveDocumentsToFolder = onMoveMarkdownFilesToFolder,
                        onRequestDeleteExplorerPaths = onDeleteExplorerPaths,
                        onCopyMarkdownFile = onCopyMarkdownFile,
                        onPasteMarkdownFile = onPasteMarkdownFile,
                        onRefresh = onRefreshTree,
                        showUserIdentity = false,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                MobileWorkspaceTab.Editor -> {
                    MarkdownEditorPanel(
                        repositoryName = repositoryName,
                        activeDocumentPath = activeDocumentPath,
                        content = editorContent,
                        isDirty = documentIsDirty,
                        isLoading = documentLoading,
                        errorMessage = documentError,
                        pendingDocumentPath = pendingDocumentPath,
                        commitMessageDraft = commitMessageDraft,
                        saveInProgress = saveInProgress,
                        saveError = pasteError ?: saveError,
                        saveSuccess = saveSuccess,
                        onContentChanged = onEditorContentChanged,
                        onCommitMessageChanged = onCommitMessageChanged,
                        onSaveDocument = onSaveDocument,
                        onRetryOpenDocument = onRetryDocumentOpen,
                        onDiscardUnsavedAndOpenPending = onDiscardUnsavedAndOpenPending,
                        onKeepEditingCurrent = onKeepEditingCurrent,
                        showChrome = false,
                        onEditorFocusChanged = { editorHasFocus = it },
                        sourceNavigation = sourceNavigation,
                        contentHorizontalPadding = 28.dp,
                        contentVerticalPadding = 30.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                MobileWorkspaceTab.Preview -> {
                    MarkdownPreviewPanel(
                        markdown = editorContent,
                        showTitle = false,
                        onNavigateToSource = { offset ->
                            onPreviewSourceSelected(offset)
                            activeTab = MobileWorkspaceTab.Editor
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                    )
                }

                MobileWorkspaceTab.History -> {
                    MarkdownHistoryPanel(
                        activeDocumentPath = activeDocumentPath,
                        historyEntries = documentHistoryEntries,
                        isLoading = documentHistoryLoading,
                        errorMessage = documentHistoryError,
                        onRetry = onRetryDocumentHistory,
                        showTitle = false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    )
                }

                MobileWorkspaceTab.Account -> {
                    MobileAccountScreen(
                        repositoryName = repositoryName,
                        displayRepositoryName = displayRepositoryName,
                        identityLogin = identityLogin,
                        identityAvatarUrl = identityAvatarUrl,
                        showNonMarkdownFiles = showNonMarkdownFiles,
                        canShareDocument = activeDocumentPath != null && !documentLoading && !shareInProgress,
                        onStartShareDocument = onStartShareDocument,
                        onStartSharedLinks = onStartSharedLinks,
                        onToggleShowNonMarkdownFiles = onToggleShowNonMarkdownFiles,
                        onBackToRepositories = onBackToRepositories,
                        onSignOut = onSignOut,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        MobileBottomNavigation(
            activeTab = activeTab,
            onTabSelected = { activeTab = it },
            identityLogin = identityLogin,
            identityAvatarUrl = identityAvatarUrl,
        )
    }
}

@Composable
private fun MobileBrandHeader(
    activeTab: MobileWorkspaceTab,
    activeDocumentPath: String?,
    showCommitAction: Boolean,
    showDoneAction: Boolean,
    canSave: Boolean,
    onSaveDocument: () -> Unit,
    onDoneEditing: () -> Unit,
    showNonMarkdownFiles: Boolean,
    canShareDocument: Boolean,
    onStartShareDocument: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onBackToRepositories: () -> Unit,
    onSignOut: () -> Unit,
) {
    val fileName = activeDocumentPath?.substringAfterLast('/')
    val folder = activeDocumentPath?.substringBeforeLast('/', missingDelimiterValue = "")?.ifBlank { null }
    val title = when (activeTab) {
        MobileWorkspaceTab.Explorer -> "rRepoDocs"
        MobileWorkspaceTab.Editor,
        MobileWorkspaceTab.Preview,
        MobileWorkspaceTab.History -> fileName ?: "No file selected"
        MobileWorkspaceTab.Account -> "Account"
    }
    val subtitle = when (activeTab) {
        MobileWorkspaceTab.Explorer -> null
        MobileWorkspaceTab.Editor,
        MobileWorkspaceTab.Preview,
        MobileWorkspaceTab.History -> folder
        MobileWorkspaceTab.Account -> "Repository and session"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (activeTab == MobileWorkspaceTab.Explorer) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(Res.drawable.app_icon),
                            contentDescription = "rRepoDocs logo",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (showDoneAction) {
                TextButton(onClick = onDoneEditing) {
                    Text("Done")
                }
            }

            if (showCommitAction) {
                IconButton(
                    enabled = canSave,
                    onClick = onSaveDocument,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Commit,
                        contentDescription = "Commit changes",
                        tint = if (canSave) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            MobileHeaderActionMenu(
                showNonMarkdownFiles = showNonMarkdownFiles,
                canShareDocument = canShareDocument,
                onStartShareDocument = onStartShareDocument,
                onStartSharedLinks = onStartSharedLinks,
                onToggleShowNonMarkdownFiles = onToggleShowNonMarkdownFiles,
                onBackToRepositories = onBackToRepositories,
                onSignOut = onSignOut,
            )
        }
    }
}

@Composable
private fun MobileHeaderActionMenu(
    showNonMarkdownFiles: Boolean,
    canShareDocument: Boolean,
    onStartShareDocument: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onBackToRepositories: () -> Unit,
    onSignOut: () -> Unit,
) {
    var actionMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { actionMenuExpanded = true },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "More actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = actionMenuExpanded,
            onDismissRequest = { actionMenuExpanded = false },
            shape = RoundedCornerShape(8.dp),
        ) {
            DropdownMenuItem(
                text = { Text("Share public preview") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                },
                enabled = canShareDocument,
                onClick = {
                    actionMenuExpanded = false
                    onStartShareDocument()
                },
            )
            DropdownMenuItem(
                text = { Text("Shared public links") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Visibility, contentDescription = null)
                },
                onClick = {
                    actionMenuExpanded = false
                    onStartSharedLinks()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        if (showNonMarkdownFiles) {
                            "Hide non-markdown files"
                        } else {
                            "Show non-markdown files"
                        },
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
                },
                onClick = {
                    actionMenuExpanded = false
                    onToggleShowNonMarkdownFiles()
                },
            )
            DropdownMenuItem(
                text = { Text("Change repository") },
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.SwapHoriz, contentDescription = null)
                },
                onClick = {
                    actionMenuExpanded = false
                    onBackToRepositories()
                },
            )
            DropdownMenuItem(
                text = { Text("Sign out") },
                leadingIcon = {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                },
                onClick = {
                    actionMenuExpanded = false
                    onSignOut()
                },
            )
        }
    }
}

@Composable
private fun MobileAccountScreen(
    repositoryName: String,
    displayRepositoryName: String,
    identityLogin: String,
    identityAvatarUrl: String?,
    showNonMarkdownFiles: Boolean,
    canShareDocument: Boolean,
    onStartShareDocument: () -> Unit,
    onStartSharedLinks: () -> Unit,
    onToggleShowNonMarkdownFiles: () -> Unit,
    onBackToRepositories: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                GitHubAvatar(
                    ownerLogin = identityLogin,
                    avatarUrl = identityAvatarUrl,
                    size = 48.dp,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = identityLogin,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = repositoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppThemeTokens.colors.borderSubtle),
            )

            Text(
                text = displayRepositoryName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            MobileAccountAction(
                icon = Icons.Outlined.SwapHoriz,
                label = "Change repository",
                onClick = onBackToRepositories,
            )
            MobileAccountAction(
                icon = Icons.Outlined.Settings,
                label = if (showNonMarkdownFiles) "Hide non-markdown files" else "Show non-markdown files",
                onClick = onToggleShowNonMarkdownFiles,
            )
            MobileAccountAction(
                icon = Icons.Outlined.Share,
                label = "Share public preview",
                enabled = canShareDocument,
                onClick = onStartShareDocument,
            )
            MobileAccountAction(
                icon = Icons.Outlined.Visibility,
                label = "Shared public links",
                onClick = onStartSharedLinks,
            )

            Spacer(modifier = Modifier.weight(1f))

            MobileAccountAction(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = "Sign out",
                destructive = true,
                onClick = onSignOut,
            )
        }
    }
}

@Composable
private fun MobileAccountAction(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun MobileBottomNavigation(
    activeTab: MobileWorkspaceTab,
    onTabSelected: (MobileWorkspaceTab) -> Unit,
    identityLogin: String,
    identityAvatarUrl: String?,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MobileBottomNavItem(
                label = "Explorer",
                icon = Icons.Outlined.FolderOpen,
                selected = activeTab == MobileWorkspaceTab.Explorer,
                onClick = { onTabSelected(MobileWorkspaceTab.Explorer) },
            )
            MobileBottomNavItem(
                label = "Editor",
                icon = Icons.Outlined.Description,
                selected = activeTab == MobileWorkspaceTab.Editor,
                onClick = { onTabSelected(MobileWorkspaceTab.Editor) },
            )
            MobileBottomNavItem(
                label = "Preview",
                icon = Icons.Outlined.Visibility,
                selected = activeTab == MobileWorkspaceTab.Preview,
                onClick = { onTabSelected(MobileWorkspaceTab.Preview) },
            )
            MobileBottomNavItem(
                label = "History",
                icon = Icons.Outlined.History,
                selected = activeTab == MobileWorkspaceTab.History,
                onClick = { onTabSelected(MobileWorkspaceTab.History) },
            )
            MobileBottomNavItem(
                label = "Account",
                icon = null,
                selected = activeTab == MobileWorkspaceTab.Account,
                onClick = { onTabSelected(MobileWorkspaceTab.Account) },
                avatarLogin = identityLogin,
                avatarUrl = identityAvatarUrl,
            )
        }
    }
}

@Composable
private fun MobileBottomNavItem(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
    avatarLogin: String? = null,
    avatarUrl: String? = null,
) {
    val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    Column(
        modifier = Modifier
            .width(68.dp)
            .height(66.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .height(2.dp)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
        Box(
            modifier = Modifier
                .padding(top = 12.dp)
                .size(23.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(23.dp),
                )
            } else {
                GitHubAvatar(
                    ownerLogin = avatarLogin ?: label,
                    avatarUrl = avatarUrl,
                    size = 23.dp,
                )
            }
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun RightContextPane(
    mode: RightPaneMode,
    onModeChanged: (RightPaneMode) -> Unit,
    activeDocumentPath: String?,
    markdown: String,
    historyEntries: List<DocumentHistoryEntry>,
    historyLoading: Boolean,
    historyError: String?,
    onRetryHistory: () -> Unit,
    onPreviewSourceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val paneShape = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 0.dp,
        bottomEnd = 0.dp,
        bottomStart = 14.dp,
    )
    Surface(
        modifier = modifier.clip(paneShape),
        color = AppThemeTokens.colors.contextPaneSurface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shape = paneShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            RightPaneTabHeader(
                mode = mode,
                onModeChanged = onModeChanged,
            )

            when (mode) {
                RightPaneMode.Preview -> {
                    if (activeDocumentPath == null) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(horizontal = 22.dp, vertical = 18.dp)
                                .fillMaxWidth(),
                        )
                        {
                            Text(
                                text = "No file selected.\nOpen a Markdown file to preview it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    } else {
                        MarkdownPreviewPanel(
                            markdown = markdown,
                            showTitle = false,
                            onNavigateToSource = onPreviewSourceSelected,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 22.dp, vertical = 18.dp),
                        )
                    }
                }

                RightPaneMode.History -> {
                    MarkdownHistoryPanel(
                        activeDocumentPath = activeDocumentPath,
                        historyEntries = historyEntries,
                        isLoading = historyLoading,
                        errorMessage = historyError,
                        onRetry = onRetryHistory,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 22.dp, vertical = 18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RightPaneTabHeader(
    mode: RightPaneMode,
    onModeChanged: (RightPaneMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppThemeTokens.colors.contextPaneSurface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RightPaneTabButton(
            label = "Preview",
            active = mode == RightPaneMode.Preview,
            onClick = { onModeChanged(RightPaneMode.Preview) },
        )
        RightPaneTabButton(
            label = "History",
            active = mode == RightPaneMode.History,
            onClick = { onModeChanged(RightPaneMode.History) },
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun RightPaneTabButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    var hovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val labelColor = when {
        active -> MaterialTheme.colorScheme.primary
        hovered -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .height(34.dp)
            .onDesktopPointerHover(
                onEnter = { hovered = true },
                onExit = { hovered = false },
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = if (active) {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = if (hovered) 0.82f else 0.42f)
        },
        shape = RoundedCornerShape(7.dp),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = labelColor,
            )
        }
    }
}

private fun repositoryFolderPaths(roots: List<RepoTreeNode>): List<String> {
    val folderPaths = mutableListOf<String>()

    fun walk(nodes: List<RepoTreeNode>) {
        nodes.forEach { node ->
            if (node.kind == RepoTreeNodeKind.Folder) {
                folderPaths += node.path
                walk(node.children)
            }
        }
    }

    walk(roots)
    return folderPaths.sortedBy { it.lowercase() }
}

@Composable
private fun CreateDocumentDialog(
    targetFolderDraft: String,
    targetFolderOptions: List<String>,
    fileNameDraft: String,
    commitMessageDraft: String,
    isCreating: Boolean,
    errorMessage: String?,
    onTargetFolderChanged: (String) -> Unit,
    onFileNameChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val folderOptions = remember(targetFolderOptions, targetFolderDraft) {
        (listOf("") + targetFolderOptions + listOf(targetFolderDraft.takeIf { it.isNotBlank() }.orEmpty()))
            .distinct()
            .sortedWith(compareBy<String> { it.isNotBlank() }.thenBy { it.lowercase() })
    }
    var targetFolderMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val selectedTargetFolderLabel = targetFolderDraft.ifBlank { "Repository root" }

    AlertDialog(
        onDismissRequest = {
            if (!isCreating) {
                onDismiss()
            }
        },
        title = { Text("Create Markdown File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                FolderPathDropdownField(
                    label = "Target folder",
                    selectedFolderLabel = selectedTargetFolderLabel,
                    folderOptions = folderOptions,
                    expanded = targetFolderMenuExpanded,
                    enabled = !isCreating,
                    onExpandedChange = { targetFolderMenuExpanded = it },
                    onFolderSelected = onTargetFolderChanged,
                )
                OutlinedTextField(
                    value = fileNameDraft,
                    onValueChange = onFileNameChanged,
                    enabled = !isCreating,
                    singleLine = true,
                    label = { Text("File name") },
                    placeholder = { Text("new-file.md") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = commitMessageDraft,
                    onValueChange = onCommitMessageChanged,
                    enabled = !isCreating,
                    singleLine = true,
                    label = { Text("Commit message") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "A .md extension is added automatically when needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCreating,
            ) {
                Text(if (isCreating) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun FolderPathDropdownField(
    label: String,
    selectedFolderLabel: String,
    folderOptions: List<String>,
    expanded: Boolean,
    enabled: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFolderSelected: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedFolderLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    onExpandedChange(true)
                },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            shape = RoundedCornerShape(8.dp),
        ) {
            folderOptions.forEach { folderPath ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = folderPath.ifBlank { "Repository root" },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onExpandedChange(false)
                        onFolderSelected(folderPath)
                    },
                )
            }
        }
    }
}

@Composable
private fun CreateFolderDialog(
    parentFolderDraft: String,
    parentFolderOptions: List<String>,
    folderNameDraft: String,
    commitMessageDraft: String,
    isCreating: Boolean,
    errorMessage: String?,
    onParentFolderChanged: (String) -> Unit,
    onFolderNameChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val folderOptions = remember(parentFolderOptions, parentFolderDraft) {
        (listOf("") + parentFolderOptions + listOf(parentFolderDraft.takeIf { it.isNotBlank() }.orEmpty()))
            .distinct()
            .sortedWith(compareBy<String> { it.isNotBlank() }.thenBy { it.lowercase() })
    }
    var parentFolderMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val selectedParentFolderLabel = parentFolderDraft.ifBlank { "Repository root" }

    AlertDialog(
        onDismissRequest = {
            if (!isCreating) {
                onDismiss()
            }
        },
        title = { Text("Create Folder") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                FolderPathDropdownField(
                    label = "Parent folder",
                    selectedFolderLabel = selectedParentFolderLabel,
                    folderOptions = folderOptions,
                    expanded = parentFolderMenuExpanded,
                    enabled = !isCreating,
                    onExpandedChange = { parentFolderMenuExpanded = it },
                    onFolderSelected = onParentFolderChanged,
                )
                OutlinedTextField(
                    value = folderNameDraft,
                    onValueChange = onFolderNameChanged,
                    enabled = !isCreating,
                    singleLine = true,
                    label = { Text("Folder name") },
                    placeholder = { Text("notes") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = commitMessageDraft,
                    onValueChange = onCommitMessageChanged,
                    enabled = !isCreating,
                    singleLine = true,
                    label = { Text("Commit message") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isCreating,
            ) {
                Text(if (isCreating) "Creating..." else "Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun RenameDocumentDialog(
    fileNameDraft: String,
    commitMessageDraft: String,
    isRenaming: Boolean,
    errorMessage: String?,
    onFileNameChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isRenaming) {
                onDismiss()
            }
        },
        title = { Text("Rename Markdown File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                OutlinedTextField(
                    value = fileNameDraft,
                    onValueChange = onFileNameChanged,
                    enabled = !isRenaming,
                    singleLine = true,
                    label = { Text("New file name") },
                    placeholder = { Text("renamed-file.md") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = commitMessageDraft,
                    onValueChange = onCommitMessageChanged,
                    enabled = !isRenaming,
                    singleLine = true,
                    label = { Text("Commit message") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "A .md extension is added automatically when needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isRenaming,
            ) {
                Text(if (isRenaming) "Renaming..." else "Rename")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRenaming,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun MoveDocumentDialog(
    destinationFolderDraft: String,
    commitMessageDraft: String,
    isMoving: Boolean,
    errorMessage: String?,
    onDestinationFolderChanged: (String) -> Unit,
    onCommitMessageChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!isMoving) {
                onDismiss()
            }
        },
        title = { Text("Move Markdown File") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                OutlinedTextField(
                    value = destinationFolderDraft,
                    onValueChange = onDestinationFolderChanged,
                    enabled = !isMoving,
                    singleLine = true,
                    label = { Text("Destination folder") },
                    placeholder = { Text("docs/guides") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = commitMessageDraft,
                    onValueChange = onCommitMessageChanged,
                    enabled = !isMoving,
                    singleLine = true,
                    label = { Text("Commit message") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isMoving,
            ) {
                Text(if (isMoving) "Moving..." else "Move")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isMoving,
            ) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ShareDocumentDialog(
    activeDocumentPath: String?,
    expiryOption: ShareExpiryOption,
    activeShare: DocumentShare?,
    isCreating: Boolean,
    createError: String?,
    isRevoking: Boolean,
    revokeError: String?,
    onExpiryChanged: (ShareExpiryOption) -> Unit,
    onConfirm: () -> Unit,
    onRevoke: () -> Unit,
    onDismiss: () -> Unit,
) {
    val shareUrl = activeShare?.shareUrl
    val isRevoked = activeShare?.revokedAt != null
    var localMessage by remember(activeShare?.shareUrl, activeShare?.revokedAt) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isCreating && !isRevoking) {
                onDismiss()
            }
        },
        title = { Text("Share Public Preview") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                Text(
                    text = activeDocumentPath ?: "No document selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "This creates a public, read-only snapshot. Anyone with the link can view this version of the Markdown until it expires or is revoked.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (shareUrl == null) {
                    Text(
                        text = "Expires",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    ShareExpiryOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isCreating) { onExpiryChanged(option) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            RadioButton(
                                selected = expiryOption == option,
                                onClick = { onExpiryChanged(option) },
                                enabled = !isCreating,
                            )
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = shareUrl,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text(if (isRevoked) "Revoked link" else "Public link") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !isRevoked,
                            onClick = {
                                localMessage = if (copyTextToClipboard(shareUrl)) {
                                    "Link copied."
                                } else {
                                    "Could not copy link."
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("Copy")
                        }
                        TextButton(
                            enabled = !isRevoked,
                            onClick = {
                                localMessage = if (openExternalUrl(shareUrl)) {
                                    null
                                } else {
                                    "Could not open link."
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("Open")
                        }
                    }
                }

                val message = localMessage ?: createError ?: revokeError
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.contains("could not", ignoreCase = true) || createError != null || revokeError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
                if (isRevoked) {
                    Text(
                        text = "This public link has been revoked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            when {
                shareUrl == null -> {
                    Button(
                        onClick = onConfirm,
                        enabled = !isCreating,
                    ) {
                        Text(if (isCreating) "Creating..." else "Create Link")
                    }
                }
                !isRevoked -> {
                    TextButton(
                        onClick = onRevoke,
                        enabled = !isRevoking,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LinkOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(if (isRevoking) "Revoking..." else "Revoke")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating && !isRevoking,
            ) {
                Text(if (shareUrl == null) "Cancel" else "Done")
            }
        },
    )
}

@Composable
private fun SharedLinksDialog(
    shares: List<DocumentShare>,
    isLoading: Boolean,
    errorMessage: String?,
    revokingShareId: String?,
    onRetry: () -> Unit,
    onRevoke: (DocumentShare) -> Unit,
    onDismiss: () -> Unit,
) {
    var localMessage by remember(shares, errorMessage) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Shared Public Links") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            ) {
                when {
                    isLoading && shares.isEmpty() -> {
                        Text(
                            text = "Loading shared links...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    errorMessage != null && shares.isEmpty() -> {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = onRetry) {
                            Text("Try again")
                        }
                    }
                    shares.isEmpty() -> {
                        Text(
                            text = "No active public links.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(shares, key = { it.id }) { share ->
                                SharedLinkRow(
                                    share = share,
                                    isRevoking = revokingShareId == share.id,
                                    onCopy = {
                                        localMessage = if (copyTextToClipboard(share.shareUrl)) {
                                            "Link copied."
                                        } else {
                                            "Could not copy link."
                                        }
                                    },
                                    onOpen = {
                                        localMessage = if (openExternalUrl(share.shareUrl)) {
                                            null
                                        } else {
                                            "Could not open link."
                                        }
                                    },
                                    onRevoke = { onRevoke(share) },
                                )
                            }
                        }
                    }
                }

                val message = localMessage ?: errorMessage?.takeIf { shares.isNotEmpty() }
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.contains("could not", ignoreCase = true) || errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onRetry,
                enabled = !isLoading,
            ) {
                Text("Refresh")
            }
        },
    )
}

@Composable
private fun SharedLinkRow(
    share: DocumentShare,
    isRevoking: Boolean,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
    onRevoke: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = share.title ?: share.documentPath?.substringAfterLast('/') ?: "Shared document",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = listOfNotNull(share.repoFullName, share.documentPath).joinToString(" / ").ifBlank { share.shareUrl },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (share.expiresAt != null) {
                Text(
                    text = "Expires ${share.expiresAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text("Copy")
                }
                TextButton(onClick = onOpen) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text("Open")
                }
                TextButton(
                    onClick = onRevoke,
                    enabled = !isRevoking,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(if (isRevoking) "Revoking..." else "Revoke")
                }
            }
        }
    }
}

private fun pendingRevokingShareId(
    pendingShareRevokeId: String?,
    isRevoking: Boolean,
): String? {
    return if (isRevoking) pendingShareRevokeId else null
}

@Composable
private fun WorkspaceHeader(
    repositoryName: String,
    repositoryOwnerLogin: String,
    sidebarVisible: Boolean,
    rightPaneVisible: Boolean,
    onToggleSidebar: () -> Unit,
    onToggleRightPane: () -> Unit,
) {
    val repositoryLabel = "$repositoryOwnerLogin → ${repositoryName.substringAfterLast('/')}"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 18.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = if (sidebarVisible) "Hide sidebar" else "Show sidebar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(Res.drawable.rdev_logo_dark),
                    contentDescription = "rDEV logo",
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    text = repositoryLabel,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(
                onClick = onToggleRightPane,
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = if (rightPaneVisible) "Hide preview" else "Show preview",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
