package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.User
import com.example.data.model.VaultSyncNetworkMode

private val RedDelete = Color(0xFFEF4444)

@Composable
fun SettingsScreen(
    user: User,
    currentTheme: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeChange: (AppThemeMode) -> Unit = {},
    syncNetworkMode: VaultSyncNetworkMode = VaultSyncNetworkMode.WIFI_ONLY,
    onSyncNetworkModeChange: (VaultSyncNetworkMode) -> Unit = {},
    onBackClick: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    var showSyncModeDialog by remember { mutableStateOf(false) }

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val brandBlue = MaterialTheme.colorScheme.primary

    val themeDisplay = when (currentTheme) {
        AppThemeMode.LIGHT -> "Light"
        AppThemeMode.DARK -> "Dark"
        AppThemeMode.SYSTEM -> "System default"
    }

    val isDarkActive = currentTheme == AppThemeMode.DARK

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: < Settings
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(surface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textDark
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark,
                    letterSpacing = (-0.4).sp
                )
            }
        }

        // Account Section
        item {
            Text(
                text = "Account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(brandBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val displayName = user.name.ifBlank {
                                if (user.email.isNotBlank()) user.email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
                            }
                            val initials = displayName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "U" }
                            Text(
                                text = initials,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            val displayName = user.name.ifBlank {
                                if (user.email.isNotBlank()) user.email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User Account"
                            }
                            Text(
                                text = displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.email.ifBlank { "Signed in" },
                                fontSize = 13.sp,
                                color = textMuted
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = textMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // General Section (With Dark Mode Quick Toggle & Appearance)
        item {
            Text(
                text = "General",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    // Quick Dark Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = brandBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Dark Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textDark
                                )
                                Text(
                                    text = if (isDarkActive) "Dark theme enabled" else "Switch to dark theme",
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }
                        Switch(
                            checked = isDarkActive,
                            onCheckedChange = { checked ->
                                onThemeChange(if (checked) AppThemeMode.DARK else AppThemeMode.LIGHT)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = brandBlue
                            )
                        )
                    }

                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))

                    SettingsItemRow(
                        icon = Icons.Default.ColorLens,
                        title = "Appearance & Theme Details",
                        value = themeDisplay,
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = onNavigateToAppearance
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    SettingsItemRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        value = null,
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = onNavigateToNotifications
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    SettingsItemRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        value = "English (US)",
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = onNavigateToLanguage
                    )
                }
            }
        }

        // Cloud Sync & Data Management Section
        item {
            Text(
                text = "Vault & Cloud Synchronization",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsItemRow(
                        icon = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) Icons.Default.Wifi else Icons.Default.SignalCellularAlt,
                        title = "Cloud Sync Network",
                        value = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) "Wi-Fi only" else "Always (Wi-Fi + Cellular)",
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = { showSyncModeDialog = true }
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(brandBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = brandBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) {
                                "Syncing paused on cellular data to protect your mobile plan. Vault files remain fully accessible locally."
                            } else {
                                "Syncing active across all networks (Wi-Fi and mobile data) for real-time cloud backup."
                            },
                            fontSize = 12.sp,
                            color = textMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Data & Privacy Section
        item {
            Text(
                text = "Data & Privacy",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsItemRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        value = null,
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = onNavigateToPrivacyPolicy
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    SettingsItemRow(
                        icon = Icons.Default.CloudQueue,
                        title = "Data Usage",
                        value = "2.4 GB used",
                        textColor = textDark,
                        mutedColor = textMuted,
                        iconColor = brandBlue,
                        onClick = {}
                    )
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = RedDelete,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Delete Account",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RedDelete
                        )
                    }
                }
            }
        }

        item {
            com.example.ui.components.AdMobTestBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSyncModeDialog) {
        AlertDialog(
            onDismissRequest = { showSyncModeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = brandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Vault Sync Network",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose when files and media should sync with Firebase Firestore and Cloud Storage:",
                        fontSize = 13.sp,
                        color = textMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Wi-Fi only
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSyncNetworkModeChange(VaultSyncNetworkMode.WIFI_ONLY)
                                showSyncModeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (syncNetworkMode == VaultSyncNetworkMode.WIFI_ONLY) brandBlue.copy(alpha = 0.12f) else surface
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
                                    onSyncNetworkModeChange(VaultSyncNetworkMode.WIFI_ONLY)
                                    showSyncModeDialog = false
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
                                    text = "Syncs files only on Wi-Fi. Conserves mobile cellular data. Recommended.",
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
                                onSyncNetworkModeChange(VaultSyncNetworkMode.ALWAYS)
                                showSyncModeDialog = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (syncNetworkMode == VaultSyncNetworkMode.ALWAYS) brandBlue.copy(alpha = 0.12f) else surface
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
                                    onSyncNetworkModeChange(VaultSyncNetworkMode.ALWAYS)
                                    showSyncModeDialog = false
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
                                    text = "Syncs continuously across both Wi-Fi and mobile data. Keeps files updated anywhere.",
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
                TextButton(onClick = { showSyncModeDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = brandBlue)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = surface
        )
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    textColor: Color,
    mutedColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = mutedColor
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = mutedColor.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
