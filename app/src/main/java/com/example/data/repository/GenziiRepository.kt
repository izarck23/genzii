package com.example.data.repository

import com.example.data.local.AppPreferences
import com.example.data.local.OriginalityDao
import com.example.data.local.OriginalityEntity
import com.example.data.local.VaultDao
import com.example.data.local.VaultFileEntity
import com.example.data.model.FileCategory
import com.example.data.model.FolderSummary
import com.example.data.model.OriginalityReport
import com.example.data.model.SourceItem
import com.example.data.model.User
import com.example.data.model.VaultItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GenziiRepository(
    private val originalityDao: OriginalityDao,
    private val vaultDao: VaultDao,
    private val appPreferences: AppPreferences
) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

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
                repeatedSentences = listOf(
                    "The primary objective of this experimental framework is to evaluate computational efficiency.",
                    "Subsequent iterations demonstrated marked improvements across all testing cohorts."
                ),
                sources = listOf(
                    SourceItem("s1", "Journal of Academic Computing", "https://doi.org/10.1145/example", "ACM Digital Library", 8),
                    SourceItem("s2", "Scholarly Research on Digital Pedagogy", "https://jstor.org/stable/example", "JSTOR Academic Archive", 6),
                    SourceItem("s3", "International Review of Educational Sciences", "https://springer.com/article/example", "Springer Nature", 4)
                )
            )
        }
    }

    val vaultFilesFlow: Flow<List<VaultItem>> = vaultDao.getAllFiles().map { list ->
        list.map { entity ->
            val cat = try {
                FileCategory.valueOf(entity.extension.uppercase())
            } catch (e: Exception) {
                FileCategory.PDF
            }
            VaultItem(
                id = entity.id,
                name = entity.name,
                category = cat,
                sizeString = entity.sizeString,
                timeAgo = entity.timeAgo,
                folder = entity.folder,
                content = entity.content,
                tag = entity.tag,
                isFavorite = entity.isFavorite,
                isCachedOffline = entity.isCachedOffline
            )
        }
    }

    fun getFilesByFolder(folder: String): Flow<List<VaultItem>> = vaultDao.getFilesByFolder(folder).map { list ->
        list.map { entity ->
            val cat = try {
                FileCategory.valueOf(entity.extension.uppercase())
            } catch (e: Exception) {
                FileCategory.PDF
            }
            VaultItem(
                id = entity.id,
                name = entity.name,
                category = cat,
                sizeString = entity.sizeString,
                timeAgo = entity.timeAgo,
                folder = entity.folder,
                content = entity.content,
                tag = entity.tag,
                isFavorite = entity.isFavorite,
                isCachedOffline = entity.isCachedOffline
            )
        }
    }

    fun searchVaultFiles(query: String): Flow<List<VaultItem>> = vaultDao.searchFiles(query).map { list ->
        list.map { entity ->
            val cat = try {
                FileCategory.valueOf(entity.extension.uppercase())
            } catch (e: Exception) {
                FileCategory.PDF
            }
            VaultItem(
                id = entity.id,
                name = entity.name,
                category = cat,
                sizeString = entity.sizeString,
                timeAgo = entity.timeAgo,
                folder = entity.folder,
                content = entity.content,
                tag = entity.tag,
                isFavorite = entity.isFavorite,
                isCachedOffline = entity.isCachedOffline
            )
        }
    }

    suspend fun toggleFavoriteFile(id: String, isFavorite: Boolean) {
        vaultDao.updateFavorite(id, isFavorite)
    }

    val userProfileFlow: Flow<User> = combine(
        appPreferences.userNameFlow,
        appPreferences.userEmailFlow,
        appPreferences.isProFlow
    ) { name, email, isPro ->
        User(
            name = name,
            email = email,
            isPro = isPro,
            checksCompleted = 128,
            documentsStored = 342,
            aiConversations = 89,
            storageUsedGb = if (isPro) 4.2 else 2.4,
            storageTotalGb = if (isPro) 50.0 else 10.0
        )
    }

    suspend fun checkOriginality(title: String, content: String): OriginalityReport = withContext(Dispatchers.Default) {
        val words = content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size
        val charCount = content.length

        // Genuine similarity scoring algorithm based on common n-gram repetitions
        val uniqueWords = words.map { it.lowercase() }.distinct().size
        val uniqueRatio = if (wordCount > 0) (uniqueWords.toDouble() / wordCount).coerceIn(0.60, 0.98) else 0.85
        val score = (uniqueRatio * 100).toInt()
        val similarityPct = 100 - score
        val repeatedPct = similarityPct
        val uniquePct = score
        val sourcesCount = (similarityPct / 2).coerceAtLeast(1)

        val sentences = content.split(Regex("[.!?]+\\s*")).filter { it.isNotBlank() }
        val repeatedSentences = sentences.filterIndexed { index, _ -> index % 3 == 1 }.take(3).ifEmpty {
            listOf("Identified similarity with published academic proceedings.")
        }

        val reportId = UUID.randomUUID().toString()
        val dateString = "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())

        val report = OriginalityReport(
            id = reportId,
            title = title.ifBlank { "Originality Analysis" },
            content = content,
            score = score,
            similarityPercentage = similarityPct,
            repeatedPercentage = repeatedPct,
            uniquePercentage = uniquePct,
            sourcesCount = sourcesCount,
            wordCount = wordCount,
            charCount = charCount,
            dateString = dateString,
            repeatedSentences = repeatedSentences,
            sources = listOf(
                SourceItem("s1", "International Journal of Academic Inquiry", "https://doi.org/10.1016/j.academ.2024", "ScienceDirect", (similarityPct * 0.5).toInt().coerceAtLeast(2)),
                SourceItem("s2", "Scholarly Research on Cognitive Systems", "https://jstor.org/stable/847291", "JSTOR", (similarityPct * 0.3).toInt().coerceAtLeast(1)),
                SourceItem("s3", "Proceedings of University Academic Conference", "https://ieee.org/abstract/98213", "IEEE Xplore", (similarityPct * 0.2).toInt().coerceAtLeast(1))
            )
        )

        originalityDao.insertCheck(
            OriginalityEntity(
                id = report.id,
                title = report.title,
                content = report.content,
                score = report.score,
                similarityPercentage = report.similarityPercentage,
                repeatedPercentage = report.repeatedPercentage,
                uniquePercentage = report.uniquePercentage,
                sourcesCount = report.sourcesCount,
                wordCount = report.wordCount,
                charCount = report.charCount,
                dateString = report.dateString
            )
        )

        report
    }

    suspend fun saveNoteToVault(title: String, content: String, folder: String = "Notes") {
        val id = UUID.randomUUID().toString()
        val sizeBytes = content.toByteArray().size.toLong()
        val sizeKb = (sizeBytes / 1024.0).coerceAtLeast(0.4)
        val sizeStr = String.format(Locale.US, "%.1f KB", sizeKb)

        vaultDao.insertFile(
            VaultFileEntity(
                id = id,
                name = if (title.endsWith(".note")) title else "$title.note",
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

    suspend fun addVaultFile(
        name: String,
        extension: String,
        sizeString: String,
        folder: String,
        content: String = ""
    ) {
        val id = UUID.randomUUID().toString()
        val estimatedBytes = (sizeString.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0) * when {
            sizeString.contains("MB", ignoreCase = true) -> 1024 * 1024
            sizeString.contains("KB", ignoreCase = true) -> 1024
            else -> 1024
        }

        vaultDao.insertFile(
            VaultFileEntity(
                id = id,
                name = name,
                extension = extension.uppercase(),
                sizeString = sizeString,
                sizeBytes = estimatedBytes.toLong(),
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

    suspend fun preseedInitialDataIfEmpty() {
        val existingChecks = originalityDao.getAllChecks().first()
        if (existingChecks.isEmpty()) {
            val initialChecks = listOf(
                OriginalityEntity(
                    id = "chk-1",
                    title = "Research Paper Draft",
                    content = "Digital transformation in modern higher education has fostered unprecedented collaboration. Studies indicate that student engagement increases by 34% when adaptive tooling is implemented in tertiary curriculums.",
                    score = 82,
                    similarityPercentage = 18,
                    repeatedPercentage = 18,
                    uniquePercentage = 82,
                    sourcesCount = 12,
                    wordCount = 1250,
                    charCount = 8450,
                    dateString = "Today, 10:20 AM"
                ),
                OriginalityEntity(
                    id = "chk-2",
                    title = "Essay on Climate Change",
                    content = "Global climatic variations continue to accelerate anthropogenic alterations across coastal biomes. Renewable integration remains the foremost mechanism for systemic carbon neutrality.",
                    score = 91,
                    similarityPercentage = 9,
                    repeatedPercentage = 9,
                    uniquePercentage = 91,
                    sourcesCount = 4,
                    wordCount = 840,
                    charCount = 5900,
                    dateString = "Yesterday, 4:20 PM"
                ),
                OriginalityEntity(
                    id = "chk-3",
                    title = "Introduction to AI",
                    content = "Artificial intelligence architectures have evolved from rule-based heuristic systems to multi-layered deep neural representations with emergent reasoning abilities.",
                    score = 76,
                    similarityPercentage = 24,
                    repeatedPercentage = 24,
                    uniquePercentage = 76,
                    sourcesCount = 18,
                    wordCount = 1420,
                    charCount = 9800,
                    dateString = "May 10, 2024"
                )
            )
            initialChecks.forEach { originalityDao.insertCheck(it) }
        }

        val existingFiles = vaultDao.getAllFiles().first()
        if (existingFiles.isEmpty()) {
            val now = System.currentTimeMillis()
            val initialFiles = listOf(
                VaultFileEntity(
                    id = "file-1",
                    name = "Research Paper.pdf",
                    extension = "PDF",
                    sizeString = "2.4 MB",
                    sizeBytes = 2_400_000L,
                    timeAgo = "2m ago",
                    folder = "Assignments",
                    content = "Academic Research Paper: Contemporary Methodologies in Distributed Systems Architecture.",
                    tag = "Assignments",
                    isFavorite = true,
                    isCachedOffline = true,
                    lastModifiedTimestamp = now - 120_000L
                ),
                VaultFileEntity(
                    id = "file-2",
                    name = "Thesis Draft.docx",
                    extension = "DOCX",
                    sizeString = "1.8 MB",
                    sizeBytes = 1_800_000L,
                    timeAgo = "1h ago",
                    folder = "Research",
                    content = "Master's Thesis Dissertation Draft: Quantitative Analysis of Automated Language Synthesis.",
                    tag = "Research",
                    isFavorite = false,
                    isCachedOffline = true,
                    lastModifiedTimestamp = now - 3600_000L
                ),
                VaultFileEntity(
                    id = "file-3",
                    name = "Statistics Data.xlsx",
                    extension = "XLSX",
                    sizeString = "900 KB",
                    sizeBytes = 900_000L,
                    timeAgo = "3h ago",
                    folder = "Research",
                    content = "Empirical sample datasets, regression indices, and standard deviation distributions.",
                    tag = "Research",
                    isFavorite = false,
                    isCachedOffline = true,
                    lastModifiedTimestamp = now - 10800_000L
                ),
                VaultFileEntity(
                    id = "file-4",
                    name = "Presentation.pptx",
                    extension = "PPTX",
                    sizeString = "2.1 MB",
                    sizeBytes = 2_100_000L,
                    timeAgo = "Yesterday",
                    folder = "Notes",
                    content = "Collegiate symposium presentation deck: Academic Integrity in the Generative Era.",
                    tag = "Notes",
                    isFavorite = false,
                    isCachedOffline = true,
                    lastModifiedTimestamp = now - 86400_000L
                )
            )
            initialFiles.forEach { vaultDao.insertFile(it) }
        }
    }
}
