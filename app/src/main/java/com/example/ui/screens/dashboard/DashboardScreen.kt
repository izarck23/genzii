package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.components.AdBannerCard
import com.example.ui.components.GenziiBrandHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
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
import com.example.ui.theme.GenziiBlueDark
import com.example.ui.theme.GenziiBlueLight
import com.example.ui.viewmodel.GenziiViewModel

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color,
    val route: String
)

data class RecentActivityItem(
    val title: String,
    val actionType: String,
    val timeAgo: String,
    val icon: ImageVector,
    val iconColor: Color,
    val route: String
)

@Composable
fun DashboardScreen(
    viewModel: GenziiViewModel,
    onNavigate: (String) -> Unit
) {
    val user by viewModel.user.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val quickActions = listOf(
        QuickAction("Check", Icons.Default.Spellcheck, AccentGreen, AccentGreenLight, Screen.OriginalityChecker.route),
        QuickAction("AI Chat", Icons.Default.AutoAwesome, AccentPurple, AccentPurpleLight, Screen.SmartAi.route),
        QuickAction("Vault", Icons.Default.Storage, AccentOrange, AccentOrangeLight, Screen.Vault.route),
        QuickAction("OCR Scan", Icons.Default.PhotoCamera, AccentTeal, AccentTealLight, Screen.OcrScanner.route),
        QuickAction("PDF Tools", Icons.Default.Description, AccentRed, AccentRedLight, Screen.PdfTools.route),
        QuickAction("Translate", Icons.Default.Translate, AccentCyan, AccentCyanLight, Screen.SmartAi.route)
    )

    val recentActivities = listOf(
        RecentActivityItem("Research Paper Draft", "Originality Check (82% Unique)", "2m ago", Icons.Default.Spellcheck, AccentGreen, Screen.OriginalityChecker.route),
        RecentActivityItem("Business Report.pdf", "Uploaded to Vault", "1h ago", Icons.Default.Storage, AccentOrange, Screen.Vault.route),
        RecentActivityItem("How to write essay.docx", "AI Conversation Note", "3h ago", Icons.Default.AutoAwesome, AccentPurple, Screen.SmartAi.route),
        RecentActivityItem("History Assignment.docx", "Document Opened", "Yesterday", Icons.Default.Description, AccentTeal, Screen.Vault.route)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // TOP BAR matching Screen 7 & 8
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GenziiBrandHeader(emblemSize = 34.dp)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Notification Bell with badge
                    Box {
                        IconButton(
                            onClick = { onNavigate(Screen.Notifications.route) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Unread notification badge
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(GenziiBlue)
                        )
                    }

                    // User Profile Avatar
                    IconButton(
                        onClick = { onNavigate(Screen.Profile.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GenziiBlueLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = GenziiBlue
                        )
                    }
                }
            }
        }

        item {
            // Search Input Bar: "Let's get things done today."
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Let's get things done today...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = GenziiBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        item {
            // Greeting Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Good morning, ${user.name.split(" ").firstOrNull() ?: "John"}! 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ready to boost your academic productivity?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isPro) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GenziiBlue)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        item {
            // Quick Actions Section matching template
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6 Grid actions in 3 columns
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (row in 0 until 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            val action = quickActions[index]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(action.route) },
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, BorderLight),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(action.bgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = action.icon,
                                            contentDescription = action.title,
                                            tint = action.iconColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = action.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Overview Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Checks",
                    value = "${user.checksCompleted}",
                    trend = "+24% this week",
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Documents",
                    value = "${user.documentsStored}",
                    trend = "+16% this week",
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "AI Chats",
                    value = "${user.aiConversations}",
                    trend = "+32% this week",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!isPro) {
            item {
                // Upgrade to Pro Banner matching Screen 8
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.Premium.route) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(GenziiBlue, Color(0xFF3B82F6))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Upgrade to Pro",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Unlock unlimited checks, standby GenAI & priority storage.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Upgrade",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GenziiBlue
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Ad Banner for free tier
                AdBannerCard(
                    onUpgradeClick = { onNavigate(Screen.Premium.route) }
                )
            }
        }

        item {
            // Recent Activity Section matching Screen 8
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "See All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GenziiBlue,
                    modifier = Modifier.clickable { onNavigate(Screen.OriginalityChecker.route) }
                )
            }
        }

        items(recentActivities) { activity ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(activity.route) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(activity.iconColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = activity.icon,
                            contentDescription = null,
                            tint = activity.iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activity.actionType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = activity.timeAgo,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    trend: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = trend,
                    fontSize = 9.sp,
                    color = AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
