package com.example.ui.screens.vault

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FileCategory
import com.example.data.model.FolderSummary
import com.example.data.model.VaultItem
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentRedLight
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentTealLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GenziiBlue
import com.example.ui.theme.GenziiBlueLight
import com.example.ui.viewmodel.GenziiViewModel

@Composable
fun VaultScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    val allFiles by viewModel.vaultFiles.collectAsState()
    val searchQuery by viewModel.vaultSearchQuery.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val user by viewModel.user.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

    var showNewNoteDialog by remember { mutableStateOf(false) }
    var selectedFileForDetail by remember { mutableStateOf<VaultItem?>(null) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "Imported_Document.pdf"
            val ext = fileName.substringAfterLast(".", "PDF").uppercase()
            viewModel.saveNewNote(
                title = fileName,
                content = "Imported document from storage: $uri",
                folder = selectedFolder ?: "Assignments"
            )
            Toast.makeText(context, "Added $fileName to Vault", Toast.LENGTH_SHORT).show()
        }
    }

    val folders = listOf(
        FolderSummary("Assignments", 12, 0xFF3B82F6),
        FolderSummary("Research", 18, 0xFF8B5CF6),
        FolderSummary("Notes", 8, 0xFF10B981),
        FolderSummary("Personal", 5, 0xFFF59E0B)
    )

    val filteredFiles = allFiles.filter { file ->
        val matchesSearch = searchQuery.isBlank() || file.name.contains(searchQuery, ignoreCase = true)
        val matchesFolder = selectedFolder == null || file.folder.equals(selectedFolder, ignoreCase = true)
        matchesSearch && matchesFolder
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // TOP BAR matching Screen 14
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Vault",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload Document",
                            tint = GenziiBlue
                        )
                    }
                }
            }

            item {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setVaultSearchQuery(it) },
                    placeholder = { Text("Search files...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = GenziiBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            item {
                // Storage Bar Indicator matching Screen 14
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Storage",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${user.storageUsedGb} GB of ${user.storageTotalGb.toInt()} GB used",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (user.storageUsedGb / user.storageTotalGb).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = GenziiBlue,
                            trackColor = GenziiBlueLight
                        )

                        if (!isPro) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Upgrade for 50 GB ->",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GenziiBlue,
                                    modifier = Modifier.clickable { onNavigateToUpgrade() }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Folders Horizontal List matching Screen 14
                Text(
                    text = "Folders",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        // "All" folder chip
                        Card(
                            modifier = Modifier.clickable { viewModel.setSelectedFolder(null) },
                            shape = RoundedCornerShape(14.dp),
                            border = if (selectedFolder == null) null else BorderStroke(1.dp, BorderLight),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFolder == null) GenziiBlue else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (selectedFolder == null) Color.White else GenziiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All Files",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selectedFolder == null) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    items(folders) { folder ->
                        val isSelected = selectedFolder.equals(folder.name, ignoreCase = true)
                        Card(
                            modifier = Modifier.clickable {
                                viewModel.setSelectedFolder(if (isSelected) null else folder.name)
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) null else BorderStroke(1.dp, BorderLight),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GenziiBlue else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(folder.iconColor),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = folder.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Files (${filteredFiles.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(filteredFiles) { file ->
                VaultFileItemCard(
                    file = file,
                    onOpen = { selectedFileForDetail = file },
                    onDelete = { viewModel.setCandidateForDeletion(file) },
                    onToggleFavorite = { viewModel.toggleFavoriteFile(file) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // FAB to Add Note / Document
        FloatingActionButton(
            onClick = { showNewNoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp),
            containerColor = GenziiBlue,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Document",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // New Note Dialog
    if (showNewNoteDialog) {
        NewNoteDialog(
            onDismiss = { showNewNoteDialog = false },
            onSave = { title, content ->
                viewModel.saveNewNote(title, content, selectedFolder ?: "Notes")
                showNewNoteDialog = false
                Toast.makeText(context, "Note saved to Vault", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // File Details Dialog
    if (selectedFileForDetail != null) {
        FileDetailsDialog(
            file = selectedFileForDetail!!,
            onDismiss = { selectedFileForDetail = null }
        )
    }
}

@Composable
fun VaultFileItemCard(
    file: VaultItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Colored file badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(file.category.colorHex).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = file.category.extension,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(file.category.colorHex)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = file.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (file.isCachedOffline) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AccentGreen.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Offline",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AccentGreen
                                )
                            }
                        }
                    }
                    Text(
                        text = "${file.sizeString} • ${file.timeAgo} • ${file.folder}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (file.isFavorite) "Favorited" else "Favorite",
                        tint = if (file.isFavorite) AccentOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open Details") },
                            onClick = {
                                showMenu = false
                                onOpen()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (file.isFavorite) "Remove from Favorites" else "Mark as Favorite") },
                            onClick = {
                                showMenu = false
                                onToggleFavorite()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete File", color = AccentRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewNoteDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "New Academic Note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Note Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("Write research notes, lecture highlights, or ideas...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                onSave(title.ifBlank { "Untitled Note" }, content)
                            }
                        }
                    ) {
                        Text("Save Note", fontWeight = FontWeight.Bold, color = GenziiBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun FileDetailsDialog(
    file: VaultItem,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(file.category.colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = file.category.extension,
                            fontWeight = FontWeight.Bold,
                            color = Color(file.category.colorHex)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = file.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Folder: ${file.folder}\nSize: ${file.sizeString}\nLast modified: ${file.timeAgo}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                if (file.content.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = file.content,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
