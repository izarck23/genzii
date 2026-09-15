package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.User
import java.io.File

private val BrandBlue = Color(0xFF005AC1)
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onLogoutClick: () -> Unit,
    onUpdateAvatarBitmap: (Bitmap) -> Unit = {},
    onUpdateAvatarUri: (Uri) -> Unit = {},
    onRemoveAvatar: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAvatarPickerSheet by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            onUpdateAvatarBitmap(bitmap)
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            showPermissionDeniedDialog = true
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateAvatarUri(uri)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar: < Profile
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
                    text = "Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    letterSpacing = (-0.4).sp
                )
            }
        }

        // Profile Avatar, Name, Email, Camera Badge
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8E2FF))
                            .clickable { showAvatarPickerSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user.avatarUrl.isNullOrBlank()) {
                            val model = if (user.avatarUrl.startsWith("/")) File(user.avatarUrl) else user.avatarUrl
                            AsyncImage(
                                model = model,
                                contentDescription = "Profile Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            val displayName = user.name.ifBlank {
                                if (user.email.isNotBlank()) user.email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
                            }
                            val initials = displayName.split(" ")
                                .filter { it.isNotBlank() }
                                .map { it.first().uppercaseChar() }
                                .take(2)
                                .joinToString("")
                                .ifBlank { "U" }
                            Text(
                                text = initials,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        }
                    }

                    // Camera Icon Badge for quick profile photo update
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BrandBlue)
                            .clickable { showAvatarPickerSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Update avatar using camera",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val displayName = user.name.ifBlank {
                    if (user.email.isNotBlank()) user.email.substringBefore("@").replaceFirstChar { it.uppercase() } else "User"
                }

                Text(
                    text = displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (user.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Premium Member Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (user.isPro) "Premium Member" else "Free Plan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandBlue
                    )
                }
            }
        }

        // Stats Row: Real dynamic stats!
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStatBox(value = "${user.checksCompleted}", label = "Checks", modifier = Modifier.weight(1f))
                ProfileStatBox(value = "${user.documentsStored}", label = "Documents", modifier = Modifier.weight(1f))
                ProfileStatBox(value = "${user.aiConversations}", label = "AI Chats", modifier = Modifier.weight(1f))
            }
        }

        // Account Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SimpleProfileRow(
                        icon = Icons.Default.PhotoCamera,
                        title = "Change Profile Picture",
                        subtitle = "Use camera or select from photos",
                        onClick = { showAvatarPickerSheet = true }
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SimpleProfileRow(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SimpleProfileRow(
                        icon = Icons.Default.BarChart,
                        title = "Account Statistics",
                        onClick = {}
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    SimpleProfileRow(
                        icon = Icons.Default.CloudQueue,
                        title = "Storage Usage",
                        subtitle = "${user.storageUsedGb} GB / ${user.storageTotalGb.toInt()} GB",
                        onClick = {}
                    )
                }
            }
        }

        // Preferences & Legal Menu
        item {
            Text(
                text = "Preferences & Security",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    MenuBadgeRow(
                        icon = Icons.Default.Notifications,
                        badgeBg = Color(0xFFEFF6FF),
                        badgeTint = BrandBlue,
                        title = "Notifications",
                        subtitle = "Manage push notifications",
                        onClick = onNavigateToNotifications
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    MenuBadgeRow(
                        icon = Icons.Default.ColorLens,
                        badgeBg = Color(0xFFFFFBEB),
                        badgeTint = Color(0xFFF59E0B),
                        title = "Appearance",
                        subtitle = "Light mode",
                        onClick = onNavigateToAppearance
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    MenuBadgeRow(
                        icon = Icons.Default.Language,
                        badgeBg = Color(0xFFEFF6FF),
                        badgeTint = BrandBlue,
                        title = "Language",
                        subtitle = "English",
                        onClick = onNavigateToLanguage
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    MenuBadgeRow(
                        icon = Icons.Default.Settings,
                        badgeBg = Color(0xFFF5F3FF),
                        badgeTint = Color(0xFF8B5CF6),
                        title = "Account Preferences",
                        subtitle = "Account details and security",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    MenuBadgeRow(
                        icon = Icons.Default.Policy,
                        badgeBg = Color(0xFFECFDF5),
                        badgeTint = Color(0xFF10B981),
                        title = "Privacy Policy",
                        subtitle = "Data security and terms",
                        onClick = onNavigateToPrivacyPolicy
                    )
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    MenuBadgeRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        badgeBg = Color(0xFFFEF2F2),
                        badgeTint = Color(0xFFEF4444),
                        title = "Log Out",
                        subtitle = "Sign out from your account",
                        onClick = onLogoutClick
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Avatar Selection Bottom Sheet
    if (showAvatarPickerSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showAvatarPickerSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Profile Picture",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Take photo with camera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showAvatarPickerSheet = false
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = BrandBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Take Photo with Camera", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text("Capture a new picture using your camera", fontSize = 12.sp, color = TextMuted)
                    }
                }

                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))

                // Option 2: Choose from Photos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showAvatarPickerSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECFDF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Choose from Gallery / Photos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text("Select an existing photo from device", fontSize = 12.sp, color = TextMuted)
                    }
                }

                // Option 3: Remove Avatar (if exists)
                if (!user.avatarUrl.isNullOrBlank()) {
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showAvatarPickerSheet = false
                                onRemoveAvatar()
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Remove Picture", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                            Text("Revert back to default initials avatar", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Permission Denied Alert Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Camera Permission Required", fontWeight = FontWeight.Bold) },
            text = { Text("Genzii needs camera access so you can take a new profile photo or avatar.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDeniedDialog = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileStatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SimpleProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun MenuBadgeRow(
    icon: ImageVector,
    badgeBg: Color,
    badgeTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
    }
}
