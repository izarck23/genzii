package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.TableChart
import com.example.data.model.FileCategory
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.service.StorageFileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandBlue = Color(0xFF005AC1)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val RedDelete = Color(0xFFEF4444)
private val GreenSecured = Color(0xFF10B981)

@Composable
fun DocumentPreviewScreen(
    file: VaultItem?,
    availableFolders: List<VaultFolder> = emptyList(),
    onBackClick: () -> Unit,
    onDeleteClick: (VaultItem) -> Unit,
    onToggleSecure: (VaultItem) -> Unit = {},
    onToggleFavorite: (VaultItem) -> Unit = {},
    onRenameFile: (VaultItem, String) -> Unit = { _, _ -> },
    onMoveFile: (VaultItem, String) -> Unit = { _, _ -> },
    onCopyFile: (VaultItem) -> Unit = {},
    onDownloadFile: (VaultItem) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showMoveDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember(file) { mutableStateOf(file?.name ?: "") }
    var selectedFolderForMove by remember(file) { mutableStateOf(file?.folder ?: "Assignments") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val fileName = file?.name ?: "Document"
    val fileSize = file?.sizeString ?: "Unknown size"
    val isSecured = file?.isSecured ?: false
    val isFavorite = file?.isFavorite ?: false

    val hasLocalFile = remember(file) {
        if (file == null || file.localFilePath.isBlank()) false
        else if (file.localFilePath.startsWith("content://") || file.localFilePath.startsWith("http://") || file.localFilePath.startsWith("https://")) true
        else File(file.localFilePath).exists()
    }

    val (badgeIcon, badgeColor, badgeBg) = when (file?.category) {
        FileCategory.PDF -> Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), Color(0xFFFEF2F2))
        FileCategory.DOCX -> Triple(Icons.Default.Description, Color(0xFF2563EB), Color(0xFFEFF6FF))
        FileCategory.XLSX -> Triple(Icons.Default.Description, Color(0xFF10B981), Color(0xFFECFDF5))
        FileCategory.PPTX -> Triple(Icons.Default.Description, Color(0xFFF97316), Color(0xFFFFF7ED))
        FileCategory.IMAGE -> Triple(Icons.Default.Image, Color(0xFF06B6D4), Color(0xFFECFEFF))
        FileCategory.VIDEO -> Triple(Icons.Default.VideoFile, Color(0xFFEC4899), Color(0xFFFDF2F8))
        FileCategory.AUDIO -> Triple(Icons.Default.AudioFile, Color(0xFFA855F7), Color(0xFFFAF5FF))
        FileCategory.TXT, FileCategory.NOTE -> Triple(Icons.Default.Edit, Color(0xFF8B5CF6), Color(0xFFF5F3FF))
        else -> Triple(Icons.Default.Description, Color(0xFF64748B), Color(0xFFF1F5F9))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextDark
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Document Details",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    letterSpacing = (-0.4).sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Favorite toggle
                IconButton(
                    onClick = { file?.let { onToggleFavorite(it) } },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFF59E0B) else TextMuted
                    )
                }

                // Secure status pill
                if (isSecured) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secured",
                                tint = GreenSecured,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Secured",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSecured
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic In-App Viewer Section
        if (file != null) {
            when {
                // 1. PDF Viewer (Paginated via PdfRenderer)
                file.category == FileCategory.PDF && hasLocalFile -> {
                    PdfPaginatedViewer(localFilePath = file.localFilePath)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Image Fullscreen & Zoom Viewer
                file.category == FileCategory.IMAGE && hasLocalFile -> {
                    ImageInteractiveViewer(localFilePath = file.localFilePath, fileName = file.name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Video Player with Controls
                file.category == FileCategory.VIDEO && hasLocalFile -> {
                    VideoPlayerView(localFilePath = file.localFilePath)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Audio Player with Controls
                file.category == FileCategory.AUDIO && hasLocalFile -> {
                    AudioPlayerView(localFilePath = file.localFilePath, trackName = file.name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5. Word Document Viewer (DOCX)
                file.category == FileCategory.DOCX -> {
                    DocxReaderViewer(filePath = file.localFilePath, content = file.content, fileName = file.name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 6. Spreadsheet Viewer (XLSX / CSV)
                (file.category == FileCategory.XLSX || file.mimeType.contains("sheet") || file.mimeType.contains("csv")) -> {
                    XlsxTableViewerView(filePath = file.localFilePath, content = file.content, fileName = file.name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 7. Text & Note Viewer / Editor
                (file.category == FileCategory.TXT || file.category == FileCategory.NOTE || file.content.isNotBlank()) -> {
                    val textContent = if (hasLocalFile && (file.category == FileCategory.TXT || file.mimeType.startsWith("text/"))) {
                        try {
                            if (file.localFilePath.startsWith("content://")) {
                                context.contentResolver.openInputStream(Uri.parse(file.localFilePath))?.bufferedReader()?.use { it.readText() } ?: file.content
                            } else {
                                File(file.localFilePath).readText()
                            }
                        } catch (e: Exception) { file.content }
                    } else {
                        file.content
                    }
                    TextReaderViewer(content = textContent, fileName = file.name)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Unsupported or Cloud-Only: Header badge
                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(badgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = file.category.name,
                                    tint = badgeColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = fileName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$fileSize • Folder: ${file.folder}",
                                fontSize = 13.sp,
                                color = TextMuted
                            )

                            if (!hasLocalFile && file.storagePath.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onDownloadFile(file) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                                ) {
                                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Download from Cloud to View", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Action Buttons Row: Open with App | Secure | Share | Move | Rename
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleActionButton(
                icon = Icons.Default.OpenInNew,
                label = "Open With",
                tint = BrandBlue,
                onClick = {
                    if (file != null && hasLocalFile) {
                        StorageFileService.openFileWithSystemViewer(
                            context = context,
                            filePath = file.localFilePath,
                            mimeType = file.mimeType
                        )
                    } else if (file != null && file.downloadUrl.isNotBlank()) {
                        // Open cloud url in browser
                        try {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(file.downloadUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "File is not stored locally yet", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            CircleActionButton(
                icon = if (isSecured) Icons.Default.LockOpen else Icons.Default.Lock,
                label = if (isSecured) "Unlock" else "Secure",
                tint = if (isSecured) GreenSecured else BrandBlue,
                onClick = { file?.let { onToggleSecure(it) } }
            )

            CircleActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = BrandBlue,
                onClick = {
                    if (file != null && hasLocalFile) {
                        StorageFileService.shareFile(
                            context = context,
                            filePath = file.localFilePath,
                            mimeType = file.mimeType,
                            title = file.name
                        )
                    } else if (file != null && file.downloadUrl.isNotBlank()) {
                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, "File download link: ${file.downloadUrl}")
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share file link"))
                    }
                }
            )

            CircleActionButton(
                icon = Icons.Default.DriveFileMove,
                label = "Move",
                tint = BrandBlue,
                onClick = { showMoveDialog = true }
            )

            CircleActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                tint = BrandBlue,
                onClick = {
                    file?.let {
                        onCopyFile(it)
                        Toast.makeText(context, "Created copy of ${it.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            CircleActionButton(
                icon = Icons.Default.Edit,
                label = "Rename",
                tint = BrandBlue,
                onClick = {
                    renameInput = file?.name ?: ""
                    showRenameDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Document Details Card
        Text(
            text = "Document Details",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                DetailRow(label = "Format", value = file?.category?.extension ?: "Unknown")
                HorizontalDivider(color = Color(0xFFF1F5F9))
                DetailRow(label = "MIME Type", value = file?.mimeType ?: "application/octet-stream")
                HorizontalDivider(color = Color(0xFFF1F5F9))
                DetailRow(label = "Size", value = fileSize)
                HorizontalDivider(color = Color(0xFFF1F5F9))
                DetailRow(label = "Folder", value = file?.folder ?: "Assignments")
                HorizontalDivider(color = Color(0xFFF1F5F9))
                DetailRow(label = "Storage", value = if (file?.storagePath?.isNotBlank() == true) "Cloud & Local" else "Local Cache")
                HorizontalDivider(color = Color(0xFFF1F5F9))
                DetailRow(label = "Security", value = if (isSecured) "Encrypted & Secured" else "Standard")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Destructive / Trash Option
        Text(
            text = "Actions",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OptionRow(
                    icon = Icons.Default.Delete,
                    label = "Move to Trash",
                    isDestructive = true,
                    onClick = { showDeleteConfirmDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Rename Dialog
    if (showRenameDialog && file != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename File", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("File Name") },
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
                        val trimmed = renameInput.trim()
                        if (trimmed.isNotBlank()) {
                            onRenameFile(file, trimmed)
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move to folder dialog
    if (showMoveDialog && file != null) {
        val targetFolders = availableFolders.filter { it.name != "All Files" }
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move to Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select destination folder for \"${file.name}\":", fontSize = 13.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    targetFolders.forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFolderForMove = folder.name }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFolderForMove == folder.name,
                                onClick = { selectedFolderForMove = folder.name }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(folder.name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMoveFile(file, selectedFolderForMove)
                        showMoveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Move")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Move to Trash confirm dialog
    if (showDeleteConfirmDialog && file != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Move to Trash?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to move \"${file.name}\" to Trash? You can restore it later from Trash.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteClick(file)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDelete)
                ) {
                    Text("Move to Trash")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ------------------------------------------------------------------------------------------------
// 1. PDF Paginated Viewer Component (Using Android PdfRenderer)
// ------------------------------------------------------------------------------------------------
@Composable
private fun PdfPaginatedViewer(localFilePath: String) {
    val context = LocalContext.current
    var totalPages by remember { mutableIntStateOf(0) }
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var isError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(localFilePath, currentPageIndex) {
        withContext(Dispatchers.IO) {
            try {
                val pfd = if (localFilePath.startsWith("content://")) {
                    context.contentResolver.openFileDescriptor(Uri.parse(localFilePath), "r")
                } else {
                    val file = File(localFilePath)
                    if (!file.exists()) null else ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                }
                if (pfd == null) {
                    isError = true
                    return@withContext
                }
                val renderer = PdfRenderer(pfd)
                totalPages = renderer.pageCount
                val validIndex = currentPageIndex.coerceIn(0, totalPages - 1)

                val page = renderer.openPage(validIndex)
                val width = page.width * 2
                val height = page.height * 2
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                renderedBitmap = bitmap
                isError = false
            } catch (e: Exception) {
                isError = true
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header bar with Pagination and Zoom controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (totalPages > 0) "Page ${currentPageIndex + 1} of $totalPages" else "PDF Document",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom out", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom in", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // PDF Page Rendering Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (renderedBitmap != null && !isError) {
                    Image(
                        bitmap = renderedBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(scaleX = zoomScale, scaleY = zoomScale)
                    )
                } else if (isError) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Unable to render PDF page inline.", fontSize = 13.sp, color = TextMuted)
                        Text("Use 'Open With' below for complete PDF viewer.", fontSize = 12.sp, color = BrandBlue)
                    }
                } else {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), color = BrandBlue)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prev / Next Page Navigation Buttons
            if (totalPages > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                        enabled = currentPageIndex > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.NavigateBefore, contentDescription = null)
                        Text("Previous")
                    }

                    Text(
                        text = "${currentPageIndex + 1} / $totalPages",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Button(
                        onClick = { if (currentPageIndex < totalPages - 1) currentPageIndex++ },
                        enabled = currentPageIndex < totalPages - 1,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("Next")
                        Icon(imageVector = Icons.Default.NavigateNext, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 2. Interactive Image Viewer (Pinch-to-zoom & Pan)
// ------------------------------------------------------------------------------------------------
@Composable
private fun ImageInteractiveViewer(localFilePath: String, fileName: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Interactive Image Viewer", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                if (scale != 1f || offsetX != 0f || offsetY != 0f) {
                    TextButton(onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }) {
                        Text("Reset Zoom", fontSize = 12.sp, color = BrandBlue)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val imageModel = remember(localFilePath) {
                if (localFilePath.startsWith("content://") || localFilePath.startsWith("http://") || localFilePath.startsWith("https://")) {
                    Uri.parse(localFilePath)
                } else {
                    File(localFilePath)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.04f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = fileName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pinch to zoom in/out • Drag to pan across image",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 3. Native Video Player Component
// ------------------------------------------------------------------------------------------------
@Composable
private fun VideoPlayerView(localFilePath: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            if (localFilePath.startsWith("content://") || localFilePath.startsWith("http://") || localFilePath.startsWith("https://")) {
                                setVideoURI(Uri.parse(localFilePath))
                            } else {
                                setVideoPath(localFilePath)
                            }
                            val mediaController = MediaController(ctx)
                            mediaController.setAnchorView(this)
                            setMediaController(mediaController)
                            setOnPreparedListener { mp ->
                                mp.isLooping = false
                                start()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Video Playback with System Controls", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 4. Native Audio Player Component
// ------------------------------------------------------------------------------------------------
@Composable
private fun AudioPlayerView(localFilePath: String, trackName: String) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(localFilePath) {
        val player = MediaPlayer().apply {
            try {
                if (localFilePath.startsWith("content://") || localFilePath.startsWith("http://") || localFilePath.startsWith("https://")) {
                    setDataSource(context, Uri.parse(localFilePath))
                } else {
                    setDataSource(localFilePath)
                }
                prepare()
                duration = this.duration.coerceAtLeast(1)
            } catch (e: Exception) {
                // Ignore prepare errors
            }
            setOnCompletionListener {
                isPlaying = false
                currentPosition = 0
            }
        }
        mediaPlayer = player

        onDispose {
            player.stop()
            player.release()
            mediaPlayer = null
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    currentPosition = it.currentPosition
                }
            }
            delay(500)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAF5FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AudioFile, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = trackName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = "Audio Track", fontSize = 12.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Seekbar Slider
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { newPos ->
                    currentPosition = newPos.toInt()
                    mediaPlayer?.seekTo(newPos.toInt())
                },
                valueRange = 0f..duration.toFloat(),
                colors = SliderDefaults.colors(thumbColor = BrandBlue, activeTrackColor = BrandBlue)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatDuration(currentPosition.toLong()), fontSize = 12.sp, color = TextMuted)
                Text(text = formatDuration(duration.toLong()), fontSize = 12.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Player Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val back10 = (currentPosition - 10000).coerceAtLeast(0)
                    mediaPlayer?.seekTo(back10)
                    currentPosition = back10
                }) {
                    Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Rewind 10s", tint = TextDark)
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        mediaPlayer?.let { player ->
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.start()
                                isPlaying = true
                            }
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BrandBlue)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(onClick = {
                    val forward10 = (currentPosition + 10000).coerceAtMost(duration)
                    mediaPlayer?.seekTo(forward10)
                    currentPosition = forward10
                }) {
                    Icon(imageVector = Icons.Default.FastForward, contentDescription = "Fast Forward 10s", tint = TextDark)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 5. Formatted Text & Note Reader Viewer
// ------------------------------------------------------------------------------------------------
@Composable
private fun TextReaderViewer(content: String, fileName: String) {
    val context = LocalContext.current
    var fontSizeSp by remember { mutableIntStateOf(14) }
    val lines = remember(content) { content.lines() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header with font size adjustment and copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Text Reader", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp - 2).coerceAtLeast(11) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    }
                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp + 2).coerceAtMost(22) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("File content", content))
                            Toast.makeText(context, "Copied content to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Line-numbered formatted reading panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp)
            ) {
                Column {
                    lines.forEachIndexed { index, line ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                            Text(
                                text = "${index + 1}".padStart(3, ' '),
                                fontSize = (fontSizeSp - 2).sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.width(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = line.ifEmpty { " " },
                                fontSize = fontSizeSp.sp,
                                fontFamily = FontFamily.SansSerif,
                                color = TextDark,
                                lineHeight = (fontSizeSp + 6).sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${lines.size} lines • ${content.split(Regex("\\s+")).filter { it.isNotBlank() }.size} words • ${content.length} characters",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// Helper Components & Formatters
// ------------------------------------------------------------------------------------------------
@Composable
private fun CircleActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isDestructive) RedDelete else BrandBlue,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) RedDelete else TextDark
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

// ------------------------------------------------------------------------------------------------
// 6. Word DOCX Document Viewer
// ------------------------------------------------------------------------------------------------
@Composable
private fun DocxReaderViewer(
    filePath: String,
    content: String,
    fileName: String
) {
    val context = LocalContext.current
    var docxText by remember { mutableStateOf(content) }
    var isLoading by remember { mutableStateOf(true) }
    var fontSizeSp by remember { mutableFloatStateOf(14f) }

    LaunchedEffect(filePath) {
        if (filePath.isNotBlank()) {
            isLoading = true
            val extracted = StorageFileService.extractDocxText(context, filePath)
            docxText = if (extracted.isNotBlank()) extracted else content.ifBlank { "No text content found in document." }
            isLoading = false
        } else {
            docxText = content.ifBlank { "No document content available." }
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Word Document Viewer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(10f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    }
                    IconButton(
                        onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(24f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("A+", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                    IconButton(
                        onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("DOCX Content", docxText))
                            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFF2563EB), strokeWidth = 2.5.dp)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 340.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = docxText,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 1.5f).sp,
                        color = TextDark,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            val wordCount = docxText.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            Text(
                text = "$wordCount words • In-app DOCX reader",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 7. Spreadsheet XLSX Table Viewer
// ------------------------------------------------------------------------------------------------
@Composable
private fun XlsxTableViewerView(
    filePath: String,
    content: String,
    fileName: String
) {
    val context = LocalContext.current
    var rows by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(filePath) {
        if (filePath.isNotBlank()) {
            isLoading = true
            val extracted = StorageFileService.extractXlsxRows(context, filePath)
            if (extracted.isNotEmpty()) {
                rows = extracted
            } else if (content.isNotBlank()) {
                // Parse CSV style lines if plain content
                rows = content.lines().map { it.split(",").map { cell -> cell.trim() } }
            }
            isLoading = false
        } else if (content.isNotBlank()) {
            rows = content.lines().map { it.split(",").map { cell -> cell.trim() } }
            isLoading = false
        } else {
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            // Header bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Spreadsheet Table Viewer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }

                Text(
                    text = "${rows.size} rows",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), color = Color(0xFF10B981), strokeWidth = 2.5.dp)
                }
            } else if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tabular data found in spreadsheet.", fontSize = 13.sp, color = TextMuted)
                }
            } else {
                val horizontalScrollState = rememberScrollState()
                val verticalScrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                ) {
                    Column {
                        rows.forEachIndexed { rowIndex, row ->
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (rowIndex == 0) Color(0xFFE2E8F0)
                                        else if (rowIndex % 2 == 1) Color.White
                                        else Color(0xFFF1F5F9)
                                    )
                                    .padding(vertical = 4.dp)
                            ) {
                                // Row number indicator
                                Text(
                                    text = "${rowIndex + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                    modifier = Modifier
                                        .width(36.dp)
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center
                                )

                                row.forEach { cellText ->
                                    Text(
                                        text = cellText,
                                        fontSize = 12.sp,
                                        fontWeight = if (rowIndex == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (rowIndex == 0) TextDark else Color(0xFF334155),
                                        modifier = Modifier
                                            .width(130.dp)
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Scroll horizontally and vertically to explore cells",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
