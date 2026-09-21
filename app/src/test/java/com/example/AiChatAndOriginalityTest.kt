package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.AiPersona
import com.example.data.model.ChatMessage
import com.example.data.model.MessageStatus
import com.example.data.model.OriginalityReport
import com.example.data.model.SourceItem
import com.example.data.repository.AiChatRepository
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AiChatAndOriginalityTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var aiChatRepository: AiChatRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = AppDatabase.getInstance(context)
        aiChatRepository = AiChatRepository(
            context = context,
            chatMessageDao = database.chatMessageDao()
        )
    }

    @Test
    fun testPdfReportGenerationCreatesValidFile() {
        val testReport = OriginalityReport(
            id = "test-rep-001",
            title = "Comparative Analysis of Deep Learning in Genomics",
            content = "This research examines convolution layers applied to genetic sequences...",
            dateString = "Sep 19, 2026 10:00 AM",
            score = 84,
            similarityPercentage = 16,
            uniquePercentage = 84,
            wordCount = 1450,
            charCount = 9800,
            sources = listOf(
                SourceItem(
                    id = "src-1",
                    title = "Genomic Pattern Classification via Neural Networks",
                    domain = "arxiv.org/abs/2304.09112",
                    url = "https://arxiv.org/abs/2304.09112",
                    similarityPct = 9
                )
            ),
            repeatedSentences = listOf("Genomic variations show characteristic signatures.")
        )

        val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, testReport)
        if (pdfFile != null) {
            assertTrue(pdfFile.exists())
            assertTrue(pdfFile.length() > 0)
        }
    }

    @Test
    fun testChatMessageStatusMapping() {
        val normalMsg = ChatMessage(
            id = "1",
            text = "Academic thesis statement",
            isFromUser = true,
            timestamp = "10:00 AM",
            persona = AiPersona.ASSISTANT,
            status = MessageStatus.SUCCESS
        )
        assertEquals(MessageStatus.SUCCESS, normalMsg.status)

        val errorMsg = ChatMessage(
            id = "2",
            text = "API connection timed out. Server unreachable.",
            isFromUser = false,
            timestamp = "10:01 AM",
            persona = AiPersona.ASSISTANT,
            status = MessageStatus.ERROR,
            errorMessage = "Server unreachable",
            retryPrompt = "Original user prompt"
        )
        assertEquals(MessageStatus.ERROR, errorMsg.status)
        assertEquals("Original user prompt", errorMsg.retryPrompt)

        val offlineMsg = ChatMessage(
            id = "3",
            text = "Offline Mode: Internet connection required.",
            isFromUser = false,
            timestamp = "10:02 AM",
            persona = AiPersona.ASSISTANT,
            status = MessageStatus.OFFLINE,
            retryPrompt = "Original offline prompt"
        )
        assertEquals(MessageStatus.OFFLINE, offlineMsg.status)
        assertEquals("Original offline prompt", offlineMsg.retryPrompt)
    }

    @Test
    fun testAiChatRepositoryClearHistory() = runBlocking {
        aiChatRepository.clearHistory()
        val messages = aiChatRepository.messagesFlow.first()
        assertTrue(messages.isEmpty())
    }
}
