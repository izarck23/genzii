package com.example.data.repository

import com.example.data.local.OriginalityDao
import com.example.data.local.OriginalityEntity
import com.example.data.model.HighlightedMatch
import com.example.data.model.MatchClassification
import com.example.data.model.OriginalityReport
import com.example.data.model.OriginalitySuggestion
import com.example.data.model.OriginalityTier
import com.example.data.model.SentenceMatch
import com.example.data.model.SourceItem
import com.example.data.model.SuggestionType
import com.example.data.service.CopyleaksDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class OriginalityRepository(
    private val originalityDao: OriginalityDao,
    private val copyleaksService: CopyleaksDetectionService = CopyleaksDetectionService()
) {

    fun getAllChecks(): Flow<List<OriginalityReport>> {
        return originalityDao.getAllChecks().map { entities ->
            entities.map { entityToReport(it) }
        }
    }

    suspend fun analyzeOriginality(
        title: String,
        content: String,
        tier: OriginalityTier = OriginalityTier.BASIC
    ): Result<OriginalityReport> = withContext(Dispatchers.IO) {
        val trimmedContent = content.trim()
        if (trimmedContent.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Document content cannot be empty."))
        }

        try {
            val report = copyleaksService.checkOriginality(
                title = title.trim().ifBlank { "Originality Analysis" },
                content = trimmedContent,
                tier = tier
            )

            originalityDao.insertCheck(reportToEntity(report))
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun compareDocuments(
        docATitle: String,
        docAContent: String,
        docBTitle: String,
        docBContent: String
    ): Result<OriginalityReport> = withContext(Dispatchers.IO) {
        try {
            val report = copyleaksService.compareDocuments(
                docATitle = docATitle.trim().ifBlank { "Document A" },
                docAContent = docAContent.trim(),
                docBTitle = docBTitle.trim().ifBlank { "Document B" },
                docBContent = docBContent.trim()
            )

            originalityDao.insertCheck(reportToEntity(report))
            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCheck(id: String) = withContext(Dispatchers.IO) {
        originalityDao.deleteCheck(id)
    }

    private fun reportToEntity(report: OriginalityReport): OriginalityEntity {
        return OriginalityEntity(
            id = report.id,
            title = report.title,
            content = report.content,
            score = report.score,
            similarityPercentage = report.similarityPercentage,
            repeatedPercentage = report.repeatedPercentage,
            uniquePercentage = report.uniquePercentage,
            healthScore = report.healthScore,
            aiScore = report.aiScore,
            citationScore = report.citationScore,
            authenticityScore = report.authenticityScore,
            tier = report.tier.name,
            sourcesCount = report.sourcesCount,
            wordCount = report.wordCount,
            charCount = report.charCount,
            dateString = report.dateString,
            sourcesJson = serializeSources(report.sources),
            highlightsJson = serializeHighlights(report.highlights),
            sentenceMatchesJson = serializeSentenceMatches(report.sentenceMatches),
            suggestionsJson = serializeSuggestions(report.suggestions),
            comparisonDocTitle = report.comparisonDocTitle,
            comparisonSimilarityPct = report.comparisonSimilarityPct,
            status = report.status
        )
    }

    private fun entityToReport(entity: OriginalityEntity): OriginalityReport {
        return OriginalityReport(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            score = entity.score,
            similarityPercentage = entity.similarityPercentage,
            repeatedPercentage = entity.repeatedPercentage,
            uniquePercentage = entity.uniquePercentage,
            healthScore = entity.healthScore,
            aiScore = entity.aiScore,
            citationScore = entity.citationScore,
            authenticityScore = entity.authenticityScore,
            tier = try { OriginalityTier.valueOf(entity.tier) } catch (e: Exception) { OriginalityTier.BASIC },
            sourcesCount = entity.sourcesCount,
            wordCount = entity.wordCount,
            charCount = entity.charCount,
            dateString = entity.dateString,
            sources = deserializeSources(entity.sourcesJson),
            highlights = deserializeHighlights(entity.highlightsJson),
            sentenceMatches = deserializeSentenceMatches(entity.sentenceMatchesJson),
            suggestions = deserializeSuggestions(entity.suggestionsJson),
            comparisonDocTitle = entity.comparisonDocTitle,
            comparisonSimilarityPct = entity.comparisonSimilarityPct,
            status = entity.status
        )
    }

    private fun serializeSources(sources: List<SourceItem>): String {
        val array = JSONArray()
        sources.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("url", s.url)
                put("domain", s.domain)
                put("similarityPct", s.similarityPct)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeSources(json: String): List<SourceItem> {
        val list = mutableListOf<SourceItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SourceItem(
                        id = obj.optString("id", "src_$i"),
                        title = obj.optString("title", "Source $i"),
                        url = obj.optString("url", ""),
                        domain = obj.optString("domain", "academic.org"),
                        similarityPct = obj.optInt("similarityPct", 0)
                    )
                )
            }
        } catch (e: Exception) { /* ignored */ }
        return list
    }

    private fun serializeHighlights(highlights: List<HighlightedMatch>): String {
        val array = JSONArray()
        highlights.forEach { h ->
            val obj = JSONObject().apply {
                put("matchedText", h.matchedText)
                put("sourceTitle", h.sourceTitle)
                put("sourceUrl", h.sourceUrl)
                put("similarityPct", h.similarityPct)
                put("isAiGenerated", h.isAiGenerated)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeHighlights(json: String): List<HighlightedMatch> {
        val list = mutableListOf<HighlightedMatch>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HighlightedMatch(
                        matchedText = obj.optString("matchedText", ""),
                        sourceTitle = obj.optString("sourceTitle", ""),
                        sourceUrl = obj.optString("sourceUrl", ""),
                        similarityPct = obj.optInt("similarityPct", 0),
                        isAiGenerated = obj.optBoolean("isAiGenerated", false)
                    )
                )
            }
        } catch (e: Exception) { /* ignored */ }
        return list
    }

    private fun serializeSentenceMatches(matches: List<SentenceMatch>): String {
        val array = JSONArray()
        matches.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("text", m.text)
                put("classification", m.classification.name)
                put("confidencePct", m.confidencePct)
                put("matchedSourceTitle", m.matchedSourceTitle)
                put("matchedSourceUrl", m.matchedSourceUrl)
                put("suggestion", m.suggestion)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeSentenceMatches(json: String): List<SentenceMatch> {
        val list = mutableListOf<SentenceMatch>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val classStr = obj.optString("classification", MatchClassification.ORIGINAL.name)
                val classification = try {
                    MatchClassification.valueOf(classStr)
                } catch (e: Exception) {
                    MatchClassification.ORIGINAL
                }
                list.add(
                    SentenceMatch(
                        id = obj.optString("id", "sent_$i"),
                        text = obj.optString("text", ""),
                        classification = classification,
                        confidencePct = obj.optInt("confidencePct", 85),
                        matchedSourceTitle = if (obj.has("matchedSourceTitle")) obj.optString("matchedSourceTitle") else null,
                        matchedSourceUrl = if (obj.has("matchedSourceUrl")) obj.optString("matchedSourceUrl") else null,
                        suggestion = if (obj.has("suggestion")) obj.optString("suggestion") else null
                    )
                )
            }
        } catch (e: Exception) { /* ignored */ }
        return list
    }

    private fun serializeSuggestions(suggestions: List<OriginalitySuggestion>): String {
        val array = JSONArray()
        suggestions.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("description", s.description)
                put("type", s.type.name)
                put("targetSentenceSnippet", s.targetSentenceSnippet)
                put("suggestedFix", s.suggestedFix)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeSuggestions(json: String): List<OriginalitySuggestion> {
        val list = mutableListOf<OriginalitySuggestion>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", SuggestionType.CITATION_ATTRIBUTION.name)
                val type = try {
                    SuggestionType.valueOf(typeStr)
                } catch (e: Exception) {
                    SuggestionType.CITATION_ATTRIBUTION
                }
                list.add(
                    OriginalitySuggestion(
                        id = obj.optString("id", "sug_$i"),
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        type = type,
                        targetSentenceSnippet = obj.optString("targetSentenceSnippet", ""),
                        suggestedFix = obj.optString("suggestedFix", "")
                    )
                )
            }
        } catch (e: Exception) { /* ignored */ }
        return list
    }
}


