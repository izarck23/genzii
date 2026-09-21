package com.example

import com.example.data.service.GeminiAcademicService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OriginalityCheckerTest {

    @Test
    fun testAcademicCorpusOriginalityAnalysis() = runBlocking {
        val service = GeminiAcademicService()
        val text = "The integration of automated digital learning platforms has transformed higher education research. Modern empirical inquiry leverages cognitive learning metrics to evaluate student retention in science."
        val report = service.checkOriginalityWithAcademicDatabases(
            title = "Test Paper",
            content = text
        )

        assertNotNull(report)
        assertEquals("Test Paper", report.title)
        assertTrue(report.score in 50..100)
        assertTrue(report.similarityPercentage in 0..50)
        assertEquals(100, report.score + report.similarityPercentage)
        assertTrue(report.sources.isNotEmpty())
        assertTrue(report.sources.any { it.domain.contains("arxiv") || it.domain.contains("ncbi") || it.domain.contains("doaj") || it.domain.contains("ieee") })
    }
}

