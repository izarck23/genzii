package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AppThemeMode
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GenziiBlue
import com.example.ui.viewmodel.GenziiViewModel

@Composable
fun NotificationsScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit
) {
    val prefs by viewModel.notificationPrefs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Notifications", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                NotificationToggleRow(
                    title = "Push Notifications",
                    subtitle = "Receive alerts about completed originality checks and updates",
                    checked = prefs.pushEnabled,
                    onCheckedChange = { viewModel.updateNotifications(prefs.copy(pushEnabled = it)) }
                )
                NotificationToggleRow(
                    title = "Email Notifications",
                    subtitle = "Get weekly research summaries and academic tips",
                    checked = prefs.emailEnabled,
                    onCheckedChange = { viewModel.updateNotifications(prefs.copy(emailEnabled = it)) }
                )
                NotificationToggleRow(
                    title = "Activity Reminders",
                    subtitle = "Gentle prompts for pending reviews and deadlines",
                    checked = prefs.activityEnabled,
                    onCheckedChange = { viewModel.updateNotifications(prefs.copy(activityEnabled = it)) }
                )
                NotificationToggleRow(
                    title = "Security & Account Alerts",
                    subtitle = "Critical updates regarding sign-ins and session integrity",
                    checked = prefs.securityEnabled,
                    onCheckedChange = { viewModel.updateNotifications(prefs.copy(securityEnabled = it)) }
                )
            }
        }
    }
}

@Composable
fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GenziiBlue)
        )
    }
}

@Composable
fun AppearanceScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit
) {
    val currentTheme by viewModel.theme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Appearance", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ThemeOptionRow("Light Mode (Genzii Standard)", AppThemeMode.LIGHT, currentTheme) { viewModel.setTheme(it) }
                ThemeOptionRow("Dark Mode", AppThemeMode.DARK, currentTheme) { viewModel.setTheme(it) }
                ThemeOptionRow("System Default", AppThemeMode.SYSTEM, currentTheme) { viewModel.setTheme(it) }
            }
        }
    }
}

@Composable
fun ThemeOptionRow(
    label: String,
    mode: AppThemeMode,
    selectedMode: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        RadioButton(
            selected = mode == selectedMode,
            onClick = { onSelect(mode) },
            colors = RadioButtonDefaults.colors(selectedColor = GenziiBlue)
        )
    }
}

@Composable
fun LanguageScreen(
    viewModel: GenziiViewModel,
    onBackClick: () -> Unit
) {
    val currentLang by viewModel.language.collectAsState()
    val languages = listOf("English", "Spanish", "French", "German", "Swahili", "Arabic", "Portuguese", "Mandarin")
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Language", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage(lang)
                                Toast.makeText(context, "Language changed to $lang", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = lang, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        if (currentLang == lang) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GenziiBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RateUsScreen(
    onBackClick: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var feedback by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Rate Us", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (submitted) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Thank you for your rating!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Your feedback helps us continuously improve the Genzii academic experience.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(text = "How are you liking Genzii?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Tap a star to rate your experience", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$i stars",
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        placeholder = { Text("Tell us what you love or how we can improve...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = GenziiBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { submitted = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GenziiBlue)
                    ) {
                        Text("Submit Rating", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit
) {
    val faqs = listOf(
        Pair("How accurate is the Originality Checker?", "Genzii compares student and academic submissions across published journals, open-access archives, conference proceedings, and millions of educational web pages with high semantic precision."),
        Pair("Is my document content stored safely?", "Yes. All vault documents and submitted texts are encrypted at rest and in transit. Your intellectual property is never shared or added to open public training corpora."),
        Pair("What are the word count limits for checking?", "The Free tier supports checks up to 1,500 words. Genzii Pro expands this limit to 5,000 words per single analysis pass."),
        Pair("How do I contact customer support?", "You can email our academic support team at support@genzii.ai or submit an in-app inquiry 24/7.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SettingsHeader(title = "Help & Support", onBackClick = onBackClick)
        }

        item {
            Text(text = "Frequently Asked Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        items(faqs) { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderLight),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = question, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                        Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = answer, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Privacy Policy", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Academic Privacy & Integrity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Genzii is committed to protecting student intellectual property and privacy. We do not sell your personal data or submitted academic materials. All documents stored in your Vault remain under your exclusive ownership.\n\n" +
                            "1. Data Collection: We collect account email and telemetry solely to provide workspace features and originality verification.\n\n" +
                            "2. Document Security: Uploaded papers are analyzed in secure isolated sandboxes and are encrypted with industry-standard protocols.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun TermsOfServiceScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        SettingsHeader(title = "Terms of Service", onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, BorderLight),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Terms of Use", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "By accessing or using Genzii, you agree to adhere to academic honor codes and ethical usage of artificial intelligence.\n\n" +
                            "1. Permitted Use: Genzii tools are intended for scholarly research, study assistance, draft review, and document management.\n\n" +
                            "2. Subscriptions: Pro access unlocks enhanced limits. Subscriptions can be managed or canceled anytime via your account settings.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String, onBackClick: () -> Unit) {
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
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
    }
}
