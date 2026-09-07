package com.example.ui.screens.checker

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OriginalityReport
import com.example.ui.components.AdBannerCard
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GenziiBlue
import com.example.ui.theme.GenziiBlueLight
import com.example.ui.viewmodel.GenziiViewModel

@Composable
fun OriginalityCheckerScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit,
    onNavigateToResults: (reportId: String) -> Unit,
    onNavigateToUpgrade: () -> Unit
) {
    var documentTitle by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    val isChecking by viewModel.isCheckingOriginality.collectAsState()
    val checks by viewModel.originalityChecks.collectAsState()
    val isPro by viewModel.isPro.collectAsState()

    val wordLimit = if (isPro) 5000 else 1500
    val words = contentText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    val wordCount = words.size
    val charCount = contentText.length

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

            // TOP HEADER matching Screen 9
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
                        text = "Originality Checker",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            // Optional Document Title
            OutlinedTextField(
                value = documentTitle,
                onValueChange = { documentTitle = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Document Title (optional)") },
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
            // Text Input Box matching Screen 9
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = contentText,
                        onValueChange = {
                            val newWords = it.trim().split(Regex("\\s+")).filter { w -> w.isNotBlank() }
                            if (newWords.size <= wordLimit) {
                                contentText = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = {
                            Text(
                                text = "Paste your content here or type to verify authenticity and citation parity...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Words: $wordCount / $wordLimit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (wordCount > wordLimit * 0.9) AccentGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Characters: $charCount",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isPro) {
                            Text(
                                text = "Pro: 5,000w",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GenziiBlue,
                                modifier = Modifier
                                    .clickable { onNavigateToUpgrade() }
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Action Buttons matching Screen 9: Clear & Check Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        contentText = ""
                        documentTitle = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    enabled = contentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear")
                }

                Button(
                    onClick = {
                        if (contentText.isNotBlank()) {
                            viewModel.runOriginalityCheck(
                                title = documentTitle.ifBlank { "Originality Check" },
                                content = contentText
                            ) { reportId ->
                                onNavigateToResults(reportId)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GenziiBlue),
                    enabled = contentText.isNotBlank() && !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyzing...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Spellcheck,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Check Content",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (!isPro) {
            item {
                AdBannerCard(onUpgradeClick = onNavigateToUpgrade)
            }
        }

        item {
            // Recent Checks Header
            Text(
                text = "Recent Checks",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(checks) { check ->
            OriginalityCheckItemCard(
                report = check,
                onClick = {
                    viewModel.selectReport(check.id)
                    onNavigateToResults(check.id)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun OriginalityCheckItemCard(
    report: OriginalityReport,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${report.score}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = report.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${report.score}% Original • ${report.dateString}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GenziiBlueLight)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "View",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GenziiBlue
                )
            }
        }
    }
}
