package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object CreateAccount : Screen("create_account")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    
    // Main 5 Tabs
    object Home : Screen("home")
    object OriginalityChecker : Screen("checker")
    object CheckResults : Screen("check_results")
    object SmartAi : Screen("smart_ai")
    object Vault : Screen("vault")
    object DocumentPreview : Screen("document_preview")
    object Profile : Screen("profile")
    object ProfileMenu : Screen("profile_menu")
    
    // Sub screens with bottom bar
    object DocumentScanner : Screen("document_scanner")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Appearance : Screen("appearance")
    object Language : Screen("language")
    object HelpSupport : Screen("help_support")
    object LogoutConfirm : Screen("logout_confirm")
    object PrivacyPolicy : Screen("privacy_policy")
}
