package com.example.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.AppPreferences
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.OcrScanDao
import com.example.data.local.OcrScanEntity
import com.example.data.local.OriginalityDao
import com.example.data.local.OriginalityEntity
import com.example.data.model.NotificationPreferences
import com.example.data.model.User
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Service that synchronizes User Profile, Preferences, and Academic History
 * (Originality Checks, AI Chat, OCR Scans) to Firebase Firestore and Storage.
 *
 * All cloud paths are strictly isolated per authenticated user:
 * - Firestore: /users/{userId}
 * - Storage:   /users/{userId}/avatar.jpg
 */
class UserDataFirestoreSyncService(
    private val context: Context,
    private val originalityDao: OriginalityDao,
    private val chatMessageDao: ChatMessageDao,
    private val ocrScanDao: OcrScanDao,
    private val appPreferences: AppPreferences,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "UserDataSyncService"
    }

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) null
            else FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not available: ${e.message}")
            null
        }
    }

    private fun getStorage(): FirebaseStorage? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) null
            else FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Storage not available: ${e.message}")
            null
        }
    }

    private fun getCurrentUserId(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Uploads local avatar image to Firebase Storage at /users/{userId}/avatar.jpg,
     * returns public download URL, and saves to Firestore profile.
     */
    suspend fun uploadAvatarToCloud(localFilePath: String): String? = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext null
        val storage = getStorage() ?: return@withContext null

        try {
            val file = File(localFilePath)
            if (!file.exists()) return@withContext null

            val storageRef = storage.reference.child("users/$uid/avatar.jpg")
            val uri = Uri.fromFile(file)
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Update user document in Firestore
            getFirestore()?.collection("users")?.document(uid)?.set(
                mapOf(
                    "avatarUrl" to downloadUrl,
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )?.await()

            Log.d(TAG, "Avatar successfully uploaded to cloud: $downloadUrl")
            return@withContext downloadUrl
        } catch (e: Exception) {
            Log.w(TAG, "Failed to upload avatar to Firebase Storage: ${e.message}")
            return@withContext null
        }
    }

    /**
     * Syncs User Profile & Preferences to Firestore under /users/{userId}.
     */
    suspend fun syncProfileAndPreferences(
        user: User,
        theme: String,
        language: String,
        vaultSyncMode: String,
        notifications: NotificationPreferences
    ) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            val profileData = mapOf(
                "uid" to uid,
                "name" to user.name,
                "email" to user.email,
                "avatarUrl" to user.avatarUrl,
                "isPro" to user.isPro,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .set(profileData, SetOptions.merge())
                .await()

            val preferencesData = mapOf(
                "theme" to theme,
                "language" to language,
                "vaultSyncMode" to vaultSyncMode,
                "pushEnabled" to notifications.pushEnabled,
                "emailEnabled" to notifications.emailEnabled,
                "activityEnabled" to notifications.activityEnabled,
                "securityEnabled" to notifications.securityEnabled,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .collection("preferences").document("settings")
                .set(preferencesData, SetOptions.merge())
                .await()

            Log.d(TAG, "User profile and preferences synced to Firestore for $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing profile to Firestore: ${e.message}")
        }
    }

    /**
     * Syncs an Originality Check record to Firestore under /users/{userId}/originality_checks/{id}.
     */
    suspend fun syncOriginalityCheck(entity: OriginalityEntity) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            val doc = mapOf(
                "id" to entity.id,
                "title" to entity.title,
                "score" to entity.score,
                "similarityPercentage" to entity.similarityPercentage,
                "repeatedPercentage" to entity.repeatedPercentage,
                "uniquePercentage" to entity.uniquePercentage,
                "sourcesCount" to entity.sourcesCount,
                "wordCount" to entity.wordCount,
                "charCount" to entity.charCount,
                "dateString" to entity.dateString,
                "sourcesJson" to entity.sourcesJson,
                "highlightsJson" to entity.highlightsJson,
                "status" to entity.status,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(uid)
                .collection("originality_checks").document(entity.id)
                .set(doc, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing originality check to Firestore: ${e.message}")
        }
    }

    /**
     * Deletes an Originality Check record from Firestore.
     */
    suspend fun deleteOriginalityCheckFromCloud(checkId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            db.collection("users").document(uid)
                .collection("originality_checks").document(checkId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting originality check from Firestore: ${e.message}")
        }
    }

    /**
     * Syncs a Chat Message to Firestore under /users/{userId}/chat_messages/{id}.
     */
    suspend fun syncChatMessage(entity: ChatMessageEntity) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            val doc = mapOf(
                "id" to entity.id,
                "conversationId" to entity.conversationId,
                "text" to entity.text,
                "isFromUser" to entity.isFromUser,
                "timestamp" to entity.timestamp,
                "timestampMillis" to entity.timestampMillis,
                "personaName" to entity.personaName,
                "toneName" to entity.toneName
            )

            db.collection("users").document(uid)
                .collection("chat_messages").document(entity.id)
                .set(doc, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing chat message to Firestore: ${e.message}")
        }
    }

    /**
     * Deletes a Chat Message from Firestore.
     */
    suspend fun deleteChatMessageFromCloud(messageId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            db.collection("users").document(uid)
                .collection("chat_messages").document(messageId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting chat message from Firestore: ${e.message}")
        }
    }

    /**
     * Clears all Chat Messages from Firestore for current user.
     */
    suspend fun clearChatMessagesFromCloud() = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            val snapshot = db.collection("users").document(uid)
                .collection("chat_messages")
                .get()
                .await()

            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing chat messages from Firestore: ${e.message}")
        }
    }

    /**
     * Syncs an OCR scan to Firestore under /users/{userId}/ocr_scans/{id}.
     */
    suspend fun syncOcrScan(entity: OcrScanEntity) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            val doc = mapOf(
                "id" to entity.id,
                "title" to entity.title,
                "extractedText" to entity.extractedText,
                "wordCount" to entity.wordCount,
                "charCount" to entity.charCount,
                "confidencePct" to entity.confidencePct,
                "language" to entity.language,
                "formattedDate" to entity.formattedDate,
                "timestamp" to entity.timestamp,
                "isSavedToVault" to entity.isSavedToVault
            )

            db.collection("users").document(uid)
                .collection("ocr_scans").document(entity.id)
                .set(doc, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing OCR scan to Firestore: ${e.message}")
        }
    }

    /**
     * Deletes an OCR scan from Firestore.
     */
    suspend fun deleteOcrScanFromCloud(scanId: String) = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            db.collection("users").document(uid)
                .collection("ocr_scans").document(scanId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting OCR scan from Firestore: ${e.message}")
        }
    }

    /**
     * Pulls cloud history into local Room database if local is empty.
     */
    suspend fun syncAllHistoryOnStartup() = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId() ?: return@withContext
        val db = getFirestore() ?: return@withContext

        try {
            // 1. Sync Originality checks from cloud if local count is low
            val localChecks = originalityDao.getAllChecks().firstOrNull() ?: emptyList()
            val remoteChecks = db.collection("users").document(uid)
                .collection("originality_checks")
                .get()
                .await()

            for (doc in remoteChecks.documents) {
                val id = doc.getString("id") ?: doc.id
                if (localChecks.none { it.id == id }) {
                    val entity = OriginalityEntity(
                        id = id,
                        title = doc.getString("title") ?: "Academic Analysis",
                        content = doc.getString("content") ?: "",
                        score = doc.getLong("score")?.toInt() ?: 85,
                        similarityPercentage = doc.getLong("similarityPercentage")?.toInt() ?: 15,
                        repeatedPercentage = doc.getLong("repeatedPercentage")?.toInt() ?: 10,
                        uniquePercentage = doc.getLong("uniquePercentage")?.toInt() ?: 85,
                        sourcesCount = doc.getLong("sourcesCount")?.toInt() ?: 2,
                        wordCount = doc.getLong("wordCount")?.toInt() ?: 500,
                        charCount = doc.getLong("charCount")?.toInt() ?: 3000,
                        dateString = doc.getString("dateString") ?: "Recent",
                        sourcesJson = doc.getString("sourcesJson") ?: "[]",
                        highlightsJson = doc.getString("highlightsJson") ?: "[]",
                        status = doc.getString("status") ?: "Completed"
                    )
                    originalityDao.insertCheck(entity)
                }
            }

            // 2. Sync Chat messages from cloud if local is empty
            val localMessages = chatMessageDao.getAllMessages().firstOrNull() ?: emptyList()
            if (localMessages.isEmpty()) {
                val remoteMessages = db.collection("users").document(uid)
                    .collection("chat_messages")
                    .orderBy("timestampMillis")
                    .get()
                    .await()

                val toInsert = mutableListOf<ChatMessageEntity>()
                for (doc in remoteMessages.documents) {
                    val id = doc.getString("id") ?: doc.id
                    toInsert.add(
                        ChatMessageEntity(
                            id = id,
                            conversationId = doc.getString("conversationId") ?: "academic_workspace",
                            text = doc.getString("text") ?: "",
                            isFromUser = doc.getBoolean("isFromUser") ?: false,
                            timestamp = doc.getString("timestamp") ?: "",
                            timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis(),
                            personaName = doc.getString("personaName") ?: "ASSISTANT",
                            toneName = doc.getString("toneName") ?: "ACADEMIC",
                            isCachedOffline = true
                        )
                    )
                }
                if (toInsert.isNotEmpty()) {
                    chatMessageDao.insertMessages(toInsert)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing history from Firestore on startup: ${e.message}")
        }
    }
}
