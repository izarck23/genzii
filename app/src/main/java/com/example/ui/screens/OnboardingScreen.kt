package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.GenziiEmblem
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanLight
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentGreenLight
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentOrangeLight
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentPurpleLight
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.AccentTealLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.GenziiBlue
import com.example.ui.theme.GenziiBlueCard
import com.example.ui.theme.GenziiBlueDark
import com.example.ui.theme.GenziiBlueGradientEnd
import com.example.ui.theme.GenziiBlueLight

data class FeatureCardItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBgColor: Color
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onLoginClick: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = 4
    val scrollState = rememberScrollState()

    val features = listOf(
        FeatureCardItem(
            title = "Originality Checker",
            description = "Detect similarities and improve your work.",
            icon = Icons.Default.Search,
            iconColor = GenziiBlue,
            iconBgColor = GenziiBlueLight
        ),
        FeatureCardItem(
            title = "Smart AI",
            description = "Get help, learn and boost your productivity.",
            icon = Icons.Default.SmartToy,
            iconColor = AccentPurple,
            iconBgColor = AccentPurpleLight
        ),
        FeatureCardItem(
            title = "OCR Scanner",
            description = "Turn images and documents into editable text.",
            icon = Icons.Default.PhotoCamera,
            iconColor = AccentGreen,
            iconBgColor = AccentGreenLight
        ),
        FeatureCardItem(
            title = "PDF Tools",
            description = "Merge, split, convert and compress files.",
            icon = Icons.Default.Description,
            iconColor = AccentOrange,
            iconBgColor = AccentOrangeLight
        ),
        FeatureCardItem(
            title = "Translation",
            description = "Translate across multiple languages.",
            icon = Icons.Default.Translate,
            iconColor = AccentCyan,
            iconBgColor = AccentCyanLight
        ),
        FeatureCardItem(
            title = "Secure Vault",
            description = "Store and organize your important files.",
            icon = Icons.Default.Lock,
            iconColor = AccentPurple,
            iconBgColor = AccentPurpleLight
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // TOP SECTION: Blue curved background matching visual reference Image 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GenziiBlueDark,
                            GenziiBlue,
                            GenziiBlueGradientEnd
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                // Genzii logo row: white text and emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        GenziiEmblem(size = 28.dp, containerColor = Color.Transparent, iconTint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Genzii",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.22f))
                            .clickable { onLoginClick() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Log In",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Heading matching reference image: "Your Smarter Academic Workspace"
                Text(
                    text = "Your Smarter\nAcademic\nWorkspace",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(
                    text = "Check, write, scan, translate, and organize your academic work in one powerful app.",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons: "Get Started ->" (white pill) & "Learn More" (outlined)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onGetStarted,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = GenziiBlue
                        ),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = "Get Started",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GenziiBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = GenziiBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (currentStep < totalSteps - 1) currentStep++ else onGetStarted()
                        },
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            text = "Learn More",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Floating Phone Mockup from generated asset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding_phones),
                        contentDescription = "Genzii App Screen Mockups",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // BOTTOM SECTION: Key Features Grid matching template
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Key Features",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 6 Grid items in 2 columns
            for (i in features.indices step 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        FeatureCard(feature = features[i])
                    }
                    if (i + 1 < features.size) {
                        Box(modifier = Modifier.weight(1f)) {
                            FeatureCard(feature = features[i + 1])
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Carousel Pager Dots & Circular Next Button matching visual reference Image 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (index in 0 until totalSteps) {
                        val isActive = index == currentStep
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 20.dp else 8.dp, 8.dp)
                                .clip(if (isActive) RoundedCornerShape(4.dp) else CircleShape)
                                .background(
                                    if (isActive) GenziiBlue else GenziiBlueLight
                                )
                                .clickable { currentStep = index }
                        )
                    }
                }

                // Blue circular next button with arrow
                IconButton(
                    onClick = {
                        if (currentStep < totalSteps - 1) {
                            currentStep++
                        } else {
                            onGetStarted()
                        }
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(GenziiBlue)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(feature: FeatureCardItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(feature.iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feature.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = feature.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )
        }
    }
}
