package com.example.data.model

data class User(
    val name: String = "John Doe",
    val email: String = "john@example.com",
    val isPro: Boolean = false,
    val checksCompleted: Int = 128,
    val documentsStored: Int = 342,
    val aiConversations: Int = 89,
    val storageUsedGb: Double = 2.4,
    val storageTotalGb: Double = 10.0
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
    val score: Int, // e.g. 82 (82% Original)
    val similarityPercentage: Int, // e.g. 18
    val repeatedPercentage: Int, // 18
    val uniquePercentage: Int, // 82
    val sourcesCount: Int, // 12
    val wordCount: Int,
    val charCount: Int,
    val dateString: String,
    val repeatedSentences: List<String> = emptyList(),
    val sources: List<SourceItem> = emptyList()
)

enum class AiPersona(val displayName: String, val description: String, val iconName: String) {
    TUTOR("Tutor", "Pedagogical & step-by-step academic explanations", "School"),
    ASSISTANT("Assistant", "Clear, concise academic productivity and writing", "SmartToy"),
    CODER("Coder", "Software engineering, algorithms & syntax review", "Code")
}

enum class AiTone(val displayName: String) {
    ACADEMIC("Academic"),
    PROFESSIONAL("Professional"),
    CONCISE("Concise"),
    DETAILED("Detailed")
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: String,
    val persona: AiPersona = AiPersona.ASSISTANT,
    val isCachedOffline: Boolean = true
)

enum class FileCategory(val extension: String, val colorHex: Long) {
    PDF("PDF", 0xFFEF4444),
    DOCX("DOCX", 0xFF2563EB),
    XLSX("XLSX", 0xFF10B981),
    PPTX("PPTX", 0xFFF97316),
    NOTE("NOTE", 0xFF8B5CF6)
}

data class VaultItem(
    val id: String,
    val name: String,
    val category: FileCategory,
    val sizeString: String,
    val timeAgo: String,
    val folder: String,
    val content: String = "",
    val tag: String = "Academic",
    val isFavorite: Boolean = false,
    val isCachedOffline: Boolean = true
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
