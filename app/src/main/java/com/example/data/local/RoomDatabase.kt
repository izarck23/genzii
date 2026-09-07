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
    val sourcesCount: Int,
    val wordCount: Int,
    val charCount: Int,
    val dateString: String
)

@Dao
interface OriginalityDao {
    @Query("SELECT * FROM originality_checks ORDER BY dateString DESC")
    fun getAllChecks(): Flow<List<OriginalityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: OriginalityEntity)

    @Query("DELETE FROM originality_checks WHERE id = :id")
    suspend fun deleteCheck(id: String)

    @Query("DELETE FROM originality_checks")
    suspend fun clearAllChecks()
}

/**
 * ChatMessageEntity models the cached offline chat conversation history
 * with timestamp indexing and persona context.
 */
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

    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentMessages(limit: Int): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestampMillis ASC")
    fun getMessagesByConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM chat_messages")
    fun getMessageCount(): Flow<Int>
}

/**
 * VaultFileEntity models cached academic document and file metadata
 * for instant offline search, categorization, and previewing.
 */
@Entity(tableName = "vault_files")
data class VaultFileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val extension: String,
    val sizeString: String,
    val sizeBytes: Long = 0L,
    val timeAgo: String,
    val folder: String,
    val content: String = "",
    val tag: String = "Academic",
    val isFavorite: Boolean = false,
    val isCachedOffline: Boolean = true,
    val lastModifiedTimestamp: Long = System.currentTimeMillis(),
    val localFilePath: String? = null
)

@Dao
interface VaultDao {
    @Query("SELECT * FROM vault_files ORDER BY lastModifiedTimestamp DESC")
    fun getAllFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE folder = :folder ORDER BY lastModifiedTimestamp DESC")
    fun getFilesByFolder(folder: String): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE isFavorite = 1 ORDER BY lastModifiedTimestamp DESC")
    fun getFavoriteFiles(): Flow<List<VaultFileEntity>>

    @Query("SELECT * FROM vault_files WHERE id = :id")
    fun getFileById(id: String): Flow<VaultFileEntity?>

    @Query("SELECT * FROM vault_files WHERE name LIKE '%' || :query || '%' OR tag LIKE '%' || :query || '%' ORDER BY lastModifiedTimestamp DESC")
    fun searchFiles(query: String): Flow<List<VaultFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: VaultFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<VaultFileEntity>)

    @Query("UPDATE vault_files SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM vault_files WHERE id = :id")
    suspend fun deleteFile(id: String)

    @Query("DELETE FROM vault_files WHERE id IN (:ids)")
    suspend fun deleteFiles(ids: List<String>)

    @Query("DELETE FROM vault_files")
    suspend fun clearAllFiles()

    @Query("SELECT COUNT(*) FROM vault_files")
    fun getFileCount(): Flow<Int>

    @Query("SELECT SUM(sizeBytes) FROM vault_files")
    fun getTotalSizeBytes(): Flow<Long?>
}

@Database(
    entities = [OriginalityEntity::class, ChatMessageEntity::class, VaultFileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun originalityDao(): OriginalityDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun vaultDao(): VaultDao

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
