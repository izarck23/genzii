package com.example.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.data.local.AppPreferences
import com.example.data.local.VaultDao
import com.example.data.local.VaultFileEntity
import com.example.data.local.VaultFolderDao
import com.example.data.local.VaultFolderEntity
import com.example.data.model.VaultSyncNetworkMode
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class VaultSyncStatus {
    object Idle : VaultSyncStatus()
    object Syncing : VaultSyncStatus()
    data class Synced(
        val lastSyncTimestamp: Long,
        val filesSynced: Int,
        val foldersSynced: Int
    ) : VaultSyncStatus()
    data class Offline(val reason: String = "Offline - Local vault ready") : VaultSyncStatus()
    data class Error(val message: String) : VaultSyncStatus()
}

/**
 * Service that synchronizes Room Database entities with Firebase Cloud Firestore.
 * Supports offline-first architecture: Room is the local source of truth for instant rendering,
 * and Firestore provides persistent cloud backup and cross-device sync.
 */
class VaultFirestoreSyncService(
    private val context: Context,
    private val vaultDao: VaultDao,
    private val vaultFolderDao: VaultFolderDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val appPreferences: AppPreferences? = null
) {
    private val tag = "VaultFirestoreSync"

    private val _syncStatus = MutableStateFlow<VaultSyncStatus>(VaultSyncStatus.Idle)
    val syncStatus: StateFlow<VaultSyncStatus> = _syncStatus.asStateFlow()

    /**
     * Safely retrieves the FirebaseFirestore instance.
     * Returns null gracefully if Firebase is not initialized, preventing runtime crashes.
     */
    private val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase is not initialized: ${e.message}")
            null
        }

    data class NetworkSyncPermission(
        val isAllowed: Boolean,
        val isWifi: Boolean,
        val isCellular: Boolean,
        val message: String?
    )

    suspend fun checkNetworkSyncPermission(): NetworkSyncPermission {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkSyncPermission(false, isWifi = false, isCellular = false, "Network service unavailable")
        val network = cm.activeNetwork
            ?: return NetworkSyncPermission(false, isWifi = false, isCellular = false, "No network connection. Local vault files are securely saved.")
        val capabilities = cm.getNetworkCapabilities(network)
            ?: return NetworkSyncPermission(false, isWifi = false, isCellular = false, "No network connection")

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) {
            return NetworkSyncPermission(false, isWifi = false, isCellular = false, "No internet access. Local vault files ready.")
        }

        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        val syncMode = appPreferences?.vaultSyncNetworkFlow?.first() ?: VaultSyncNetworkMode.WIFI_ONLY

        if (syncMode == VaultSyncNetworkMode.WIFI_ONLY && !isWifi) {
            return NetworkSyncPermission(
                isAllowed = false,
                isWifi = false,
                isCellular = isCellular,
                message = "Sync paused on mobile data (Wi-Fi only setting active)"
            )
        }

        return NetworkSyncPermission(
            isAllowed = true,
            isWifi = isWifi,
            isCellular = isCellular,
            message = null
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun sanitizeUserKey(emailOrUid: String): String {
        return if (emailOrUid.isBlank()) {
            "default_user"
        } else {
            emailOrUid.trim().lowercase()
                .replace(".", "_")
                .replace("@", "_at_")
                .replace("#", "_")
                .replace("$", "_")
                .replace("[", "_")
                .replace("]", "_")
                .replace("/", "_")
        }
    }

    /**
     * Performs a full bi-directional synchronization between local Room database and Cloud Firestore.
     */
    suspend fun syncAllWithCloud(userIdentifier: String): VaultSyncStatus = withContext(Dispatchers.IO) {
        _syncStatus.value = VaultSyncStatus.Syncing

        val perm = checkNetworkSyncPermission()
        if (!perm.isAllowed) {
            val status = VaultSyncStatus.Offline(perm.message ?: "Sync paused")
            _syncStatus.value = status
            return@withContext status
        }

        val db = firestore
        if (db == null) {
            val status = VaultSyncStatus.Offline("Cloud synchronization active in local-first mode.")
            _syncStatus.value = status
            return@withContext status
        }

        val userKey = sanitizeUserKey(userIdentifier)
        val filesCollection = db.collection("users").document(userKey).collection("vault_files")
        val foldersCollection = db.collection("users").document(userKey).collection("vault_folders")

        try {
            // 1. Push local folders to Firestore
            val localFolders = vaultFolderDao.getAllFoldersSnapshot()
            for (folder in localFolders) {
                val folderMap = hashMapOf(
                    "name" to folder.name,
                    "iconColorHex" to folder.iconColorHex,
                    "bgColorHex" to folder.bgColorHex,
                    "isSecured" to folder.isSecured,
                    "createdAt" to folder.createdAt,
                    "updatedAt" to System.currentTimeMillis()
                )
                foldersCollection.document(folder.name).set(folderMap, SetOptions.merge()).awaitTask()
            }

            // 2. Push local files to Firestore
            val localFiles = vaultDao.getAllFilesSnapshot()
            for (file in localFiles) {
                val fileMap = hashMapOf(
                    "id" to file.id,
                    "name" to file.name,
                    "extension" to file.extension,
                    "sizeString" to file.sizeString,
                    "sizeBytes" to file.sizeBytes,
                    "timeAgo" to file.timeAgo,
                    "folder" to file.folder,
                    "content" to file.content,
                    "tag" to file.tag,
                    "isFavorite" to file.isFavorite,
                    "isCachedOffline" to file.isCachedOffline,
                    "lastModifiedTimestamp" to file.lastModifiedTimestamp,
                    "mimeType" to file.mimeType,
                    "isSecured" to file.isSecured,
                    "updatedAt" to System.currentTimeMillis()
                )
                filesCollection.document(file.id).set(fileMap, SetOptions.merge()).awaitTask()
            }

            // 3. Pull remote folders from Firestore
            val remoteFoldersSnapshot = foldersCollection.get().awaitTask()
            val existingFolderNames = localFolders.map { it.name }.toSet()
            for (doc in remoteFoldersSnapshot.documents) {
                val folderName = doc.getString("name") ?: doc.id
                if (!existingFolderNames.contains(folderName)) {
                    val iconColor = doc.getLong("iconColorHex") ?: 0xFF005AC1
                    val bgColor = doc.getLong("bgColorHex") ?: 0xFFEFF6FF
                    val isSecured = doc.getBoolean("isSecured") ?: false
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                    val folderId = doc.getString("id") ?: doc.id
                    vaultFolderDao.insertFolder(
                        VaultFolderEntity(
                            id = folderId,
                            name = folderName,
                            iconColorHex = iconColor,
                            bgColorHex = bgColor,
                            isSecured = isSecured,
                            createdAt = createdAt
                        )
                    )
                }
            }

            // 4. Pull remote files from Firestore
            val remoteFilesSnapshot = filesCollection.get().awaitTask()
            val localFileMap = localFiles.associateBy { it.id }
            for (doc in remoteFilesSnapshot.documents) {
                val fileId = doc.getString("id") ?: doc.id
                val remoteUpdated = doc.getLong("updatedAt") ?: doc.getLong("lastModifiedTimestamp") ?: 0L
                val localFile = localFileMap[fileId]

                if (localFile == null || remoteUpdated > localFile.lastModifiedTimestamp) {
                    val entity = VaultFileEntity(
                        id = fileId,
                        name = doc.getString("name") ?: "Document",
                        extension = doc.getString("extension") ?: "pdf",
                        sizeString = doc.getString("sizeString") ?: "1.2 MB",
                        sizeBytes = doc.getLong("sizeBytes") ?: 0L,
                        timeAgo = doc.getString("timeAgo") ?: "Just now",
                        folder = doc.getString("folder") ?: "Assignments",
                        content = doc.getString("content") ?: "",
                        tag = doc.getString("tag") ?: "Academic",
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        isCachedOffline = true,
                        lastModifiedTimestamp = remoteUpdated,
                        localFilePath = doc.getString("localFilePath") ?: "",
                        mimeType = doc.getString("mimeType") ?: "*/*",
                        isSecured = doc.getBoolean("isSecured") ?: false
                    )
                    vaultDao.insertFile(entity)
                }
            }

            val resultStatus = VaultSyncStatus.Synced(
                lastSyncTimestamp = System.currentTimeMillis(),
                filesSynced = localFiles.size.coerceAtLeast(remoteFilesSnapshot.size()),
                foldersSynced = localFolders.size.coerceAtLeast(remoteFoldersSnapshot.size())
            )
            _syncStatus.value = resultStatus
            resultStatus
        } catch (e: Exception) {
            Log.e(tag, "Vault sync error", e)
            val errStatus = VaultSyncStatus.Error(e.localizedMessage ?: "Sync error occurred")
            _syncStatus.value = errStatus
            errStatus
        }
    }

    /**
     * Uploads or updates a single file in Cloud Firestore.
     */
    fun syncFileToCloud(userIdentifier: String, file: VaultFileEntity) {
        scope.launch {
            val perm = checkNetworkSyncPermission()
            if (!perm.isAllowed) {
                Log.d(tag, "Skipping cloud sync for file ${file.name}: ${perm.message}")
                return@launch
            }
            val db = firestore ?: return@launch
            try {
                val userKey = sanitizeUserKey(userIdentifier)
                val fileMap = hashMapOf(
                    "id" to file.id,
                    "name" to file.name,
                    "extension" to file.extension,
                    "sizeString" to file.sizeString,
                    "sizeBytes" to file.sizeBytes,
                    "timeAgo" to file.timeAgo,
                    "folder" to file.folder,
                    "content" to file.content,
                    "tag" to file.tag,
                    "isFavorite" to file.isFavorite,
                    "isCachedOffline" to file.isCachedOffline,
                    "lastModifiedTimestamp" to file.lastModifiedTimestamp,
                    "mimeType" to file.mimeType,
                    "isSecured" to file.isSecured,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(userKey)
                    .collection("vault_files").document(file.id)
                    .set(fileMap, SetOptions.merge())
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to upload file ${file.name} to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Deletes a single file from Cloud Firestore.
     */
    fun deleteFileFromCloud(userIdentifier: String, fileId: String) {
        scope.launch {
            val perm = checkNetworkSyncPermission()
            if (!perm.isAllowed) return@launch
            val db = firestore ?: return@launch
            try {
                val userKey = sanitizeUserKey(userIdentifier)
                db.collection("users").document(userKey)
                    .collection("vault_files").document(fileId)
                    .delete()
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete file $fileId from Firestore: ${e.message}")
            }
        }
    }

    /**
     * Deletes multiple files from Cloud Firestore.
     */
    fun deleteFilesFromCloud(userIdentifier: String, fileIds: List<String>) {
        scope.launch {
            val perm = checkNetworkSyncPermission()
            if (!perm.isAllowed) return@launch
            val db = firestore ?: return@launch
            try {
                val userKey = sanitizeUserKey(userIdentifier)
                val batch = db.batch()
                for (id in fileIds) {
                    val docRef = db.collection("users").document(userKey)
                        .collection("vault_files").document(id)
                    batch.delete(docRef)
                }
                batch.commit().awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to batch delete files from Firestore: ${e.message}")
            }
        }
    }

    /**
     * Uploads or updates a folder in Cloud Firestore.
     */
    fun syncFolderToCloud(userIdentifier: String, folder: VaultFolderEntity) {
        scope.launch {
            val perm = checkNetworkSyncPermission()
            if (!perm.isAllowed) {
                Log.d(tag, "Skipping cloud sync for folder ${folder.name}: ${perm.message}")
                return@launch
            }
            val db = firestore ?: return@launch
            try {
                val userKey = sanitizeUserKey(userIdentifier)
                val folderMap = hashMapOf(
                    "name" to folder.name,
                    "iconColorHex" to folder.iconColorHex,
                    "bgColorHex" to folder.bgColorHex,
                    "isSecured" to folder.isSecured,
                    "createdAt" to folder.createdAt,
                    "updatedAt" to System.currentTimeMillis()
                )
                db.collection("users").document(userKey)
                    .collection("vault_folders").document(folder.name)
                    .set(folderMap, SetOptions.merge())
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to upload folder ${folder.name} to Firestore: ${e.message}")
            }
        }
    }

    /**
     * Deletes a folder from Cloud Firestore.
     */
    fun deleteFolderFromCloud(userIdentifier: String, folderName: String) {
        scope.launch {
            val perm = checkNetworkSyncPermission()
            if (!perm.isAllowed) return@launch
            val db = firestore ?: return@launch
            try {
                val userKey = sanitizeUserKey(userIdentifier)
                db.collection("users").document(userKey)
                    .collection("vault_folders").document(folderName)
                    .delete()
                    .awaitTask()
            } catch (e: Exception) {
                Log.w(tag, "Failed to delete folder $folderName from Firestore: ${e.message}")
            }
        }
    }
}

/**
 * Extension function to safely await a Google Play Services / Firebase Task inside a coroutine.
 */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) {
            cont.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (cont.isActive) {
            cont.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (cont.isActive) {
            cont.cancel()
        }
    }
}
