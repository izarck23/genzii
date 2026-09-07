package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.model.AiPersona
import com.example.data.model.AiTone
import com.example.data.model.AppThemeMode
import com.example.data.model.ChatMessage
import com.example.data.model.NotificationPreferences
import com.example.data.model.OriginalityReport
import com.example.data.model.User
import com.example.data.model.VaultItem
import com.example.data.repository.AiChatRepository
import com.example.data.repository.GenziiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GenziiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferences = AppPreferences(application)

    private val repository = GenziiRepository(
        originalityDao = database.originalityDao(),
        vaultDao = database.vaultDao(),
        appPreferences = preferences
    )

    private val chatRepository = AiChatRepository(
        chatMessageDao = database.chatMessageDao()
    )

    val user: StateFlow<User> = repository.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = User()
    )

    val theme: StateFlow<AppThemeMode> = preferences.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppThemeMode.LIGHT
    )

    val isLoggedIn: StateFlow<Boolean> = preferences.isLoggedInFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isPro: StateFlow<Boolean> = preferences.isProFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val language: StateFlow<String> = preferences.languageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "English"
    )

    val notificationPrefs: StateFlow<NotificationPreferences> = preferences.notificationPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationPreferences()
    )

    val originalityChecks: StateFlow<List<OriginalityReport>> = repository.originalityChecksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val vaultFiles: StateFlow<List<VaultItem>> = repository.vaultFilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.messagesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Interactive UI states
    private val _currentReport = MutableStateFlow<OriginalityReport?>(null)
    val currentReport: StateFlow<OriginalityReport?> = _currentReport.asStateFlow()

    private val _isCheckingOriginality = MutableStateFlow(false)
    val isCheckingOriginality: StateFlow<Boolean> = _isCheckingOriginality.asStateFlow()

    private val _currentPersona = MutableStateFlow(AiPersona.ASSISTANT)
    val currentPersona: StateFlow<AiPersona> = _currentPersona.asStateFlow()

    private val _currentTone = MutableStateFlow(AiTone.ACADEMIC)
    val currentTone: StateFlow<AiTone> = _currentTone.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _vaultSearchQuery = MutableStateFlow("")
    val vaultSearchQuery: StateFlow<String> = _vaultSearchQuery.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder: StateFlow<String?> = _selectedFolder.asStateFlow()

    private val _deleteCandidateFile = MutableStateFlow<VaultItem?>(null)
    val deleteCandidateFile: StateFlow<VaultItem?> = _deleteCandidateFile.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preseedInitialDataIfEmpty()
            val initialMsgs = chatRepository.messagesFlow.first()
            chatRepository.preseedWelcomeIfEmpty(initialMsgs.size)
        }
    }

    fun login(email: String, name: String = "John Doe") {
        viewModelScope.launch {
            preferences.setUserProfile(name = name.ifBlank { "John Doe" }, email = email.ifBlank { "john@example.com" })
            preferences.setLoggedIn(true)
        }
    }

    fun register(name: String, email: String) {
        viewModelScope.launch {
            preferences.setUserProfile(name = name, email = email)
            preferences.setLoggedIn(true)
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferences.setLoggedIn(false)
            _showLogoutDialog.value = false
        }
    }

    fun setShowLogoutDialog(show: Boolean) {
        _showLogoutDialog.value = show
    }

    fun setCandidateForDeletion(file: VaultItem?) {
        _deleteCandidateFile.value = file
    }

    fun deleteConfirmedFile() {
        val file = _deleteCandidateFile.value ?: return
        viewModelScope.launch {
            repository.deleteVaultFile(file.id)
            _deleteCandidateFile.value = null
        }
    }

    fun runOriginalityCheck(title: String, content: String, onFinished: (String) -> Unit) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isCheckingOriginality.value = true
            val report = repository.checkOriginality(title, content)
            _currentReport.value = report
            _isCheckingOriginality.value = false
            onFinished(report.id)
        }
    }

    fun selectReport(reportId: String) {
        val found = originalityChecks.value.find { it.id == reportId }
        _currentReport.value = found
    }

    fun setAiPersona(persona: AiPersona) {
        _currentPersona.value = persona
    }

    fun setAiTone(tone: AiTone) {
        _currentTone.value = tone
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || _isAiGenerating.value) return
        viewModelScope.launch {
            _isAiGenerating.value = true
            chatRepository.sendMessage(prompt, _currentPersona.value, _currentTone.value)
            _isAiGenerating.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearHistory()
        }
    }

    fun saveMessageToVault(message: ChatMessage, title: String = "AI Conversation Note") {
        viewModelScope.launch {
            repository.saveNoteToVault(title, message.text, "Notes")
        }
    }

    fun saveNewNote(title: String, content: String, folder: String = "Notes") {
        viewModelScope.launch {
            repository.saveNoteToVault(title, content, folder)
        }
    }

    fun setVaultSearchQuery(query: String) {
        _vaultSearchQuery.value = query
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun toggleFavoriteFile(file: VaultItem) {
        viewModelScope.launch {
            repository.toggleFavoriteFile(file.id, !file.isFavorite)
        }
    }

    fun setTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            preferences.setTheme(theme)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
        }
    }

    fun updateNotifications(prefs: NotificationPreferences) {
        viewModelScope.launch {
            preferences.updateNotifications(prefs)
        }
    }

    fun upgradeToPro() {
        viewModelScope.launch {
            preferences.setPro(true)
        }
    }

    fun cancelPro() {
        viewModelScope.launch {
            preferences.setPro(false)
        }
    }
}
