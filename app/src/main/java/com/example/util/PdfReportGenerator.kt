package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.OriginalityReport
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    private const val TAG = "PdfReportGenerator"
    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points

    fun generateOriginalityPdf(context: Context, report: OriginalityReport): File? {
        return try {
            val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 90, 193) // BrandBlue
            textSize = 18f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(71, 85, 105)
            textSize = 10f
        }
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 41, 59)
            textSize = 12f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 9.5f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            isFakeBoldText = true
        }
        val disclaimerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8f
        }

        var y = 40f
        val margin = 40f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Header Background Banner
        val headerRect = RectF(margin, y, margin + contentWidth, y + 54f)
        paint.color = Color.rgb(239, 246, 255)
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)
        paint.color = Color.rgb(191, 219, 254)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(headerRect, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        canvas.drawText("GENZII ACADEMIC SUITE", margin + 14f, y + 24f, titlePaint)
        canvas.drawText("Official Originality & Academic Similarity Report", margin + 14f, y + 42f, subtitlePaint)
        y += 72f

        // Document Details Section
        canvas.drawText("DOCUMENT DETAILS", margin, y, headingPaint)
        y += 16f

        val detailsRect = RectF(margin, y, margin + contentWidth, y + 50f)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(detailsRect, 6f, 6f, paint)

        canvas.drawText("Title:", margin + 10f, y + 18f, boldBodyPaint)
        val safeTitle = report.title.take(65)
        canvas.drawText(safeTitle, margin + 50f, y + 18f, bodyPaint)

        canvas.drawText("Date:", margin + 10f, y + 36f, boldBodyPaint)
        canvas.drawText(report.dateString, margin + 50f, y + 36f, bodyPaint)

        canvas.drawText("Word Count:", margin + 280f, y + 18f, boldBodyPaint)
        canvas.drawText("${report.wordCount} words", margin + 355f, y + 18f, bodyPaint)

        canvas.drawText("Characters:", margin + 280f, y + 36f, boldBodyPaint)
        canvas.drawText("${report.charCount}", margin + 355f, y + 36f, bodyPaint)
        y += 66f

        // Metric Score Summary Boxes (3 columns)
        canvas.drawText("ORIGINALITY ANALYSIS SUMMARY", margin, y, headingPaint)
        y += 16f

        val boxWidth = (contentWidth - 20f) / 3f
        val boxHeight = 55f

        // Box 1: Originality Score
        drawMetricBox(
            canvas = canvas,
            x = margin,
            y = y,
            w = boxWidth,
            h = boxHeight,
            label = "Originality Score",
            value = "${report.score}%",
            valueColor = if (report.score >= 75) Color.rgb(16, 185, 129) else Color.rgb(245, 158, 11)
        )

        // Box 2: Similarity Percentage
        drawMetricBox(
            canvas = canvas,
            x = margin + boxWidth + 10f,
            y = y,
            w = boxWidth,
            h = boxHeight,
            label = "Matching Similarity",
            value = "${report.similarityPercentage}%",
            valueColor = if (report.similarityPercentage <= 20) Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
        )

        // Box 3: Unique Content
        drawMetricBox(
            canvas = canvas,
            x = margin + (boxWidth + 10f) * 2f,
            y = y,
            w = boxWidth,
            h = boxHeight,
            label = "Unique Content",
            value = "${report.uniquePercentage}%",
            valueColor = Color.rgb(0, 90, 193)
        )
        y += boxHeight + 20f

        // Evidence-based Matching Sources Section
        canvas.drawText("EVIDENCE-BASED MATCHING SOURCES", margin, y, headingPaint)
        y += 14f

        val sources = report.sources.take(4)
        if (sources.isEmpty()) {
            canvas.drawText("No significant similarity matches detected above the threshold.", margin, y + 14f, bodyPaint)
            y += 26f
        } else {
            for (source in sources) {
                val itemRect = RectF(margin, y, margin + contentWidth, y + 36f)
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRoundRect(itemRect, 4f, 4f, paint)

                // Match badge
                paint.color = Color.rgb(239, 246, 255)
                canvas.drawRoundRect(RectF(margin + 6f, y + 6f, margin + 74f, y + 30f), 4f, 4f, paint)
                val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(0, 90, 193)
                    textSize = 8.5f
                    isFakeBoldText = true
                }
                canvas.drawText("${source.similarityPct}% match", margin + 12f, y + 21f, badgeTextPaint)

                // Title and domain
                val srcTitle = source.title.take(55)
                canvas.drawText(srcTitle, margin + 84f, y + 16f, boldBodyPaint)
                canvas.drawText("${source.domain} • ${source.url.take(50)}", margin + 84f, y + 29f, subtitlePaint)

                y += 42f
            }
        }
        y += 10f

        // Flagged / Evidence Excerpts Section
        canvas.drawText("MATCHED TEXT EXCERPTS & EVIDENCE", margin, y, headingPaint)
        y += 14f

        val matches = report.highlights.take(3)
        if (matches.isEmpty() && report.repeatedSentences.isEmpty()) {
            canvas.drawText("All sentences appear original or properly distinct.", margin, y + 14f, bodyPaint)
            y += 28f
        } else {
            val excerpts = if (matches.isNotEmpty()) {
                matches.map { it.matchedText to "${it.sourceTitle} (${it.similarityPct}% match)" }
            } else {
                report.repeatedSentences.take(3).map { it to "Matching academic corpus" }
            }

            for ((quote, srcInfo) in excerpts) {
                val quoteRect = RectF(margin, y, margin + contentWidth, y + 42f)
                paint.color = Color.rgb(255, 251, 235) // Warm amber tint
                canvas.drawRoundRect(quoteRect, 4f, 4f, paint)
                paint.color = Color.rgb(254, 243, 199)
                paint.style = Paint.Style.STROKE
                canvas.drawRoundRect(quoteRect, 4f, 4f, paint)
                paint.style = Paint.Style.FILL

                // Left amber accent stripe
                paint.color = Color.rgb(245, 158, 11)
                canvas.drawRect(RectF(margin, y, margin + 4f, y + 42f), paint)

                val safeQuote = "\"" + quote.take(90) + "\""
                canvas.drawText(safeQuote, margin + 12f, y + 18f, bodyPaint)
                canvas.drawText("Matched: $srcInfo", margin + 12f, y + 33f, subtitlePaint)

                y += 48f
            }
        }
        y += 16f

        // Mandatory Honesty & Accuracy Disclaimer
        val disclaimerRect = RectF(margin, PAGE_HEIGHT - 90f, margin + contentWidth, PAGE_HEIGHT - 35f)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(disclaimerRect, 6f, 6f, paint)

        val discX = margin + 12f
        var discY = PAGE_HEIGHT - 74f
        val discTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(51, 65, 85)
            textSize = 8.5f
            isFakeBoldText = true
        }
        canvas.drawText("ACCURACY & INTEGRITY NOTICE", discX, discY, discTitlePaint)
        discY += 13f

        canvas.drawText(
            "This report provides statistical similarity matching against indexed academic publications and preprints.",
            discX,
            discY,
            disclaimerPaint
        )
        discY += 11f
        canvas.drawText(
            "It does not guarantee 100% detection of all potential sources or replace professional human editorial review.",
            discX,
            discY,
            disclaimerPaint
        )

        pdfDocument.finishPage(page)

        // Write to file
        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val sanitizedTitle = report.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(24)
        val file = File(outputDir, "Originality_Report_${sanitizedTitle}_${System.currentTimeMillis()}.pdf")

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        file
    } catch (t: Throwable) {
        Log.e(TAG, "Error generating or writing PDF: ${t.message}", t)
        null
    }
}

    private fun drawMetricBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        label: String,
        value: String,
        valueColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(248, 250, 252)
        val rect = RectF(x, y, x + w, y + h)
        canvas.drawRoundRect(rect, 6f, 6f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(rect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(100, 116, 139)
            textSize = 9f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = valueColor
            textSize = 17f
            isFakeBoldText = true
        }

        canvas.drawText(label, x + 10f, y + 20f, labelPaint)
        canvas.drawText(value, x + 10f, y + 44f, valuePaint)
    }

    fun openOrSharePdf(context: Context, file: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, file.name)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Save or Share Originality PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
            Toast.makeText(context, "PDF Report generated: ${file.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share PDF: ${e.message}", e)
            Toast.makeText(context, "Could not open PDF viewer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
