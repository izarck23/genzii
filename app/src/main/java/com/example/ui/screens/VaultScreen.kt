package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.service.StorageFileService
import com.example.data.sync.VaultSyncStatus
import com.example.data.sync.VaultTransferProgress
import java.util.Locale

private val BrandBlue = Color(0xFF005AC1)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val GreenSecured = Color(0xFF10B981)
private val RedDelete = Color(0xFFEF4444)

enum class VaultTab(val title: String) {
    FILES("Explorer"),
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
    onSyncClick: () -> Unit = {}
) {
    val context = LocalContext.current

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                            color = TextDark,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "${files.count { !it.isTrash }} files • Cloud Synchronized",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Cloud Sync Status Pill Button
                        Surface(
                            onClick = onSyncClick,
                            shape = RoundedCornerShape(12.dp),
                            color = when (syncStatus) {
                                is VaultSyncStatus.Syncing -> Color(0xFFEFF6FF)
                                is VaultSyncStatus.Synced -> Color(0xFFECFDF5)
                                is VaultSyncStatus.Offline -> Color(0xFFF8FAFC)
                                is VaultSyncStatus.Error -> Color(0xFFFEF2F2)
                                else -> Color(0xFFF1F5F9)
                            },
                            border = BorderStroke(1.dp, when (syncStatus) {
                                is VaultSyncStatus.Syncing -> Color(0xFF93C5FD)
                                is VaultSyncStatus.Synced -> Color(0xFF6EE7B7)
                                is VaultSyncStatus.Offline -> BorderColor
                                is VaultSyncStatus.Error -> Color(0xFFFCA5A5)
                                else -> BorderColor
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
                                        is VaultSyncStatus.Syncing -> BrandBlue
                                        is VaultSyncStatus.Synced -> Color(0xFF059669)
                                        is VaultSyncStatus.Offline -> TextMuted
                                        is VaultSyncStatus.Error -> RedDelete
                                        else -> TextMuted
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
                                        is VaultSyncStatus.Syncing -> BrandBlue
                                        is VaultSyncStatus.Synced -> Color(0xFF059669)
                                        is VaultSyncStatus.Offline -> TextMuted
                                        is VaultSyncStatus.Error -> RedDelete
                                        else -> TextMuted
                                    }
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
                                    color = BrandBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Live Upload / Download Progress Banner
            if (activeTransfers.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEFF6FF))
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
                                    color = BrandBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${transfer.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { transfer.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandBlue,
                                trackColor = Color(0xFFBFDBFE)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Navigation Tabs: Explorer | Recent | Favorites | Trash
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = BrandBlue,
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
                            color = if (folderStack.isEmpty()) Color(0xFFEFF6FF) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = "Root",
                                    tint = if (folderStack.isEmpty()) BrandBlue else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All Folders",
                                    fontSize = 13.sp,
                                    fontWeight = if (folderStack.isEmpty()) FontWeight.Bold else FontWeight.Medium,
                                    color = if (folderStack.isEmpty()) BrandBlue else TextMuted
                                )
                            }
                        }

                        folderStack.forEachIndexed { index, folder ->
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextMuted,
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
                                color = if (isLast) Color(0xFFEFF6FF) else Color.Transparent
                            ) {
                                Text(
                                    text = folder.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLast) BrandBlue else TextMuted,
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
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark, fontSize = 14.sp),
                        placeholder = { Text("Search vault files, tags, notes...", fontSize = 13.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = BrandBlue,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
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
                                .background(Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort", tint = BrandBlue, modifier = Modifier.size(22.dp))
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
                                            color = if (currentSort == option) BrandBlue else TextDark
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
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Cloud Storage Usage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = "$totalActiveMB / 100 MB", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { storagePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrandBlue,
                            trackColor = Color(0xFFF1F5F9)
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
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
                                color = BrandBlue
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
                                        Icon(imageVector = Icons.Default.Restore, contentDescription = "Restore", tint = BrandBlue)
                                    }
                                } else {
                                    // Move to folder
                                    IconButton(onClick = { showMoveSelectedDialog = true }) {
                                        Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = "Move", tint = BrandBlue)
                                    }
                                    // Copy selected
                                    IconButton(onClick = { showCopySelectedDialog = true }) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = BrandBlue)
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
                        Text(text = "Trash (${displayedFiles.size} items)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        TextButton(onClick = { showEmptyTrashConfirmDialog = true }) {
                            Text(text = "Empty Trash", color = RedDelete, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                            color = TextDark
                        )
                        IconButton(
                            onClick = { showCreateFolderDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "Add subfolder", tint = BrandBlue, modifier = Modifier.size(18.dp))
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
                            VaultTab.RECENT -> "Recent Documents"
                            VaultTab.FAVORITES -> "Starred Documents"
                            VaultTab.TRASH -> "Trash Items"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "${displayedFiles.size} files",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Display Files List
            if (displayedFiles.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderColor)
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
                                tint = TextMuted,
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
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Use '+' to upload documents, assignments, notes or media.",
                                fontSize = 12.sp,
                                color = TextMuted,
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
                                onFileClick(file)
                            }
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
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AddOptionRow(
                        icon = Icons.Default.UploadFile,
                        title = "Upload Files from Device",
                        subtitle = "Select PDFs, documents, images, audio, or videos",
                        color = BrandBlue,
                        onClick = {
                            showAddMenuSheet = false
                            filePickerLauncher.launch(arrayOf("*/*"))
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
                        icon = Icons.Default.NoteAdd,
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
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                cursorColor = BrandBlue,
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BorderColor
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
                            Text("Lock & Encrypt Folder", fontSize = 13.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rename File Dialog
        if (renameTargetFile != null) {
            AlertDialog(
                onDismissRequest = { renameTargetFile = null },
                title = { Text("Rename File", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        label = { Text("New file name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = BrandBlue,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderColor
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetFile = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rename Folder Dialog
        if (renameTargetFolder != null) {
            AlertDialog(
                onDismissRequest = { renameTargetFolder = null },
                title = { Text("Rename Folder", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameInputText,
                        onValueChange = { renameInputText = it },
                        label = { Text("New folder name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = BrandBlue,
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderColor
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Rename")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { renameTargetFolder = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Create Note Dialog
        if (showNewNoteDialog) {
            var noteTitle by remember { mutableStateOf("") }
            var noteBody by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showNewNoteDialog = false },
                title = { Text("Create Note", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                cursorColor = BrandBlue,
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BorderColor
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
                            textStyle = androidx.compose.ui.text.TextStyle(color = TextDark),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                cursorColor = BrandBlue,
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BorderColor
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewNoteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Move Selected Files Dialog
        if (showMoveSelectedDialog) {
            var targetFolderForMove by remember { mutableStateOf(folders.firstOrNull()?.name ?: "Assignments") }
            AlertDialog(
                onDismissRequest = { showMoveSelectedDialog = false },
                title = { Text("Move ${selectedFileIds.size} Files", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Choose destination folder:", fontSize = 13.sp, color = TextMuted)
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
                                    onClick = { targetFolderForMove = folder.name }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder.name, fontSize = 14.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Move")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMoveSelectedDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Copy Selected Files Dialog
        if (showCopySelectedDialog) {
            var targetFolderForCopy by remember { mutableStateOf(folders.firstOrNull()?.name ?: "Assignments") }
            AlertDialog(
                onDismissRequest = { showCopySelectedDialog = false },
                title = { Text("Copy ${selectedFileIds.size} Files", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Choose destination folder for duplicate:", fontSize = 13.sp, color = TextMuted)
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
                                    onClick = { targetFolderForCopy = folder.name }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder.name, fontSize = 14.sp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Copy")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCopySelectedDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Move to Trash confirm dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
                text = { Text("Move ${selectedFileIds.size} files to Trash? You can restore them anytime.") },
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
                }
            )
        }

        // Empty Trash Confirm Dialog
        if (showEmptyTrashConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showEmptyTrashConfirmDialog = false },
                title = { Text("Empty Trash?", fontWeight = FontWeight.Bold) },
                text = { Text("This will permanently delete all files in Trash from both cloud storage and local device. This action cannot be undone.") },
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
                }
            )
        }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
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
                        Text(text = folder.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
                        if (folder.isSecured) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Secured", tint = GreenSecured, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(text = "${folder.count} items", fontSize = 12.sp, color = TextMuted)
                }
            }

            var showMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Folder options", tint = TextMuted, modifier = Modifier.size(18.dp))
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
    onToggleFavorite: () -> Unit,
    onRename: () -> Unit,
    onCopy: () -> Unit = {},
    onShare: () -> Unit = {},
    onTrash: () -> Unit = {},
    onRestore: () -> Unit = {},
    onDeletePermanently: () -> Unit = {}
) {
    val (icon, tint, bg) = when (file.category) {
        FileCategory.PDF -> Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), Color(0xFFFEF2F2))
        FileCategory.DOCX -> Triple(Icons.Default.Description, Color(0xFF2563EB), Color(0xFFEFF6FF))
        FileCategory.XLSX -> Triple(Icons.Default.TableChart, Color(0xFF10B981), Color(0xFFECFDF5))
        FileCategory.IMAGE -> Triple(Icons.Default.Image, Color(0xFF06B6D4), Color(0xFFECFEFF))
        FileCategory.VIDEO -> Triple(Icons.Default.Videocam, Color(0xFFEC4899), Color(0xFFFDF2F8))
        FileCategory.AUDIO -> Triple(Icons.Default.Audiotrack, Color(0xFFA855F7), Color(0xFFFAF5FF))
        FileCategory.NOTE, FileCategory.TXT -> Triple(Icons.Default.NoteAdd, Color(0xFF8B5CF6), Color(0xFFF5F3FF))
        else -> Triple(Icons.Default.Description, Color(0xFF64748B), Color(0xFFF1F5F9))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
        ),
        border = BorderStroke(1.dp, if (isSelected) BrandBlue else BorderColor)
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
                    colors = CheckboxDefaults.colors(checkedColor = BrandBlue)
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
                    color = TextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${file.sizeString} • ${file.folder}",
                        fontSize = 12.sp,
                        color = TextMuted,
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
                        tint = if (file.isFavorite) Color(0xFFF59E0B) else TextMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                var showMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Open Details") },
                            onClick = {
                                showMenu = false
                                onClick()
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
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(text = subtitle, fontSize = 12.sp, color = TextMuted)
        }
    }
}
