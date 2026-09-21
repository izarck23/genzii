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
import com.example.data.model.OcrScanRecord
import com.example.data.model.OriginalityReport
import com.example.data.model.User
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.model.VaultSyncNetworkMode
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
        scope = viewModelScope,
        appPreferences = preferences
    )
    val vaultSyncService = VaultFirestoreSyncService(
        context = application,
        vaultDao = database.vaultDao(),
        vaultFolderDao = database.vaultFolderDao(),
        scope = viewModelScope,
        appPreferences = preferences
    )
    private val repository = GenziiRepository(
        context = application,
        originalityDao = database.originalityDao(),
        vaultDao = database.vaultDao(),
        vaultFolderDao = database.vaultFolderDao(),
        ocrScanDao = database.ocrScanDao(),
        appPreferences = preferences,
        vaultSyncService = vaultSyncService,
        firebaseVaultService = firebaseVaultService
    )
    private val chatRepository = AiChatRepository(
        chatMessageDao = database.chatMessageDao(),
        context = application
    )
    val authRepository = AuthRepository(
        context = application,
        appPreferences = preferences,
        userAccountDao = database.userAccountDao()
    )
    val userDataSyncService = com.example.data.sync.UserDataFirestoreSyncService(
        context = application,
        originalityDao = database.originalityDao(),
        chatMessageDao = database.chatMessageDao(),
        ocrScanDao = database.ocrScanDao(),
        appPreferences = preferences,
        scope = viewModelScope
    )

    val vaultSyncStatus: StateFlow<VaultSyncStatus> = firebaseVaultService.syncStatus
    val activeTransfers: StateFlow<Map<String, VaultTransferProgress>> = firebaseVaultService.activeTransfers

    init {
        viewModelScope.launch {
            try {
                authRepository.syncCurrentUserOnStartup()
                repository.preseedInitialDataIfEmpty()
                repository.syncDeviceStorageFiles()
                scanDeviceStorage()
                repository.syncVaultWithCloud()
                userDataSyncService.syncAllHistoryOnStartup()
            } catch (e: Exception) {
                android.util.Log.w("GenziiViewModel", "Startup sync handled gracefully: ${e.message}")
            }
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

    val vaultSyncNetworkMode: StateFlow<VaultSyncNetworkMode> = preferences.vaultSyncNetworkFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = VaultSyncNetworkMode.WIFI_ONLY
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

    val ocrScans: StateFlow<List<OcrScanRecord>> = repository.ocrScansFlow.stateIn(
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

    private val _originalityError = MutableStateFlow<String?>(null)
    val originalityError: StateFlow<String?> = _originalityError.asStateFlow()

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

    private val _deviceStorageFiles = MutableStateFlow<List<VaultItem>>(emptyList())
    val deviceStorageFiles: StateFlow<List<VaultItem>> = _deviceStorageFiles.asStateFlow()

    private val _isScanningDeviceStorage = MutableStateFlow(false)
    val isScanningDeviceStorage: StateFlow<Boolean> = _isScanningDeviceStorage.asStateFlow()

    private val _deviceStorageFilter = MutableStateFlow("All")
    val deviceStorageFilter: StateFlow<String> = _deviceStorageFilter.asStateFlow()

    val lastActiveRoute: StateFlow<String> = preferences.lastActiveRouteFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "home"
    )

    val bonusAiCredits: StateFlow<Int> = preferences.bonusAiCreditsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        viewModelScope.launch {
            authRepository.syncCurrentUserOnStartup()
            repository.preseedInitialDataIfEmpty()
            val initialMsgs = chatRepository.messagesFlow.first()
            chatRepository.preseedWelcomeIfEmpty(initialMsgs.size)
            scanDeviceStorage()

            // Restore last active report and preview file so pages reload accurately after restart
            launch {
                val lastRepId = preferences.lastReportIdFlow.first()
                originalityChecks.collect { reports ->
                    if (_currentReport.value == null && reports.isNotEmpty()) {
                        val matching = if (lastRepId != null) reports.find { it.id == lastRepId } else null
                        _currentReport.value = matching ?: reports.first()
                    }
                }
            }
            launch {
                val lastFileId = preferences.lastPreviewFileIdFlow.first()
                vaultFiles.collect { files ->
                    if (_previewFile.value == null && files.isNotEmpty()) {
                        val matching = if (lastFileId != null) files.find { it.id == lastFileId } else null
                        _previewFile.value = matching ?: files.first()
                    }
                }
            }
        }
    }

    fun setLastActiveRoute(route: String) {
        val transientRoutes = setOf("splash", "welcome", "login", "create_account", "forgot_password", "reset_password")
        if (route !in transientRoutes && route.isNotBlank()) {
            viewModelScope.launch {
                preferences.setLastActiveRoute(route)
            }
        }
    }

    fun addBonusAiCredits(credits: Int) {
        viewModelScope.launch {
            preferences.addBonusAiCredits(credits)
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
        viewModelScope.launch {
            preferences.setLastPreviewFileId(file?.id)
        }
    }

    fun selectReport(reportId: String) {
        val found = originalityChecks.value.find { it.id == reportId }
        _currentReport.value = found ?: originalityChecks.value.firstOrNull()
        viewModelScope.launch {
            preferences.setLastReportId(reportId)
        }
    }

    private var aiJob: kotlinx.coroutines.Job? = null

    fun clearOriginalityError() {
        _originalityError.value = null
    }

    fun runOriginalityCheck(title: String, content: String, onFinished: (String) -> Unit) {
        if (content.isBlank()) return
        val isOnline = com.example.util.NetworkUtils.isNetworkAvailable(getApplication())
        if (!isOnline) {
            _originalityError.value = "Originality checking is online-only. Please connect to the internet to query academic databases."
            return
        }
        viewModelScope.launch {
            _isCheckingOriginality.value = true
            _originalityError.value = null
            try {
                val report = repository.checkOriginality(title, content)
                _currentReport.value = report
                preferences.setLastReportId(report.id)
                database.originalityDao().getCheckById(report.id)?.let { entity ->
                    userDataSyncService.syncOriginalityCheck(entity)
                }
                _isCheckingOriginality.value = false
                onFinished(report.id)
            } catch (e: Exception) {
                _isCheckingOriginality.value = false
                _originalityError.value = "Originality check failed: ${e.localizedMessage ?: "Unable to contact verification servers"}. Please retry."
            }
        }
    }

    fun deleteOriginalityReport(reportId: String) {
        viewModelScope.launch {
            repository.deleteOriginalityCheck(reportId)
            userDataSyncService.deleteOriginalityCheckFromCloud(reportId)
            if (_currentReport.value?.id == reportId) {
                _currentReport.value = originalityChecks.value.firstOrNull { it.id != reportId }
            }
        }
    }

    fun clearAllOriginalityReports() {
        viewModelScope.launch {
            repository.clearAllOriginalityChecks()
            _currentReport.value = null
        }
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank() || _isAiGenerating.value) return
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _isAiGenerating.value = true
            chatRepository.sendMessage(prompt, _currentPersona.value, _currentTone.value)
            _isAiGenerating.value = false
            try {
                database.chatMessageDao().getAllMessages().first().takeLast(2).forEach { entity ->
                    userDataSyncService.syncChatMessage(entity)
                }
            } catch (e: Exception) {
                // Ignore background sync errors
            }
        }
    }

    fun deleteChatMessage(id: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
            userDataSyncService.deleteChatMessageFromCloud(id)
        }
    }

    fun retryAiMessage(failedMessage: ChatMessage) {
        val prompt = failedMessage.retryPrompt ?: return
        sendAiMessage(prompt)
    }

    fun cancelAiGeneration() {
        aiJob?.cancel()
        _isAiGenerating.value = false
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearHistory()
            userDataSyncService.clearChatMessagesFromCloud()
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

    fun scanDeviceStorage(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            _isScanningDeviceStorage.value = true
            try {
                val files = repository.getDeviceStorageFiles()
                _deviceStorageFiles.value = files
                onComplete(files.size)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(0)
            } finally {
                _isScanningDeviceStorage.value = false
            }
        }
    }

    fun setDeviceStorageFilter(filter: String) {
        _deviceStorageFilter.value = filter
    }

    fun importDeviceFileToVault(item: VaultItem, targetFolder: String = "Assignments", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.importDeviceFileToVault(item, targetFolder)
            onComplete()
        }
    }

    fun scanAndSyncDeviceMedia(onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = repository.syncDeviceStorageFiles()
            scanDeviceStorage()
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

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_selectedFolder.value == folderId) {
                _selectedFolder.value = null
            }
        }
    }

    fun moveFolder(folderId: String, newParentFolderId: String?, newPath: String) {
        viewModelScope.launch {
            repository.moveFolder(folderId, newParentFolderId, newPath)
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

    fun saveOcrScan(
        title: String,
        extractedText: String,
        confidencePct: Int = 95,
        imagePath: String? = null,
        onComplete: ((OcrScanRecord) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val record = repository.saveOcrScan(title, extractedText, confidencePct, imagePath)
            database.ocrScanDao().getScanById(record.id)?.let { entity ->
                userDataSyncService.syncOcrScan(entity)
            }
            onComplete?.invoke(record)
        }
    }

    fun deleteOcrScan(id: String) {
        viewModelScope.launch {
            repository.deleteOcrScan(id)
            userDataSyncService.deleteOcrScanFromCloud(id)
        }
    }

    fun saveOcrScanToVault(
        scanId: String,
        title: String,
        content: String,
        onComplete: ((VaultItem) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val item = repository.saveOcrScanToVault(scanId, title, content)
            onComplete?.invoke(item)
        }
    }

    fun setTheme(theme: AppThemeMode) {
        viewModelScope.launch {
            preferences.setTheme(theme)
            syncPreferencesToCloud()
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferences.setLanguage(language)
            syncPreferencesToCloud()
        }
    }

    fun updateNotifications(prefs: NotificationPreferences) {
        viewModelScope.launch {
            preferences.updateNotifications(prefs)
            syncPreferencesToCloud()
        }
    }

    fun setVaultSyncNetworkMode(mode: VaultSyncNetworkMode) {
        viewModelScope.launch {
            preferences.setVaultSyncNetworkMode(mode)
            repository.syncVaultWithCloud()
            syncPreferencesToCloud()
        }
    }

    private fun syncPreferencesToCloud() {
        viewModelScope.launch {
            try {
                userDataSyncService.syncProfileAndPreferences(
                    user = user.value,
                    theme = theme.value.name,
                    language = language.value,
                    vaultSyncMode = vaultSyncNetworkMode.value.name,
                    notifications = notificationPrefs.value
                )
            } catch (e: Exception) {
                // Ignore background sync errors
            }
        }
    }
}
