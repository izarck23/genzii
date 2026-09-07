package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppThemeMode
import com.example.data.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "genzii_settings")

class AppPreferences(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val IS_PRO_KEY = booleanPreferencesKey("is_pro")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val PUSH_NOTIF_KEY = booleanPreferencesKey("push_notif")
        private val EMAIL_NOTIF_KEY = booleanPreferencesKey("email_notif")
        private val ACTIVITY_NOTIF_KEY = booleanPreferencesKey("activity_notif")
        private val SECURITY_NOTIF_KEY = booleanPreferencesKey("security_notif")
    }

    val themeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[THEME_KEY]) {
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            else -> AppThemeMode.LIGHT // Default to clean light academic style matching reference
        }
    }

    val isLoggedInFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN_KEY] ?: false
    }

    val isProFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_PRO_KEY] ?: false
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_NAME_KEY] ?: "John Doe"
    }

    val userEmailFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[USER_EMAIL_KEY] ?: "john@example.com"
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

    suspend fun setTheme(theme: AppThemeMode) {
        context.dataStore.edit { it[THEME_KEY] = theme.name }
    }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        context.dataStore.edit { it[IS_LOGGED_IN_KEY] = loggedIn }
    }

    suspend fun setPro(isPro: Boolean) {
        context.dataStore.edit { it[IS_PRO_KEY] = isPro }
    }

    suspend fun setUserProfile(name: String, email: String) {
        context.dataStore.edit {
            it[USER_NAME_KEY] = name
            it[USER_EMAIL_KEY] = email
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
}
