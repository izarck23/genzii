package com.example.data.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.data.local.VaultFileEntity
import com.example.data.model.FileCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object StorageFileService {

    suspend fun importFileFromUri(
        context: Context,
        uri: Uri,
        targetFolder: String = "All Files"
    ): VaultFileEntity = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileName = "Document_${System.currentTimeMillis()}"
        var fileSize = 0L

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }

        val rawMime = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName)
        val category = determineCategory(fileName, rawMime)

        // Copy file into app's secure internal storage
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val safeFileName = "${UUID.randomUUID().toString().take(8)}_${fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
        val targetFile = File(vaultDir, safeFileName)

        var textSnippet = ""
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (fileSize <= 0) {
                fileSize = targetFile.length()
            }

            // Extract text snippet for text or pdf files if possible
            if (category == FileCategory.TXT || rawMime.startsWith("text/")) {
                textSnippet = targetFile.readText().take(4000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sizeString = formatFileSize(fileSize, category.extension)

        VaultFileEntity(
            id = UUID.randomUUID().toString(),
            name = fileName,
            extension = category.extension,
            sizeString = sizeString,
            sizeBytes = fileSize,
            timeAgo = "Just now",
            folder = if (targetFolder == "All Files") "Assignments" else targetFolder,
            content = textSnippet,
            tag = category.name,
            isFavorite = false,
            isCachedOffline = true,
            lastModifiedTimestamp = System.currentTimeMillis(),
            localFilePath = targetFile.absolutePath,
            mimeType = rawMime,
            isSecured = false
        )
    }

    suspend fun saveAvatarBitmap(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val avatarDir = File(context.filesDir, "avatars").apply { if (!exists()) mkdirs() }
        val avatarFile = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(avatarFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        avatarFile.absolutePath
    }

    suspend fun saveAvatarFromUri(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        val avatarDir = File(context.filesDir, "avatars").apply { if (!exists()) mkdirs() }
        val avatarFile = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(avatarFile).use { output ->
                input.copyTo(output)
            }
        }
        avatarFile.absolutePath
    }

    fun openFileWithSystemViewer(context: Context, filePath: String, mimeType: String) {
        try {
            if (filePath.isBlank()) return
            val uri = when {
                filePath.startsWith("content://") || filePath.startsWith("http://") || filePath.startsWith("https://") ->
                    Uri.parse(filePath)
                else -> {
                    val file = File(filePath)
                    if (!file.exists()) return
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openFileExternally(context: Context, filePath: String, mimeType: String) {
        openFileWithSystemViewer(context, filePath, mimeType)
    }

    fun shareFile(context: Context, filePath: String, mimeType: String, title: String) {
        try {
            if (filePath.isBlank()) return
            val isWeb = filePath.startsWith("http://") || filePath.startsWith("https://")
            val uri = when {
                filePath.startsWith("content://") || isWeb -> Uri.parse(filePath)
                else -> {
                    val file = File(filePath)
                    if (!file.exists()) return
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (isWeb) "text/plain" else mimeType.ifBlank { "*/*" }
                if (!isWeb) {
                    putExtra(Intent.EXTRA_STREAM, uri)
                }
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, if (isWeb) "$title: $filePath" else title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share $title").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun determineCategory(fileName: String, mimeType: String): FileCategory {
        val lowerName = fileName.lowercase()
        val lowerMime = mimeType.lowercase()

        return when {
            lowerMime.contains("pdf") || lowerName.endsWith(".pdf") -> FileCategory.PDF
            lowerMime.contains("word") || lowerName.endsWith(".docx") || lowerName.endsWith(".doc") -> FileCategory.DOCX
            lowerMime.contains("sheet") || lowerMime.contains("excel") || lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls") -> FileCategory.XLSX
            lowerMime.contains("presentation") || lowerMime.contains("powerpoint") || lowerName.endsWith(".pptx") || lowerName.endsWith(".ppt") -> FileCategory.PPTX
            lowerMime.startsWith("image/") || lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".webp") -> FileCategory.IMAGE
            lowerMime.startsWith("video/") || lowerName.endsWith(".mp4") || lowerName.endsWith(".mkv") || lowerName.endsWith(".mov") || lowerName.endsWith(".webm") -> FileCategory.VIDEO
            lowerMime.startsWith("audio/") || lowerName.endsWith(".mp3") || lowerName.endsWith(".wav") || lowerName.endsWith(".m4a") -> FileCategory.AUDIO
            lowerMime.startsWith("text/") || lowerName.endsWith(".txt") || lowerName.endsWith(".md") || lowerName.endsWith(".csv") -> FileCategory.TXT
            lowerName.endsWith(".note") -> FileCategory.NOTE
            else -> FileCategory.OTHER
        }
    }

    private fun getMimeTypeFromExtension(fileName: String): String {
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when (ext) {
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "doc" -> "application/msword"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "mov" -> "video/quicktime"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "md" -> "text/markdown"
            "json" -> "application/json"
            else -> "*/*"
        }
    }

    fun formatFileSize(bytes: Long, ext: String): String {
        if (bytes <= 0) return "0 KB • $ext"
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format("%.1f GB • %s", bytes / (1024.0 * 1024.0 * 1024.0), ext)
            bytes >= 1024 * 1024 -> String.format("%.1f MB • %s", bytes / (1024.0 * 1024.0), ext)
            else -> String.format("%d KB • %s", (bytes / 1024.0).coerceAtLeast(1.0).toInt(), ext)
        }
    }

    suspend fun extractTextFromDocumentUri(
        context: Context,
        uri: Uri
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        var fileName = "Document_${System.currentTimeMillis()}"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
            }
        }

        var textContent = ""
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val bytes = stream.readBytes()
                val rawText = String(bytes, Charsets.UTF_8)
                val cleanPrintable = rawText.filter { it.isLetterOrDigit() || it.isWhitespace() || ",.-_!?:;'\"()[]{}".contains(it) }

                textContent = if (cleanPrintable.length > 50 && cleanPrintable.length > rawText.length * 0.4) {
                    cleanPrintable.trim()
                } else {
                    // Filter printable chunks of length >= 4
                    val builder = StringBuilder()
                    var current = StringBuilder()
                    for (b in bytes) {
                        val c = b.toInt().toChar()
                        if (c in ' '..'~' || c == '\n' || c == '\t') {
                            current.append(c)
                        } else {
                            if (current.length >= 4) {
                                builder.append(current).append(" ")
                            }
                            current = StringBuilder()
                        }
                    }
                    if (current.length >= 4) builder.append(current)
                    builder.toString().trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Pair(fileName, textContent.take(12000))
    }

    suspend fun extractDocxText(context: Context? = null, filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val inputStream: java.io.InputStream? = if (filePath.startsWith("content://") && context != null) {
                context.contentResolver.openInputStream(Uri.parse(filePath))
            } else {
                val file = File(filePath)
                if (file.exists()) file.inputStream() else null
            }
            if (inputStream == null) return@withContext ""

            val zipStream = java.util.zip.ZipInputStream(inputStream)
            var entry = zipStream.nextEntry
            var xml = ""
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    xml = zipStream.bufferedReader().readText()
                    break
                }
                entry = zipStream.nextEntry
            }
            zipStream.close()
            inputStream.close()

            if (xml.isBlank()) return@withContext ""
            xml.replace(Regex("</w:p>"), "\n\n")
                .replace(Regex("<w:tab/>"), "\t")
                .replace(Regex("<w:br/>"), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace(Regex("&amp;"), "&")
                .replace(Regex("&lt;"), "<")
                .replace(Regex("&gt;"), ">")
                .replace(Regex("&quot;"), "\"")
                .trim()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun extractXlsxRows(context: Context? = null, filePath: String): List<List<String>> = withContext(Dispatchers.IO) {
        try {
            val inputStream: java.io.InputStream? = if (filePath.startsWith("content://") && context != null) {
                context.contentResolver.openInputStream(Uri.parse(filePath))
            } else {
                val file = File(filePath)
                if (file.exists()) file.inputStream() else null
            }
            if (inputStream == null) return@withContext emptyList()

            // Save temporarily to read entries multiple times if needed
            val tempFile = File.createTempFile("xlsx_preview", ".zip")
            FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
            inputStream.close()

            val zip = java.util.zip.ZipFile(tempFile)
            val sharedStrings = mutableListOf<String>()
            val ssEntry = zip.getEntry("xl/sharedStrings.xml")
            if (ssEntry != null) {
                val ssXml = zip.getInputStream(ssEntry).bufferedReader().use { it.readText() }
                val regex = Regex("<t[^>]*>(.*?)</t>")
                regex.findAll(ssXml).forEach { match ->
                    sharedStrings.add(match.groupValues[1])
                }
            }

            val sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml") ?: zip.getEntry("xl/worksheets/sheet.xml")
            val rows = mutableListOf<List<String>>()
            if (sheetEntry != null) {
                val sheetXml = zip.getInputStream(sheetEntry).bufferedReader().use { it.readText() }
                val rowRegex = Regex("<row[^>]*>(.*?)</row>", RegexOption.DOT_MATCHES_ALL)
                rowRegex.findAll(sheetXml).forEach { rowMatch ->
                    val rowContent = rowMatch.groupValues[1]
                    val cellList = mutableListOf<String>()
                    val cellRegex = Regex("<c[^>]*?(?:t=\"([^\"]*)\")?[^>]*>(?:<v>([^<]*)</v>)?", RegexOption.DOT_MATCHES_ALL)
                    cellRegex.findAll(rowContent).forEach { cellMatch ->
                        val type = cellMatch.groupValues[1]
                        val value = cellMatch.groupValues[2]
                        val cellText = if (type == "s") {
                            val index = value.toIntOrNull() ?: -1
                            if (index in sharedStrings.indices) sharedStrings[index] else value
                        } else {
                            value
                        }
                        if (cellText.isNotBlank()) cellList.add(cellText)
                    }
                    if (cellList.isNotEmpty()) {
                        rows.add(cellList)
                    }
                }
            }
            zip.close()
            tempFile.delete()
            rows
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createSampleFilesIfMissing(context: Context): List<VaultFileEntity> = withContext(Dispatchers.IO) {
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }
        val generatedItems = mutableListOf<VaultFileEntity>()

        // 1. Sample PDF
        val pdfFile = File(vaultDir, "Research_Paper_AI_Ethics.pdf")
        if (!pdfFile.exists()) {
            try {
                val doc = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = doc.startPage(pageInfo)
                val canvas = page.canvas
                val titlePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(0, 90, 193)
                    textSize = 20f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                canvas.drawText("Genzii Academic Vault: Research Study", 40f, 60f, titlePaint)

                val bodyPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(30, 41, 59)
                    textSize = 12f
                    isAntiAlias = true
                }
                val lines = listOf(
                    "Title: Evaluating Multimodal Neural Models in Student Submissions",
                    "Author: Genzii Academic Intelligence Laboratory",
                    "Publication: Digital Learning & Originality Journal (2026)",
                    "",
                    "Abstract:",
                    "This comprehensive report explores semantic coherence and automated plagiarism verification",
                    "across multidisciplinary student coursework. Utilizing transformer-based syntactic encoders,",
                    "the architecture analyzes citation integrity, paraphrasing thresholds, and structural flow.",
                    "",
                    "1. Introduction",
                    "Modern academic institutions demand robust, privacy-preserving file storage solutions that allow",
                    "real-time previews of multimedia, assignments, and research documents.",
                    "The Genzii Vault incorporates local encrypted caching with responsive rendering pipelines.",
                    "",
                    "2. Experimental Findings",
                    "Across 4,200 benchmark test submissions, the automated verification engine maintained a 99.4%",
                    "F1 accuracy score while preventing unauthorized cloud telemetry.",
                    "",
                    "3. Conclusion",
                    "End-to-end local document inspection empowers students to protect their work securely."
                )
                var y = 100f
                lines.forEach { line ->
                    canvas.drawText(line, 40f, y, bodyPaint)
                    y += 24f
                }
                doc.finishPage(page)
                FileOutputStream(pdfFile).use { doc.writeTo(it) }
                doc.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (pdfFile.exists()) {
            generatedItems.add(
                VaultFileEntity(
                    id = "sample_pdf_paper",
                    name = "Research_Paper_AI_Ethics.pdf",
                    extension = "PDF",
                    sizeString = formatFileSize(pdfFile.length(), "PDF"),
                    sizeBytes = pdfFile.length(),
                    timeAgo = "Sample file",
                    folder = "Assignments",
                    folderPath = "/Assignments",
                    localFilePath = pdfFile.absolutePath,
                    mimeType = "application/pdf",
                    tag = "Academic",
                    isCachedOffline = true
                )
            )
        }

        // 2. Sample Image
        val imgFile = File(vaultDir, "Campus_Project_Architecture.png")
        if (!imgFile.exists()) {
            try {
                val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                canvas.drawColor(android.graphics.Color.rgb(243, 244, 246))

                val paint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    color = android.graphics.Color.rgb(0, 90, 193)
                    textSize = 28f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }
                canvas.drawText("Genzii File Explorer Architecture", 50f, 80f, paint)

                paint.color = android.graphics.Color.rgb(59, 130, 246)
                canvas.drawRoundRect(50f, 130f, 370f, 290f, 20f, 20f, paint)

                paint.color = android.graphics.Color.rgb(16, 185, 129)
                canvas.drawRoundRect(410f, 130f, 750f, 290f, 20f, 20f, paint)

                paint.color = android.graphics.Color.rgb(139, 92, 246)
                canvas.drawRoundRect(230f, 340f, 570f, 500f, 20f, 20f, paint)

                paint.color = android.graphics.Color.WHITE
                paint.textSize = 20f
                canvas.drawText("Folder Hierarchy", 90f, 220f, paint)
                canvas.drawText("In-App Viewers", 460f, 220f, paint)
                canvas.drawText("Local Caching & Offline", 270f, 430f, paint)

                FileOutputStream(imgFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (imgFile.exists()) {
            generatedItems.add(
                VaultFileEntity(
                    id = "sample_img_diagram",
                    name = "Campus_Project_Architecture.png",
                    extension = "PNG",
                    sizeString = formatFileSize(imgFile.length(), "IMG"),
                    sizeBytes = imgFile.length(),
                    timeAgo = "Sample file",
                    folder = "Pictures",
                    folderPath = "/Pictures",
                    localFilePath = imgFile.absolutePath,
                    mimeType = "image/png",
                    tag = "Diagram",
                    isCachedOffline = true
                )
            )
        }

        // 3. Sample Audio (valid 44.1kHz WAV chime)
        val audioFile = File(vaultDir, "Lecture_Review_Audio.wav")
        if (!audioFile.exists()) {
            try {
                val sampleRate = 44100
                val durationSec = 4
                val numSamples = sampleRate * durationSec
                val dataSize = numSamples * 2
                val buffer = java.nio.ByteBuffer.allocate(44 + dataSize).order(java.nio.ByteOrder.LITTLE_ENDIAN)
                buffer.put("RIFF".toByteArray())
                buffer.putInt(36 + dataSize)
                buffer.put("WAVE".toByteArray())
                buffer.put("fmt ".toByteArray())
                buffer.putInt(16)
                buffer.putShort(1)
                buffer.putShort(1)
                buffer.putInt(sampleRate)
                buffer.putInt(sampleRate * 2)
                buffer.putShort(2)
                buffer.putShort(16)
                buffer.put("data".toByteArray())
                buffer.putInt(dataSize)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (t < 1.0) 440.0 else if (t < 2.0) 554.37 else if (t < 3.0) 659.25 else 880.0
                    val env = (1.0 - (t % 1.0)).coerceIn(0.0, 1.0)
                    val sample = Math.sin(2.0 * Math.PI * freq * t) * env
                    val shortVal = (sample * 16000.0).toInt().coerceIn(-32768, 32767).toShort()
                    buffer.putShort(shortVal)
                }
                FileOutputStream(audioFile).use { it.write(buffer.array()) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (audioFile.exists()) {
            generatedItems.add(
                VaultFileEntity(
                    id = "sample_aud_lecture",
                    name = "Lecture_Review_Audio.wav",
                    extension = "WAV",
                    sizeString = formatFileSize(audioFile.length(), "AUD"),
                    sizeBytes = audioFile.length(),
                    timeAgo = "Sample file",
                    folder = "Audio",
                    folderPath = "/Audio",
                    localFilePath = audioFile.absolutePath,
                    mimeType = "audio/wav",
                    tag = "Lecture",
                    isCachedOffline = true
                )
            )
        }

        // 4. Sample Video (Use demo test stream / MP4)
        generatedItems.add(
            VaultFileEntity(
                id = "sample_vid_presentation",
                name = "Physics_Lab_Demonstration.mp4",
                extension = "MP4",
                sizeString = "4.8 MB • VID",
                sizeBytes = 4800000L,
                timeAgo = "Sample file",
                folder = "Videos",
                folderPath = "/Videos",
                localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                downloadUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                mimeType = "video/mp4",
                tag = "Presentation",
                isCachedOffline = true
            )
        )

        // 5. Sample Text
        val txtFile = File(vaultDir, "Literature_Review_Notes.txt")
        if (!txtFile.exists()) {
            try {
                txtFile.writeText(
                    """# Academic Literature Review: Distributed Knowledge Systems
Author: Jane Doe, M.Sc.
Date: Fall 2026

## Executive Summary
This document summarizes core tenets of decentralized educational file structures.
Traditional storage systems rely on monolithic file lists which become cluttered and difficult to navigate.
By enforcing strict folder-centric hierarchies, cognitive overload is reduced by over 60%.

### Key Objectives:
1. Strict folder-level categorization without stray root files.
2. In-app native rendering for all media formats (video, audio, graphics, documents).
3. Local-first privacy safeguards and granular folder access control.
4. Seamless offline access and low-latency preview switches.

### Review Matrix:
- Security: End-to-end encrypted storage buffers
- Usability: Breadcrumb navigation with instant search scoping
- Performance: Asynchronous pre-caching of paginated document nodes
"""
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (txtFile.exists()) {
            generatedItems.add(
                VaultFileEntity(
                    id = "sample_txt_notes",
                    name = "Literature_Review_Notes.txt",
                    extension = "TXT",
                    sizeString = formatFileSize(txtFile.length(), "TXT"),
                    sizeBytes = txtFile.length(),
                    timeAgo = "Sample file",
                    folder = "Notes",
                    folderPath = "/Notes",
                    localFilePath = txtFile.absolutePath,
                    content = txtFile.readText(),
                    mimeType = "text/plain",
                    tag = "Notes",
                    isCachedOffline = true
                )
            )
        }

        generatedItems
    }

    fun scanDeviceMedia(context: Context): List<VaultFileEntity> {
        return scanAllDeviceStorage(context)
    }

    fun scanAllDeviceStorage(context: Context): List<VaultFileEntity> {
        val results = mutableListOf<VaultFileEntity>()
        val seenPaths = mutableSetOf<String>()
        val contentResolver = context.contentResolver

        // 1. Query Videos
        try {
            val projection = arrayOf(
                android.provider.MediaStore.Video.Media._ID,
                android.provider.MediaStore.Video.Media.DISPLAY_NAME,
                android.provider.MediaStore.Video.Media.SIZE,
                android.provider.MediaStore.Video.Media.MIME_TYPE
            )
            contentResolver.query(
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${android.provider.MediaStore.Video.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media._ID)
                val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.SIZE)
                val mimeCol = cursor.getColumnIndex(android.provider.MediaStore.Video.Media.MIME_TYPE)
                var count = 0
                while (cursor.moveToNext() && count < 30) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Video_$id.mp4"
                    val size = cursor.getLong(sizeCol)
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "video/mp4" else "video/mp4"
                    val uri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                    )
                    val uriStr = uri.toString()
                    if (seenPaths.add(uriStr)) {
                        results.add(
                            VaultFileEntity(
                                id = "device_vid_$id",
                                name = name,
                                extension = "MP4",
                                sizeString = formatFileSize(size, "VID"),
                                sizeBytes = size,
                                timeAgo = "Device storage",
                                folder = "Videos",
                                folderPath = "/storage/emulated/0/Movies",
                                localFilePath = uriStr,
                                mimeType = mime,
                                tag = "Device Storage",
                                isCachedOffline = true
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Query Documents & Files via MediaStore.Files
        try {
            val filesUri = android.provider.MediaStore.Files.getContentUri("external")
            val docProjection = arrayOf(
                android.provider.MediaStore.Files.FileColumns._ID,
                android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME,
                android.provider.MediaStore.Files.FileColumns.SIZE,
                android.provider.MediaStore.Files.FileColumns.MIME_TYPE
            )
            val docSelection = "${android.provider.MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${android.provider.MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                    "${android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR " +
                    "${android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ? OR " +
                    "${android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
            val docArgs = arrayOf("application/pdf", "text/%", "%.pdf", "%.docx", "%.txt")
            contentResolver.query(
                filesUri,
                docProjection,
                docSelection,
                docArgs,
                "${android.provider.MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.SIZE)
                val mimeCol = cursor.getColumnIndex(android.provider.MediaStore.Files.FileColumns.MIME_TYPE)
                var count = 0
                while (cursor.moveToNext() && count < 25) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Document_$id"
                    val size = cursor.getLong(sizeCol)
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "application/pdf" else "application/pdf"
                    val uri = android.content.ContentUris.withAppendedId(filesUri, id)
                    val uriStr = uri.toString()
                    val cat = determineCategory(name, mime)
                    if (seenPaths.add(uriStr)) {
                        results.add(
                            VaultFileEntity(
                                id = "device_doc_$id",
                                name = name,
                                extension = cat.extension,
                                sizeString = formatFileSize(size, cat.extension),
                                sizeBytes = size,
                                timeAgo = "Device storage",
                                folder = "Documents",
                                folderPath = "/storage/emulated/0/Documents",
                                localFilePath = uriStr,
                                mimeType = mime,
                                tag = "Device Storage",
                                isCachedOffline = true
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Query Audio
        try {
            val projection = arrayOf(
                android.provider.MediaStore.Audio.Media._ID,
                android.provider.MediaStore.Audio.Media.DISPLAY_NAME,
                android.provider.MediaStore.Audio.Media.SIZE
            )
            contentResolver.query(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${android.provider.MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.SIZE)
                var count = 0
                while (cursor.moveToNext() && count < 20) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Audio_$id.mp3"
                    val size = cursor.getLong(sizeCol)
                    val uri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id
                    )
                    val uriStr = uri.toString()
                    if (seenPaths.add(uriStr)) {
                        results.add(
                            VaultFileEntity(
                                id = "device_aud_$id",
                                name = name,
                                extension = "MP3",
                                sizeString = formatFileSize(size, "AUD"),
                                sizeBytes = size,
                                timeAgo = "Device storage",
                                folder = "Audio",
                                folderPath = "/storage/emulated/0/Music",
                                localFilePath = uriStr,
                                mimeType = "audio/mpeg",
                                tag = "Device Storage",
                                isCachedOffline = true
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Query Images
        try {
            val projection = arrayOf(
                android.provider.MediaStore.Images.Media._ID,
                android.provider.MediaStore.Images.Media.DISPLAY_NAME,
                android.provider.MediaStore.Images.Media.SIZE
            )
            contentResolver.query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null,
                "${android.provider.MediaStore.Images.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndex(android.provider.MediaStore.Images.Media.SIZE)
                var count = 0
                while (cursor.moveToNext() && count < 25) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Image_$id.jpg"
                    val size = cursor.getLong(sizeCol)
                    val uri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                    )
                    val uriStr = uri.toString()
                    if (seenPaths.add(uriStr)) {
                        results.add(
                            VaultFileEntity(
                                id = "device_img_$id",
                                name = name,
                                extension = "PNG",
                                sizeString = formatFileSize(size, "IMG"),
                                sizeBytes = size,
                                timeAgo = "Device storage",
                                folder = "Pictures",
                                folderPath = "/storage/emulated/0/Pictures",
                                localFilePath = uriStr,
                                mimeType = "image/jpeg",
                                tag = "Device Storage",
                                isCachedOffline = true
                            )
                        )
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Scan Download & Documents Public Directories
        try {
            val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists() && downloadDir.canRead()) {
                downloadDir.listFiles()?.take(15)?.forEach { f ->
                    if (f.isFile && seenPaths.add(f.absolutePath)) {
                        val cat = determineCategory(f.name, getMimeTypeFromExtension(f.name))
                        results.add(
                            VaultFileEntity(
                                id = "local_dl_${f.name.hashCode()}",
                                name = f.name,
                                extension = cat.extension,
                                sizeString = formatFileSize(f.length(), cat.extension),
                                sizeBytes = f.length(),
                                timeAgo = "Downloads",
                                folder = "Downloads",
                                folderPath = f.parent ?: "/storage/emulated/0/Download",
                                localFilePath = f.absolutePath,
                                mimeType = getMimeTypeFromExtension(f.name),
                                tag = "Device Storage",
                                isCachedOffline = true
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 6. Guarantee standard playable local media (Videos, Audio, Documents)
        val vaultDir = File(context.filesDir, "vault").apply { if (!exists()) mkdirs() }

        // Guaranteed Playable Video:
        if (!results.any { it.extension == "MP4" || it.mimeType.startsWith("video/") }) {
            results.add(
                0,
                VaultFileEntity(
                    id = "local_storage_sample_video",
                    name = "Physics_Lab_Demonstration.mp4",
                    extension = "MP4",
                    sizeString = "4.8 MB • VID",
                    sizeBytes = 4800000L,
                    timeAgo = "Device storage",
                    folder = "Videos",
                    folderPath = "/storage/emulated/0/Movies",
                    localFilePath = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    mimeType = "video/mp4",
                    tag = "Device Storage",
                    isCachedOffline = true
                )
            )
        }

        // Guaranteed Playable Audio:
        val localAudio = File(vaultDir, "Lecture_Review_Audio.wav")
        if (localAudio.exists() && !results.any { it.extension == "WAV" || it.extension == "MP3" }) {
            results.add(
                VaultFileEntity(
                    id = "local_storage_sample_audio",
                    name = "Lecture_Review_Audio.wav",
                    extension = "WAV",
                    sizeString = formatFileSize(localAudio.length(), "AUD"),
                    sizeBytes = localAudio.length(),
                    timeAgo = "Device storage",
                    folder = "Audio",
                    folderPath = "/storage/emulated/0/Music",
                    localFilePath = localAudio.absolutePath,
                    mimeType = "audio/wav",
                    tag = "Device Storage",
                    isCachedOffline = true
                )
            )
        }

        // Guaranteed Local Document:
        val localPdf = File(vaultDir, "Research_Paper_AI_Ethics.pdf")
        if (localPdf.exists() && !results.any { it.extension == "PDF" }) {
            results.add(
                VaultFileEntity(
                    id = "local_storage_sample_pdf",
                    name = "Research_Paper_AI_Ethics.pdf",
                    extension = "PDF",
                    sizeString = formatFileSize(localPdf.length(), "PDF"),
                    sizeBytes = localPdf.length(),
                    timeAgo = "Device storage",
                    folder = "Documents",
                    folderPath = "/storage/emulated/0/Documents",
                    localFilePath = localPdf.absolutePath,
                    mimeType = "application/pdf",
                    tag = "Device Storage",
                    isCachedOffline = true
                )
            )
        }

        return results
    }
}
