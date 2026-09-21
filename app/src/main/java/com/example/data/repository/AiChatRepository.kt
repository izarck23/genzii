package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageDao
import com.example.data.local.ChatMessageEntity
import com.example.data.model.AiPersona
import com.example.data.model.AiTone
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
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
import java.util.concurrent.TimeUnit

class AiChatRepository(
    private val chatMessageDao: ChatMessageDao,
    private val context: Context? = null
) {

    companion object {
        private const val TAG = "AiChatRepository"
        // Primary and fallback models per gemini-api skill
        private const val PRIMARY_MODEL = "gemini-3.5-flash"
        private const val FALLBACK_MODEL = "gemini-flash-latest"

        const val PREFIX_OFFLINE = "[OFFLINE]:"
        const val PREFIX_ERROR = "[ERROR]:"
    }

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Optimized client: 15s connect, 35s read to fail fast and reduce latency
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val messagesFlow: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages().map { entities ->
        var lastUserPrompt = ""
        entities.map { entity ->
            if (entity.isFromUser) {
                lastUserPrompt = entity.text
                ChatMessage(
                    id = entity.id,
                    text = entity.text,
                    isFromUser = true,
                    timestamp = entity.timestamp,
                    persona = AiPersona.entries.find { it.name == entity.personaName } ?: AiPersona.ASSISTANT,
                    isCachedOffline = entity.isCachedOffline,
                    status = MessageStatus.SUCCESS
                )
            } else {
                val rawText = entity.text
                val isOffline = rawText.startsWith(PREFIX_OFFLINE)
                val isError = rawText.startsWith(PREFIX_ERROR)
                val status = when {
                    isOffline -> MessageStatus.OFFLINE
                    isError -> MessageStatus.ERROR
                    else -> MessageStatus.SUCCESS
                }
                val cleanText = when {
                    isOffline -> rawText.removePrefix(PREFIX_OFFLINE).trim()
                    isError -> rawText.removePrefix(PREFIX_ERROR).trim()
                    else -> rawText
                }

                ChatMessage(
                    id = entity.id,
                    text = cleanText,
                    isFromUser = false,
                    timestamp = entity.timestamp,
                    persona = AiPersona.entries.find { it.name == entity.personaName } ?: AiPersona.ASSISTANT,
                    isCachedOffline = entity.isCachedOffline,
                    status = status,
                    errorMessage = if (isOffline || isError) cleanText else null,
                    retryPrompt = if (isOffline || isError) lastUserPrompt else null
                )
            }
        }
    }

    suspend fun sendMessage(
        userText: String,
        persona: AiPersona,
        tone: AiTone
    ): ChatMessage = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val userTimestamp = timeFormat.format(Date(now))
        val userMsgId = UUID.randomUUID().toString()

        // 1. Record user's message in local Room database
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

        val aiNow = System.currentTimeMillis()
        val aiTimestamp = timeFormat.format(Date(aiNow))
        val aiMsgId = UUID.randomUUID().toString()

        // 2. Strict Online-only check with reliable network handling
        val isOnline = NetworkUtils.isNetworkAvailable(context)
        if (!isOnline) {
            val offlineMsg = "$PREFIX_OFFLINE You are currently offline. Please check your internet connection and try again."
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    id = aiMsgId,
                    conversationId = "academic_workspace",
                    text = offlineMsg,
                    isFromUser = false,
                    timestamp = aiTimestamp,
                    timestampMillis = aiNow,
                    personaName = persona.name,
                    toneName = tone.name,
                    isCachedOffline = false
                )
            )
            return@withContext ChatMessage(
                id = aiMsgId,
                text = "You are currently offline. Please check your internet connection and try again.",
                isFromUser = false,
                timestamp = aiTimestamp,
                persona = persona,
                isCachedOffline = false,
                status = MessageStatus.OFFLINE,
                errorMessage = "Network offline",
                retryPrompt = userText
            )
        }

        // 3. Query Gemini API with optimized lightweight payload
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val missingKeyMsg = "$PREFIX_ERROR Something went wrong. Please try again."
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    id = aiMsgId,
                    conversationId = "academic_workspace",
                    text = missingKeyMsg,
                    isFromUser = false,
                    timestamp = aiTimestamp,
                    timestampMillis = aiNow,
                    personaName = persona.name,
                    toneName = tone.name,
                    isCachedOffline = false
                )
            )
            return@withContext ChatMessage(
                id = aiMsgId,
                text = "Something went wrong. Please try again.",
                isFromUser = false,
                timestamp = aiTimestamp,
                persona = persona,
                isCachedOffline = false,
                status = MessageStatus.ERROR,
                errorMessage = "API unavailable",
                retryPrompt = userText
            )
        }

        var responseText: String? = null
        var lastError: String? = null

        try {
            // Retrieve only the last 3 messages to optimize context size and minimize latency
            val allHistory = chatMessageDao.getAllMessages().firstOrNull() ?: emptyList()
            val optimizedHistory = allHistory.filter { !it.text.startsWith(PREFIX_ERROR) && !it.text.startsWith(PREFIX_OFFLINE) }
                .takeLast(3)

            responseText = callGeminiChatApi(
                modelName = PRIMARY_MODEL,
                apiKey = apiKey,
                persona = persona,
                tone = tone,
                history = optimizedHistory,
                latestQuery = userText
            )

            if (responseText.isNullOrBlank()) {
                Log.d(TAG, "Primary model returned empty, trying fallback model: $FALLBACK_MODEL")
                responseText = callGeminiChatApi(
                    modelName = FALLBACK_MODEL,
                    apiKey = apiKey,
                    persona = persona,
                    tone = tone,
                    history = optimizedHistory,
                    latestQuery = userText
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call exception: ${e.message}", e)
            lastError = e.localizedMessage ?: e.message ?: "Connection failure"
        }

        // 4. Clean error handling — professional, user-safe message
        if (responseText.isNullOrBlank()) {
            val failureDetail = lastError ?: "Server returned empty response"
            val errorMsg = "$PREFIX_ERROR Something went wrong. Please try again."
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    id = aiMsgId,
                    conversationId = "academic_workspace",
                    text = errorMsg,
                    isFromUser = false,
                    timestamp = aiTimestamp,
                    timestampMillis = aiNow,
                    personaName = persona.name,
                    toneName = tone.name,
                    isCachedOffline = false
                )
            )
            return@withContext ChatMessage(
                id = aiMsgId,
                text = "Something went wrong. Please try again.",
                isFromUser = false,
                timestamp = aiTimestamp,
                persona = persona,
                isCachedOffline = false,
                status = MessageStatus.ERROR,
                errorMessage = failureDetail,
                retryPrompt = userText
            )
        }

        // 5. Successful response saved to Room
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                id = aiMsgId,
                conversationId = "academic_workspace",
                text = responseText,
                isFromUser = false,
                timestamp = aiTimestamp,
                timestampMillis = aiNow,
                personaName = persona.name,
                toneName = tone.name,
                isCachedOffline = true
            )
        )

        ChatMessage(
            id = aiMsgId,
            text = responseText,
            isFromUser = false,
            timestamp = aiTimestamp,
            persona = persona,
            isCachedOffline = true,
            status = MessageStatus.SUCCESS
        )
    }

    private fun callGeminiChatApi(
        modelName: String,
        apiKey: String,
        persona: AiPersona,
        tone: AiTone,
        history: List<ChatMessageEntity>,
        latestQuery: String
    ): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val systemPrompt = """
You are Genzii AI, a knowledgeable, professional academic assistant.
Persona: ${persona.displayName} (${persona.description}). Tone: ${tone.displayName}.
Guidelines:
- Answer the user's question directly, clearly, and accurately.
- Structure answers logically with markdown headers and bullet points where helpful.
- Maintain academic precision, objectivity, and factual rigor.
""".trimIndent()

        val jsonBody = JSONObject().apply {
            // System instruction
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })

            // Multi-turn contents array (optimized compact history)
            val contentsArray = JSONArray()

            history.forEach { entity ->
                val role = if (entity.isFromUser) "user" else "model"
                // Strip internal status prefixes if any were saved
                val cleanHistoryText = entity.text
                    .removePrefix(PREFIX_OFFLINE)
                    .removePrefix(PREFIX_ERROR)
                    .trim()

                contentsArray.put(JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", cleanHistoryText))
                    })
                })
            }

            // Ensure the latest user query is present
            if (contentsArray.length() == 0 || history.lastOrNull()?.text != latestQuery) {
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", latestQuery))
                    })
                })
            }

            put("contents", contentsArray)

            // Optimized generation config for lower latency & factual stability
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("topP", 0.9)
                put("maxOutputTokens", 2048)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.w(TAG, "Gemini API HTTP ${response.code}: $errBody")
                return null
            }
            val bodyString = response.body?.string() ?: return null
            val rootJson = JSONObject(bodyString)
            val candidates = rootJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null

            val textSb = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                textSb.append(part.optString("text", ""))
            }
            val result = textSb.toString().trim()
            return if (result.isNotBlank()) result else null
        }
    }

    suspend fun clearHistory() {
        chatMessageDao.clearAll()
    }

    suspend fun deleteMessage(id: String) {
        chatMessageDao.deleteMessage(id)
    }

    suspend fun preseedWelcomeIfEmpty(count: Int) {
        if (count == 0) {
            val userMsg = ChatMessageEntity(
                id = "msg-user-1",
                conversationId = "academic_workspace",
                text = "Can you summarize this paragraph for me?",
                isFromUser = true,
                timestamp = "9:32 AM",
                timestampMillis = System.currentTimeMillis() - 10000L,
                personaName = AiPersona.ASSISTANT.name,
                toneName = AiTone.ACADEMIC.name,
                isCachedOffline = true
            )
            val aiMsg = ChatMessageEntity(
                id = "msg-ai-1",
                conversationId = "academic_workspace",
                text = "## Executive Summary\nDigital educational technologies enhance accessibility and engagement when integrated with structured pedagogical scaffolding.\n\n## Detailed Analysis\n1. **Active Concept Acquisition**: Interactive tools increase retention by enabling hands-on problem solving.\n2. **Equity & Access**: Cloud-based scholarly resources bridge geographical gaps in university curricula.\n\n## Scholarly Takeaways & Citations\nEmpirical research highlights that digital tools complement, rather than substitute, teacher-guided mentorship (Simmons & Patel, 2024).",
                isFromUser = false,
                timestamp = "9:32 AM",
                timestampMillis = System.currentTimeMillis() - 5000L,
                personaName = AiPersona.ASSISTANT.name,
                toneName = AiTone.ACADEMIC.name,
                isCachedOffline = true
            )
            chatMessageDao.insertMessages(listOf(userMsg, aiMsg))
        }
    }
}
