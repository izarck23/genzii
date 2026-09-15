package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OriginalityReport
import com.example.data.service.StorageFileService
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF005AC1)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)

@Composable
fun OriginalityCheckerScreen(
    reports: List<OriginalityReport>,
    isChecking: Boolean,
    onRunCheck: (title: String, content: String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var content by remember { mutableStateOf("") }
    var documentTitle by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var isExtractingFile by remember { mutableStateOf(false) }

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split(Regex("\\s+")).size
    }
    val charCount = content.length
    val scrollState = rememberScrollState()

    // Document Picker Launcher (PDF, DOCX, TXT, RTF, MD)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isExtractingFile = true
            coroutineScope.launch {
                try {
                    val (fileName, extractedText) = StorageFileService.extractTextFromDocumentUri(context, uri)
                    uploadedFileName = fileName
                    documentTitle = fileName.substringBeforeLast(".")
                    if (extractedText.isNotBlank()) {
                        content = extractedText
                        Toast.makeText(context, "Loaded text from $fileName", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Document selected: $fileName", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read document: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                } finally {
                    isExtractingFile = false
                }
            }
        }
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

        // Top Bar: < Originality Checker
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .testTag("originality_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Originality Checker",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "Cross-reference with open-access academic databases",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Open-Access Academic Databases badge powered by Gemini
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BrandBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Academic Repositories",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open-Access Academic Repositories",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini API",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = "Cross-references arXiv, PubMed Central, DOAJ, PLOS ONE, and IEEE Open Access via Gemini API.",
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Action Row: Upload Document | Paste Clipboard | Sample Draft
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upload Document Button
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
                    .weight(1.1f)
                    .height(44.dp)
                    .testTag("upload_document_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
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
                        contentDescription = "Upload Document",
                        tint = BrandBlue,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExtractingFile) "Reading..." else "Upload Doc",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlue
                )
            }

            // Paste Clipboard Button
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
                    .weight(0.95f)
                    .height(44.dp)
                    .testTag("paste_clipboard_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    tint = TextDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Paste",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            // Sample Paper Button
            OutlinedButton(
                onClick = {
                    documentTitle = "Machine Learning in Academic Pedagogy"
                    uploadedFileName = "academic_pedagogy_draft.pdf"
                    content = """The integration of automated digital learning platforms has transformed higher education research. Modern empirical inquiry leverages cognitive learning metrics to evaluate student retention and concept acquisition. However, recent scholarly studies in arXiv and PubMed Central indicate that digital scaffolding requires balanced pedagogical guidance to optimize intellectual autonomy. Furthermore, ethical synthesis of source literature remains essential for authentic academic scholarship."""
                    Toast.makeText(context, "Loaded academic sample paper", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(0.95f)
                    .height(44.dp)
                    .testTag("load_sample_paper_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Sample",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }
        }

        // Uploaded File Badge / Chip (if a document is selected)
        if (uploadedFileName != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Document",
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uploadedFileName ?: "Document",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
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
                            contentDescription = "Remove Document",
                            tint = TextMuted,
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
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("originality_content_input"),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = TextDark, fontSize = 14.sp),
                    placeholder = {
                        Text(
                            text = "Paste text or upload a document (.pdf, .docx, .txt) to cross-reference against arXiv, PubMed, DOAJ & IEEE repositories...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextDark,
                        unfocusedTextColor = TextDark,
                        cursorColor = BrandBlue,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$wordCount/5000 words",
                        fontSize = 12.sp,
                        color = if (wordCount > 5000) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        fontWeight = if (wordCount > 5000) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stat Boxes: Words | Characters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CheckerStatBox(
                label = "Words",
                value = "$wordCount",
                modifier = Modifier.weight(1f)
            )
            CheckerStatBox(
                label = "Characters",
                value = "$charCount",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row: Clear | Check Content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    content = ""
                    uploadedFileName = null
                    documentTitle = ""
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("clear_content_button"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Clear",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            Button(
                onClick = {
                    val titleToUse = if (documentTitle.isNotBlank()) {
                        documentTitle
                    } else if (uploadedFileName != null) {
                        uploadedFileName!!.substringBeforeLast(".")
                    } else {
                        "Academic Originality Analysis"
                    }

                    val finalContent = content.ifBlank {
                        "The integration of digital learning technologies in academic environments provides substantial benefits for collaborative inquiry and knowledge retention."
                    }
                    onRunCheck(titleToUse, finalContent)
                },
                enabled = !isChecking && !isExtractingFile,
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("run_originality_check_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checking...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cross-Reference",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Illustration + Bottom Description
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CheckerDocIllustration(modifier = Modifier.size(160.dp, 120.dp))
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Academic Integrity Verification",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Cross-reference text against millions of peer-reviewed articles and preprints in open-access scientific repositories.",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CheckerStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}

@Composable
private fun CheckerDocIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Soft blue ambient glow
        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.40f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Base document
        drawRoundRect(
            color = Color(0xFF93C5FD),
            topLeft = Offset(w * 0.28f, h * 0.22f),
            size = Size(w * 0.44f, h * 0.58f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Front document white
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.32f, h * 0.26f),
            size = Size(w * 0.44f, h * 0.58f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        // Lines on document
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(w * 0.38f, h * 0.36f),
            end = Offset(w * 0.68f, h * 0.36f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(w * 0.38f, h * 0.44f),
            end = Offset(w * 0.64f, h * 0.44f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(w * 0.38f, h * 0.52f),
            end = Offset(w * 0.58f, h * 0.52f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Magnifying glass over document
        drawCircle(
            color = BrandBlue,
            radius = w * 0.14f,
            center = Offset(w * 0.66f, h * 0.52f),
            style = Stroke(width = 4.dp.toPx())
        )
        drawLine(
            color = BrandBlue,
            start = Offset(w * 0.76f, h * 0.62f),
            end = Offset(w * 0.88f, h * 0.74f),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Green check badge
        drawCircle(
            color = Color(0xFF10B981),
            radius = w * 0.08f,
            center = Offset(w * 0.66f, h * 0.52f)
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.62f, h * 0.52f),
            end = Offset(w * 0.65f, h * 0.56f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White,
            start = Offset(w * 0.65f, h * 0.56f),
            end = Offset(w * 0.71f, h * 0.48f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
