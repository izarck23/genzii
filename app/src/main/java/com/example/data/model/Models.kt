package com.example.data.model

data class User(
    val name: String = "",
    val email: String = "",
    val isPro: Boolean = false,
    val checksCompleted: Int = 0,
    val documentsStored: Int = 0,
    val aiConversations: Int = 0,
    val storageUsedGb: Double = 0.0,
    val storageTotalGb: Double = 15.0,
    val avatarUrl: String? = null
)

enum class MatchClassification(val displayName: String, val colorHex: Long) {
    ORIGINAL("Original Human Text", 0xFF10B981),
    PARAPHRASED("Paraphrased Content", 0xFFF59E0B),
    VERBATIM_MATCH("Direct Plagiarism Match", 0xFFEF4444),
    AI_GENERATED("AI Generated Text", 0xFF8B5CF6)
}

enum class SuggestionType(val label: String) {
    CITATION_ATTRIBUTION("Add Citation"),
    PARAPHRASE_REWRITE("Paraphrase Sentence"),
    AI_HUMANIZE_BURSTINESS("Humanize Structure"),
    QUOTATION_STYLE("Insert Direct Quotes")
}

enum class OriginalityTier {
    BASIC,
    PREMIUM
}

data class SentenceMatch(
    val id: String,
    val text: String,
    val classification: MatchClassification,
    val confidencePct: Int,
    val matchedSourceTitle: String? = null,
    val matchedSourceUrl: String? = null,
    val suggestion: String? = null
)

data class OriginalitySuggestion(
    val id: String,
    val title: String,
    val description: String,
    val type: SuggestionType,
    val targetSentenceSnippet: String = "",
    val suggestedFix: String = ""
)

data class HighlightedMatch(
    val matchedText: String,
    val sourceTitle: String,
    val sourceUrl: String = "",
    val similarityPct: Int = 0,
    val isAiGenerated: Boolean = false
)

data class SourceItem(
    val id: String,
    val title: String,
    val url: String,
    val domain: String,
    val similarityPct: Int
)

data class OriginalityReport(
    val id: String,
    val title: String,
    val content: String,
    val score: Int = 82, // Originality percentage
    val similarityPercentage: Int = 18, // Plagiarism score
    val repeatedPercentage: Int = 18,
    val uniquePercentage: Int = 82,
    val healthScore: Int = 86, // Originality Health Score (0-100)
    val aiScore: Int = 12, // AI content probability (0-100)
    val citationScore: Int = 92, // Citation & attribution score (0-100)
    val authenticityScore: Int = 88, // Perplexity & structural burstiness (0-100)
    val tier: OriginalityTier = OriginalityTier.BASIC,
    val sourcesCount: Int = 12,
    val wordCount: Int = 1250,
    val charCount: Int = 8450,
    val dateString: String = "Just now",
    val repeatedSentences: List<String> = emptyList(),
    val sources: List<SourceItem> = emptyList(),
    val highlights: List<HighlightedMatch> = emptyList(),
    val sentenceMatches: List<SentenceMatch> = emptyList(),
    val suggestions: List<OriginalitySuggestion> = emptyList(),
    val comparisonDocTitle: String? = null,
    val comparisonSimilarityPct: Int? = null,
    val status: String = "completed"
)

enum class AiPersona(val displayName: String, val description: String) {
    TUTOR("Tutor", "Pedagogical & step-by-step academic explanations"),
    ASSISTANT("Assistant", "Clear, concise academic productivity and writing"),
    CODER("Coder", "Software engineering, algorithms & syntax review")
}

enum class AiTone(val displayName: String) {
    ACADEMIC("Academic"),
    PROFESSIONAL("Professional"),
    CONCISE("Concise"),
    DETAILED("Detailed")
}

enum class MessageStatus {
    SUCCESS,
    LOADING,
    ERROR,
    OFFLINE
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val persona: AiPersona = AiPersona.ASSISTANT,
    val isCachedOffline: Boolean = true,
    val status: MessageStatus = MessageStatus.SUCCESS,
    val errorMessage: String? = null,
    val retryPrompt: String? = null
)

enum class FileCategory(val extension: String, val colorHex: Long) {
    PDF("PDF", 0xFFEF4444),
    DOCX("DOCX", 0xFF2563EB),
    XLSX("XLSX", 0xFF10B981),
    PPTX("PPTX", 0xFFF97316),
    NOTE("NOTE", 0xFF8B5CF6),
    IMAGE("IMG", 0xFF06B6D4),
    VIDEO("VID", 0xFFEC4899),
    AUDIO("AUD", 0xFFA855F7),
    TXT("TXT", 0xFF64748B),
    OTHER("FILE", 0xFF475569)
}

data class VaultItem(
    val id: String,
    val name: String,
    val category: FileCategory,
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
    val localFilePath: String = "",
    val mimeType: String = "*/*",
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)

data class VaultFolder(
    val id: String = "",
    val name: String,
    val parentFolderId: String? = null,
    val path: String = "",
    val count: Int = 0,
    val iconColorHex: Long = 0xFF005AC1,
    val bgColorHex: Long = 0xFFEFF6FF,
    val isSecured: Boolean = false,
    val isTrash: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class FolderSummary(
    val name: String,
    val count: Int,
    val iconColor: Long
)

data class NotificationPreferences(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val activityEnabled: Boolean = true,
    val securityEnabled: Boolean = true
)

enum class AppThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class OcrScanRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val extractedText: String,
    val wordCount: Int = 0,
    val charCount: Int = 0,
    val confidencePct: Int = 95,
    val language: String = "en",
    val formattedDate: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSavedToVault: Boolean = false,
    val vaultFileId: String? = null,
    val imagePath: String? = null
)
