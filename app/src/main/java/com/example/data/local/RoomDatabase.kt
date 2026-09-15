package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "originality_checks")
data class OriginalityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val score: Int,
    val similarityPercentage: Int,
    val repeatedPercentage: Int,
    val uniquePercentage: Int,
    val healthScore: Int = 85,
    val aiScore: Int = 12,
    val citationScore: Int = 90,
    val authenticityScore: Int = 85,
    val tier: String = "BASIC",
    val sourcesCount: Int,
    val wordCount: Int,
    val charCount: Int,
    val dateString: String,
    val sourcesJson: String = "[]",
    val highlightsJson: String = "[]",
    val sentenceMatchesJson: String = "[]",
    val suggestionsJson: String = "[]",
    val comparisonDocTitle: String? = null,
    val comparisonSimilarityPct: Int? = null,
    val status: String = "completed"
)

@Dao
interface OriginalityDao {
    @Query("SELECT * FROM originality_checks ORDER BY rowid DESC")
    fun getAllChecks(): Flow<List<OriginalityEntity>>

    @Query("SELECT * FROM originality_checks WHERE id = :id")
    suspend fun getCheckById(id: String): OriginalityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: OriginalityEntity)

    @Query("DELETE FROM originality_checks WHERE id = :id")
    suspend fun deleteCheck(id: String)

    @Query("DELETE FROM originality_checks")
    suspend fun clearAllChecks()
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String = "academic_workspace",
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val personaName: String,
    val toneName: String = "ACADEMIC",
    val isCachedOffline: Boolean = true
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()
}

@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val extension: String,
    val sizeString: String,
    val sizeBytes: Long = 0L,
    val timeAgo: String = "Just now",
    val folder: String = "Assignments",
    val parentFolderId: String? = null,
    val folderPath: String = "",
    val storagePath: String = "",
    val downloadUrl: String = "",
    val ownerUid: String = "",
    val content: String = "",
    val tag: String = "Academic",
    val isFavorite: Boolean = false,
    val isSecured: Boolean = false,
    val isTrash: Boolean = false,
    val isCachedOffline: Boolean = true,
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val localFilePath: String = "",
    val mimeType: String = "*/*"
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_files WHERE isTrash = 0 ORDER BY lastModifiedTimestamp DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isTrash = 0 ORDER BY lastModifiedTimestamp DESC")
    suspend fun getAllFilesSnapshot(): List<VaultFileEntity>

    @Query("SELECT * FROM vault_files WHERE isTrash = 1 ORDER BY lastModifiedTimestamp DESC")
    fun getTrashFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isFavorite = 1 AND isTrash = 0 ORDER BY lastModifiedTimestamp DESC")
    fun getFavoriteFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: String): VaultFileEntity?

    @Query("SELECT * FROM vault_files WHERE folder = :folder AND isTrash = 0 ORDER BY lastModifiedTimestamp DESC")
    fun getFilesByFolder(folder: String): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE name LIKE '%' || :query || '%' AND isTrash = 0 ORDER BY lastModifiedTimestamp DESC")
    fun searchFiles(query: String): Flow<List<VaultFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VaultFileEntity>)

    @Query("UPDATE vault_files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE vault_files SET isSecured = :isSecured WHERE id = :id")
    suspend fun updateSecured(id: String, isSecured: Boolean)

    @Query("UPDATE vault_files SET name = :newName, lastModifiedTimestamp = :timestamp WHERE id = :id")
    suspend fun renameFile(id: String, newName: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET isTrash = :isTrash, lastModifiedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateTrash(id: String, isTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET isTrash = :isTrash, lastModifiedTimestamp = :timestamp WHERE id IN (:ids)")
    suspend fun updateTrashMultiple(ids: List<String>, isTrash: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE vault_files SET folder = :newFolder, parentFolderId = :newParentId, folderPath = :newPath WHERE id = :id")
    suspend fun updateFileFolder(id: String, newFolder: String, newParentId: String? = null, newPath: String = "")

    @Query("UPDATE vault_files SET folder = :newFolder, parentFolderId = :newParentId, folderPath = :newPath WHERE id IN (:ids)")
    suspend fun moveFilesToFolder(ids: List<String>, newFolder: String, newParentId: String? = null, newPath: String = "")

    @Query("UPDATE vault_files SET isSecured = :isSecured WHERE id IN (:ids)")
    suspend fun updateFilesSecured(ids: List<String>, isSecured: Boolean)

    @Query("UPDATE vault_files SET storagePath = :storagePath, downloadUrl = :downloadUrl WHERE id = :id")
    suspend fun updateStorageInfo(id: String, storagePath: String, downloadUrl: String)

    @Query("DELETE FROM vault_files WHERE id IN (:ids)")
    suspend fun deleteFiles(ids: List<String>)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFile(id: String)

    @Query("DELETE FROM vault_files WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM vault_files")
    suspend fun clearAllFiles()
}

@Entity(tableName = "vault_folders")
data class VaultFolderEntity(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val parentFolderId: String? = null,
    val path: String = "",
    val iconColorHex: Long = 0xFF005AC1,
    val bgColorHex: Long = 0xFFEFF6FF,
    val isSecured: Boolean = false,
    val isTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface VaultFolderDao {
    @Query("SELECT * FROM vault_folders WHERE isTrash = 0 ORDER BY createdAt ASC")
    fun getAllFolders(): Flow<List<VaultFolderEntity>>

    @Query("SELECT * FROM vault_folders WHERE isTrash = 0 ORDER BY createdAt ASC")
    suspend fun getAllFoldersSnapshot(): List<VaultFolderEntity>

    @Query("SELECT * FROM vault_folders WHERE isTrash = 1 ORDER BY createdAt ASC")
    fun getTrashFolders(): Flow<List<VaultFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: VaultFolderEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolders(folders: List<VaultFolderEntity>)

    @Query("UPDATE vault_folders SET isSecured = :isSecured WHERE id = :id OR name = :id")
    suspend fun updateSecured(id: String, isSecured: Boolean)

    @Query("UPDATE vault_folders SET name = :newName WHERE id = :id OR name = :id")
    suspend fun renameFolder(id: String, newName: String)

    @Query("UPDATE vault_folders SET isTrash = :isTrash WHERE id = :id OR name = :id")
    suspend fun updateTrash(id: String, isTrash: Boolean)

    @Query("DELETE FROM vault_folders WHERE id = :id OR name = :id")
    suspend fun deleteFolder(id: String)
}

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val email: String,
    val uid: String = "",
    val name: String,
    val avatarUrl: String? = null,
    val isPro: Boolean = true,
    val authProvider: String = "email",
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET name = :name WHERE email = :email")
    suspend fun updateProfile(email: String, name: String)

    @Query("UPDATE user_accounts SET avatarUrl = :avatarUrl WHERE email = :email")
    suspend fun updateAvatar(email: String, avatarUrl: String)

    @Query("DELETE FROM user_accounts WHERE email = :email")
    suspend fun deleteUser(email: String)
}

@Database(
    entities = [OriginalityEntity::class, ChatMessageEntity::class, VaultFileEntity::class, VaultFolderEntity::class, UserAccountEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun originalityDao(): OriginalityDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun vaultDao(): VaultDao
    abstract fun vaultFolderDao(): VaultFolderDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genzii_database.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
