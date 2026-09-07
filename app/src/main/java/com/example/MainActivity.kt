package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.AppThemeMode
import com.example.ui.components.ConfirmLogoutDialog
import com.example.ui.components.DeleteFileDialog
import com.example.ui.components.GenziiBottomBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.auth.CreateAccountScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.ResetPasswordScreen
import com.example.ui.screens.chat.SmartAiChatScreen
import com.example.ui.screens.checker.CheckResultsScreen
import com.example.ui.screens.checker.OriginalityCheckerScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.premium.PremiumScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.settings.AppearanceScreen
import com.example.ui.screens.settings.HelpSupportScreen
import com.example.ui.screens.settings.LanguageScreen
import com.example.ui.screens.settings.NotificationsScreen
import com.example.ui.screens.settings.PrivacyPolicyScreen
import com.example.ui.screens.settings.RateUsScreen
import com.example.ui.screens.settings.TermsOfServiceScreen
import com.example.ui.screens.tools.OcrScannerScreen
import com.example.ui.screens.tools.PdfToolsScreen
import com.example.ui.screens.vault.VaultScreen
import com.example.ui.theme.GenziiTheme
import com.example.ui.viewmodel.GenziiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GenziiViewModel = viewModel()
            val themeMode by viewModel.theme.collectAsState()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            GenziiTheme(darkTheme = isDarkTheme) {
                GenziiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun GenziiApp(viewModel: GenziiViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    val deleteCandidateFile by viewModel.deleteCandidateFile.collectAsState()

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.OriginalityChecker.route,
        Screen.SmartAi.route,
        Screen.Vault.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar && currentRoute != null) {
                GenziiBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
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
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        if (isLoggedIn) {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Screen 2: Welcome Screen
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                    onContinueAsGuestClick = {
                        viewModel.login("guest@genzii.ai", "Guest Student")
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Screen 3: Onboarding Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onGetStarted = { navController.navigate(Screen.CreateAccount.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) }
                )
            }

            // Screen 4: Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginSuccess = { email ->
                        viewModel.login(email)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            }

            // Screen 5: Create Account Screen
            composable(Screen.CreateAccount.route) {
                CreateAccountScreen(
                    onBackClick = { navController.popBackStack() },
                    onRegisterSuccess = { name, email ->
                        viewModel.register(name, email)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onTermsClick = { navController.navigate(Screen.TermsOfService.route) },
                    onPrivacyClick = { navController.navigate(Screen.PrivacyPolicy.route) }
                )
            }

            // Screen 6: Forgot Password Screen
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() },
                    onResetSent = { navController.navigate(Screen.ResetPassword.route) },
                    onLoginClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Reset Password Screen
            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    onBackClick = { navController.popBackStack() },
                    onPasswordResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Screen 7 & 8: Main Dashboard Workspace
            composable(Screen.Home.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Screen 9: Originality Checker Screen
            composable(Screen.OriginalityChecker.route) {
                OriginalityCheckerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToResults = { reportId ->
                        navController.navigate(Screen.CheckResults.createRoute(reportId))
                    },
                    onNavigateToUpgrade = { navController.navigate(Screen.Premium.route) }
                )
            }

            // Screen 10 & 11: Check Results & Highlights
            composable(
                route = Screen.CheckResults.route,
                arguments = listOf(navArgument("reportId") { type = NavType.StringType })
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
                CheckResultsScreen(
                    reportId = reportId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Screen 12 & 13: Smart AI Chat
            composable(Screen.SmartAi.route) {
                SmartAiChatScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Screen 14 & 15: Vault Document Management
            composable(Screen.Vault.route) {
                VaultScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToUpgrade = { navController.navigate(Screen.Premium.route) }
                )
            }

            // Screen 15 & 16: Profile & Account Settings
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // OCR Scanner Tool
            composable(Screen.OcrScanner.route) {
                OcrScannerScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onSendToChecker = { text ->
                        viewModel.runOriginalityCheck(
                            title = "Scanned OCR Document",
                            content = text
                        ) { reportId ->
                            navController.navigate(Screen.CheckResults.createRoute(reportId))
                        }
                    }
                )
            }

            // PDF Tools Suite
            composable(Screen.PdfTools.route) {
                PdfToolsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToVault = {
                        navController.navigate(Screen.Vault.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            // Settings: Notifications
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings: Appearance (Theme)
            composable(Screen.Appearance.route) {
                AppearanceScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings: Language
            composable(Screen.Language.route) {
                LanguageScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings: Rate Us
            composable(Screen.RateUs.route) {
                RateUsScreen(onBackClick = { navController.popBackStack() })
            }

            // Settings: Help & Support
            composable(Screen.HelpSupport.route) {
                HelpSupportScreen(onBackClick = { navController.popBackStack() })
            }

            // Legal: Privacy Policy
            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
            }

            // Legal: Terms of Service
            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen(onBackClick = { navController.popBackStack() })
            }

            // Monetization: Premium Upgrade
            composable(Screen.Premium.route) {
                PremiumScreen(
                    viewModel = viewModel,
                    onCloseClick = { navController.popBackStack() }
                )
            }
        }
    }

    // Modal Confirmation Dialogs at root
    if (showLogoutDialog) {
        ConfirmLogoutDialog(
            onDismiss = { viewModel.setShowLogoutDialog(false) },
            onConfirm = {
                viewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    deleteCandidateFile?.let { fileToDelete ->
        DeleteFileDialog(
            file = fileToDelete,
            onDismiss = { viewModel.setCandidateForDeletion(null) },
            onConfirm = { viewModel.deleteConfirmedFile() }
        )
    }
}
