package com.rdev.rrepodocs.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TextFormat
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.unit.DpOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rdev.rrepodocs.domain.model.RepoTreeNode
import com.rdev.rrepodocs.domain.model.RepoTreeNodeKind
import com.rdev.rrepodocs.presentation.app.AppThemeTokens
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RepoTreePanel(
    repositoryName: String,
    repositoryOwnerLogin: String,
    repositoryOwnerAvatarUrl: String?,
    activeDocumentPath: String?,
    showNonMarkdownFiles: Boolean,
    treeRoots: List<RepoTreeNode>,
    expandedFolderPaths: Set<String>,
    selectedExplorerPaths: Set<String>,
    selectedMarkdownPath: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleFolder: (String) -> Unit,
    onSelectExplorerPath: (String, Boolean) -> Unit,
    onSelectMarkdownFile: (String) -> Unit,
    onStartCreateDocument: () -> Unit,
    onStartCreateFolder: (String?) -> Unit,
    createInProgress: Boolean,
    createFolderInProgress: Boolean,
    onStartRenameDocument: () -> Unit,
    renameInProgress: Boolean,
    onStartMoveDocument: () -> Unit,
    moveInProgress: Boolean,
    copiedMarkdownPath: String?,
    pasteInProgress: Boolean,
    onRequestMoveDocumentToFolder: (String, String) -> Unit,
    onRequestMoveDocumentsToFolder: (List<String>, String) -> Unit,
    onRequestDeleteExplorerPaths: (List<String>) -> Unit,
    onCopyMarkdownFile: (String?) -> Unit,
    onPasteMarkdownFile: (String?) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    showUserIdentity: Boolean = true,
) {
    var contextMenuPath by remember { mutableStateOf<String?>(null) }
    var contextMenuPositionInRoot by remember { mutableStateOf<Offset?>(null) }
    var draggingFilePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var dragPointerInRoot by remember { mutableStateOf<Offset?>(null) }
    var hoveredPath by remember { mutableStateOf<String?>(null) }
    val rowBoundsInRoot = remember { mutableStateMapOf<String, Rect>() }
    val visibleTreeRoots = remember(treeRoots, showNonMarkdownFiles) {
        if (showNonMarkdownFiles) {
            treeRoots
        } else {
            filterTreeToMarkdownOnly(treeRoots)
        }
    }
    val displayRepoName = remember(repositoryName) {
        repositoryName.substringAfterLast('/').uppercase()
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(0.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val contextMenuOffset = contextMenuPositionInRoot?.let { position ->
                with(density) { DpOffset(position.x.toDp(), position.y.toDp()) }
            } ?: DpOffset.Zero
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppThemeTokens.spacing.lg, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Button(
                    onClick = onStartCreateDocument,
                    enabled = !createInProgress && !createFolderInProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(
                        text = "+  New Document",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 5.dp),
                    )
                }

                TextButton(
                    onClick = { onStartCreateFolder(null) },
                    enabled = !createInProgress && !createFolderInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreateNewFolder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New Folder")
                }

                TextButton(
                    onClick = onRefresh,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoading) "Syncing" else "Sync")
                }

                Spacer(modifier = Modifier.height(4.dp))
                LeftRailSection(icon = Icons.Outlined.History, label = "RECENT")
                LeftRailSection(icon = Icons.Outlined.StarOutline, label = "STARRED")
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = displayRepoName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                when {
                    isLoading && visibleTreeRoots.isEmpty() -> {
                        Text(
                            text = "Loading files...",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    errorMessage != null -> {
                        Column(verticalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm)) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                            TextButton(onClick = onRefresh) { Text("Try again") }
                        }
                    }

                    else -> {
                        val rows = flattenTreeRows(visibleTreeRoots, expandedFolderPaths)
                        if (rows.isEmpty()) {
                            Text(
                                text = "No files found in this repository.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(rows, key = { it.node.path }) { row ->
                                    val node = row.node
                                    val isFolder = node.kind == RepoTreeNodeKind.Folder
                                    val isExpanded = node.path in expandedFolderPaths
                            val isSelectableMarkdown = !isFolder && node.isMarkdownFile
                                    val isContextActionable = true
                                    val isSelected = selectedMarkdownPath == node.path
                                    val isExplorerSelected = node.path in selectedExplorerPaths
                                    val isHovered = hoveredPath == node.path
                                    val canClick = isFolder || isSelectableMarkdown
                                    val selectedAccentColor = MaterialTheme.colorScheme.primary
                                    val isDragTarget = isFolder &&
                                        draggingFilePaths.isNotEmpty() &&
                                        dragPointerInRoot?.let { pointer ->
                                            rowBoundsInRoot[node.path]?.contains(pointer) == true
                                        } == true
                                    val rowBackgroundTarget = when {
                                        isSelected || isExplorerSelected -> AppThemeTokens.colors.accentBlueSubtle
                                        isDragTarget -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        isHovered -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.24f)
                                        else -> Color.Transparent
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = rowBackgroundTarget,
                                                shape = RoundedCornerShape(4.dp),
                                            )
                                            .drawBehind {
                                                if (isSelected) {
                                                    drawLine(
                                                        color = selectedAccentColor,
                                                        start = Offset(0f, 0f),
                                                        end = Offset(0f, size.height),
                                                        strokeWidth = 3.dp.toPx(),
                                                    )
                                                }
                                            }
                                            .onGloballyPositioned { coordinates ->
                                                rowBoundsInRoot[node.path] = coordinates.boundsInRoot()
                                            }
                                            .padding(
                                                start = (row.depth * 18).dp + 8.dp,
                                                top = 10.dp,
                                                end = 8.dp,
                                                bottom = 10.dp,
                                            )
                                            .then(
                                                if (canClick) {
                                                    Modifier.onExplorerPrimaryClick(enabled = true) { additiveSelection ->
                                                        onSelectExplorerPath(node.path, additiveSelection)
                                                        if (isFolder && !additiveSelection) {
                                                            onToggleFolder(node.path)
                                                        } else if (isSelectableMarkdown && !additiveSelection) {
                                                            onSelectMarkdownFile(node.path)
                                                        }
                                                    }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                            .onDesktopFileDrag(
                                                enabled = isSelectableMarkdown,
                                                onDrag = { pointerPosition, dragAmount ->
                                                    if (draggingFilePaths.isEmpty()) {
                                                        draggingFilePaths = selectedExplorerPaths
                                                            .filter { selectedPath ->
                                                                selectedPath == node.path ||
                                                                    rows.any { it.node.path == selectedPath && it.node.isMarkdownFile }
                                                            }
                                                            .ifEmpty { listOf(node.path) }
                                                        val bounds = rowBoundsInRoot[node.path]
                                                        dragPointerInRoot = if (bounds != null) {
                                                            Offset(bounds.left + pointerPosition.x, bounds.top + pointerPosition.y)
                                                        } else {
                                                            pointerPosition
                                                        }
                                                    } else {
                                                        dragPointerInRoot = dragPointerInRoot?.plus(dragAmount)
                                                    }
                                                },
                                                onDragEnd = {
                                                    val sourcePaths = draggingFilePaths
                                                    val pointer = dragPointerInRoot
                                                    val destinationFolder = rowBoundsInRoot.entries
                                                        .firstOrNull { entry ->
                                                            val path = entry.key
                                                            val targetNode = rows.firstOrNull { it.node.path == path }?.node
                                                            targetNode?.kind == RepoTreeNodeKind.Folder &&
                                                                pointer != null &&
                                                                entry.value.contains(pointer)
                                                        }
                                                        ?.key
                                                    if (
                                                        sourcePaths.isNotEmpty() &&
                                                        destinationFolder != null &&
                                                        sourcePaths.any { parentFolderOf(it) != destinationFolder }
                                                    ) {
                                                        if (sourcePaths.size == 1) {
                                                            onRequestMoveDocumentToFolder(sourcePaths.first(), destinationFolder)
                                                        } else {
                                                            onRequestMoveDocumentsToFolder(sourcePaths, destinationFolder)
                                                        }
                                                    }
                                                    draggingFilePaths = emptyList()
                                                    dragPointerInRoot = null
                                                },
                                                onDragCancel = {
                                                    draggingFilePaths = emptyList()
                                                    dragPointerInRoot = null
                                                },
                                            )
                                            .onDesktopContextPress(enabled = isContextActionable) { localPress ->
                                                    onSelectExplorerPath(node.path, node.path in selectedExplorerPaths)
                                                    if (isSelectableMarkdown) {
                                                        onSelectMarkdownFile(node.path)
                                                    }
                                                    contextMenuPath = node.path
                                                    val rowBounds = rowBoundsInRoot[node.path]
                                                    contextMenuPositionInRoot = if (rowBounds != null) {
                                                        Offset(rowBounds.left + localPress.x, rowBounds.top + localPress.y)
                                                    } else {
                                                        rowBounds?.topLeft ?: Offset.Zero
                                                    }
                                                }
                                            .onDesktopPointerHover(
                                                onEnter = { hoveredPath = node.path },
                                                onExit = {
                                                    if (hoveredPath == node.path) hoveredPath = null
                                                },
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(AppThemeTokens.spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (isFolder) {
                                            Icon(
                                                imageVector = if (isExpanded) {
                                                    Icons.Outlined.KeyboardArrowDown
                                                } else {
                                                    Icons.AutoMirrored.Outlined.KeyboardArrowRight
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.width(16.dp))
                                        }

                                        Icon(
                                            imageVector = when {
                                                isFolder && isExpanded -> Icons.Outlined.FolderOpen
                                                isFolder -> Icons.Outlined.Folder
                                                node.path.endsWith(".json", ignoreCase = true) -> Icons.Outlined.Settings
                                                node.path.endsWith(".md", ignoreCase = true) -> Icons.Outlined.Description
                                                else -> Icons.AutoMirrored.Outlined.Article
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp),
                                        )

                                        Text(
                                            text = node.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isFolder || isSelectableMarkdown -> MaterialTheme.colorScheme.onSurface
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )

                                        if (isDragTarget) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(999.dp),
                                            ) {
                                                Text(
                                                    text = "Släpp för att flytta hit",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (showUserIdentity) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(AppThemeTokens.colors.borderSubtle.copy(alpha = 0.72f)),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GitHubUserIdentity(
                                ownerLogin = repositoryOwnerLogin,
                                avatarUrl = repositoryOwnerAvatarUrl,
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = contextMenuPath != null && contextMenuPositionInRoot != null,
                onDismissRequest = {
                    contextMenuPath = null
                    contextMenuPositionInRoot = null
                },
                offset = contextMenuOffset,
                shape = RoundedCornerShape(10.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                val selectedPath = contextMenuPath
                val selectedNode = selectedPath?.let { path ->
                    flattenTreeRows(visibleTreeRoots, expandedFolderPaths)
                        .firstOrNull { it.node.path == path }
                        ?.node
                }
                val selectedIsFolder = selectedNode?.kind == RepoTreeNodeKind.Folder
                val selectedIsMarkdown = selectedNode?.isMarkdownFile == true && selectedNode.kind != RepoTreeNodeKind.Folder
                val pasteDestinationFolder = selectedPath?.let { path ->
                    if (selectedIsFolder) path else path.substringBeforeLast('/', "")
                } ?: ""
                DropdownMenuItem(
                    text = { Text("New Folder") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null) },
                    enabled = selectedIsFolder && !createFolderInProgress,
                    onClick = {
                        onStartCreateFolder(if (selectedIsFolder) selectedPath else pasteDestinationFolder)
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.DeleteOutline, contentDescription = null) },
                    enabled = selectedPath != null,
                    onClick = {
                        val paths = selectedExplorerPaths
                            .takeIf { selectedPath in it }
                            ?.toList()
                            ?: listOfNotNull(selectedPath)
                        onRequestDeleteExplorerPaths(paths)
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
                DropdownMenuItem(
                    text = { Text("Copy") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null) },
                    enabled = selectedIsMarkdown,
                    onClick = {
                        if (selectedIsMarkdown) {
                            onCopyMarkdownFile(selectedPath)
                        }
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
                DropdownMenuItem(
                    text = { Text("Paste") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.ContentPaste, contentDescription = null) },
                    enabled = !copiedMarkdownPath.isNullOrBlank() && !pasteInProgress,
                    onClick = {
                        onPasteMarkdownFile(pasteDestinationFolder)
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
                DropdownMenuItem(
                    text = { Text("Rename") },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.TextFormat, contentDescription = null) },
                    enabled = selectedIsMarkdown,
                    onClick = {
                        if (selectedPath != null && selectedIsMarkdown) {
                            onSelectMarkdownFile(selectedPath)
                            onStartRenameDocument()
                        }
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
                DropdownMenuItem(
                    text = { Text("Move") },
                    leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Outlined.DriveFileMove, contentDescription = null) },
                    enabled = selectedIsMarkdown,
                    onClick = {
                        if (selectedPath != null && selectedIsMarkdown) {
                            onSelectMarkdownFile(selectedPath)
                            onStartMoveDocument()
                        }
                        contextMenuPath = null
                        contextMenuPositionInRoot = null
                    },
                )
            }
        }
    }
}

@Composable
private fun LeftRailSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
        )
    }
}

@Composable
private fun GitHubUserIdentity(
    ownerLogin: String,
    avatarUrl: String?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        GitHubAvatar(
            ownerLogin = ownerLogin,
            avatarUrl = avatarUrl,
        )
        Text(
            text = ownerLogin,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun GitHubAvatar(
    ownerLogin: String,
    avatarUrl: String?,
    size: Dp = 30.dp,
) {
    val primaryUrl = avatarUrl?.takeUnless { it.isBlank() } ?: "https://avatars.githubusercontent.com/$ownerLogin?size=96"
    val fallbackUrl = "https://avatars.githubusercontent.com/$ownerLogin?size=96"
    var useFallbackUrl by remember(ownerLogin, avatarUrl) { mutableStateOf(false) }
    val resolvedUrl = if (useFallbackUrl) fallbackUrl else primaryUrl
    var avatarBitmap by remember(resolvedUrl) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(resolvedUrl) {
        avatarBitmap = loadGitHubAvatarBitmap(resolvedUrl).getOrNull()
        if (avatarBitmap == null && !useFallbackUrl && fallbackUrl != primaryUrl) {
            useFallbackUrl = true
        }
    }

    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = ownerLogin.take(1).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            avatarBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "GitHub avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(999.dp)),
                )
            }
        }
    }
}

private suspend fun loadGitHubAvatarBitmap(url: String): Result<ImageBitmap> {
    val client = HttpClient()
    return try {
        runCatching {
            client.get(url) {
                header(HttpHeaders.UserAgent, "rRepoDocs")
            }.bodyAsBytes().decodeToImageBitmap()
        }
    } finally {
        client.close()
    }
}

private data class TreeRow(
    val node: RepoTreeNode,
    val depth: Int,
)

private fun flattenTreeRows(
    roots: List<RepoTreeNode>,
    expandedFolderPaths: Set<String>,
): List<TreeRow> {
    val output = mutableListOf<TreeRow>()

    fun walk(nodes: List<RepoTreeNode>, depth: Int) {
        nodes.forEach { node ->
            output += TreeRow(node = node, depth = depth)
            if (node.kind == RepoTreeNodeKind.Folder && node.path in expandedFolderPaths) {
                walk(node.children, depth + 1)
            }
        }
    }

    walk(roots, depth = 0)
    return output
}

private fun parentFolderOf(path: String): String =
    path.substringBeforeLast('/', "")

private fun filterTreeToMarkdownOnly(nodes: List<RepoTreeNode>): List<RepoTreeNode> {
    return nodes.mapNotNull { node ->
        when (node.kind) {
            RepoTreeNodeKind.File -> {
                if (node.isMarkdownFile) node else null
            }
            RepoTreeNodeKind.Folder -> {
                val filteredChildren = filterTreeToMarkdownOnly(node.children)
                val isPlaceholderOnlyFolder = node.children.any { child ->
                    child.kind == RepoTreeNodeKind.File && child.name == ".gitkeep"
                }
                if (filteredChildren.isEmpty() && !isPlaceholderOnlyFolder) {
                    null
                } else {
                    node.copy(children = filteredChildren)
                }
            }
        }
    }
}
