package com.example.ui.screens.tools

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.GenziiViewModel

data class PdfToolItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val outputName: String,
    val outputSize: String
)

@Composable
fun PdfToolsScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit,
    onNavigateToVault: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var completedToolName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val tools = listOf(
        PdfToolItem(
            title = "Merge Documents",
            description = "Combine multiple lecture notes and research drafts into a single PDF.",
            icon = Icons.AutoMirrored.Filled.CallMerge,
            color = AccentRed,
            bgColor = AccentRedLight,
            outputName = "Merged_Academic_Dossier.pdf",
            outputSize = "3.8 MB"
        ),
        PdfToolItem(
            title = "Split PDF",
            description = "Extract chapter pages or specific study modules into individual files.",
            icon = Icons.AutoMirrored.Filled.CallSplit,
            color = AccentOrange,
            bgColor = AccentOrangeLight,
            outputName = "Extracted_Chapter_3.pdf",
            outputSize = "1.2 MB"
        ),
        PdfToolItem(
            title = "Convert Note to PDF",
            description = "Transform rich markdown notes into formatted academic PDFs.",
            icon = Icons.Default.Transform,
            color = AccentPurple,
            bgColor = AccentPurpleLight,
            outputName = "Converted_Lecture_Notes.pdf",
            outputSize = "840 KB"
        ),
        PdfToolItem(
            title = "Compress PDF",
            description = "Reduce file size up to 60% for smooth university portal uploads.",
            icon = Icons.Default.Compress,
            color = AccentTeal,
            bgColor = AccentTealLight,
            outputName = "Compressed_Thesis.pdf",
            outputSize = "1.4 MB"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                text = "PDF Tools",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing PDF operation...", fontWeight = FontWeight.SemiBold)
                }
            }
        } else if (completedToolName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentTeal,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$completedToolName Completed!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The resulting file has been safely preserved in your Academic Vault.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToVault,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GenziiBlue)
                    ) {
                        Text("View in Vault", color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        tools.forEach { tool ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable {
                        isProcessing = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isProcessing = false
                            completedToolName = tool.title
                            viewModel.saveNewNote(
                                title = tool.outputName,
                                content = "Processed academic document via Genzii ${tool.title}.",
                                folder = "Assignments"
                            )
                        }, 700)
                    },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tool.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tool.icon,
                            contentDescription = null,
                            tint = tool.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tool.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tool.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
