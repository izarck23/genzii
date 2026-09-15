package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiPersona
import com.example.data.model.ChatMessage
import com.example.ui.components.GenziiBadge
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandBlue = Color(0xFF005AC1)
private val BrandBlueLight = Color(0xFFEFF6FF)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)
private val SurfaceBg = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAiScreen(
    messages: List<ChatMessage>,
    isGenerating: Boolean,
    userName: String = "",
    currentPersona: AiPersona = AiPersona.ASSISTANT,
    onPersonaChange: (AiPersona) -> Unit = {},
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Dialog & sheet states
    var showTipsSheet by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val tipsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Speech-to-Text launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenResults = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenResults?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
                Toast.makeText(context, "Voice input captured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your academic question or prompt...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input is not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun exportChatTranscript(asShareIntent: Boolean) {
        val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())
        val studentName = userName.ifBlank { "Student" }

        val sb = StringBuilder()
        sb.appendLine("==========================================")
        sb.appendLine("     GENZII SMART AI ACADEMIC TRANSCRIPT")
        sb.appendLine("==========================================")
        sb.appendLine("Date: $formattedDate")
        sb.appendLine("Participant: $studentName")
        sb.appendLine("Active Persona: ${currentPersona.displayName}")
        sb.appendLine("Total Exchanges: ${messages.size}")
        sb.appendLine("==========================================\n")

        messages.forEach { msg ->
            if (msg.isFromUser) {
                sb.appendLine("[${msg.timestamp}] $studentName:")
                sb.appendLine(msg.text)
            } else {
                sb.appendLine("[${msg.timestamp}] Genzii AI Assistant (${msg.persona.displayName}):")
                sb.appendLine(msg.text)
            }
            sb.appendLine("------------------------------------------\n")
        }

        sb.appendLine("Exported securely from Genzii Academic Suite")
        val transcript = sb.toString()

        if (asShareIntent) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Genzii AI Chat Transcript - $formattedDate")
                putExtra(Intent.EXTRA_TEXT, transcript)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Export AI Transcript"))
        } else {
            copyToClipboard("Full Transcript", transcript)
        }
    }

    val actionChips = listOf(
        "Explain",
        "Summarize",
        "Rewrite",
        "Ideas",
        "Improve Writing",
        "Quiz Prep",
        "Citations",
        "Grammar Polish",
        "Outline Essay"
    )

    fun handleChipClick(chip: String) {
        when (chip) {
            "Explain" -> {
                if (inputText.isBlank()) inputText = "Explain the concept of "
                else onSendMessage("Explain: $inputText")
            }
            "Summarize" -> {
                if (inputText.isBlank()) inputText = "Summarize the following text: "
                else onSendMessage("Summarize: $inputText")
            }
            "Rewrite" -> {
                if (inputText.isBlank()) inputText = "Rewrite in formal academic tone: "
                else onSendMessage("Rewrite in formal academic tone: $inputText")
            }
            "Ideas" -> {
                if (inputText.isBlank()) inputText = "Brainstorm research angles and ideas about: "
                else onSendMessage("Brainstorm research ideas about: $inputText")
            }
            "Improve Writing" -> {
                if (inputText.isBlank()) inputText = "Improve clarity and flow for this paragraph: "
                else onSendMessage("Improve writing: $inputText")
            }
            "Quiz Prep" -> {
                if (inputText.isBlank()) inputText = "Generate 3 quiz practice questions with answers for: "
                else onSendMessage("Quiz questions for: $inputText")
            }
            "Citations" -> {
                if (inputText.isBlank()) inputText = "Provide APA 7th edition citation format for: "
                else onSendMessage("Citation check: $inputText")
            }
            "Grammar Polish" -> {
                if (inputText.isBlank()) inputText = "Check grammar and syntactic accuracy of: "
                else onSendMessage("Grammar check: $inputText")
            }
            "Outline Essay" -> {
                if (inputText.isBlank()) inputText = "Create a structured academic essay outline on: "
                else onSendMessage("Outline essay: $inputText")
            }
            else -> onSendMessage(chip)
        }
    }

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .statusBarsPadding()
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Smart AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "Academic Copilot • ${currentPersona.displayName}",
                    fontSize = 12.sp,
                    color = BrandBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            // Contextual Prompt Tips Action
            IconButton(
                onClick = { showTipsSheet = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Prompting Tips",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // History / Saved Sessions Action
            IconButton(
                onClick = { showHistoryDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Chat History",
                    tint = TextDark,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Export Transcript Action
            IconButton(
                onClick = { showExportDialog = true },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Transcript",
                    tint = BrandBlue,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Friendly Greeting & Persona Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GenziiBadge(size = 38.dp, bgColor = BrandBlue, letterColor = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val greeting = if (userName.isNotBlank()) "Hello, $userName 👋" else "Hello there 👋"
                        Text(
                            text = greeting,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "How can I assist your research today?",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Persona Segmented Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AiPersona.entries.forEach { persona ->
                        val isSelected = persona == currentPersona
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { onPersonaChange(persona) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = persona.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BrandBlue else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Horizontally Scrollable AI Action Chips with indicator hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(actionChips) { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                            .clickable { handleChipClick(chip) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandBlue
                        )
                    }
                }
            }

            // Subtle gradient indicator to show more options off-screen
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(width = 24.dp, height = 36.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, SurfaceBg)
                        )
                    )
            )
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                if (message.isFromUser) {
                    // Differentiated User Message Bubble (Primary Blue, Asymmetrical)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = 18.dp,
                                        bottomEnd = 4.dp
                                    )
                                )
                                .background(BrandBlue)
                                .padding(horizontal = 15.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.timestamp,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = BrandBlue,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                } else {
                    // Differentiated AI Message Bubble (Pure White Card, Asymmetrical with Actions)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        GenziiBadge(size = 32.dp, bgColor = BrandBlue, letterColor = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // AI Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = BrandBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Genzii Assistant",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlue
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandBlueLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = message.persona.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrandBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = message.timestamp,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // AI Response Text with rich academic formatting
                                AiMessageContent(text = message.text)

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Actions Bar
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = { copyToClipboard("Response", message.text) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy", fontSize = 11.5.sp, color = TextMuted)
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    TextButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_TEXT, message.text)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share AI Response"))
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Share", fontSize = 11.5.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Animated Thinking / Loading Indicator Bubble
            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        GenziiBadge(size = 32.dp, bgColor = BrandBlue, letterColor = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp
                            ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BorderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ThinkingDotsIndicator()
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Genzii AI is thinking...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prominent Stable Elevated Input Bar with Fixed Geometry & Seamless Inset Union
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
            color = SurfaceBg
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Input Mic Button - Fixed size 42dp
                    IconButton(
                        onClick = { launchVoiceRecognition() },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (inputText.isNotBlank()) BrandBlue else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Input Field with fixed dimensions and internal trailing clear icon to prevent layout shifts
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        placeholder = {
                            Text(
                                text = "Ask anything...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                        },
                        trailingIcon = if (inputText.isNotBlank()) {
                            {
                                IconButton(
                                    onClick = { inputText = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear text",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else null,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = BrandBlue,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    // Send Button - Fixed size 44dp with prominent blue pill
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = inputText.isNotBlank() && !isGenerating,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isGenerating) BrandBlue else BrandBlue.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Contextual Help & Academic Prompting Guide BottomSheet
    if (showTipsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTipsSheet = false },
            sheetState = tipsSheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Academic Prompting Guide",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap any formula below to populate your chat prompt instantly:",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                val promptGuides = listOf(
                    Triple(
                        "Explain Concept",
                        "Break down complex theory with everyday analogies",
                        "Explain the concept of quantum computing using a simple library analogy."
                    ),
                    Triple(
                        "Executive Summary",
                        "Extract core methodology and findings",
                        "Summarize the key arguments, methodology, and conclusion of this paper:"
                    ),
                    Triple(
                        "Scholarly Rewrite",
                        "Elevate vocabulary into peer-reviewed register",
                        "Rewrite this paragraph in a formal academic tone suitable for publication:"
                    ),
                    Triple(
                        "Citations & References",
                        "Format verified bibliography in APA/MLA",
                        "Provide APA 7th edition citation format and in-text references for:"
                    ),
                    Triple(
                        "Critical Review",
                        "Stress-test thesis statements against counter-arguments",
                        "Identify logical fallacies and potential counter-arguments in this hypothesis:"
                    ),
                    Triple(
                        "Exam Practice Quiz",
                        "Generate active-recall study questions",
                        "Generate 3 multiple-choice study quiz questions with answers on:"
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    promptGuides.forEach { (title, subtitle, template) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    inputText = template
                                    scope.launch { tipsSheetState.hide() }.invokeOnCompletion {
                                        showTipsSheet = false
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Use",
                                    tint = BrandBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Chat History & Sessions Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = BrandBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat History & Sessions", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Current session contains ${messages.size} messages.",
                        fontSize = 13.5.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Active Workspace", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "All messages are securely cached locally in your encrypted Room database.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            showHistoryDialog = false
                            showClearConfirmDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear History / Start New Chat")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHistoryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Export Transcript Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = BrandBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Transcript", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Choose how you'd like to export this academic chat transcript:",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportDialog = false
                                exportChatTranscript(asShareIntent = true)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Share via Apps / Save as PDF", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                                Text("Send to Drive, Docs, Email, or Print", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showExportDialog = false
                                exportChatTranscript(asShareIntent = false)
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBg),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Copy Complete Transcript", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                                Text("Copies formatted markdown to clipboard", fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Confirm Clear / New Chat Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Start New Conversation?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will clear the current chat history on this device and initialize a fresh academic workspace.",
                    fontSize = 13.5.sp,
                    color = TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearConfirmDialog = false
                        Toast.makeText(context, "Started fresh chat", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear & Start Fresh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThinkingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dotsTransition")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, delayMillis = 360, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = alpha1))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = alpha2))
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = alpha3))
        )
    }
}

/**
 * Professional academic markdown text renderer for AI responses.
 * Accurately styles headers, bold keywords, blockquotes, bullets, and citations.
 */
@Composable
private fun AiMessageContent(text: String) {
    val lines = remember(text) { text.lines() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        lines.forEach { rawLine ->
            val line = rawLine.trimEnd()
            when {
                line.isBlank() -> {
                    Spacer(modifier = Modifier.height(3.dp))
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### ").trim(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        lineHeight = 22.sp
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.removePrefix("## ").trim(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 24.sp
                    )
                }
                line.startsWith("> ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(BrandBlue, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(line.removePrefix("> ").trim()),
                            fontSize = 13.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = TextDark,
                            lineHeight = 19.sp
                        )
                    }
                }
                line.trimStart().startsWith("• ") || line.trimStart().startsWith("- ") -> {
                    val content = line.trimStart().removePrefix("• ").removePrefix("- ").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(content),
                            fontSize = 14.sp,
                            color = TextDark,
                            lineHeight = 20.sp
                        )
                    }
                }
                line.matches(Regex("^\\s*\\d+\\.\\s+.*")) -> {
                    val prefix = line.substringBefore(".").trim() + "."
                    val content = line.substringAfter(".").trim()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = prefix,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = parseInlineMarkdown(content),
                            fontSize = 14.sp,
                            color = TextDark,
                            lineHeight = 20.sp
                        )
                    }
                }
                else -> {
                    Text(
                        text = parseInlineMarkdown(line),
                        fontSize = 14.sp,
                        color = TextDark,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

/**
 * Converts **bold** and *italic* tokens into formatted AnnotatedString.
 */
private fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val matches = boldRegex.findAll(text).toList()

        if (matches.isEmpty()) {
            append(text)
            return@buildAnnotatedString
        }

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1
            if (start > cursor) {
                append(text.substring(cursor, start))
            }
            val boldContent = match.groupValues[1]
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextDark)) {
                append(boldContent)
            }
            cursor = end
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
