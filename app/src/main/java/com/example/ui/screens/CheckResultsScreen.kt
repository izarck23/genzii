package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AdMobTestBanner
import com.example.data.model.OriginalityReport
import com.example.data.model.SourceItem
import com.example.util.PdfReportGenerator
import android.widget.Toast
import androidx.compose.material.icons.filled.PictureAsPdf

private val GreenAccent = Color(0xFF10B981)
private val RedAccent = Color(0xFFEF4444)

@Composable
fun CheckResultsScreen(
    report: OriginalityReport?,
    onBackClick: () -> Unit,
    onViewFullReportClick: () -> Unit = {},
    onDeleteReport: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val textDark = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val brandBlue = MaterialTheme.colorScheme.primary

    val score = report?.score ?: 82
    val similarity = report?.similarityPercentage ?: 18
    val unique = report?.uniquePercentage ?: 82
    val words = report?.wordCount ?: 1250
    val chars = report?.charCount ?: 8450
    val sources = report?.sources ?: emptyList()
    val repeatedSentences = report?.repeatedSentences ?: emptyList()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top bar: < Results
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(surface)
                    .testTag("results_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Originality Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = report?.title ?: "Academic Cross-Reference",
                    fontSize = 12.sp,
                    color = textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (report != null) {
                IconButton(
                    onClick = {
                        val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, report)
                        if (pdfFile != null) {
                            PdfReportGenerator.openOrSharePdf(context, pdfFile)
                        } else {
                            Toast.makeText(context, "Could not generate PDF report", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(surface)
                        .testTag("download_pdf_top_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Download PDF",
                        tint = brandBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (onDeleteReport != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            onDeleteReport(report.id)
                            Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(surface)
                            .testTag("delete_report_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Report",
                            tint = textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Circular Gauge Meter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            OriginalityGauge(percentage = score, trackColor = borderColor.copy(alpha = 0.5f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score%",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = textDark,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = if (score >= 75) "Original" else "Moderate Match",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (score >= 75) GreenAccent else Color(0xFFF59E0B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row: Similarity | Words | Characters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ResultMetricColumn(label = "Similarity", value = "$similarity%", valueColor = if (similarity > 20) RedAccent else Color(0xFFF59E0B))
            ResultMetricColumn(label = "Words", value = "%,d".format(words), valueColor = textDark)
            ResultMetricColumn(label = "Characters", value = "%,d".format(chars), valueColor = textDark)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Breakdown items
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BreakdownRow(
                    dotColor = RedAccent,
                    label = "Repeated / Matched Content",
                    value = "$similarity%"
                )
                BreakdownRow(
                    dotColor = GreenAccent,
                    label = "Unique Academic Writing",
                    value = "$unique%"
                )
                BreakdownRow(
                    dotColor = brandBlue,
                    label = "Open-Access Sources Identified",
                    value = "${sources.size.coerceAtLeast(report?.sourcesCount ?: 1)}"
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Content Insights Card
        Text(
            text = "Academic Integrity Insights",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textDark
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            border = BorderStroke(1.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(GreenAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (score >= 75) "Document exhibits strong scholarly originality." else "Some passages closely reflect existing published literature.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textDark
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(brandBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = brandBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Cross-referenced with arXiv, PubMed, DOAJ, and IEEE databases via Gemini AI.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = textDark
                    )
                }
            }
        }

        // Matched Open-Access Sources Section
        if (sources.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matched Open-Access Literature",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textDark
                )
                Text(
                    text = "${sources.size} citations",
                    fontSize = 12.sp,
                    color = textMuted
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    sources.forEachIndexed { index, source ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = borderColor.copy(alpha = 0.5f),
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                        SourceItemRow(
                            source = source,
                            onOpenUrl = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        )
                    }
                }
            }
        }

        // Flagged Repeated Sentences Section
        if (repeatedSentences.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Flagged Sentences for Citation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surface),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeatedSentences.forEach { sentence ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RedAccent.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = "Quote",
                                    tint = RedAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sentence,
                                    fontSize = 12.sp,
                                    color = textDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Accuracy & Integrity Notice Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = surface,
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Accuracy Notice",
                    tint = textMuted,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Notice on Accuracy",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Originality similarity analysis provides statistical text matching against open-access databases and preprints. It does not guarantee 100% detection of all potential sources or replace human editorial review.",
                        fontSize = 11.sp,
                        color = textMuted,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Download Result as PDF | Check Another Document
        if (report != null) {
            Button(
                onClick = {
                    val pdfFile = PdfReportGenerator.generateOriginalityPdf(context, report)
                    if (pdfFile != null) {
                        PdfReportGenerator.openOrSharePdf(context, pdfFile)
                    } else {
                        Toast.makeText(context, "Could not generate PDF report", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("download_pdf_report_button"),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download Result as PDF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("check_another_document_button"),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, brandBlue.copy(alpha = 0.5f))
        ) {
            Text(
                text = "Check Another Document",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = brandBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        AdMobTestBanner()

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SourceItemRow(
    source: SourceItem,
    onOpenUrl: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenUrl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = source.domain,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${source.similarityPct}% match",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        IconButton(
            onClick = onOpenUrl,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open Source",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun OriginalityGauge(percentage: Int, trackColor: Color) {
    Canvas(modifier = Modifier.size(150.dp)) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)

        // Background track
        drawCircle(
            color = trackColor,
            radius = radius,
            center = centerOffset,
            style = Stroke(width = strokeWidth)
        )

        // Foreground active arc
        val sweepAngle = 360f * (percentage / 100f)
        val arcColor = if (percentage >= 75) GreenAccent else Color(0xFFF59E0B)
        drawArc(
            color = arcColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
            size = Size(radius * 2f, radius * 2f)
        )
    }
}

@Composable
private fun ResultMetricColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun BreakdownRow(dotColor: Color, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
