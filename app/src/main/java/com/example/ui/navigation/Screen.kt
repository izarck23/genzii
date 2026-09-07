package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object CreateAccount : Screen("create_account")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    
    // Main Bottom Navigation Screens
    object Home : Screen("home")
    object OriginalityChecker : Screen("checker")
    object CheckResults : Screen("check_results/{reportId}") {
        fun createRoute(reportId: String) = "check_results/$reportId"
    }
    object SmartAi : Screen("smart_ai")
    object Vault : Screen("vault")
    object Profile : Screen("profile")
    
    // Tools
    object OcrScanner : Screen("ocr_scanner")
    object PdfTools : Screen("pdf_tools")
    
    // Settings & Account
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object Appearance : Screen("appearance")
    object Language : Screen("language")
    object RateUs : Screen("rate_us")
    object ShareApp : Screen("share_app")
    object HelpSupport : Screen("help_support")
    
    // Legal & Monetization
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsOfService : Screen("terms_of_service")
    object Premium : Screen("premium")
}
