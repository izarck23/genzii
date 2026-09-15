package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.repository.AiChatRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.GenziiRepository
import com.example.data.service.StorageFileService
import com.example.data.sync.FirebaseVaultService
import com.example.data.sync.VaultFirestoreSyncService
import com.example.data.sync.VaultSyncStatus
import com.example.data.sync.VaultTransferProgress
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
    val firebaseVaultService = FirebaseVaultService(
        context = application,
        vaultDao = database.vaultDao(),
        vaultFolderDao = database.vaultFolderDao(),
        scope = viewModelScope
    )
    val vaultSyncService = VaultFirestoreSyncService(
        context = application,
        vaultDao = database.vaultDao(),
        vaultFolderDao = database.vaultFolderDao(),
        scope = viewModelScope
    )
    private val repository = GenziiRepository(
        context = application,
        originalityDao = database.originalityDao(),
        vaultDao = database.vaultDao(),
        vaultFolderDao = database.vaultFolderDao(),
        appPreferences = preferences,
        vaultSyncService = vaultSyncService,
        firebaseVaultService = firebaseVaultService
    )
    private val chatRepository = AiChatRepository(
        chatMessageDao = database.chatMessageDao()
    )
    val authRepository = AuthRepository(
        context = application,
        appPreferences = preferences,
        userAccountDao = database.userAccountDao()
    )

    val vaultSyncStatus: StateFlow<VaultSyncStatus> = firebaseVaultService.syncStatus
    val activeTransfers: StateFlow<Map<String, VaultTransferProgress>> = firebaseVaultService.activeTransfers

    init {
        viewModelScope.launch {
            repository.syncVaultWithCloud()
        }
    }

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

    val hasCompletedOnboarding: StateFlow<Boolean> = preferences.hasCompletedOnboardingFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
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

    val trashFiles: StateFlow<List<VaultItem>> = repository.trashFilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteFiles: StateFlow<List<VaultItem>> = repository.favoriteFilesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val vaultFolders: StateFlow<List<VaultFolder>> = repository.vaultFoldersFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.messagesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
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

    private val _previewFile = MutableStateFlow<VaultItem?>(null)
    val previewFile: StateFlow<VaultItem?> = _previewFile.asStateFlow()

    private val _showLogoutDialog = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.syncCurrentUserOnStartup()
            repository.preseedInitialDataIfEmpty()
            val initialMsgs = chatRepository.messagesFlow.first()
            chatRepository.preseedWelcomeIfEmpty(initialMsgs.size)
        }
    }

    fun signInWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Google Sign-In failed.")
            }
        }
    }

    fun signInWithFacebook(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithFacebook(context)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Facebook Sign-In failed.")
            }
        }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Invalid email or password.")
            }
        }
    }

    fun signUpWithEmail(
        name: String,
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(name, email, pass)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Sign up failed.")
            }
        }
    }

    fun resetPassword(
        email: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                onSuccess(result.getOrNull() ?: "Password reset instructions sent.")
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to send reset email.")
            }
        }
    }

    fun updatePassword(
        newPass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.updatePassword(newPass)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to update password.")
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            _showLogoutDialog.value = false
            onComplete()
        }
    }

    fun setShowLogoutDialog(show: Boolean) {
        _showLogoutDialog.value = show
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            authRepository.updateDisplayName(name, user.value.email)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setCompletedOnboarding(true)
        }
    }

    fun setPreviewFile(file: VaultItem?) {
        _previewFile.value = file
    }

    fun selectReport(reportId: String) {
        val found = originalityChecks.value.find { it.id == reportId }
        _currentReport.value = found ?: originalityChecks.value.firstOrNull()
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

    fun setAiPersona(persona: AiPersona) {
        _currentPersona.value = persona
    }

    fun setVaultSearchQuery(query: String) {
        _vaultSearchQuery.value = query
    }

    fun setSelectedFolder(folder: String?) {
        _selectedFolder.value = folder
    }

    fun createVaultFolder(name: String, colorHex: Long = 0xFF005AC1, isSecured: Boolean = false) {
        viewModelScope.launch {
            repository.createFolder(name = name, colorHex = colorHex, isSecured = isSecured)
        }
    }

    fun deleteVaultFolder(name: String) {
        viewModelScope.launch {
            repository.deleteFolder(name)
            if (_selectedFolder.value == name) {
                _selectedFolder.value = null
            }
        }
    }

    fun scanAndSyncDeviceMedia(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.syncDeviceStorageFiles()
            onComplete(count)
        }
    }

    fun importFilesFromUris(
        uris: List<Uri>,
        targetFolderId: String? = null,
        targetFolderName: String = "Assignments",
        targetFolderPath: String = "/Assignments"
    ) {
        viewModelScope.launch {
            uris.forEach { uri ->
                repository.uploadFile(
                    uri = uri,
                    targetFolderId = targetFolderId,
                    targetFolderName = targetFolderName,
                    targetFolderPath = targetFolderPath
                )
            }
        }
    }

    fun setFileSecured(fileId: String, isSecured: Boolean) {
        viewModelScope.launch {
            repository.setFileSecured(fileId, isSecured)
            val current = _previewFile.value
            if (current != null && current.id == fileId) {
                _previewFile.value = current.copy(isSecured = isSecured)
            }
        }
    }

    fun createFolder(
        name: String,
        parentFolderId: String? = null,
        parentPath: String = "",
        colorHex: Long = 0xFF005AC1,
        isSecured: Boolean = false
    ) {
        viewModelScope.launch {
            repository.createFolder(
                name = name,
                parentFolderId = parentFolderId,
                parentPath = parentPath,
                colorHex = colorHex,
                isSecured = isSecured
            )
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            repository.renameFolder(folderId, newName)
        }
    }

    fun renameFile(fileId: String, newName: String) {
        viewModelScope.launch {
            repository.renameFile(fileId, newName)
            val current = _previewFile.value
            if (current != null && current.id == fileId) {
                _previewFile.value = current.copy(name = newName)
            }
        }
    }

    fun setFilesSecured(fileIds: List<String>, isSecured: Boolean) {
        setMultipleFilesSecured(fileIds, isSecured)
    }

    fun setMultipleFilesSecured(fileIds: List<String>, isSecured: Boolean) {
        viewModelScope.launch {
            repository.setFilesSecured(fileIds, isSecured)
        }
    }

    fun moveFilesToFolder(
        fileIds: List<String>,
        targetFolder: String,
        targetFolderId: String? = null,
        targetFolderPath: String = ""
    ) {
        viewModelScope.launch {
            repository.moveFilesToFolder(
                fileIds = fileIds,
                newFolder = targetFolder,
                targetFolderId = targetFolderId,
                targetFolderPath = targetFolderPath
            )
        }
    }

    fun copyFiles(
        fileIds: List<String>,
        targetFolder: String,
        targetFolderId: String? = null,
        targetFolderPath: String = ""
    ) {
        viewModelScope.launch {
            repository.copyFiles(
                fileIds = fileIds,
                targetFolderName = targetFolder,
                targetFolderId = targetFolderId,
                targetFolderPath = targetFolderPath
            )
        }
    }

    fun trashFiles(fileIds: List<String>) {
        viewModelScope.launch {
            repository.trashFiles(fileIds)
            if (_previewFile.value?.id in fileIds) {
                _previewFile.value = null
            }
        }
    }

    fun restoreFiles(fileIds: List<String>) {
        viewModelScope.launch {
            repository.restoreFiles(fileIds)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun deletePermanently(fileId: String) {
        viewModelScope.launch {
            repository.deletePermanently(fileId)
            if (_previewFile.value?.id == fileId) {
                _previewFile.value = null
            }
        }
    }

    fun downloadFileLocally(file: VaultItem, onDone: (java.io.File?) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.downloadFileLocally(file)
            val localFile = result.getOrNull()
            if (localFile != null && _previewFile.value?.id == file.id) {
                _previewFile.value = _previewFile.value?.copy(localFilePath = localFile.absolutePath)
            }
            onDone(localFile)
        }
    }

    fun deleteFiles(fileIds: List<String>) {
        trashFiles(fileIds)
    }

    fun deleteMultipleFiles(fileIds: List<String>) {
        trashFiles(fileIds)
    }

    fun syncVaultNow() {
        viewModelScope.launch {
            repository.syncVaultWithCloud()
        }
    }

    fun updateUserAvatar(avatarUriOrPath: String) {
        viewModelScope.launch {
            authRepository.updateAvatar(avatarUriOrPath, user.value.email)
        }
    }

    fun saveAvatarBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val path = StorageFileService.saveAvatarBitmap(getApplication(), bitmap)
            authRepository.updateAvatar(path, user.value.email)
        }
    }

    fun saveAvatarUri(uri: Uri) {
        viewModelScope.launch {
            val path = StorageFileService.saveAvatarFromUri(getApplication(), uri)
            authRepository.updateAvatar(path, user.value.email)
        }
    }

    fun removeAvatar() {
        viewModelScope.launch {
            authRepository.updateAvatar("", user.value.email)
        }
    }

    fun toggleFavoriteFile(file: VaultItem) {
        viewModelScope.launch {
            repository.toggleFavoriteFile(file.id, !file.isFavorite)
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            repository.deleteVaultFile(fileId)
            if (_previewFile.value?.id == fileId) {
                _previewFile.value = null
            }
        }
    }

    fun saveNewNote(title: String, content: String, folder: String = "Notes") {
        viewModelScope.launch {
            repository.saveNoteToVault(title, content, folder)
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
}
