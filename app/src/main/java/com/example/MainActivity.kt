package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.AppThemeMode
import com.example.ui.components.AdMobManager
import com.example.ui.components.GenziiBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.CheckResultsScreen
import com.example.ui.screens.DocumentPreviewScreen
import com.example.ui.screens.DocumentScannerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.auth.WelcomeLandingScreen
import com.example.ui.screens.OriginalityCheckerScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartAiScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.screens.auth.CreateAccountScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.ResetPasswordScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GenziiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GenziiViewModel = viewModel()
            val currentTheme by viewModel.theme.collectAsState()
            val isDarkTheme = when (currentTheme) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                GenziiApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun GenziiApp(
    viewModel: GenziiViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val user by viewModel.user.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val lastActiveRoute by viewModel.lastActiveRoute.collectAsState()
    val bonusAiCredits by viewModel.bonusAiCredits.collectAsState()
    val themeMode by viewModel.theme.collectAsState()
    val reports by viewModel.originalityChecks.collectAsState()
    val vaultFiles by viewModel.vaultFiles.collectAsState()
    val vaultFolders by viewModel.vaultFolders.collectAsState()
    val trashFiles by viewModel.trashFiles.collectAsState()
    val favoriteFiles by viewModel.favoriteFiles.collectAsState()
    val activeTransfers by viewModel.activeTransfers.collectAsState()
    val selectedVaultFolder by viewModel.selectedFolder.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChecking by viewModel.isCheckingOriginality.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val vaultSearchQuery by viewModel.vaultSearchQuery.collectAsState()
    val currentReport by viewModel.currentReport.collectAsState()
    val previewFile by viewModel.previewFile.collectAsState()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    val vaultSyncStatus by viewModel.vaultSyncStatus.collectAsState()
    val vaultSyncNetworkMode by viewModel.vaultSyncNetworkMode.collectAsState()
    val currentPersona by viewModel.currentPersona.collectAsState()
    val ocrScans by viewModel.ocrScans.collectAsState()
    val originalityError by viewModel.originalityError.collectAsState()
    val deviceStorageFiles by viewModel.deviceStorageFiles.collectAsState()
    val isScanningDeviceStorage by viewModel.isScanningDeviceStorage.collectAsState()
    val deviceStorageFilter by viewModel.deviceStorageFilter.collectAsState()

    // Initialize AdMob test ads on startup
    LaunchedEffect(Unit) {
        AdMobManager.initialize(context)
    }

    // Persist active screen route for flawless restoration across app restarts
    LaunchedEffect(currentRoute) {
        viewModel.setLastActiveRoute(currentRoute)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val route = currentRoute
            if (route == Screen.Welcome.route || route == Screen.Login.route || route == Screen.CreateAccount.route) {
                val destination = if (lastActiveRoute.isNotBlank() && lastActiveRoute != Screen.Welcome.route && lastActiveRoute != Screen.Login.route) {
                    lastActiveRoute
                } else {
                    Screen.Home.route
                }
                navController.navigate(destination) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
        }
    }

    val isKeyboardOpen = WindowInsets.isImeVisible

    val bottomBarRoutes = setOf(
        Screen.Home.route,
        Screen.OriginalityChecker.route,
        Screen.CheckResults.route,
        Screen.Vault.route,
        Screen.DocumentPreview.route,
        Screen.Profile.route,
        Screen.Settings.route,
        Screen.Notifications.route,
        Screen.Appearance.route,
        Screen.Language.route,
        Screen.PrivacyPolicy.route
    )

    val showBottomBar = currentRoute in bottomBarRoutes && !isKeyboardOpen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                GenziiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        if (destination == currentRoute) return@GenziiBottomBar
                        if (destination == Screen.Home.route) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        } else {
                            navController.navigate(destination) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) 72.dp else 0.dp)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                // Screen: Splash (Initial Loading & Returning User Direct Navigation)
                composable(Screen.Splash.route) {
                    SplashScreen(
                        isLoggedIn = isLoggedIn,
                        lastActiveRoute = lastActiveRoute,
                        onNavigateToApp = { destination ->
                            navController.navigate(destination) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        onNavigateToWelcome = {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Screen 0: Welcome Landing Screen
                composable(Screen.Welcome.route) {
                    WelcomeLandingScreen(
                        onSignInClick = {
                            navController.navigate(Screen.Login.route)
                        },
                        onSignUpClick = {
                            navController.navigate(Screen.CreateAccount.route)
                        }
                    )
                }

                // Screen 5: Login
                composable(Screen.Login.route) {
                    LoginScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        },
                        onGoogleSignInClick = { context, onSuccess, onError ->
                            viewModel.signInWithGoogle(
                                context = context,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        },
                        onFacebookSignInClick = { context, onSuccess, onError ->
                            viewModel.signInWithFacebook(
                                context = context,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        },
                        onForgotPasswordClick = {
                            navController.navigate(Screen.ForgotPassword.route)
                        },
                        onCreateAccountClick = {
                            navController.navigate(Screen.CreateAccount.route)
                        },
                        onSignInWithEmail = { email, pass, onSuccess, onError ->
                            viewModel.signInWithEmail(
                                email = email,
                                pass = pass,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        }
                    )
                }

                // Screen 6: Create Account
                composable(Screen.CreateAccount.route) {
                    CreateAccountScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onLoginClick = {
                            navController.navigate(Screen.Login.route)
                        },
                        onSignUpWithEmail = { name, email, pass, onSuccess, onError ->
                            viewModel.signUpWithEmail(
                                name = name,
                                email = email,
                                pass = pass,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        },
                        onGoogleSignUpClick = { context, onSuccess, onError ->
                            viewModel.signInWithGoogle(
                                context = context,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        },
                        onFacebookSignUpClick = { context, onSuccess, onError ->
                            viewModel.signInWithFacebook(
                                context = context,
                                onSuccess = {
                                    onSuccess()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                },
                                onError = onError
                            )
                        },
                        onPrivacyPolicyClick = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        }
                    )
                }

                // Screen 7: Forgot Password
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmitClick = { email, onSuccess, onError ->
                            viewModel.resetPassword(
                                email = email,
                                onSuccess = onSuccess,
                                onError = onError
                            )
                        },
                        onNavigateToReset = {
                            navController.navigate(Screen.ResetPassword.route)
                        }
                    )
                }

                // Screen 8: Reset Password
                composable(Screen.ResetPassword.route) {
                    ResetPasswordScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSubmitNewPassword = { newPass, onSuccess, onError ->
                            viewModel.updatePassword(
                                newPass = newPass,
                                onSuccess = onSuccess,
                                onError = onError
                            )
                        },
                        onSuccessLoginRedirect = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.ResetPassword.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Screen 9: Home Dashboard
                composable(Screen.Home.route) {
                    HomeScreen(
                        user = user,
                        recentReports = reports,
                        recentFiles = vaultFiles,
                        onNavigateToChecker = {
                            navController.navigate(Screen.OriginalityChecker.route)
                        },
                        onNavigateToAi = {
                            navController.navigate(Screen.SmartAi.route)
                        },
                        onNavigateToVault = {
                            navController.navigate(Screen.Vault.route)
                        },
                        onNavigateToOcr = {
                            navController.navigate(Screen.DocumentScanner.route)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onSelectReport = { reportId ->
                            viewModel.selectReport(reportId)
                            navController.navigate(Screen.CheckResults.route)
                        }
                    )
                }

                // Document Scanner with CameraX and ML Kit OCR
                composable(Screen.DocumentScanner.route) {
                    DocumentScannerScreen(
                        savedScans = ocrScans,
                        onScanSaved = { title, text, imagePath ->
                            viewModel.saveOcrScan(title, text, 95, imagePath)
                        },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onUseInChecker = { text ->
                            viewModel.runOriginalityCheck("Scanned Document", text) {
                                navController.navigate(Screen.CheckResults.route)
                            }
                        },
                        onSaveToVault = { title, content ->
                            viewModel.saveNewNote(title, content, "Scanned")
                        },
                        onDeleteScan = { id ->
                            viewModel.deleteOcrScan(id)
                        }
                    )
                }

                // Screen 10: Originality Checker
                composable(Screen.OriginalityChecker.route) {
                    OriginalityCheckerScreen(
                        reports = reports,
                        isChecking = isChecking,
                        errorMessage = originalityError,
                        onDismissError = { viewModel.clearOriginalityError() },
                        onRunCheck = { title, content ->
                            viewModel.runOriginalityCheck(title, content) { _ ->
                                navController.navigate(Screen.CheckResults.route)
                            }
                        },
                        onOpenReport = { reportId ->
                            viewModel.selectReport(reportId)
                            navController.navigate(Screen.CheckResults.route)
                        },
                        onDeleteReport = { reportId ->
                            viewModel.deleteOriginalityReport(reportId)
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 11: Check Results
                composable(Screen.CheckResults.route) {
                    CheckResultsScreen(
                        report = currentReport ?: reports.firstOrNull(),
                        onBackClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    navController.popBackStack()
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onViewFullReportClick = {
                            if (activity != null) {
                                AdMobManager.showInterstitial(activity) {
                                    navController.popBackStack()
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onDeleteReport = { reportId ->
                            viewModel.deleteOriginalityReport(reportId)
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 12: Smart AI
                composable(Screen.SmartAi.route) {
                    val displayName = user.name.ifBlank {
                        if (user.email.isNotBlank()) user.email.substringBefore("@").replaceFirstChar { it.uppercase() } else ""
                    }
                    SmartAiScreen(
                        messages = chatMessages,
                        isGenerating = isAiGenerating,
                        userName = displayName,
                        currentPersona = currentPersona,
                        onPersonaChange = { persona ->
                            viewModel.setAiPersona(persona)
                        },
                        onSendMessage = { prompt ->
                            viewModel.sendAiMessage(prompt)
                        },
                        onRetryMessage = { failedMessage ->
                            viewModel.retryAiMessage(failedMessage)
                        },
                        onCancelGeneration = {
                            viewModel.cancelAiGeneration()
                        },
                        onClearChat = {
                            viewModel.clearChat()
                        },
                        onDeleteMessage = { messageId ->
                            viewModel.deleteChatMessage(messageId)
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 13: Vault
                composable(Screen.Vault.route) {
                    VaultScreen(
                        files = vaultFiles,
                        folders = vaultFolders,
                        trashFiles = trashFiles,
                        favoriteFiles = favoriteFiles,
                        activeTransfers = activeTransfers,
                        selectedFolder = selectedVaultFolder,
                        searchQuery = vaultSearchQuery,
                        onSearchChange = { viewModel.setVaultSearchQuery(it) },
                        onFolderSelect = { viewModel.setSelectedFolder(it) },
                        onFileClick = { file ->
                            viewModel.setPreviewFile(file)
                            navController.navigate(Screen.DocumentPreview.route)
                        },
                        onImportFiles = { uris, targetFolderId, targetFolderName, targetFolderPath ->
                            viewModel.importFilesFromUris(uris, targetFolderId, targetFolderName, targetFolderPath)
                        },
                        onCreateFolder = { name, parentFolderId, parentPath, colorHex, isSecured ->
                            viewModel.createFolder(name, parentFolderId, parentPath, colorHex, isSecured)
                        },
                        onRenameFolder = { folderId, newName ->
                            viewModel.renameFolder(folderId, newName)
                        },
                        onDeleteFolder = { folderId ->
                            viewModel.deleteFolder(folderId)
                        },
                        onMoveFolder = { folderId, newParent, newPath ->
                            viewModel.moveFolder(folderId, newParent, newPath)
                        },
                        onRenameFile = { fileId, newName ->
                            viewModel.renameFile(fileId, newName)
                        },
                        onToggleSecure = { file ->
                            viewModel.setFileSecured(file.id, !file.isSecured)
                        },
                        onToggleFavorite = { file ->
                            viewModel.toggleFavoriteFile(file)
                        },
                        onMoveMultipleFiles = { fileIds, targetFolderName, targetFolderId, targetFolderPath ->
                            viewModel.moveFilesToFolder(fileIds, targetFolderName, targetFolderId, targetFolderPath)
                        },
                        onCopyMultipleFiles = { fileIds, targetFolderName, targetFolderId, targetFolderPath ->
                            viewModel.copyFiles(fileIds, targetFolderName, targetFolderId, targetFolderPath)
                        },
                        onSetMultipleFilesSecured = { fileIds, isSecured ->
                            viewModel.setFilesSecured(fileIds, isSecured)
                        },
                        onTrashMultipleFiles = { fileIds ->
                            viewModel.trashFiles(fileIds)
                        },
                        onRestoreMultipleFiles = { fileIds ->
                            viewModel.restoreFiles(fileIds)
                        },
                        onEmptyTrash = {
                            viewModel.emptyTrash()
                        },
                        onDeletePermanently = { fileId ->
                            viewModel.deletePermanently(fileId)
                        },
                        onCreateNewNote = { title, content, folder ->
                            viewModel.saveNewNote(title, content, folder)
                        },
                        syncStatus = vaultSyncStatus,
                        syncNetworkMode = vaultSyncNetworkMode,
                        onSetSyncNetworkMode = { viewModel.setVaultSyncNetworkMode(it) },
                        onSyncClick = {
                            viewModel.syncVaultNow()
                        },
                        deviceStorageFiles = deviceStorageFiles,
                        isScanningDeviceStorage = isScanningDeviceStorage,
                        deviceStorageFilter = deviceStorageFilter,
                        onScanDeviceStorage = { viewModel.scanDeviceStorage() },
                        onSetDeviceStorageFilter = { viewModel.setDeviceStorageFilter(it) },
                        onImportDeviceFileToVault = { viewModel.importDeviceFileToVault(it) }
                    )
                }

                // Screen 14: Document Preview
                composable(Screen.DocumentPreview.route) {
                    DocumentPreviewScreen(
                        file = previewFile ?: vaultFiles.firstOrNull(),
                        availableFolders = vaultFolders,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onDeleteClick = { file ->
                            viewModel.trashFiles(listOf(file.id))
                            navController.popBackStack()
                        },
                        onToggleSecure = { file ->
                            viewModel.setFileSecured(file.id, !file.isSecured)
                        },
                        onToggleFavorite = { file ->
                            viewModel.toggleFavoriteFile(file)
                        },
                        onRenameFile = { file, newName ->
                            viewModel.renameFile(file.id, newName)
                        },
                        onMoveFile = { file, targetFolder ->
                            val folderObj = vaultFolders.find { it.name == targetFolder }
                            viewModel.moveFilesToFolder(
                                listOf(file.id),
                                targetFolder,
                                folderObj?.id,
                                folderObj?.path ?: "/$targetFolder"
                            )
                        },
                        onCopyFile = { file ->
                            viewModel.copyFiles(
                                listOf(file.id),
                                file.folder,
                                file.parentFolderId,
                                file.folderPath
                            )
                        },
                        onDownloadFile = { file ->
                            viewModel.downloadFileLocally(file) { }
                        }
                    )
                }

                // Screen 15: Profile
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        user = user,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onNavigateToAppearance = {
                            navController.navigate(Screen.Appearance.route)
                        },
                        onNavigateToLanguage = {
                            navController.navigate(Screen.Language.route)
                        },
                        onNavigateToPrivacyPolicy = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        },
                        onLogoutClick = {
                            viewModel.setShowLogoutDialog(true)
                        },
                        onUpdateAvatarBitmap = { bitmap ->
                            viewModel.saveAvatarBitmap(bitmap)
                        },
                        onUpdateAvatarUri = { uri ->
                            viewModel.saveAvatarUri(uri)
                        },
                        onRemoveAvatar = {
                            viewModel.removeAvatar()
                        }
                    )
                }

                // Screen 16: Settings
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        user = user,
                        currentTheme = themeMode,
                        onThemeChange = { viewModel.setTheme(it) },
                        syncNetworkMode = vaultSyncNetworkMode,
                        onSyncNetworkModeChange = { viewModel.setVaultSyncNetworkMode(it) },
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onNavigateToNotifications = {
                            navController.navigate(Screen.Notifications.route)
                        },
                        onNavigateToAppearance = {
                            navController.navigate(Screen.Appearance.route)
                        },
                        onNavigateToLanguage = {
                            navController.navigate(Screen.Language.route)
                        },
                        onNavigateToPrivacyPolicy = {
                            navController.navigate(Screen.PrivacyPolicy.route)
                        }
                    )
                }

                // Screen 17: Notifications
                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 18: Appearance
                composable(Screen.Appearance.route) {
                    AppearanceScreen(
                        currentTheme = themeMode,
                        onThemeChange = { viewModel.setTheme(it) },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 19: Language
                composable(Screen.Language.route) {
                    LanguageScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                // Screen 20: Privacy Policy
                composable(Screen.PrivacyPolicy.route) {
                    PrivacyPolicyScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }

    // Logout Confirmation Dialog matching template
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowLogoutDialog(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Log Out",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out of Genzii?",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setShowLogoutDialog(false) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
