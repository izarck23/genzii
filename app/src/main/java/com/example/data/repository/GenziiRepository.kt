package com.example.data.repository

import android.content.Context
import com.example.data.local.AppPreferences
import com.example.data.local.OriginalityDao
import com.example.data.local.OriginalityEntity
import com.example.data.local.VaultDao
import com.example.data.local.VaultFileEntity
import com.example.data.local.VaultFolderDao
import com.example.data.local.VaultFolderEntity
import com.example.data.model.FileCategory
import com.example.data.model.OriginalityReport
import com.example.data.model.SourceItem
import com.example.data.model.User
import com.example.data.model.VaultFolder
import com.example.data.model.VaultItem
import com.example.data.service.StorageFileService
import com.example.data.sync.FirebaseVaultService
import com.example.data.sync.VaultFirestoreSyncService
import com.example.data.sync.VaultSyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import java.io.File
import java.util.UUID

class GenziiRepository(
    private val context: Context? = null,
    private val originalityDao: OriginalityDao,
    private val vaultDao: VaultDao,
    private val vaultFolderDao: VaultFolderDao,
    private val appPreferences: AppPreferences,
    private val vaultSyncService: VaultFirestoreSyncService? = null,
    private val firebaseVaultService: FirebaseVaultService? = null
) {
    private val originalityEngine = OriginalityRepository(originalityDao)

    val originalityChecksFlow: Flow<List<OriginalityReport>> = originalityDao.getAllChecks().map { list ->
        list.map { entity ->
            OriginalityReport(
                id = entity.id,
                title = entity.title,
                content = entity.content,
                score = entity.score,
                similarityPercentage = entity.similarityPercentage,
                repeatedPercentage = entity.repeatedPercentage,
                uniquePercentage = entity.uniquePercentage,
                sourcesCount = entity.sourcesCount,
                wordCount = entity.wordCount,
                charCount = entity.charCount,
                dateString = entity.dateString,
                sources = parseSources(entity.sourcesJson),
                highlights = parseHighlights(entity.highlightsJson),
                repeatedSentences = parseHighlights(entity.highlightsJson).map { it.matchedText }.ifEmpty {
                    entity.content.split(Regex("(?<=[.!?])\\s+")).filter { it.length > 25 }.take(2)
                },
                status = entity.status
            )
        }
    }

    private fun parseHighlights(json: String): List<com.example.data.model.HighlightedMatch> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<com.example.data.model.HighlightedMatch>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.data.model.HighlightedMatch(
                        matchedText = obj.optString("matchedText", ""),
                        sourceTitle = obj.optString("sourceTitle", "Academic Source"),
                        sourceUrl = obj.optString("sourceUrl", ""),
                        similarityPct = obj.optInt("similarityPct", 0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseSources(json: String): List<SourceItem> {
        if (json.isBlank() || json == "[]") return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<SourceItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SourceItem(
                        id = obj.optString("id", "src-$i"),
                        title = obj.optString("title", "Academic Source"),
                        url = obj.optString("url", ""),
                        domain = obj.optString("domain", "web"),
                        similarityPct = obj.optInt("similarityPct", 0)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun VaultFileEntity.toVaultItem(): VaultItem {
        val extUpper = extension.uppercase()
        val cat = when (extUpper) {
            "PDF" -> FileCategory.PDF
            "DOC", "DOCX" -> FileCategory.DOCX
            "XLS", "XLSX", "CSV" -> FileCategory.XLSX
            "PPT", "PPTX" -> FileCategory.PPTX
            "PNG", "JPG", "JPEG", "WEBP", "IMG", "IMAGE" -> FileCategory.IMAGE
            "MP4", "MKV", "MOV", "AVI", "VID", "VIDEO" -> FileCategory.VIDEO
            "MP3", "WAV", "M4A", "AUD", "AUDIO" -> FileCategory.AUDIO
            "NOTE" -> FileCategory.NOTE
            "TXT" -> FileCategory.TXT
            else -> StorageFileService.determineCategory(name, mimeType)
        }
        return VaultItem(
            id = id,
            name = name,
            category = cat,
            sizeString = sizeString,
            sizeBytes = sizeBytes,
            timeAgo = timeAgo,
            folder = folder,
            parentFolderId = parentFolderId,
            folderPath = folderPath,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            ownerUid = ownerUid,
            content = content,
            tag = tag,
            isFavorite = isFavorite,
            isSecured = isSecured,
            isTrash = isTrash,
            isCachedOffline = isCachedOffline,
            localFilePath = localFilePath,
            mimeType = mimeType,
            createdAt = createdAt,
            lastModifiedTimestamp = lastModifiedTimestamp
        )
    }

    val vaultFilesFlow: Flow<List<VaultItem>> = vaultDao.getAllFiles().map { list ->
        list.map { it.toVaultItem() }
    }

    val trashFilesFlow: Flow<List<VaultItem>> = vaultDao.getTrashFiles().map { list ->
        list.map { it.toVaultItem() }
    }

    val favoriteFilesFlow: Flow<List<VaultItem>> = vaultDao.getFavoriteFiles().map { list ->
        list.map { it.toVaultItem() }
    }

    val vaultFoldersFlow: Flow<List<VaultFolder>> = combine(
        vaultFolderDao.getAllFolders(),
        vaultDao.getAllFiles()
    ) { folderEntities, files ->
        val result = mutableListOf<VaultFolder>()
        // Root "All Files" with real count of active files
        result.add(
            VaultFolder(
                id = "root_all_files",
                name = "All Files",
                count = files.size,
                iconColorHex = 0xFFF59E0B,
                bgColorHex = 0xFFFFFBEB,
                isSecured = false
            )
        )
        // Dynamic folders with actual count from files in the vault
        folderEntities.forEach { fe ->
            val count = files.count { it.folder.equals(fe.name, ignoreCase = true) || it.parentFolderId == fe.id }
            result.add(
                VaultFolder(
                    id = fe.id,
                    name = fe.name,
                    parentFolderId = fe.parentFolderId,
                    path = fe.path,
                    count = count,
                    iconColorHex = fe.iconColorHex,
                    bgColorHex = fe.bgColorHex,
                    isSecured = fe.isSecured,
                    isTrash = fe.isTrash,
                    createdAt = fe.createdAt
                )
            )
        }
        result
    }

    fun searchVaultFiles(query: String): Flow<List<VaultItem>> = vaultDao.searchFiles(query).map { list ->
        list.map { it.toVaultItem() }
    }

    suspend fun uploadFile(
        uri: android.net.Uri,
        targetFolderId: String?,
        targetFolderName: String,
        targetFolderPath: String
    ): Result<VaultFileEntity> {
        val email = appPreferences.userEmailFlow.first()
        return if (firebaseVaultService != null) {
            firebaseVaultService.uploadFile(
                userIdentifier = email,
                uri = uri,
                targetFolderId = targetFolderId,
                targetFolderName = targetFolderName,
                targetFolderPath = targetFolderPath
            )
        } else {
            // Local fallback
            if (context != null) {
                val entity = StorageFileService.importFileFromUri(
                    context = context,
                    uri = uri,
                    targetFolder = targetFolderName
                )
                vaultDao.insertFile(entity)
                Result.success(entity)
            } else {
                Result.failure(Exception("Context not available"))
            }
        }
    }

    suspend fun downloadFileLocally(file: VaultItem): Result<File> {
        val entity = vaultDao.getFileById(file.id)
            ?: return Result.failure(Exception("File not found"))
        val email = appPreferences.userEmailFlow.first()
        return firebaseVaultService?.downloadFileLocally(email, entity)
            ?: Result.failure(Exception("Sync service not available"))
    }

    suspend fun toggleFavoriteFile(id: String, isFavorite: Boolean) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.toggleFavorite(email, id, isFavorite)
        } else {
            vaultDao.updateFavorite(id, isFavorite)
            vaultDao.getFileById(id)?.let { file ->
                vaultSyncService?.syncFileToCloud(email, file)
            }
        }
    }

    suspend fun setFileSecured(fileId: String, isSecured: Boolean) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.toggleSecure(email, fileId, isSecured)
        } else {
            vaultDao.updateSecured(fileId, isSecured)
            vaultDao.getFileById(fileId)?.let { file ->
                vaultSyncService?.syncFileToCloud(email, file)
            }
        }
    }

    suspend fun setFilesSecured(fileIds: List<String>, isSecured: Boolean) {
        val email = appPreferences.userEmailFlow.first()
        fileIds.forEach { id ->
            if (firebaseVaultService != null) {
                firebaseVaultService.toggleSecure(email, id, isSecured)
            } else {
                vaultDao.updateSecured(id, isSecured)
                vaultDao.getFileById(id)?.let { file ->
                    vaultSyncService?.syncFileToCloud(email, file)
                }
            }
        }
    }

    suspend fun moveFilesToFolder(
        fileIds: List<String>,
        newFolder: String,
        targetFolderId: String? = null,
        targetFolderPath: String = ""
    ) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.moveFiles(
                userIdentifier = email,
                fileIds = fileIds,
                targetFolderId = targetFolderId,
                targetFolderName = newFolder,
                targetFolderPath = targetFolderPath
            )
        } else {
            vaultDao.moveFilesToFolder(fileIds, newFolder, targetFolderId, targetFolderPath)
            fileIds.forEach { id ->
                vaultDao.getFileById(id)?.let { file ->
                    vaultSyncService?.syncFileToCloud(email, file)
                }
            }
        }
    }

    suspend fun copyFiles(
        fileIds: List<String>,
        targetFolderName: String,
        targetFolderId: String? = null,
        targetFolderPath: String = ""
    ) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.copyFiles(
                userIdentifier = email,
                fileIds = fileIds,
                targetFolderId = targetFolderId,
                targetFolderName = targetFolderName,
                targetFolderPath = targetFolderPath
            )
        } else {
            fileIds.forEach { id ->
                val original = vaultDao.getFileById(id) ?: return@forEach
                val newId = UUID.randomUUID().toString()
                val copyName = if (original.folder == targetFolderName) "${original.name} (Copy)" else original.name
                val copyEntity = original.copy(
                    id = newId,
                    name = copyName,
                    folder = targetFolderName,
                    parentFolderId = targetFolderId,
                    folderPath = targetFolderPath,
                    lastModifiedTimestamp = System.currentTimeMillis()
                )
                vaultDao.insertFile(copyEntity)
            }
        }
    }

    suspend fun trashFiles(fileIds: List<String>) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.setFilesTrash(email, fileIds, isTrash = true)
        } else {
            vaultDao.updateTrashMultiple(fileIds, true)
        }
    }

    suspend fun restoreFiles(fileIds: List<String>) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.setFilesTrash(email, fileIds, isTrash = false)
        } else {
            vaultDao.updateTrashMultiple(fileIds, false)
        }
    }

    suspend fun deletePermanently(fileId: String) {
        val email = appPreferences.userEmailFlow.first()
        val file = vaultDao.getFileById(fileId) ?: return
        if (firebaseVaultService != null) {
            firebaseVaultService.deletePermanently(email, file)
        } else {
            vaultDao.deleteFile(fileId)
            vaultSyncService?.deleteFileFromCloud(email, fileId)
        }
    }

    suspend fun emptyTrash() {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.emptyTrash(email)
        } else {
            vaultDao.emptyTrash()
        }
    }

    suspend fun renameFile(fileId: String, newName: String) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.renameFile(email, fileId, newName)
        } else {
            vaultDao.renameFile(fileId, newName)
        }
    }

    suspend fun renameFolder(folderId: String, newName: String) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.renameFolder(email, folderId, newName)
        } else {
            vaultFolderDao.renameFolder(folderId, newName)
        }
    }

    suspend fun deleteMultipleFiles(fileIds: List<String>) {
        trashFiles(fileIds)
    }

    suspend fun createFolder(
        name: String,
        parentFolderId: String? = null,
        parentPath: String = "",
        colorHex: Long = 0xFF005AC1,
        isSecured: Boolean = false
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val bgHex = when (colorHex) {
            0xFF3B82F6 -> 0xFFEFF6FF
            0xFF10B981 -> 0xFFECFDF5
            0xFF8B5CF6 -> 0xFFF5F3FF
            0xFFF97316 -> 0xFFFFF7ED
            0xFFEF4444 -> 0xFFFEF2F2
            else -> 0xFFEFF6FF
        }
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.createFolder(
                userIdentifier = email,
                name = trimmed,
                parentFolderId = parentFolderId,
                parentPath = parentPath,
                iconColorHex = colorHex,
                bgColorHex = bgHex,
                isSecured = isSecured
            )
        } else {
            val folderEntity = VaultFolderEntity(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                parentFolderId = parentFolderId,
                path = if (parentPath.isBlank()) "/$trimmed" else "$parentPath/$trimmed",
                iconColorHex = colorHex,
                bgColorHex = bgHex,
                isSecured = isSecured
            )
            vaultFolderDao.insertFolder(folderEntity)
            vaultSyncService?.syncFolderToCloud(email, folderEntity)
        }
    }

    suspend fun deleteFolder(folderIdOrName: String) {
        val email = appPreferences.userEmailFlow.first()
        if (firebaseVaultService != null) {
            firebaseVaultService.deleteFolderPermanently(email, folderIdOrName)
        } else {
            vaultFolderDao.deleteFolder(folderIdOrName)
            vaultSyncService?.deleteFolderFromCloud(email, folderIdOrName)
        }
    }

    suspend fun insertImportedFile(file: VaultFileEntity) {
        vaultDao.insertFile(file)
        val email = appPreferences.userEmailFlow.first()
        vaultSyncService?.syncFileToCloud(email, file)
    }

    suspend fun insertImportedFiles(files: List<VaultFileEntity>) {
        vaultDao.insertFiles(files)
        val email = appPreferences.userEmailFlow.first()
        files.forEach { file ->
            vaultSyncService?.syncFileToCloud(email, file)
        }
    }

    suspend fun syncVaultWithCloud(): VaultSyncStatus {
        val email = appPreferences.userEmailFlow.first()
        firebaseVaultService?.startRealtimeListeners(email)
        return vaultSyncService?.syncAllWithCloud(email) ?: VaultSyncStatus.Offline("Local vault ready")
    }

    val userProfileFlow: Flow<User> = combine(
        combine(appPreferences.userNameFlow, appPreferences.userEmailFlow, appPreferences.userAvatarFlow) { name, email, avatar ->
            Triple(name, email, avatar)
        },
        appPreferences.isProFlow,
        originalityDao.getAllChecks(),
        vaultDao.getAllFiles()
    ) { (name, email, avatar), isPro, checks, files ->
        val totalBytes = files.sumOf { it.sizeBytes }
        val usedGb = totalBytes / (1024.0 * 1024.0 * 1024.0)
        val formattedGb = String.format(java.util.Locale.US, "%.1f", usedGb).toDoubleOrNull() ?: 0.0
        User(
            name = name,
            email = email,
            isPro = isPro,
            checksCompleted = checks.size,
            documentsStored = files.size,
            aiConversations = 0,
            storageUsedGb = formattedGb,
            storageTotalGb = if (isPro) 50.0 else 15.0,
            avatarUrl = avatar
        )
    }

    suspend fun checkOriginality(title: String, content: String): OriginalityReport {
        val result = originalityEngine.analyzeOriginality(title, content)
        return result.getOrThrow()
    }

    suspend fun saveNoteToVault(title: String, content: String, folder: String = "Notes") {
        val id = UUID.randomUUID().toString()
        val sizeBytes = content.toByteArray().size.toLong()
        val sizeStr = "${(sizeBytes / 1024.0).coerceAtLeast(0.5).toInt()} KB • NOTE"
        val fileName = if (title.endsWith(".note") || title.endsWith(".txt")) title else "$title.note"
        vaultDao.insertFile(
            VaultFileEntity(
                id = id,
                name = fileName,
                extension = "NOTE",
                sizeString = sizeStr,
                sizeBytes = sizeBytes,
                timeAgo = "Just now",
                folder = folder,
                content = content,
                tag = "Academic",
                isFavorite = false,
                isCachedOffline = true,
                lastModifiedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteVaultFile(id: String) {
        vaultDao.deleteFile(id)
    }

    suspend fun syncDeviceStorageFiles(): Int {
        val ctx = context ?: return 0
        val deviceFiles = StorageFileService.scanDeviceMedia(ctx)
        if (deviceFiles.isNotEmpty()) {
            vaultDao.insertFiles(deviceFiles)
        }
        return deviceFiles.size
    }

    suspend fun preseedInitialDataIfEmpty() {
        val existingFolders = vaultFolderDao.getAllFolders().first()
        if (existingFolders.isEmpty()) {
            val defaultFolders = listOf(
                VaultFolderEntity(id = "folder_assignments", name = "Assignments", path = "/Assignments", iconColorHex = 0xFF3B82F6, bgColorHex = 0xFFEFF6FF, isSecured = false),
                VaultFolderEntity(id = "folder_research", name = "Research", path = "/Research", iconColorHex = 0xFF10B981, bgColorHex = 0xFFECFDF5, isSecured = false),
                VaultFolderEntity(id = "folder_notes", name = "Notes", path = "/Notes", iconColorHex = 0xFF8B5CF6, bgColorHex = 0xFFF5F3FF, isSecured = false),
                VaultFolderEntity(id = "folder_pictures", name = "Pictures", path = "/Pictures", iconColorHex = 0xFF06B6D4, bgColorHex = 0xFFECFEFF, isSecured = false),
                VaultFolderEntity(id = "folder_videos", name = "Videos", path = "/Videos", iconColorHex = 0xFFEC4899, bgColorHex = 0xFFFDF2F8, isSecured = false),
                VaultFolderEntity(id = "folder_audio", name = "Audio", path = "/Audio", iconColorHex = 0xFFA855F7, bgColorHex = 0xFFFAF5FF, isSecured = false),
                VaultFolderEntity(id = "folder_personal", name = "Personal", path = "/Personal", iconColorHex = 0xFFF97316, bgColorHex = 0xFFFFF7ED, isSecured = true)
            )
            vaultFolderDao.insertFolders(defaultFolders)
        }

        val existingFiles = vaultDao.getAllFilesSnapshot()
        if (existingFiles.isEmpty()) {
            val ctx = context
            if (ctx != null) {
                val sampleFiles = StorageFileService.createSampleFilesIfMissing(ctx)
                if (sampleFiles.isNotEmpty()) {
                    vaultDao.insertFiles(sampleFiles)
                }
            }
        }
    }
}

