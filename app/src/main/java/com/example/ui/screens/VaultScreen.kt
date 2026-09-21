package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import com.example.data.model.VaultSyncNetworkMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import android.content.res.Configuration
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.PlayCircle
import com.example.ui.components.GenziiVideoPlayer
import com.example.data.model.FileCategory
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.service.StorageFileService
import com.example.data.sync.VaultSyncStatus
import com.example.data.sync.VaultTransferProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatFileDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        "Recent"
    }
}

private val BrandBlue = Color(0xFF005AC1)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val GreenSecured = Color(0xFF10B981)
private val RedDelete = Color(0xFFEF4444)

enum class VaultTab(val title: String) {
    FILES("Explorer"),
    DEVICE_STORAGE("Device Storage"),
    RECENT("Recent"),
    FAVORITES("Favorites"),
    TRASH("Trash")
}

enum class VaultSortOption(val title: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)"),
    TYPE("Type")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    files: List<VaultItem>,
    folders: List<VaultFolder>,
    trashFiles: List<VaultItem> = emptyList(),
    favoriteFiles: List<VaultItem> = emptyList(),
    activeTransfers: Map<String, VaultTransferProgress> = emptyMap(),
    selectedFolder: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onFolderSelect: (String?) -> Unit,
    onFileClick: (VaultItem) -> Unit,
    onImportFiles: (List<Uri>, targetFolderId: String?, targetFolderName: String, targetFolderPath: String) -> Unit,
    onCreateFolder: (name: String, parentFolderId: String?, parentPath: String, colorHex: Long, isSecured: Boolean) -> Unit,
    onRenameFolder: (folderId: String, newName: String) -> Unit = { _, _ -> },
    onDeleteFolder: (folderId: String) -> Unit = {},
    onMoveFolder: (folderId: String, newParentFolderId: String?, newPath: String) -> Unit = { _, _, _ -> },
    onRenameFile: (fileId: String, newName: String) -> Unit = { _, _ -> },
    onToggleSecure: (VaultItem) -> Unit,
    onToggleFavorite: (VaultItem) -> Unit = {},
    onMoveMultipleFiles: (fileIds: List<String>, targetFolderName: String, targetFolderId: String?, targetFolderPath: String) -> Unit,
    onCopyMultipleFiles: (fileIds: List<String>, targetFolderName: String, targetFolderId: String?, targetFolderPath: String) -> Unit = { _, _, _, _ -> },
    onSetMultipleFilesSecured: (List<String>, Boolean) -> Unit,
    onTrashMultipleFiles: (List<String>) -> Unit,
    onRestoreMultipleFiles: (List<String>) -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    onDeletePermanently: (String) -> Unit = {},
    onCreateNewNote: (title: String, content: String, folder: String) -> Unit,
    syncStatus: VaultSyncStatus = VaultSyncStatus.Idle,
    syncNetworkMode: VaultSyncNetworkMode = VaultSyncNetworkMode.WIFI_ONLY,
    onSetSyncNetworkMode: (VaultSyncNetworkMode) -> Unit = {},
    onSyncClick: () -> Unit = {},
    onScanDocumentClick: () -> Unit = {},
    deviceStorageFiles: List<VaultItem> = emptyList(),
    isScanningDeviceStorage: Boolean = false,
    deviceStorageFilter: String = "All",
    onScanDeviceStorage: () -> Unit = {},
    onSetDeviceStorageFilter: (String) -> Unit = {},
    onImportDeviceFileToVault: (VaultItem) -> Unit = {}
) {
    val context = LocalContext.current

    var showNetworkModeDialog by remember { mutableStateOf(false) }

    // Full-Screen Preview overlay state for saved or accessed files
    var fullScreenPreviewFile by remember { mutableStateOf<VaultItem?>(null) }

    // Permissions for Storage (files, media, videos, audio) and Camera
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }
    }

    var hasStoragePermissions by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasStoragePermissions = perms.values.any { it }
        val allGranted = perms.values.all { it }
        if (allGranted) {
            Toast.makeText(context, "Storage and Camera access granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Device storage access updated", Toast.LENGTH_SHORT).show()
        }
    }

    // Active View Tab: Explorer, Recent, Favorites, Trash
    var selectedTab by remember { mutableStateOf(VaultTab.FILES) }

    // Hierarchical Folder Navigation Stack
    // folderStack holds the chain of folders: [Parent, Child, SubChild]
    val folderStack = remember { mutableStateListOf<VaultFolder>() }
    val currentFolder = folderStack.lastOrNull()

    // Sort Option
    var currentSort by remember { mutableStateOf(VaultSortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Category Filter Chip
    var selectedCategoryFilter by remember { mutableStateOf<FileCategory?>(null) }

    // Multi-selection state
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedFileIds = remember { mutableStateListOf<String>() }

    // Dialogs & Sheets State
    var showAddMenuSheet by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showNewNoteDialog by remember { mutableStateOf(false) }
    var showMoveSelectedDialog by remember { mutableStateOf(false) }
    var showCopySelectedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEmptyTrashConfirmDialog by remember { mutableStateOf(false) }

    var renameTargetFile by remember { mutableStateOf<VaultItem?>(null) }
    var renameTargetFolder by remember { mutableStateOf<VaultFolder?>(null) }
    var renameInputText by remember { mutableStateOf("") }
    var deleteTargetFolder by remember { mutableStateOf<VaultFolder?>(null) }
    var moveTargetFolder by remember { mutableStateOf<VaultFolder?>(null) }
    var moveSingleTargetFile by remember { mutableStateOf<VaultItem?>(null) }

    // Storage file picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val targetId = currentFolder?.id
            val targetName = currentFolder?.name ?: "Assignments"
            val targetPath = currentFolder?.path ?: "/$targetName"
            onImportFiles(uris, targetId, targetName, targetPath)
        }
    }

    // Active file set depending on selected tab
    val baseFiles = when (selectedTab) {
        VaultTab.FILES -> {
            if (currentFolder != null) {
                files.filter { !it.isTrash && (it.parentFolderId == currentFolder.id || it.folder.equals(currentFolder.name, ignoreCase = true)) }
            } else {
                files.filter { !it.isTrash }
            }
        }
        VaultTab.DEVICE_STORAGE -> {
            when (deviceStorageFilter) {
                "Videos" -> deviceStorageFiles.filter { it.category == FileCategory.VIDEO || it.mimeType.startsWith("video/") }
                "Documents" -> deviceStorageFiles.filter { it.category == FileCategory.PDF || it.category == FileCategory.DOCX || it.category == FileCategory.TXT || it.category == FileCategory.XLSX || it.category == FileCategory.PPTX }
                "Audio" -> deviceStorageFiles.filter { it.category == FileCategory.AUDIO || it.mimeType.startsWith("audio/") }
                "Pictures" -> deviceStorageFiles.filter { it.category == FileCategory.IMAGE || it.mimeType.startsWith("image/") }
                "Downloads" -> deviceStorageFiles.filter { it.folder.equals("Downloads", ignoreCase = true) || it.folderPath.contains("Download", ignoreCase = true) }
                else -> deviceStorageFiles
            }
        }
        VaultTab.RECENT -> files.filter { !it.isTrash }.sortedByDescending { it.createdAt }.take(20)
        VaultTab.FAVORITES -> files.filter { !it.isTrash && it.isFavorite }
        VaultTab.TRASH -> trashFiles.ifEmpty { files.filter { it.isTrash } }
    }

    // Subfolders in current level
    val visibleSubfolders = remember(folders, currentFolder) {
        if (currentFolder == null) {
            // Root level folders: folders with no parent or parentFolderId is null / blank
            folders.filter { !it.isTrash && (it.parentFolderId.isNullOrBlank() || it.parentFolderId == "root_all_files") && it.id != "root_all_files" }
        } else {
            // Children of currentFolder
            folders.filter { !it.isTrash && it.parentFolderId == currentFolder.id }
        }
    }

    // Filter & Sort files
    val displayedFiles = remember(baseFiles, searchQuery, selectedCategoryFilter, currentSort) {
        var result = baseFiles.filter { file ->
            val matchesSearch = if (searchQuery.isBlank()) true
            else file.name.contains(searchQuery, ignoreCase = true) || file.folder.contains(searchQuery, ignoreCase = true) || file.tag.contains(searchQuery, ignoreCase = true)

            val matchesCat = if (selectedCategoryFilter == null) true
            else file.category == selectedCategoryFilter

            matchesSearch && matchesCat
        }

        result = when (currentSort) {
            VaultSortOption.NAME_ASC -> result.sortedBy { it.name.lowercase(Locale.ROOT) }
            VaultSortOption.NAME_DESC -> result.sortedByDescending { it.name.lowercase(Locale.ROOT) }
            VaultSortOption.DATE_DESC -> result.sortedByDescending { it.createdAt }
            VaultSortOption.DATE_ASC -> result.sortedBy { it.createdAt }
            VaultSortOption.SIZE_DESC -> result.sortedByDescending { it.sizeBytes }
            VaultSortOption.SIZE_ASC -> result.sortedBy { it.sizeBytes }
            VaultSortOption.TYPE -> result.sortedBy { it.category.name }
        }
        result
    }

    // Calculate real storage usage
    val totalActiveBytes = remember(files) { files.filter { !it.isTrash }.sumOf { it.sizeBytes } }
    val totalActiveMB = remember(totalActiveBytes) { String.format(Locale.US, "%.1f MB", totalActiveBytes / (1024.0 * 1024.0)) }
    val storagePercent = remember(totalActiveBytes) { (totalActiveBytes / (100.0 * 1024 * 1024)).toFloat().coerceIn(0.01f, 1f) }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val brandBlue = MaterialTheme.colorScheme.primary
    val brandBlueLight = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .testTag("vault_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title + Sync Status + Selection Mode Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Genzii Vault",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${files.count { !it.isTrash }} files • Cloud Synchronized",
                            fontSize = 12.sp,
                            color = textMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Cloud Sync Status Pill Button
                        Surface(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(12.dp),
                            color = when (syncStatus) {
                                is VaultSyncStatus.Syncing -> brandBlueLight
                                is VaultSyncStatus.Synced -> Color(0xFF10B981).copy(alpha = 0.15f)
                                is VaultSyncStatus.Offline -> surfaceVariant
                                is VaultSyncStatus.Error -> RedDelete.copy(alpha = 0.15f)
                                else -> surfaceVariant
                            },
                            border = BorderStroke(1.dp, when (syncStatus) {
                                is VaultSyncStatus.Syncing -> brandBlue
                                is VaultSyncStatus.Synced -> Color(0xFF10B981)
                                is VaultSyncStatus.Offline -> borderColor
                                is VaultSyncStatus.Error -> RedDelete
                                else -> borderColor
                            })
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (syncStatus) {
                                        is VaultSyncStatus.Syncing -> Icons.Default.Sync
                                        is VaultSyncStatus.Synced -> Icons.Default.CloudDone
                                        is VaultSyncStatus.Offline -> Icons.Default.CloudOff
                                        is VaultSyncStatus.Error -> Icons.Default.CloudQueue
                                        else -> Icons.Default.CloudQueue
                                    },
                                    contentDescription = "Sync",
                                    tint = when (syncStatus) {
                                        is VaultSyncStatus.Syncing -> brandBlue
                                        is VaultSyncStatus.Synced -> Color(0xFF10B981)
                                        is VaultSyncStatus.Offline -> textMuted
                                        is VaultSyncStatus.Error -> RedDelete
                                        else -> textMuted
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (syncStatus) {
                                        is VaultSyncStatus.Syncing -> "Syncing..."
                                        is VaultSyncStatus.Synced -> "Synced"
                                        is VaultSyncStatus.Offline -> "Offline"
                                        is VaultSyncStatus.Error -> "Error"
                                        else -> "Sync"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when (syncStatus) {
                                        is VaultSyncStatus.Syncing -> brandBlue
                                        is VaultSyncStatus.Synced -> Color(0xFF10B981)
                                        is VaultSyncStatus.Offline -> textMuted
                                        is VaultSyncStatus.Error -> RedDelete
                                        else -> textMuted
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Network Mode Badge (Wi-Fi only vs Always)
                        Surface(
                            onClick = { showNetworkModeDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = surface,
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                                    contentDescription = "Sync Network Mode",
                                    tint = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) brandBlue else Color(0xFF0D9488),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) "Wi-Fi" else "Always",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (displayedFiles.isNotEmpty() || isSelectionMode) {
                            TextButton(
                                onClick = {
                                    isSelectionMode = !isSelectionMode
                                    if (!isSelectionMode) selectedFileIds.clear()
                                }
                            ) {
                                Text(
                                    text = if (isSelectionMode) "Done" else "Select",
                                    color = brandBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Status Banner: Loading, Success, Offline, Error and Retry states
            when (syncStatus) {
                is VaultSyncStatus.Syncing -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = brandBlueLight,
                            border = BorderStroke(1.dp, brandBlue.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = brandBlue
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Syncing with cloud...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brandBlue
                                    )
                                    Text(
                                        text = "Uploading updates and synchronizing files. Local vault is fully active.",
                                        fontSize = 11.sp,
                                        color = textDark
                                    )
                                }
                            }
                        }
                    }
                }
                is VaultSyncStatus.Error -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = RedDelete.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, RedDelete.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Sync Error",
                                    tint = RedDelete,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sync error occurred",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RedDelete
                                    )
                                    Text(
                                        text = syncStatus.message,
                                        fontSize = 11.sp,
                                        color = textDark,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Button(
                                    onClick = onSyncClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = RedDelete),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry", modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                is VaultSyncStatus.Offline -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (syncStatus.reason.contains("Wi-Fi", ignoreCase = true)) Icons.Default.Wifi else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (syncStatus.reason.contains("mobile data", ignoreCase = true) || syncStatus.reason.contains("Wi-Fi only", ignoreCase = true)) "Cloud sync paused on mobile data" else "Offline Mode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = "${syncStatus.reason}. Local files and folders are fully available.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY && (syncStatus.reason.contains("mobile data", ignoreCase = true) || syncStatus.reason.contains("Wi-Fi only", ignoreCase = true))) {
                                            onSetSyncNetworkMode(VaultSyncNetworkMode.ALWAYS)
                                        }
                                        onSyncClick()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD97706))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color(0xFFD97706), modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Retry", fontSize = 11.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                is VaultSyncStatus.Synced -> {
                    // Success state indication
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Synced", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vault synchronized • Local storage and cloud are up to date",
                                    fontSize = 11.sp,
                                    color = Color(0xFF065F46),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                VaultSyncStatus.Idle -> { /* Idle */ }
            }

            // Live Upload / Download Progress Banner
            if (activeTransfers.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(brandBlueLight)
                            .padding(12.dp)
                    ) {
                        activeTransfers.values.forEach { transfer ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (transfer.isUpload) "Uploading: ${transfer.fileName}" else "Downloading: ${transfer.fileName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = brandBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${transfer.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { transfer.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = brandBlue,
                                trackColor = surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Navigation Tabs: Explorer | Device Storage | Recent | Favorites | Trash
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = BrandBlue,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = BrandBlue
                        )
                    },
                    divider = {}
                ) {
                    VaultTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = {
                                selectedTab = tab
                                if (tab != VaultTab.FILES) {
                                    folderStack.clear()
                                }
                            },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) BrandBlue else TextMuted
                                )
                            }
                        )
                    }
                }
            }

            // Device Storage Section Header & Direct Quick Filter Chips
            if (selectedTab == VaultTab.DEVICE_STORAGE) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(brandBlueLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            tint = brandBlue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Internal Device Storage",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textDark
                                        )
                                        Text(
                                            text = "Direct read & play • No upload required",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }
                                }

                                Button(
                                    onClick = onScanDeviceStorage,
                                    enabled = !isScanningDeviceStorage,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    if (isScanningDeviceStorage) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scanning...", fontSize = 11.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rescan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Storage Filter Chips
                            val deviceFilters = listOf(
                                "All" to deviceStorageFiles.size,
                                "Videos" to deviceStorageFiles.count { it.category == FileCategory.VIDEO || it.mimeType.startsWith("video/") },
                                "Documents" to deviceStorageFiles.count { it.category == FileCategory.PDF || it.category == FileCategory.DOCX || it.category == FileCategory.TXT || it.category == FileCategory.XLSX || it.category == FileCategory.PPTX },
                                "Audio" to deviceStorageFiles.count { it.category == FileCategory.AUDIO || it.mimeType.startsWith("audio/") },
                                "Pictures" to deviceStorageFiles.count { it.category == FileCategory.IMAGE || it.mimeType.startsWith("image/") },
                                "Downloads" to deviceStorageFiles.count { it.folder.equals("Downloads", ignoreCase = true) || it.folderPath.contains("Download", ignoreCase = true) }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                deviceFilters.forEach { (catName, count) ->
                                    val isSelected = deviceStorageFilter == catName
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSetDeviceStorageFilter(catName) },
                                        label = {
                                            Text(
                                                text = "$catName ($count)",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = brandBlue,
                                            selectedLabelColor = Color.White,
                                            containerColor = surfaceVariant,
                                            labelColor = textDark
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Breadcrumb Navigation Bar (Hierarchical Folder Path)
            if (selectedTab == VaultTab.FILES) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { folderStack.clear() },
                            shape = RoundedCornerShape(8.dp),
                            color = if (folderStack.isEmpty()) brandBlueLight else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Root",
                                    tint = if (folderStack.isEmpty()) brandBlue else textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Folders",
                                    fontSize = 13.sp,
                                    fontWeight = if (folderStack.isEmpty()) FontWeight.Bold else FontWeight.Medium,
                                    color = if (folderStack.isEmpty()) brandBlue else textMuted
                                )
                            }
                        }

                        folderStack.forEachIndexed { index, folder ->
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            val isLast = index == folderStack.size - 1
                            Surface(
                                onClick = {
                                    while (folderStack.size > index + 1) {
                                        folderStack.removeAt(folderStack.size - 1)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isLast) brandBlueLight else Color.Transparent
                            ) {
                                Text(
                                    text = folder.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLast) brandBlue else textMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar & Sort Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = textDark, fontSize = 14.sp),
                        placeholder = { Text("Search vault files, tags, notes...", fontSize = 13.sp, color = textMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = textMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = textMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            cursorColor = brandBlue,
                            focusedBorderColor = brandBlue,
                            unfocusedBorderColor = borderColor,
                            focusedContainerColor = surface,
                            unfocusedContainerColor = surface
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort button with dropdown
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(surface)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = brandBlue, modifier = Modifier.size(22.dp))
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            VaultSortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.title,
                                            fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (currentSort == option) brandBlue else textDark
                                        )
                                    },
                                    onClick = {
                                        currentSort = option
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // File Type Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Formats", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    val mainCategories = listOf(
                        FileCategory.PDF to "PDF",
                        FileCategory.DOCX to "Documents",
                        FileCategory.IMAGE to "Images",
                        FileCategory.VIDEO to "Videos",
                        FileCategory.AUDIO to "Audio",
                        FileCategory.NOTE to "Notes"
                    )
                    items(mainCategories) { (cat, title) ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text(title, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Storage Usage Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Cloud Storage Usage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textDark)
                            Text(text = "$totalActiveMB / 100 MB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = brandBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { storagePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = brandBlue,
                            trackColor = surfaceVariant
                        )
                    }
                }
            }

            // Multi-Select Action Banner
            if (isSelectionMode && selectedFileIds.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = brandBlueLight),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedFileIds.size} selected",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandBlue
                            )

                            Row {
                                if (selectedTab == VaultTab.TRASH) {
                                    // Restore selected
                                    IconButton(
                                        onClick = {
                                            onRestoreMultipleFiles(selectedFileIds.toList())
                                            selectedFileIds.clear()
                                            isSelectionMode = false
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore", tint = brandBlue)
                                    }
                                } else {
                                    // Move to folder
                                    IconButton(onClick = { showMoveSelectedDialog = true }) {
                                        Icon(imageVector = Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move", tint = brandBlue)
                                    }
                                    // Copy selected
                                    IconButton(onClick = { showCopySelectedDialog = true }) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = brandBlue)
                                    }
                                    // Secure
                                    IconButton(onClick = {
                                        onSetMultipleFilesSecured(selectedFileIds.toList(), true)
                                        selectedFileIds.clear()
                                        isSelectionMode = false
                                    }) {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure", tint = GreenSecured)
                                    }
                                }
                                // Delete / Trash
                                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedDelete)
                                }
                            }
                        }
                    }
                }
            }

            // Empty Trash button if on Trash tab
            if (selectedTab == VaultTab.TRASH && displayedFiles.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Trash (${displayedFiles.size} items)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
                        TextButton(onClick = { showEmptyTrashConfirmDialog = true }) {
                            Text(text = "Empty Trash", color = RedDelete, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Storage & Media Permission Banner if not yet granted
            if (!hasStoragePermissions) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { permissionLauncher.launch(permissionsToRequest) }
                            .testTag("grant_storage_permission_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(brandBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Storage & Camera Permissions",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Grant access to preview local device files, photos, videos, and camera scan.",
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }

                            Button(
                                onClick = { permissionLauncher.launch(permissionsToRequest) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Grant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Folder Cards (If in Explorer tab)
            if (selectedTab == VaultTab.FILES && visibleSubfolders.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentFolder == null) "Folders" else "Subfolders",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                        IconButton(
                            onClick = { showCreateFolderDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Add subfolder", tint = brandBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                items(visibleSubfolders) { folder ->
                    FolderRowCard(
                        folder = folder,
                        onClick = {
                            folderStack.add(folder)
                            onFolderSelect(folder.name)
                        },
                        onRename = {
                            renameTargetFolder = folder
                            renameInputText = folder.name
                        }
                    )
                }
            }

            // Files Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedTab) {
                            VaultTab.FILES -> if (currentFolder == null) "All Documents" else "${currentFolder.name} Files"
                            VaultTab.DEVICE_STORAGE -> "Device Storage ($deviceStorageFilter)"
                            VaultTab.RECENT -> "Recent Documents"
                            VaultTab.FAVORITES -> "Starred Documents"
                            VaultTab.TRASH -> "Trash Items"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                    Text(
                        text = "${displayedFiles.size} files",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                }
            }

            // Display Files List
            if (displayedFiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = when (selectedTab) {
                                    VaultTab.TRASH -> Icons.Default.Delete
                                    VaultTab.FAVORITES -> Icons.Default.StarBorder
                                    else -> Icons.Default.FolderOpen
                                },
                                contentDescription = null,
                                tint = textMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = when (selectedTab) {
                                    VaultTab.TRASH -> "Trash is empty"
                                    VaultTab.FAVORITES -> "No starred documents yet"
                                    else -> "No files found in this folder"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use '+' to upload documents, assignments, notes or media.",
                                fontSize = 12.sp,
                                color = textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayedFiles, key = { it.id }) { file ->
                    val isSelected = file.id in selectedFileIds
                    VaultFileRowCard(
                        file = file,
                        isSelectionMode = isSelectionMode,
                        isSelected = isSelected,
                        onToggleSelect = {
                            if (isSelected) selectedFileIds.remove(file.id)
                            else selectedFileIds.add(file.id)
                        },
                        onClick = {
                            if (isSelectionMode) {
                                if (isSelected) selectedFileIds.remove(file.id)
                                else selectedFileIds.add(file.id)
                            } else {
                                fullScreenPreviewFile = file
                            }
                        },
                        onFullScreenPreview = {
                            fullScreenPreviewFile = file
                        },
                        onOpenDetails = {
                            onFileClick(file)
                        },
                        onToggleFavorite = { onToggleFavorite(file) },
                        onRename = {
                            renameTargetFile = file
                            renameInputText = file.name
                        },
                        onCopy = {
                            onCopyMultipleFiles(listOf(file.id), file.folder, file.parentFolderId, file.folderPath)
                            Toast.makeText(context, "Created copy of ${file.name}", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            if (file.localFilePath.isNotBlank() && java.io.File(file.localFilePath).exists()) {
                                StorageFileService.shareFile(context, file.localFilePath, file.mimeType, file.name)
                            } else if (file.downloadUrl.isNotBlank()) {
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(android.content.Intent.EXTRA_TEXT, "File download link: ${file.downloadUrl}")
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share file link"))
                            } else {
                                Toast.makeText(context, "Cloud-only file. Open details to download.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTrash = {
                            onTrashMultipleFiles(listOf(file.id))
                        },
                        onRestore = {
                            onRestoreMultipleFiles(listOf(file.id))
                        },
                        onDeletePermanently = {
                            onDeletePermanently(file.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button for Adding / Uploading Files
        FloatingActionButton(
            onClick = { showAddMenuSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 28.dp)
                .navigationBarsPadding(),
            containerColor = BrandBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add files")
        }

        // Add / Upload Menu Bottom Sheet
        if (showAddMenuSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddMenuSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = surface,
                contentColor = textDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "Add to ${currentFolder?.name ?: "Vault"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AddOptionRow(
                        icon = Icons.Default.UploadFile,
                        title = "Upload Files from Device",
                        subtitle = "Select PDFs, documents, images, audio, or videos",
                        color = brandBlue,
                        onClick = {
                            showAddMenuSheet = false
                            val missingPermissions = permissionsToRequest.filter { perm ->
                                ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
                            }
                            if (missingPermissions.isNotEmpty()) {
                                permissionLauncher.launch(missingPermissions.toTypedArray())
                            }
                            filePickerLauncher.launch(arrayOf("*/*"))
                        }
                    )

                    AddOptionRow(
                        icon = Icons.Default.CameraAlt,
                        title = "Scan Document with Camera",
                        subtitle = "Capture text and receipts using CameraX and ML Kit OCR",
                        color = Color(0xFF0284C7),
                        onClick = {
                            showAddMenuSheet = false
                            onScanDocumentClick()
                        }
                    )

                    AddOptionRow(
                        icon = if (hasStoragePermissions) Icons.Default.Check else Icons.Default.LockOpen,
                        title = "Storage & Camera Access",
                        subtitle = if (hasStoragePermissions) "Permissions active for files & camera" else "Grant access to local storage & camera",
                        color = if (hasStoragePermissions) Color(0xFF10B981) else Color(0xFFF43F5E),
                        onClick = {
                            showAddMenuSheet = false
                            permissionLauncher.launch(permissionsToRequest)
                        }
                    )

                    AddOptionRow(
                        icon = Icons.Default.CreateNewFolder,
                        title = "Create New Folder",
                        subtitle = if (currentFolder == null) "Create top-level folder" else "Create subfolder inside ${currentFolder.name}",
                        color = Color(0xFF10B981),
                        onClick = {
                            showAddMenuSheet = false
                            showCreateFolderDialog = true
                        }
                    )

                    AddOptionRow(
                        icon = Icons.AutoMirrored.Filled.NoteAdd,
                        title = "Create Note / Document",
                        subtitle = "Write research notes or outlines directly",
                        color = Color(0xFF8B5CF6),
                        onClick = {
                            showAddMenuSheet = false
                            showNewNoteDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Create Folder Dialog
        if (showCreateFolderDialog) {
            var folderNameInput by remember { mutableStateOf("") }
            var isSecuredFolder by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text(if (currentFolder == null) "New Folder" else "New Subfolder", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = folderNameInput,
                            onValueChange = { folderNameInput = it },
                            label = { Text("Folder Name") },
                            placeholder = { Text("e.g. Thesis Drafts") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textDark,
                                unfocusedTextColor = textDark,
                                cursorColor = brandBlue,
                                focusedBorderColor = brandBlue,
                                unfocusedBorderColor = borderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSecuredFolder,
                                onCheckedChange = { isSecuredFolder = it },
                                colors = CheckboxDefaults.colors(checkedColor = GreenSecured)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock & Encrypt Folder", fontSize = 13.sp, color = textDark)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = folderNameInput.trim()
                            if (trimmed.isNotBlank()) {
                                val parentId = currentFolder?.id
                                val parentPath = currentFolder?.path ?: ""
                                onCreateFolder(trimmed, parentId, parentPath, 0xFF005AC1, isSecuredFolder)
                                showCreateFolderDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Rename File Dialog
        if (renameTargetFile != null) {
            AlertDialog(
                onDismissRequest = { renameTargetFile = null },
                title = { Text("Rename File", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        label = { Text("New file name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = textDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            cursorColor = brandBlue,
                            focusedBorderColor = brandBlue,
                            unfocusedBorderColor = borderColor
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = renameInputText.trim()
                            if (trimmed.isNotBlank()) {
                                onRenameFile(renameTargetFile!!.id, trimmed)
                                renameTargetFile = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetFile = null }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Rename Folder Dialog
        if (renameTargetFolder != null) {
            AlertDialog(
                onDismissRequest = { renameTargetFolder = null },
                title = { Text("Rename Folder", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        label = { Text("New folder name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = textDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            cursorColor = brandBlue,
                            focusedBorderColor = brandBlue,
                            unfocusedBorderColor = borderColor
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = renameInputText.trim()
                            if (trimmed.isNotBlank()) {
                                onRenameFolder(renameTargetFolder!!.id, trimmed)
                                renameTargetFolder = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetFolder = null }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Create Note Dialog
        if (showNewNoteDialog) {
            var noteTitle by remember { mutableStateOf("") }
            var noteBody by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNewNoteDialog = false },
                title = { Text("Create Note", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textDark,
                                unfocusedTextColor = textDark,
                                cursorColor = brandBlue,
                                focusedBorderColor = brandBlue,
                                unfocusedBorderColor = borderColor
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = noteBody,
                            onValueChange = { noteBody = it },
                            label = { Text("Note content") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textDark,
                                unfocusedTextColor = textDark,
                                cursorColor = brandBlue,
                                focusedBorderColor = brandBlue,
                                unfocusedBorderColor = borderColor
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val t = noteTitle.trim().ifBlank { "Untitled Note" }
                            val targetFolderName = currentFolder?.name ?: "Notes"
                            onCreateNewNote(t, noteBody, targetFolderName)
                            showNewNoteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewNoteDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Move Selected Files Dialog
        if (showMoveSelectedDialog) {
            var targetFolderForMove by remember { mutableStateOf(folders.firstOrNull()?.name ?: "Assignments") }
            AlertDialog(
                onDismissRequest = { showMoveSelectedDialog = false },
                title = { Text("Move ${selectedFileIds.size} Files", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column {
                        Text("Choose destination folder:", fontSize = 13.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        folders.filter { it.name != "All Files" }.forEach { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetFolderForMove = folder.name }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = targetFolderForMove == folder.name,
                                    onClick = { targetFolderForMove = folder.name },
                                    colors = RadioButtonDefaults.colors(selectedColor = brandBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder.name, fontSize = 14.sp, color = textDark)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val destFolder = folders.find { it.name == targetFolderForMove }
                            onMoveMultipleFiles(
                                selectedFileIds.toList(),
                                targetFolderForMove,
                                destFolder?.id,
                                destFolder?.path ?: "/$targetFolderForMove"
                            )
                            selectedFileIds.clear()
                            isSelectionMode = false
                            showMoveSelectedDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Move")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMoveSelectedDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Copy Selected Files Dialog
        if (showCopySelectedDialog) {
            var targetFolderForCopy by remember { mutableStateOf(folders.firstOrNull()?.name ?: "Assignments") }
            AlertDialog(
                onDismissRequest = { showCopySelectedDialog = false },
                title = { Text("Copy ${selectedFileIds.size} Files", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column {
                        Text("Choose destination folder for duplicate:", fontSize = 13.sp, color = textMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        folders.filter { it.name != "All Files" }.forEach { folder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetFolderForCopy = folder.name }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = targetFolderForCopy == folder.name,
                                    onClick = { targetFolderForCopy = folder.name },
                                    colors = RadioButtonDefaults.colors(selectedColor = brandBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder.name, fontSize = 14.sp, color = textDark)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val destFolder = folders.find { it.name == targetFolderForCopy }
                            onCopyMultipleFiles(
                                selectedFileIds.toList(),
                                targetFolderForCopy,
                                destFolder?.id,
                                destFolder?.path ?: "/$targetFolderForCopy"
                            )
                            Toast.makeText(context, "Copied ${selectedFileIds.size} files", Toast.LENGTH_SHORT).show()
                            selectedFileIds.clear()
                            isSelectionMode = false
                            showCopySelectedDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                    ) {
                        Text("Copy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCopySelectedDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Move to Trash confirm dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Move to Trash?", fontWeight = FontWeight.Bold, color = textDark) },
                text = { Text("Move ${selectedFileIds.size} files to Trash? You can restore them anytime.", color = textDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedTab == VaultTab.TRASH) {
                                selectedFileIds.forEach { onDeletePermanently(it) }
                            } else {
                                onTrashMultipleFiles(selectedFileIds.toList())
                            }
                            selectedFileIds.clear()
                            isSelectionMode = false
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedDelete)
                    ) {
                        Text(if (selectedTab == VaultTab.TRASH) "Delete Permanently" else "Move to Trash")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Empty Trash Confirm Dialog
        if (showEmptyTrashConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashConfirmDialog = false },
                title = { Text("Empty Trash?", fontWeight = FontWeight.Bold, color = textDark) },
                text = { Text("This will permanently delete all files in Trash from both cloud storage and local device. This action cannot be undone.", color = textDark) },
                confirmButton = {
                    Button(
                        onClick = {
                            onEmptyTrash()
                            showEmptyTrashConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedDelete)
                    ) {
                        Text("Empty Trash")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmptyTrashConfirmDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = surface
            )
        }

        // Full-Screen Preview Overlay for files saved or accessed
        if (fullScreenPreviewFile != null) {
            VaultFullScreenPreviewOverlay(
                file = fullScreenPreviewFile!!,
                onDismiss = { fullScreenPreviewFile = null },
                onOpenDetails = {
                    val file = fullScreenPreviewFile!!
                    fullScreenPreviewFile = null
                    onFileClick(file)
                },
                onToggleFavorite = {
                    val file = fullScreenPreviewFile!!
                    onToggleFavorite(file)
                    fullScreenPreviewFile = file.copy(isFavorite = !file.isFavorite)
                },
                onShare = {
                    val file = fullScreenPreviewFile!!
                    if (file.localFilePath.isNotBlank() && java.io.File(file.localFilePath).exists()) {
                        StorageFileService.shareFile(context, file.localFilePath, file.mimeType, file.name)
                    } else if (file.downloadUrl.isNotBlank()) {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "File download link: ${file.downloadUrl}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share file link"))
                    } else {
                        Toast.makeText(context, "No shareable file resource found", Toast.LENGTH_SHORT).show()
                    }
                },
                onCopy = {
                    val file = fullScreenPreviewFile!!
                    onCopyMultipleFiles(listOf(file.id), file.folder, file.parentFolderId, file.folderPath)
                    Toast.makeText(context, "Duplicate created in ${file.folder}", Toast.LENGTH_SHORT).show()
                },
                onTrash = {
                    val file = fullScreenPreviewFile!!
                    onTrashMultipleFiles(listOf(file.id))
                    fullScreenPreviewFile = null
                    Toast.makeText(context, "Moved to Trash", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    if (showNetworkModeDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkModeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Vault Sync Network",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose when files, documents, and notes should sync with Firestore and Cloud Storage:",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Wi-Fi only
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetSyncNetworkMode(VaultSyncNetworkMode.WIFI_ONLY)
                                showNetworkModeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) brandBlueLight else surfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) brandBlue else borderColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY,
                                onClick = {
                                    onSetSyncNetworkMode(VaultSyncNetworkMode.WIFI_ONLY)
                                    showNetworkModeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = brandBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Wi-Fi only",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                                Text(
                                    text = "Syncs files only on Wi-Fi. Preserves cellular data plan.",
                                    fontSize = 12.sp,
                                    color = textMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: Always
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSetSyncNetworkMode(VaultSyncNetworkMode.ALWAYS)
                                showNetworkModeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (syncNetworkMode == VaultSyncNetworkMode.ALWAYS) brandBlueLight else surfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (syncNetworkMode == VaultSyncNetworkMode.ALWAYS) brandBlue else borderColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = syncNetworkMode == VaultSyncNetworkMode.ALWAYS,
                                onClick = {
                                    onSetSyncNetworkMode(VaultSyncNetworkMode.ALWAYS)
                                    showNetworkModeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = brandBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Always",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                                Text(
                                    text = "Syncs continuously across both Wi-Fi and mobile data.",
                                    fontSize = 12.sp,
                                    color = textMuted,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNetworkModeDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = brandBlue)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = surface
        )
    }
}

// ------------------------------------------------------------------------------------------------
// Subcomponents
// ------------------------------------------------------------------------------------------------

@Composable
private fun FolderRowCard(
    folder: VaultFolder,
    onClick: () -> Unit,
    onRename: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(folder.bgColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = folder.name,
                        tint = Color(folder.iconColorHex),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = folder.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = textDark)
                        if (folder.isSecured) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secured", tint = GreenSecured, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(text = "${folder.count} items", fontSize = 12.sp, color = textMuted)
                }
            }

            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Folder options", tint = textMuted, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            showMenu = false
                            onRename()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultFileRowCard(
    file: VaultItem,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit,
    onFullScreenPreview: () -> Unit = {},
    onOpenDetails: () -> Unit = {},
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit = {},
    onShare: () -> Unit = {},
    onTrash: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDeletePermanently: () -> Unit = {}
) {
    val surface = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val brandBlue = MaterialTheme.colorScheme.primary
    val brandBlueLight = MaterialTheme.colorScheme.primaryContainer

    val (icon, tint, bg) = when (file.category) {
        FileCategory.PDF -> Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.12f))
        FileCategory.DOCX -> Triple(Icons.Default.Description, Color(0xFF2563EB), Color(0xFF2563EB).copy(alpha = 0.12f))
        FileCategory.XLSX -> Triple(Icons.Default.TableChart, Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.12f))
        FileCategory.IMAGE -> Triple(Icons.Default.Image, Color(0xFF06B6D4), Color(0xFF06B6D4).copy(alpha = 0.12f))
        FileCategory.VIDEO -> Triple(Icons.Default.Videocam, Color(0xFFEC4899), Color(0xFFEC4899).copy(alpha = 0.12f))
        FileCategory.AUDIO -> Triple(Icons.Default.Audiotrack, Color(0xFFA855F7), Color(0xFFA855F7).copy(alpha = 0.12f))
        FileCategory.NOTE, FileCategory.TXT -> Triple(Icons.AutoMirrored.Filled.NoteAdd, Color(0xFF8B5CF6), Color(0xFF8B5CF6).copy(alpha = 0.12f))
        else -> Triple(Icons.Default.Description, textMuted, textMuted.copy(alpha = 0.12f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) brandBlueLight.copy(alpha = 0.4f) else surface
        ),
        border = BorderStroke(1.dp, if (isSelected) brandBlue else borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = brandBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${file.sizeString} • ${file.folder}",
                        fontSize = 12.sp,
                        color = textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (file.isSecured) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GreenSecured, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Star",
                        tint = if (file.isFavorite) Color(0xFFF59E0B) else textMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = textMuted, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = brandBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Full Screen Preview", fontWeight = FontWeight.SemiBold, color = brandBlue)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onFullScreenPreview()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Open Details") },
                            onClick = {
                                showMenu = false
                                onOpenDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename") },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Make a Copy") },
                            onClick = {
                                showMenu = false
                                onCopy()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share File") },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        if (file.isTrash) {
                            DropdownMenuItem(
                                text = { Text("Restore") },
                                onClick = {
                                    showMenu = false
                                    onRestore()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Permanently", color = RedDelete) },
                                onClick = {
                                    showMenu = false
                                    onDeletePermanently()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Move to Trash", color = RedDelete) },
                                onClick = {
                                    showMenu = false
                                    onTrash()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textDark)
            Text(text = subtitle, fontSize = 12.sp, color = textMuted)
        }
    }
}

@Composable
fun VaultFullScreenPreviewOverlay(
    file: VaultItem,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onTrash: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember(file.id, file.isFavorite) { mutableStateOf(file.isFavorite) }

    // Zoom & Pan state for images
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Audio Playback simulation state
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioProgress by remember { mutableFloatStateOf(0.25f) }

    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val brandBlue = MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                Surface(
                    color = surface,
                    contentColor = onSurface,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close preview",
                                tint = onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = file.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${file.category.name} • ${file.sizeString} • ${file.folder}",
                                    fontSize = 11.sp,
                                    color = onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (file.isSecured) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Secured",
                                        tint = GreenSecured,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        if (file.category == FileCategory.IMAGE && (scale != 1f || offset != Offset.Zero)) {
                            IconButton(onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset zoom",
                                    tint = brandBlue
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                isFavorite = !isFavorite
                                onToggleFavorite()
                            }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFBBF24) else onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = onSurface
                            )
                        }
                    }
                }

                // Main Content Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (file.category) {
                        FileCategory.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.75f, 5.0f)
                                            offset = if (scale > 1f) {
                                                Offset(offset.x + pan.x, offset.y + pan.y)
                                            } else {
                                                Offset.Zero
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                if (scale > 1f) {
                                                    scale = 1f
                                                    offset = Offset.Zero
                                                } else {
                                                    scale = 2.5f
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val model = file.localFilePath.ifBlank { file.downloadUrl }
                                AsyncImage(
                                    model = model,
                                    contentDescription = file.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offset.x,
                                            translationY = offset.y
                                        ),
                                    contentScale = ContentScale.Fit
                                )

                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = surface.copy(alpha = 0.85f),
                                    contentColor = onSurface,
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Text(
                                        text = "${(scale * 100).toInt()}% • Pinch / Double-Tap",
                                        color = onSurface,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        FileCategory.NOTE, FileCategory.TXT -> {
                            val textContent = remember(file.localFilePath) {
                                val localFile = if (file.localFilePath.isNotBlank()) java.io.File(file.localFilePath) else null
                                if (localFile != null && localFile.exists()) {
                                    try {
                                        localFile.readText()
                                    } catch (e: Exception) {
                                        "Unable to read file contents: ${e.message}"
                                    }
                                } else {
                                    """# ${file.name}
                                    |
                                    |Folder: ${file.folder}
                                    |Category: Academic Notes
                                    |Last Modified: ${file.createdAt}
                                    |
                                    |Summary & Key Points:
                                    |1. Comprehensive research draft outlining thesis hypothesis and primary literature citations.
                                    |2. Qualitative data synthesized with cross-referenced methodology notes.
                                    |3. Prepared for originality verification and peer-review examination.
                                    """.trimMargin()
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "DOCUMENT VIEWER",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = brandBlue,
                                            letterSpacing = 1.2.sp
                                        )
                                        Text(
                                            text = "${textContent.split("\\s+".toRegex()).size} words",
                                            fontSize = 11.sp,
                                            color = onSurfaceVariant
                                        )
                                    }
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = borderColor
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = textContent,
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            color = onSurface
                                        )
                                    }
                                }
                            }
                        }

                        FileCategory.PDF -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Portable Document Format • ${file.sizeString}",
                                        fontSize = 13.sp,
                                        color = onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            if (file.localFilePath.isNotBlank() && java.io.File(file.localFilePath).exists()) {
                                                StorageFileService.openFileExternally(context, file.localFilePath, "application/pdf")
                                            } else {
                                                Toast.makeText(context, "Opening document viewer...", Toast.LENGTH_SHORT).show()
                                                onOpenDetails()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open in PDF Reader", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        FileCategory.AUDIO -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = Color(0xFFA855F7),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Audio Recording • ${file.sizeString}",
                                        fontSize = 13.sp,
                                        color = onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Waveform Visualizer
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(surfaceVariant)
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val waveformHeights = listOf(14, 28, 42, 22, 38, 50, 32, 18, 44, 30, 48, 26, 36, 16, 40, 24, 34)
                                        waveformHeights.forEachIndexed { index, h ->
                                            val barColor = if (index.toFloat() / waveformHeights.size <= audioProgress) {
                                                Color(0xFFA855F7)
                                            } else {
                                                borderColor
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(h.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(barColor)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                isAudioPlaying = !isAudioPlaying
                                                if (isAudioPlaying) audioProgress = 0.65f
                                            },
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFA855F7))
                                        ) {
                                            Icon(
                                                imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isAudioPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FileCategory.VIDEO -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEC4899).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = Color(0xFFEC4899),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Video Media • ${file.sizeString}",
                                        fontSize = 13.sp,
                                        color = onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            if (file.localFilePath.isNotBlank() && java.io.File(file.localFilePath).exists()) {
                                                StorageFileService.openFileExternally(context, file.localFilePath, file.mimeType.ifBlank { "video/*" })
                                            } else {
                                                onOpenDetails()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Play in System Video Player", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        else -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = surface),
                                border = BorderStroke(1.dp, borderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = brandBlue,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = file.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${file.category.name} • ${file.sizeString} • ${file.folder}",
                                        fontSize = 13.sp,
                                        color = onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = {
                                            if (file.localFilePath.isNotBlank() && java.io.File(file.localFilePath).exists()) {
                                                StorageFileService.openFileExternally(context, file.localFilePath, file.mimeType)
                                            } else {
                                                onOpenDetails()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = brandBlue),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Open with System App", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Quick Action Bar
                Surface(
                    color = surface,
                    contentColor = onSurface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenDetails,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Text("Full Details", color = onSurface, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onShare,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCopy,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = onSurface, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Duplicate", color = onSurface, fontSize = 12.sp)
                        }

                        IconButton(onClick = onTrash) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Trash",
                                tint = RedDelete
                            )
                        }
                    }
                }
            }
        }
    }
}
