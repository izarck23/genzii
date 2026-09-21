package com.example.ui.screens

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import com.example.ui.components.AdMobManager
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OriginalityReport
import com.example.data.service.StorageFileService
import com.example.util.NetworkUtils
import com.example.util.PdfReportGenerator
import com.example.util.rememberNetworkAvailable
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF005AC1)
private val GreenAccent = Color(0xFF10B981)
private val AmberAccent = Color(0xFFF59E0B)
private val RedAccent = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalityCheckerScreen(
    reports: List<OriginalityReport> = emptyList(),
    isChecking: Boolean = false,
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onRunCheck: (title: String, content: String) -> Unit,
    onOpenReport: (reportId: String) -> Unit = {},
    onDeleteReport: (String) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isOnline by rememberNetworkAvailable(context)

    var content by remember { mutableStateOf("") }
    var documentTitle by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var isExtractingFile by remember { mutableStateOf(false) }

    // Result Preview Sheet state
    var previewReport by remember { mutableStateOf<OriginalityReport?>(null) }
    var showPreviewSheet by remember { mutableStateOf(false) }
    var showReportsHistorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split(Regex("\\s+")).size
    }
    val charCount = content.length

    // System Document Picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isExtractingFile = true
            scope.launch {
                try {
                    val fileName = getFileNameFromUri(context, uri)
                    val extractedText = extractTextFromUri(context, uri)
                    uploadedFileName = fileName
                    documentTitle = fileName.substringBeforeLast(".")
                    content = extractedText
                    Toast.makeText(context, "Loaded $fileName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read document: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isExtractingFile = false
                }
            }
        }
    }

    val scrollState = rememberScrollState()
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // Top Navigation & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(surface)
                    .testTag("originality_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Originality Checker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Check your content for matching or similar sources.",
                    fontSize = 12.sp,
                    color = textMuted
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showReportsHistorySheet = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Reports History",
                        tint = textDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Connection Status Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOnline) GreenAccent.copy(alpha = 0.12f) else AmberAccent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (isOnline) GreenAccent.copy(alpha = 0.4f) else AmberAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) GreenAccent else AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOnline) GreenAccent else AmberAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Honest Error or Offline State Card
        AnimatedVisibility(visible = !isOnline || errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("originality_error_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFECACA))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (!isOnline) Icons.Default.WifiOff else Icons.Default.WarningAmber,
                        contentDescription = "Alert",
                        tint = RedAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (!isOnline) "Active Internet Required" else "Originality Verification Notice",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = errorMessage
                                ?: "Originality checking is online-only. An active internet connection is required to query academic repositories and search indexes. Please check your connection and retry.",
                            fontSize = 12.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 16.sp
                        )
                    }
                    if (errorMessage != null) {
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = RedAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Controls Row: Upload | Paste | Sample
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upload Button
            OutlinedButton(
                onClick = {
                    documentPickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "text/*",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/rtf"
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("originality_upload_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = surface)
            ) {
                if (isExtractingFile) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BrandBlue
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Upload",
                        tint = BrandBlue,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExtractingFile) "Reading..." else "Upload",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            }

            // Paste Button
            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    val clipData = clipboard?.primaryClip
                    if (clipData != null && clipData.itemCount > 0) {
                        val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                        if (pasteText.isNotBlank()) {
                            content = pasteText
                            Toast.makeText(context, "Pasted ${pasteText.length} characters", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("originality_paste_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = surface)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    tint = textDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Paste",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textDark
                )
            }

            // Sample Button
            OutlinedButton(
                onClick = {
                    documentTitle = "Cognitive Learning & Academic Synthesis"
                    uploadedFileName = "sample_academic_paper.pdf"
                    content = """Automated digital learning platforms have transformed university pedagogical inquiry. Modern empirical evaluations demonstrate that cognitive scaffolds increase student engagement and concept retention. However, recent scholarly publications indicate that digital learning requires active mentorship to preserve student autonomy. Critical analysis and ethical synthesis of source literature remain indispensable foundations of authentic scholarship."""
                    Toast.makeText(context, "Loaded sample text", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("originality_sample_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = surface)
            ) {
                Text(
                    text = "Sample",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textDark
                )
            }
        }

        // Uploaded File Chip (if file was selected)
        if (uploadedFileName != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = surface,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "File",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uploadedFileName ?: "Document",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            uploadedFileName = null
                            documentTitle = ""
                            content = ""
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove file",
                            tint = textMuted,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text Area Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("originality_text_area"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = textDark, fontSize = 14.sp),
                    placeholder = {
                        Text(
                            text = "Type or paste your text here to check for matching or similar sources...",
                            color = textMuted,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textDark,
                        unfocusedTextColor = textDark,
                        cursorColor = BrandBlue,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                HorizontalDivider(color = borderColor.copy(alpha = 0.4f), thickness = 0.8.dp)

                Spacer(modifier = Modifier.height(8.dp))

                // Word / Character Count Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$wordCount words • $charCount characters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (wordCount > 5000) RedAccent else textMuted
                    )

                    if (content.isNotBlank()) {
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            color = BrandBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable {
                                    content = ""
                                    uploadedFileName = null
                                    documentTitle = ""
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Check Originality Button
        Button(
            onClick = {
                if (!isOnline) {
                    Toast.makeText(
                        context,
                        "Originality checking requires an active internet connection.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@Button
                }

                val titleToUse = if (documentTitle.isNotBlank()) {
                    documentTitle
                } else if (uploadedFileName != null) {
                    uploadedFileName!!.substringBeforeLast(".")
                } else {
                    "Academic Text Originality Analysis"
                }

                val finalContent = content.ifBlank {
                    "Automated digital learning platforms have transformed university pedagogical inquiry. Modern empirical evaluations demonstrate that cognitive scaffolds increase student engagement and concept retention."
                }

                onRunCheck(titleToUse, finalContent)
            },
            enabled = !isChecking && !isExtractingFile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("check_originality_button"),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
        ) {
            if (isChecking) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Checking Originality...",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check Originality",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Rewarded Ad Card: Earn Bonus AI Credits
        var isWatchingRewardAd by remember { mutableStateOf(false) }
        val activity = context as? Activity

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rewarded_ad_card"),
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BrandBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bonus AI Credits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = textDark
                    )
                    Text(
                        text = "Watch short ad to get +500 tokens",
                        fontSize = 11.5.sp,
                        color = textMuted
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (activity != null && !isWatchingRewardAd) {
                            isWatchingRewardAd = true
                            AdMobManager.showRewarded(
                                activity = activity,
                                onRewardEarned = { amount, _ ->
                                    isWatchingRewardAd = false
                                    Toast.makeText(context, "🎉 +500 Bonus AI Credits added to your account!", Toast.LENGTH_LONG).show()
                                },
                                onDismissed = {
                                    isWatchingRewardAd = false
                                },
                                onError = { msg ->
                                    isWatchingRewardAd = false
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    },
                    enabled = !isWatchingRewardAd,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (isWatchingRewardAd) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Earn +500", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Result Preview & Download Result as PDF Section
        val latestReport = reports.firstOrNull()
        if (latestReport != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("latest_result_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (latestReport.score >= 75) GreenAccent.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${latestReport.score}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (latestReport.score >= 75) GreenAccent else AmberAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Result Preview: ${latestReport.title}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${latestReport.similarityPercentage}% matching similarity • ${latestReport.wordCount} words",
                                fontSize = 11.5.sp,
                                color = textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Result Preview Button
                        OutlinedButton(
                            onClick = {
                                previewReport = latestReport
                                showPreviewSheet = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("result_preview_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = BrandBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Result Preview",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlue
                            )
                        }

                        // Download Result as PDF Button
                        Button(
                            onClick = {
                                val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, latestReport)
                                if (pdfFile != null) {
                                    PdfReportGenerator.openOrSharePdf(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Could not create PDF report", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(40.dp)
                                .testTag("download_pdf_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Download PDF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Accuracy & Integrity Notice Card (Prominent & Honest)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surface,
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Notice",
                    tint = textMuted,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Notice on Accuracy & Service Guarantees",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Originality similarity analysis provides statistical and text-matching indicators based on indexed sources. It is not an absolute guarantee or 100% proof of originality or plagiarism.",
                        fontSize = 11.sp,
                        color = textMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // History Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Verification History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
                if (reports.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = BrandBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${reports.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (reports.size > 3) {
                Text(
                    text = "View All (${reports.size})",
                    fontSize = 12.sp,
                    color = BrandBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showReportsHistorySheet = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (reports.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = textMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Previous Reports",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Run an originality check above to inspect sources, similarity scores, and export PDF results.",
                        fontSize = 11.5.sp,
                        color = textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reports.take(4).forEach { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReport(report.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (report.score >= 75) GreenAccent.copy(alpha = 0.15f)
                                            else AmberAccent.copy(alpha = 0.15f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${report.score}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (report.score >= 75) GreenAccent else AmberAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = report.title,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${report.dateString} • ${report.similarityPercentage}% match • ${report.wordCount} words",
                                        fontSize = 11.sp,
                                        color = textMuted
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        onDeleteReport(report.id)
                                        Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Report",
                                        tint = textMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onOpenReport(report.id) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("View Analysis", fontSize = 11.5.sp, color = BrandBlue, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, report)
                                        if (pdfFile != null) {
                                            PdfReportGenerator.openOrSharePdf(context, pdfFile)
                                        } else {
                                            Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal BottomSheet for Result Preview
    if (showPreviewSheet && previewReport != null) {
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = surface
        ) {
            val rep = previewReport!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Result Preview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                        Text(
                            text = rep.title,
                            fontSize = 12.sp,
                            color = textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { showPreviewSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Score metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = if (rep.score >= 75) GreenAccent.copy(alpha = 0.1f) else AmberAccent.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Originality", fontSize = 11.sp, color = textMuted)
                            Text("${rep.score}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (rep.score >= 75) GreenAccent else AmberAccent)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = if (rep.similarityPercentage <= 20) GreenAccent.copy(alpha = 0.1f) else RedAccent.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Similarity", fontSize = 11.sp, color = textMuted)
                            Text("${rep.similarityPercentage}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (rep.similarityPercentage <= 20) GreenAccent else RedAccent)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Unique", fontSize = 11.sp, color = textMuted)
                            Text("${rep.uniquePercentage}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Evidence-Based Matching Sources
                Text(
                    text = "Evidence-Based Matching Sources",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (rep.sources.isEmpty()) {
                    Text(
                        text = "No matching external academic publications detected above the confidence threshold.",
                        fontSize = 12.sp,
                        color = textMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rep.sources.take(3).forEach { source ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bg,
                                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = source.title,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${source.domain} • ${source.similarityPct}% match",
                                            fontSize = 11.sp,
                                            color = BrandBlue
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                                context.startActivity(browserIntent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = "Open",
                                            tint = textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Matched Excerpt Evidence
                if (rep.highlights.isNotEmpty() || rep.repeatedSentences.isNotEmpty()) {
                    Text(
                        text = "Matched Excerpt Evidence",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val excerpt = rep.highlights.firstOrNull()?.matchedText
                        ?: rep.repeatedSentences.firstOrNull() ?: ""

                    if (excerpt.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "\"$excerpt\"",
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Evidence: High semantic similarity with indexed academic corpus.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Modal Action Row: Download PDF | View Full Report
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, rep)
                            if (pdfFile != null) {
                                PdfReportGenerator.openOrSharePdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            showPreviewSheet = false
                            onOpenReport(rep.id)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(23.dp),
                        border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f))
                    ) {
                        Text("Full Report", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Reports History ModalBottomSheet
    if (showReportsHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showReportsHistorySheet = false },
            containerColor = surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BrandBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = BrandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Verification History",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = "${reports.size} academic verification reports",
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                    }

                    IconButton(onClick = { showReportsHistorySheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (reports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = textMuted.copy(alpha = 0.4f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No Verification Reports",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Run a check to see detailed originality indices and source breakdowns.",
                                fontSize = 12.sp,
                                color = textMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(reports, key = { it.id }) { report ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showReportsHistorySheet = false
                                        onOpenReport(report.id)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (report.score >= 75) GreenAccent.copy(alpha = 0.15f)
                                                else AmberAccent.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${report.score}%",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (report.score >= 75) GreenAccent else AmberAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = report.title,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${report.dateString} • ${report.similarityPercentage}% similarity",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, report)
                                            if (pdfFile != null) {
                                                PdfReportGenerator.openOrSharePdf(context, pdfFile)
                                            }
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "PDF",
                                            tint = BrandBlue,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteReport(report.id)
                                            Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = textMuted,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "Document"
    val returnCursor = context.contentResolver.query(uri, null, null, null, null)
    returnCursor?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}

private suspend fun extractTextFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val text = inputStream.bufferedReader().use { it.readText() }
            text.replace("\u0000", "").trim()
        } ?: ""
    } catch (_: Exception) {
        ""
    }
}

