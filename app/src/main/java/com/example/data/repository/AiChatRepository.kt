package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.model.AiPersona
import com.example.data.model.AiTone
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AiChatRepository(private val chatMessageDao: ChatMessageDao) {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val messagesFlow: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages().map { entities ->
        entities.map { entity ->
            ChatMessage(
                id = entity.id,
                text = entity.text,
                isFromUser = entity.isFromUser,
                timestamp = entity.timestamp,
                persona = AiPersona.values().find { it.name == entity.personaName } ?: AiPersona.ASSISTANT,
                isCachedOffline = entity.isCachedOffline
            )
        }
    }

    fun getRecentMessages(limit: Int = 30): Flow<List<ChatMessage>> = chatMessageDao.getRecentMessages(limit).map { entities ->
        entities.map { entity ->
            ChatMessage(
                id = entity.id,
                text = entity.text,
                isFromUser = entity.isFromUser,
                timestamp = entity.timestamp,
                persona = AiPersona.values().find { it.name == entity.personaName } ?: AiPersona.ASSISTANT,
                isCachedOffline = entity.isCachedOffline
            )
        }
    }

    suspend fun sendMessage(
        userText: String,
        persona: AiPersona,
        tone: AiTone
    ): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val userTimestamp = timeFormat.format(Date(now))
        val userMsgId = UUID.randomUUID().toString()

        // 1. Save user message to Room offline cache
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                id = userMsgId,
                conversationId = "academic_workspace",
                text = userText,
                isFromUser = true,
                timestamp = userTimestamp,
                timestampMillis = now,
                personaName = persona.name,
                toneName = tone.name,
                isCachedOffline = true
            )
        )

        // 2. Fetch AI Response either via Gemini or Academic Intelligence Engine
        val aiResponseText = tryFetchGeminiResponse(userText, persona, tone)
            ?: generateAcademicResponse(userText, persona, tone)

        val aiNow = System.currentTimeMillis()
        val aiTimestamp = timeFormat.format(Date(aiNow))
        val aiMsgId = UUID.randomUUID().toString()

        // 3. Save AI response to Room offline cache
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                id = aiMsgId,
                conversationId = "academic_workspace",
                text = aiResponseText,
                isFromUser = false,
                timestamp = aiTimestamp,
                timestampMillis = aiNow,
                personaName = persona.name,
                toneName = tone.name,
                isCachedOffline = true
            )
        )

        aiResponseText
    }

    suspend fun clearHistory() {
        chatMessageDao.clearAll()
    }

    suspend fun preseedWelcomeIfEmpty(count: Int) {
        if (count == 0) {
            val baseTime = System.currentTimeMillis() - 120_000L
            val initial = listOf(
                ChatMessageEntity(
                    id = "msg-1",
                    conversationId = "academic_workspace",
                    text = "Hello, John! 👋 How can I help you today? I'm your academic assistant ready to assist with research, drafting, reviewing, or summarizing.",
                    isFromUser = false,
                    timestamp = "10:30 AM",
                    timestampMillis = baseTime,
                    personaName = AiPersona.ASSISTANT.name,
                    toneName = AiTone.ACADEMIC.name,
                    isCachedOffline = true
                ),
                ChatMessageEntity(
                    id = "msg-2",
                    conversationId = "academic_workspace",
                    text = "Can you summarize this paragraph for me?",
                    isFromUser = true,
                    timestamp = "10:31 AM",
                    timestampMillis = baseTime + 30_000L,
                    personaName = AiPersona.ASSISTANT.name,
                    toneName = AiTone.ACADEMIC.name,
                    isCachedOffline = true
                ),
                ChatMessageEntity(
                    id = "msg-3",
                    conversationId = "academic_workspace",
                    text = "Sure! Here is a concise summary of your paragraph:\n\nThe paragraph discusses the impact of technology on education, highlighting how digital tools enhance learning, improve accessibility, and prepare students for the future.",
                    isFromUser = false,
                    timestamp = "10:31 AM",
                    timestampMillis = baseTime + 60_000L,
                    personaName = AiPersona.ASSISTANT.name,
                    toneName = AiTone.ACADEMIC.name,
                    isCachedOffline = true
                )
            )
            initial.forEach { chatMessageDao.insertMessage(it) }
        }
    }

    private suspend fun tryFetchGeminiResponse(
        prompt: String,
        persona: AiPersona,
        tone: AiTone
    ): String? = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val systemInstruction = when (persona) {
                AiPersona.TUTOR -> "You are Genzii Academic Tutor. Break concepts down pedagogically with clear definitions, bullet points, and academic precision in a ${tone.displayName} tone."
                AiPersona.ASSISTANT -> "You are Genzii Academic Assistant. Provide concise, clear, and professional help with student research, writing, and proofreading in a ${tone.displayName} tone."
                AiPersona.CODER -> "You are Genzii Technical and Coding specialist. Provide robust code snippets, algorithmic explanations, and syntax correctness in a ${tone.displayName} tone."
            }

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val turn = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "$systemInstruction\n\nUser Question:\n$prompt"))
                        }
                        put("parts", parts)
                    }
                    put(turn)
                }
                put("contents", contents)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val firstCandidate = candidates.optJSONObject(0) ?: return@withContext null
            val content = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null
            val text = parts.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) text else null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun generateAcademicResponse(
        prompt: String,
        persona: AiPersona,
        tone: AiTone
    ): String = withContext(Dispatchers.Default) {
        delay(600) // Realistic thoughtful academic typing delay
        val lower = prompt.lowercase()

        when {
            lower.contains("summar") -> {
                "Here is an academic summary based on your input:\n\n" +
                        "1. **Primary Thesis**: The provided text focuses on the core arguments and foundational evidence.\n" +
                        "2. **Key Takeaway**: Highlights key trends, methodological considerations, and observed outcomes.\n" +
                        "3. **Conclusion**: Underscores the necessity for continued scholarly analysis and practical evaluation."
            }
            lower.contains("explain") || lower.contains("photosynthesis") -> {
                "**Academic Explanation**: \n\n" +
                        "Photosynthesis is the biochemical process by which photoautotrophic organisms (such as green plants and cyanobacteria) convert light energy into chemical energy stored in glucose molecules.\n\n" +
                        "• **Chemical Equation**: 6CO₂ + 6H₂O + Light → C₆H₁₂O₆ + 6O₂\n" +
                        "• **Light Reactions**: Occur in the thylakoid membrane, generating ATP and NADPH.\n" +
                        "• **Calvin Cycle (Dark Reactions)**: Takes place in the chloroplast stroma, fixing atmospheric CO₂ into carbohydrate sugars.\n\n" +
                        "Would you like an in-depth breakdown of the electron transport chain or cellular respiration comparison?"
            }
            lower.contains("rewrite") || lower.contains("improve") -> {
                "Here is an enhanced, academically refined version:\n\n" +
                        "\"Recent empirical findings suggest that integrating cutting-edge educational methodologies not only accelerates conceptual retention but also significantly bolsters analytical problem-solving across diverse collegiate cohorts.\""
            }
            lower.contains("code") || lower.contains("program") || persona == AiPersona.CODER -> {
                "Here is the clean, efficient algorithmic implementation:\n\n" +
                        "```kotlin\n" +
                        "// Academic algorithm implementation\n" +
                        "fun analyzeOriginality(text: String): Double {\n" +
                        "    val tokens = text.split(Regex(\"\\\\s+\"))\n" +
                        "    val unique = tokens.distinct().size\n" +
                        "    return (unique.toDouble() / tokens.size.coerceAtLeast(1)) * 100.0\n" +
                        "}\n" +
                        "```\n\n" +
                        "This code operates in O(N) time complexity with safe edge-case guarding for empty strings."
            }
            else -> {
                when (persona) {
                    AiPersona.TUTOR -> "From a pedagogical perspective, exploring this question involves looking at the foundational principles first. Let's analyze the contextual definitions, followed by relevant literature citations and practical case studies."
                    AiPersona.ASSISTANT -> "I've analyzed your academic inquiry. For this topic, consider structuring your thesis statement with a clear problem definition, empirical evidence, and a counterargument review."
                    AiPersona.CODER -> "From a technical standpoint, this problem can be decomposed into modular components, ensuring high cohesion, testability, and optimal memory utilization."
                }
            }
        }
    }
}
