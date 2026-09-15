package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.local.VaultDao
import com.example.data.local.VaultFileEntity
import com.example.data.local.VaultFolderDao
import com.example.data.local.VaultFolderEntity
import com.example.data.model.FileCategory
import com.example.data.service.StorageFileService
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class VaultTransferProgress(
    val fileId: String,
    val fileName: String,
    val progressPercent: Int, // 0 - 100
    val isUpload: Boolean = true,
    val error: String? = null
)

class FirebaseVaultService(
    private val context: Context,
    private val vaultDao: VaultDao,
    private val vaultFolderDao: VaultFolderDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "FirebaseVaultService"

    private val _syncStatus = MutableStateFlow<VaultSyncStatus>(VaultSyncStatus.Idle)
    val syncStatus: StateFlow<VaultSyncStatus> = _syncStatus.asStateFlow()

    private val _activeTransfers = MutableStateFlow<Map<String, VaultTransferProgress>>(emptyMap())
    val activeTransfers: StateFlow<Map<String, VaultTransferProgress>> = _activeTransfers.asStateFlow()

    private var filesListenerReg: ListenerRegistration? = null
    private var foldersListenerReg: ListenerRegistration? = null
    private var currentListeningUserKey: String? = null

    private val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w(tag, "Firestore not available: ${e.message}")
            null
        }

    private val storage: FirebaseStorage?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseStorage.getInstance()
            } else null
        } catch (e: Exception) {
            Log.w(tag, "FirebaseStorage not available: ${e.message}")
            null
        }

    fun getEffectiveUserKey(providedIdentifier: String): String {
        val authUid = try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }
        if (!authUid.isNullOrBlank()) {
            return authUid
        }
        return if (providedIdentifier.isBlank()) {
            "default_vault_user"
        } else {
            providedIdentifier.trim().lowercase()
                .replace(".", "_")
                .replace("@", "_at_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Attaches Firestore realtime snapshot listeners to immediately observe changes
     * on the authenticated user's vault files and folders collection.
     */
    fun startRealtimeListeners(userIdentifier: String) {
        val userKey = getEffectiveUserKey(userIdentifier)
        if (currentListeningUserKey == userKey && filesListenerReg != null) {
            return
        }
        stopRealtimeListeners()
        currentListeningUserKey = userKey

        val db = firestore ?: return
        if (!isNetworkAvailable()) {
            _syncStatus.value = VaultSyncStatus.Offline("Offline - Local vault ready")
            return
        }

        _syncStatus.value = VaultSyncStatus.Syncing

        // Listen for Folders changes
        val foldersRef = db.collection("users").document(userKey).collection("vault_folders")
        foldersListenerReg = foldersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "Folders snapshot listener error", error)
                _syncStatus.value = VaultSyncStatus.Error(error.localizedMessage ?: "Sync error")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                scope.launch {
                    val remoteFolderIds = mutableSetOf<String>()
                    val folderEntities = mutableListOf<VaultFolderEntity>()
                    for (doc in snapshot.documents) {
                        val folderId = doc.getString("id") ?: doc.id
                        remoteFolderIds.add(folderId)
                        val name = doc.getString("name") ?: folderId
                        val parentFolderId = doc.getString("parentFolderId")
                        val path = doc.getString("path") ?: "/$name"
                        val iconColor = doc.getLong("iconColorHex") ?: 0xFF005AC1
                        val bgColor = doc.getLong("bgColorHex") ?: 0xFFEFF6FF
                        val isSecured = doc.getBoolean("isSecured") ?: false
                        val isTrash = doc.getBoolean("isTrash") ?: false
                        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                        folderEntities.add(
                            VaultFolderEntity(
                                id = folderId,
                                name = name,
                                parentFolderId = parentFolderId,
                                path = path,
                                iconColorHex = iconColor,
                                bgColorHex = bgColor,
                                isSecured = isSecured,
                                isTrash = isTrash,
                                createdAt = createdAt
                            )
                        )
                    }
                    vaultFolderDao.insertFolders(folderEntities)
                }
            }
        }

        // Listen for Files changes
        val filesRef = db.collection("users").document(userKey).collection("vault_files")
        filesListenerReg = filesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "Files snapshot listener error", error)
                _syncStatus.value = VaultSyncStatus.Error(error.localizedMessage ?: "Sync error")
                return@addSnapshotListener
            }
            if (snapshot != null) {
                scope.launch {
                    val fileEntities = mutableListOf<VaultFileEntity>()
                    for (doc in snapshot.documents) {
                        val fileId = doc.getString("id") ?: doc.id
                        val name = doc.getString("name") ?: "Document"
                        val extension = doc.getString("extension") ?: "pdf"
                        val sizeString = doc.getString("sizeString") ?: "0 KB"
                        val sizeBytes = doc.getLong("sizeBytes") ?: 0L
                        val timeAgo = doc.getString("timeAgo") ?: "Just now"
                        val folder = doc.getString("folder") ?: "Assignments"
                        val parentFolderId = doc.getString("parentFolderId")
                        val folderPath = doc.getString("folderPath") ?: "/$folder"
                        val storagePath = doc.getString("storagePath") ?: ""
                        val downloadUrl = doc.getString("downloadUrl") ?: ""
                        val ownerUid = doc.getString("ownerUid") ?: userKey
                        val content = doc.getString("content") ?: ""
                        val tag = doc.getString("tag") ?: "Academic"
                        val isFavorite = doc.getBoolean("isFavorite") ?: false
                        val isSecured = doc.getBoolean("isSecured") ?: false
                        val isTrash = doc.getBoolean("isTrash") ?: false
                        val isCachedOffline = doc.getBoolean("isCachedOffline") ?: true
                        val lastModified = doc.getLong("lastModifiedTimestamp") ?: System.currentTimeMillis()
                        val createdAt = doc.getLong("createdAt") ?: lastModified
                        val localPath = doc.getString("localFilePath") ?: ""
                        val mimeType = doc.getString("mimeType") ?: "*/*"

                        // Check if local cached file still exists on disk
                        val verifiedLocalPath = if (localPath.isNotBlank() && File(localPath).exists()) {
                            localPath
                        } else {
                            ""
                        }

                        fileEntities.add(
                            VaultFileEntity(
                                id = fileId,
                                name = name,
                                extension = extension,
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
                                isCachedOffline = verifiedLocalPath.isNotBlank(),
                                lastModifiedTimestamp = lastModified,
                                createdAt = createdAt,
                                localFilePath = verifiedLocalPath,
                                mimeType = mimeType
                            )
                        )
                    }
                    vaultDao.insertFiles(fileEntities)
                    _syncStatus.value = VaultSyncStatus.Synced(
                        lastSyncTimestamp = System.currentTimeMillis(),
                        filesSynced = fileEntities.size,
                        foldersSynced = snapshot.size()
                    )
                }
            }
        }
    }

    fun stopRealtimeListeners() {
        filesListenerReg?.remove()
        filesListenerReg = null
        foldersListenerReg?.remove()
        foldersListenerReg = null
        currentListeningUserKey = null
    }

    /**
     * Uploads a file from content Uri to Firebase Storage with realtime progress,
     * extracts metadata, writes metadata to Cloud Firestore, and caches locally.
     */
    suspend fun uploadFile(
        userIdentifier: String,
        uri: Uri,
        targetFolderId: String?,
        targetFolderName: String,
        targetFolderPath: String
    ): Result<VaultFileEntity> = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        val contentResolver = context.contentResolver

        var fileName = "File_${System.currentTimeMillis()}"
        var fileSize = 0L

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx != -1) fileName = cursor.getString(nameIdx) ?: fileName
                if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx)
            }
        }

        val rawMime = contentResolver.getType(uri) ?: "application/octet-stream"
        val category = StorageFileService.determineCategory(fileName, rawMime)
        val fileId = UUID.randomUUID().toString()

        // 1. Copy locally to app's secure files directory for immediate preview & offline caching
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val safeLocalName = "${fileId.take(8)}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val localTargetFile = File(vaultDir, safeLocalName)

        var textSnippet = ""
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(localTargetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (fileSize <= 0) fileSize = localTargetFile.length()

            if (category == FileCategory.TXT || rawMime.startsWith("text/")) {
                textSnippet = localTargetFile.readText().take(4000)
            }
        } catch (e: Exception) {
            Log.w(tag, "Local file copy error", e)
        }

        val sizeString = StorageFileService.formatFileSize(fileSize, category.extension)
        val now = System.currentTimeMillis()

        // Report initial progress
        updateProgress(fileId, fileName, 0, isUpload = true)

        var storagePath = ""
        var downloadUrl = ""

        val st = storage
        if (st != null && isNetworkAvailable()) {
            try {
                storagePath = "users/$userKey/vault/$fileId/$safeLocalName"
                val storageRef = st.reference.child(storagePath)

                // Upload with progress listener
                val uploadTask = storageRef.putFile(Uri.fromFile(localTargetFile))
                uploadTask.addOnProgressListener { taskSnapshot ->
                    val total = taskSnapshot.totalByteCount
                    val transferred = taskSnapshot.bytesTransferred
                    if (total > 0) {
                        val pct = ((100.0 * transferred) / total).toInt().coerceIn(0, 99)
                        updateProgress(fileId, fileName, pct, isUpload = true)
                    }
                }

                uploadTask.awaitTask()
                updateProgress(fileId, fileName, 100, isUpload = true)

                // Get download URL
                try {
                    downloadUrl = storageRef.downloadUrl.awaitTask().toString()
                } catch (e: Exception) {
                    Log.w(tag, "Could not fetch download URL", e)
                }
            } catch (e: Exception) {
                Log.e(tag, "Firebase Storage upload failed", e)
                updateProgress(fileId, fileName, 0, isUpload = true, error = e.localizedMessage)
                removeProgressAfterDelay(fileId)
            }
        }

        val entity = VaultFileEntity(
            id = fileId,
            name = fileName,
            extension = category.extension,
            sizeString = sizeString,
            sizeBytes = fileSize,
            timeAgo = "Just now",
            folder = targetFolderName,
            parentFolderId = targetFolderId,
            folderPath = targetFolderPath,
            storagePath = storagePath,
            downloadUrl = downloadUrl,
            ownerUid = userKey,
            content = textSnippet,
            tag = category.name,
            isFavorite = false,
            isSecured = false,
            isTrash = false,
            isCachedOffline = true,
            lastModifiedTimestamp = now,
            createdAt = now,
            localFilePath = localTargetFile.absolutePath,
            mimeType = rawMime
        )

        // Save into local Room database immediately
        vaultDao.insertFile(entity)

        // Save metadata into Firestore
        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                val fileMap = hashMapOf<String, Any?>(
                    "id" to entity.id,
                    "name" to entity.name,
                    "extension" to entity.extension,
                    "sizeString" to entity.sizeString,
                    "sizeBytes" to entity.sizeBytes,
                    "timeAgo" to entity.timeAgo,
                    "folder" to entity.folder,
                    "parentFolderId" to entity.parentFolderId,
                    "folderPath" to entity.folderPath,
                    "storagePath" to entity.storagePath,
                    "downloadUrl" to entity.downloadUrl,
                    "ownerUid" to entity.ownerUid,
                    "content" to entity.content,
                    "tag" to entity.tag,
                    "isFavorite" to entity.isFavorite,
                    "isSecured" to entity.isSecured,
                    "isTrash" to entity.isTrash,
                    "isCachedOffline" to true,
                    "lastModifiedTimestamp" to entity.lastModifiedTimestamp,
                    "createdAt" to entity.createdAt,
                    "mimeType" to entity.mimeType
                )
                db.collection("users").document(userKey)
                    .collection("vault_files").document(entity.id)
                    .set(fileMap, SetOptions.merge())
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to write file metadata to Firestore", e)
            }
        }

        removeProgressAfterDelay(fileId)
        Result.success(entity)
    }

    /**
     * Downloads a file from Firebase Storage to the local cache if not yet cached.
     */
    suspend fun downloadFileLocally(
        userIdentifier: String,
        file: VaultFileEntity
    ): Result<File> = withContext(Dispatchers.IO) {
        if (file.localFilePath.isNotBlank()) {
            val local = File(file.localFilePath)
            if (local.exists() && local.length() > 0) {
                return@withContext Result.success(local)
            }
        }

        val userKey = getEffectiveUserKey(userIdentifier)
        val st = storage ?: return@withContext Result.failure(Exception("Storage unavailable"))
        val storagePath = if (file.storagePath.isNotBlank()) file.storagePath else "users/$userKey/vault/${file.id}/${file.name}"
        val storageRef = st.reference.child(storagePath)

        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val safeLocalName = "${file.id.take(8)}_${file.name.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val localTarget = File(vaultDir, safeLocalName)

        updateProgress(file.id, file.name, 0, isUpload = false)

        try {
            val downloadTask = storageRef.getFile(localTarget)
            downloadTask.addOnProgressListener { taskSnapshot ->
                val total = taskSnapshot.totalByteCount
                val transferred = taskSnapshot.bytesTransferred
                if (total > 0) {
                    val pct = ((100.0 * transferred) / total).toInt().coerceIn(0, 99)
                    updateProgress(file.id, file.name, pct, isUpload = false)
                }
            }
            downloadTask.awaitTask()
            updateProgress(file.id, file.name, 100, isUpload = false)
            removeProgressAfterDelay(file.id)

            // Update local path in Room
            val updated = file.copy(
                localFilePath = localTarget.absolutePath,
                isCachedOffline = true
            )
            vaultDao.insertFile(updated)

            Result.success(localTarget)
        } catch (e: Exception) {
            Log.e(tag, "Download failed", e)
            updateProgress(file.id, file.name, 0, isUpload = false, error = e.localizedMessage)
            removeProgressAfterDelay(file.id)
            Result.failure(e)
        }
    }

    suspend fun createFolder(
        userIdentifier: String,
        name: String,
        parentFolderId: String?,
        parentPath: String,
        iconColorHex: Long = 0xFF005AC1,
        bgColorHex: Long = 0xFFEFF6FF,
        isSecured: Boolean = false
    ): VaultFolderEntity = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        val folderId = UUID.randomUUID().toString()
        val path = if (parentPath.isBlank() || parentPath == "/") "/$name" else "$parentPath/$name"
        val folder = VaultFolderEntity(
            id = folderId,
            name = name,
            parentFolderId = parentFolderId,
            path = path,
            iconColorHex = iconColorHex,
            bgColorHex = bgColorHex,
            isSecured = isSecured,
            isTrash = false,
            createdAt = System.currentTimeMillis()
        )
        vaultFolderDao.insertFolder(folder)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                val folderMap = hashMapOf<String, Any?>(
                    "id" to folder.id,
                    "name" to folder.name,
                    "parentFolderId" to folder.parentFolderId,
                    "path" to folder.path,
                    "iconColorHex" to folder.iconColorHex,
                    "bgColorHex" to folder.bgColorHex,
                    "isSecured" to folder.isSecured,
                    "isTrash" to false,
                    "createdAt" to folder.createdAt,
                    "ownerUid" to userKey
                )
                db.collection("users").document(userKey)
                    .collection("vault_folders").document(folder.id)
                    .set(folderMap, SetOptions.merge())
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to create folder in Firestore", e)
            }
        }
        folder
    }

    suspend fun renameFolder(
        userIdentifier: String,
        folderId: String,
        newName: String
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultFolderDao.renameFolder(folderId, newName)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_folders").document(folderId)
                    .update("name", newName)
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to rename folder in Firestore", e)
            }
        }
    }

    suspend fun renameFile(
        userIdentifier: String,
        fileId: String,
        newName: String
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.renameFile(fileId, newName)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_files").document(fileId)
                    .update(
                        mapOf(
                            "name" to newName,
                            "lastModifiedTimestamp" to System.currentTimeMillis()
                        )
                    ).awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to rename file in Firestore", e)
            }
        }
    }

    suspend fun copyFile(
        userIdentifier: String,
        fileId: String,
        targetFolderId: String?,
        targetFolderName: String,
        targetFolderPath: String
    ): Result<VaultFileEntity> = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        val original = vaultDao.getFileById(fileId)
            ?: return@withContext Result.failure(Exception("Original file not found"))

        val newFileId = UUID.randomUUID().toString()
        val newName = if (original.folder == targetFolderName) {
            val dotIndex = original.name.lastIndexOf('.')
            if (dotIndex != -1) {
                "${original.name.substring(0, dotIndex)} (Copy).${original.name.substring(dotIndex + 1)}"
            } else {
                "${original.name} (Copy)"
            }
        } else {
            original.name
        }

        var newLocalPath = ""
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val safeLocalName = "${newFileId.take(8)}_${newName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val targetLocalFile = File(vaultDir, safeLocalName)

        if (original.localFilePath.isNotBlank()) {
            val srcLocal = File(original.localFilePath)
            if (srcLocal.exists()) {
                try {
                    srcLocal.copyTo(targetLocalFile, overwrite = true)
                    newLocalPath = targetLocalFile.absolutePath
                } catch (e: Exception) {
                    Log.w(tag, "Failed to copy local file", e)
                }
            }
        }

        var newStoragePath = ""
        var newDownloadUrl = ""

        val st = storage
        if (st != null && isNetworkAvailable()) {
            try {
                newStoragePath = "users/$userKey/vault/$newFileId/$safeLocalName"
                val newRef = st.reference.child(newStoragePath)
                if (targetLocalFile.exists() && targetLocalFile.length() > 0) {
                    val uploadTask = newRef.putFile(Uri.fromFile(targetLocalFile))
                    uploadTask.awaitTask()
                    newDownloadUrl = try {
                        newRef.downloadUrl.awaitTask().toString()
                    } catch (e: Exception) { "" }
                } else if (original.storagePath.isNotBlank()) {
                    val temp = File.createTempFile("copy_", ".tmp", context.cacheDir)
                    st.reference.child(original.storagePath).getFile(temp).awaitTask()
                    val uploadTask = newRef.putFile(Uri.fromFile(temp))
                    uploadTask.awaitTask()
                    newDownloadUrl = try {
                        newRef.downloadUrl.awaitTask().toString()
                    } catch (e: Exception) { "" }
                    temp.copyTo(targetLocalFile, overwrite = true)
                    newLocalPath = targetLocalFile.absolutePath
                    temp.delete()
                }
            } catch (e: Exception) {
                Log.w(tag, "Storage copy failed, keeping local copy", e)
            }
        }

        val now = System.currentTimeMillis()
        val copyEntity = VaultFileEntity(
            id = newFileId,
            name = newName,
            extension = original.extension,
            sizeString = original.sizeString,
            sizeBytes = original.sizeBytes,
            timeAgo = "Just now",
            folder = targetFolderName,
            parentFolderId = targetFolderId,
            folderPath = targetFolderPath,
            storagePath = newStoragePath.ifBlank { original.storagePath },
            downloadUrl = newDownloadUrl.ifBlank { original.downloadUrl },
            ownerUid = userKey,
            content = original.content,
            tag = original.tag,
            isFavorite = false,
            isSecured = original.isSecured,
            isTrash = false,
            isCachedOffline = newLocalPath.isNotBlank(),
            lastModifiedTimestamp = now,
            createdAt = now,
            localFilePath = newLocalPath,
            mimeType = original.mimeType
        )

        vaultDao.insertFile(copyEntity)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                val fileMap = hashMapOf<String, Any?>(
                    "id" to copyEntity.id,
                    "name" to copyEntity.name,
                    "extension" to copyEntity.extension,
                    "sizeString" to copyEntity.sizeString,
                    "sizeBytes" to copyEntity.sizeBytes,
                    "timeAgo" to copyEntity.timeAgo,
                    "folder" to copyEntity.folder,
                    "parentFolderId" to copyEntity.parentFolderId,
                    "folderPath" to copyEntity.folderPath,
                    "storagePath" to copyEntity.storagePath,
                    "downloadUrl" to copyEntity.downloadUrl,
                    "ownerUid" to copyEntity.ownerUid,
                    "content" to copyEntity.content,
                    "tag" to copyEntity.tag,
                    "isFavorite" to copyEntity.isFavorite,
                    "isSecured" to copyEntity.isSecured,
                    "isTrash" to copyEntity.isTrash,
                    "isCachedOffline" to copyEntity.isCachedOffline,
                    "lastModifiedTimestamp" to copyEntity.lastModifiedTimestamp,
                    "createdAt" to copyEntity.createdAt,
                    "mimeType" to copyEntity.mimeType
                )
                db.collection("users").document(userKey)
                    .collection("vault_files").document(copyEntity.id)
                    .set(fileMap, SetOptions.merge())
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to write copied file metadata to Firestore", e)
            }
        }

        Result.success(copyEntity)
    }

    suspend fun copyFiles(
        userIdentifier: String,
        fileIds: List<String>,
        targetFolderId: String?,
        targetFolderName: String,
        targetFolderPath: String
    ) = withContext(Dispatchers.IO) {
        for (id in fileIds) {
            copyFile(userIdentifier, id, targetFolderId, targetFolderName, targetFolderPath)
        }
    }

    suspend fun moveFiles(
        userIdentifier: String,
        fileIds: List<String>,
        targetFolderId: String?,
        targetFolderName: String,
        targetFolderPath: String
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.moveFilesToFolder(fileIds, targetFolderName, targetFolderId, targetFolderPath)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                val batch = db.batch()
                for (id in fileIds) {
                    val docRef = db.collection("users").document(userKey)
                        .collection("vault_files").document(id)
                    batch.update(
                        docRef,
                        mapOf(
                            "folder" to targetFolderName,
                            "parentFolderId" to targetFolderId,
                            "folderPath" to targetFolderPath,
                            "lastModifiedTimestamp" to System.currentTimeMillis()
                        )
                    )
                }
                batch.commit().awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to move files in Firestore", e)
            }
        }
    }

    suspend fun setFilesTrash(
        userIdentifier: String,
        fileIds: List<String>,
        isTrash: Boolean
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.updateTrashMultiple(fileIds, isTrash)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                val batch = db.batch()
                for (id in fileIds) {
                    val docRef = db.collection("users").document(userKey)
                        .collection("vault_files").document(id)
                    batch.update(
                        docRef,
                        mapOf(
                            "isTrash" to isTrash,
                            "lastModifiedTimestamp" to System.currentTimeMillis()
                        )
                    )
                }
                batch.commit().awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to update trash status in Firestore", e)
            }
        }
    }

    suspend fun deletePermanently(
        userIdentifier: String,
        file: VaultFileEntity
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.deleteFile(file.id)

        // Delete local cache file
        if (file.localFilePath.isNotBlank()) {
            try {
                val local = File(file.localFilePath)
                if (local.exists()) local.delete()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete local cache", e)
            }
        }

        // Delete from Firestore
        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_files").document(file.id)
                    .delete()
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete file from Firestore", e)
            }
        }

        // Delete binary from Firebase Storage
        val st = storage
        if (st != null && file.storagePath.isNotBlank() && isNetworkAvailable()) {
            try {
                st.reference.child(file.storagePath).delete().awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete binary from Firebase Storage", e)
            }
        }
    }

    suspend fun deleteFolderPermanently(
        userIdentifier: String,
        folderId: String
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultFolderDao.deleteFolder(folderId)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_folders").document(folderId)
                    .delete()
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete folder from Firestore", e)
            }
        }
    }

    suspend fun emptyTrash(userIdentifier: String) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        val trashFiles = vaultDao.getAllFilesSnapshot().filter { it.isTrash }
        for (f in trashFiles) {
            deletePermanently(userKey, f)
        }
        vaultDao.emptyTrash()
    }

    suspend fun toggleFavorite(
        userIdentifier: String,
        fileId: String,
        isFavorite: Boolean
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.updateFavorite(fileId, isFavorite)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_files").document(fileId)
                    .update("isFavorite", isFavorite)
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to update favorite in Firestore", e)
            }
        }
    }

    suspend fun toggleSecure(
        userIdentifier: String,
        fileId: String,
        isSecured: Boolean
    ) = withContext(Dispatchers.IO) {
        val userKey = getEffectiveUserKey(userIdentifier)
        vaultDao.updateSecured(fileId, isSecured)

        val db = firestore
        if (db != null && isNetworkAvailable()) {
            try {
                db.collection("users").document(userKey)
                    .collection("vault_files").document(fileId)
                    .update("isSecured", isSecured)
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to update secured in Firestore", e)
            }
        }
    }

    private fun updateProgress(
        fileId: String,
        fileName: String,
        progress: Int,
        isUpload: Boolean,
        error: String? = null
    ) {
        val current = _activeTransfers.value.toMutableMap()
        current[fileId] = VaultTransferProgress(
            fileId = fileId,
            fileName = fileName,
            progressPercent = progress,
            isUpload = isUpload,
            error = error
        )
        _activeTransfers.value = current
    }

    private fun removeProgressAfterDelay(fileId: String) {
        scope.launch {
            kotlinx.coroutines.delay(2500)
            val current = _activeTransfers.value.toMutableMap()
            current.remove(fileId)
            _activeTransfers.value = current
        }
    }
}
