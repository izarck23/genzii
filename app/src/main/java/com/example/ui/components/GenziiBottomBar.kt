package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen

enum class BottomTab(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Screen.Home.route, Icons.Filled.Home, Icons.Outlined.Home),
    CHECK("Check", Screen.OriginalityChecker.route, Icons.Filled.Description, Icons.Outlined.Description),
    AI("AI", Screen.SmartAi.route, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    VAULT("Vault", Screen.Vault.route, Icons.Filled.Folder, Icons.Outlined.Folder),
    PROFILE("Profile", Screen.Profile.route, Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun GenziiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val activeTab = when (currentRoute) {
        Screen.Home.route -> BottomTab.HOME
        Screen.OriginalityChecker.route, Screen.CheckResults.route -> BottomTab.CHECK
        Screen.SmartAi.route -> BottomTab.AI
        Screen.Vault.route, Screen.DocumentPreview.route -> BottomTab.VAULT
        Screen.Profile.route, Screen.ProfileMenu.route, Screen.Settings.route,
        Screen.Notifications.route, Screen.Appearance.route, Screen.Language.route,
        Screen.HelpSupport.route, Screen.LogoutConfirm.route, Screen.PrivacyPolicy.route -> BottomTab.PROFILE
        else -> BottomTab.HOME
    }

    val barBackground = MaterialTheme.colorScheme.surface
    val barDivider = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(barBackground)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(thickness = 1.dp, color = barDivider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = activeTab == tab
                val iconColor = if (isSelected) selectedColor else unselectedColor
                val textColor = if (isSelected) selectedColor else unselectedColor
                val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bottom_nav_${tab.name.lowercase()}")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigate(tab.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tab.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
