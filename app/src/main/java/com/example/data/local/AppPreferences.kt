package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.NotificationPreferences
import com.example.data.model.VaultSyncNetworkMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "genzii_settings")

class AppPreferences(private val context: Context) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val IS_PRO_KEY = booleanPreferencesKey("is_pro")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_AVATAR_KEY = stringPreferencesKey("user_avatar")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val PUSH_NOTIF_KEY = booleanPreferencesKey("push_notif")
        private val EMAIL_NOTIF_KEY = booleanPreferencesKey("email_notif")
        private val ACTIVITY_NOTIF_KEY = booleanPreferencesKey("activity_notif")
        private val SECURITY_NOTIF_KEY = booleanPreferencesKey("security_notif")
        private val VAULT_SYNC_NETWORK_KEY = stringPreferencesKey("vault_sync_network_mode")
        private val LAST_ACTIVE_ROUTE_KEY = stringPreferencesKey("last_active_route")
        private val BONUS_AI_CREDITS_KEY = androidx.datastore.preferences.core.intPreferencesKey("bonus_ai_credits")
        private val LAST_REPORT_ID_KEY = stringPreferencesKey("last_report_id")
        private val LAST_PREVIEW_FILE_ID_KEY = stringPreferencesKey("last_preview_file_id")
    }

    val lastActiveRouteFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LAST_ACTIVE_ROUTE_KEY] ?: "home"
    }

    val bonusAiCreditsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[BONUS_AI_CREDITS_KEY] ?: 0
    }

    val lastReportIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_REPORT_ID_KEY]
    }

    val lastPreviewFileIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[LAST_PREVIEW_FILE_ID_KEY]
    }

    val themeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            else -> AppThemeMode.LIGHT
        }
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    val hasCompletedOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED_KEY] ?: false
    }

    val isProFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PRO_KEY] ?: false
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: ""
    }

    val userEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY] ?: ""
    }

    val userAvatarFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_AVATAR_KEY]
    }

    val languageFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "English"
    }

    val notificationPreferencesFlow: Flow<NotificationPreferences> = context.dataStore.data.map { prefs ->
        NotificationPreferences(
            pushEnabled = prefs[PUSH_NOTIF_KEY] ?: true,
            emailEnabled = prefs[EMAIL_NOTIF_KEY] ?: true,
            activityEnabled = prefs[ACTIVITY_NOTIF_KEY] ?: true,
            securityEnabled = prefs[SECURITY_NOTIF_KEY] ?: true
        )
    }

    val vaultSyncNetworkFlow: Flow<VaultSyncNetworkMode> = context.dataStore.data.map { prefs ->
        VaultSyncNetworkMode.fromString(prefs[VAULT_SYNC_NETWORK_KEY])
    }

    suspend fun setVaultSyncNetworkMode(mode: VaultSyncNetworkMode) {
        context.dataStore.edit { it[VAULT_SYNC_NETWORK_KEY] = mode.name }
    }

    suspend fun setTheme(theme: AppThemeMode) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN_KEY] = loggedIn }
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
    }

    suspend fun setPro(isPro: Boolean) {
        context.dataStore.edit { it[IS_PRO_KEY] = isPro }
    }

    suspend fun setUserProfile(name: String, email: String, avatarUrl: String? = null) {
        context.dataStore.edit {
            it[USER_NAME_KEY] = name
            it[USER_EMAIL_KEY] = email
            if (avatarUrl != null) {
                it[USER_AVATAR_KEY] = avatarUrl
            }
        }
    }

    suspend fun updateUserAvatar(avatarUrl: String) {
        context.dataStore.edit {
            it[USER_AVATAR_KEY] = avatarUrl
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = language }
    }

    suspend fun updateNotifications(prefs: NotificationPreferences) {
        context.dataStore.edit {
            it[PUSH_NOTIF_KEY] = prefs.pushEnabled
            it[EMAIL_NOTIF_KEY] = prefs.emailEnabled
            it[ACTIVITY_NOTIF_KEY] = prefs.activityEnabled
            it[SECURITY_NOTIF_KEY] = prefs.securityEnabled
        }
    }

    suspend fun setLastActiveRoute(route: String) {
        context.dataStore.edit { it[LAST_ACTIVE_ROUTE_KEY] = route }
    }

    suspend fun addBonusAiCredits(credits: Int) {
        context.dataStore.edit {
            val current = it[BONUS_AI_CREDITS_KEY] ?: 0
            it[BONUS_AI_CREDITS_KEY] = current + credits
        }
    }

    suspend fun setLastReportId(reportId: String?) {
        context.dataStore.edit {
            if (reportId != null) {
                it[LAST_REPORT_ID_KEY] = reportId
            } else {
                it.remove(LAST_REPORT_ID_KEY)
            }
        }
    }

    suspend fun setLastPreviewFileId(fileId: String?) {
        context.dataStore.edit {
            if (fileId != null) {
                it[LAST_PREVIEW_FILE_ID_KEY] = fileId
            } else {
                it.remove(LAST_PREVIEW_FILE_ID_KEY)
            }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it[IS_LOGGED_IN_KEY] = false
            it[LAST_ACTIVE_ROUTE_KEY] = "home"
            it.remove(USER_NAME_KEY)
            it.remove(USER_EMAIL_KEY)
            it.remove(USER_AVATAR_KEY)
            it.remove(IS_PRO_KEY)
        }
    }
}
