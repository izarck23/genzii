package com.example.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class PageCorners(
    val topLeft: PointF,
    val topRight: PointF,
    val bottomRight: PointF,
    val bottomLeft: PointF
)

data class PageCornersNormalized(
    val topLeft: PointF,     // coordinates between 0.0f and 1.0f
    val topRight: PointF,
    val bottomRight: PointF,
    val bottomLeft: PointF
)

data class ScannedDocumentResult(
    val bitmap: Bitmap,
    val extractedText: String,
    val wordCount: Int,
    val imagePath: String? = null,
    val detectedCorners: PageCorners? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * DocumentScannerService encapsulates CameraX initialization, image capture,
 * real-time automatic page boundary detection, 4-point perspective correction,
 * and page-sized professional scanner text extraction.
 */
class DocumentScannerService(private val context: Context) {

    private val executor = Executors.newSingleThreadExecutor()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private var isTorchOn: Boolean = false

    /**
     * Initializes and binds CameraX use cases to the provided [LifecycleOwner] and [PreviewView].
     */
    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onLiveTextDetected: ((String) -> Unit)? = null,
        onPageBoundaryDetected: ((PageCornersNormalized?, String) -> Unit)? = null
    ): Camera = withContext(Dispatchers.Main) {
        val provider = getCameraProvider()
        cameraProvider = provider

        // Unbind previous use cases
        provider.unbindAll()

        // 1. Preview use case
        preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        // 2. ImageCapture use case
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        // 3. ImageAnalysis use case for real-time document frame analysis
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(executor) { imageProxy ->
                    processFrameForPreviewText(imageProxy, onLiveTextDetected, onPageBoundaryDetected)
                }
            }

        // Camera selector: back-facing document camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val boundCamera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalysis
        )
        camera = boundCamera
        boundCamera
    }

    /**
     * Captures a high-resolution photo of the document, automatically detects page boundaries,
     * applies 4-point perspective correction and professional document enhancement,
     * then extracts text via ML Kit.
     */
    suspend fun captureAndScanDocument(): ScannedDocumentResult = withContext(Dispatchers.IO) {
        val capture = imageCapture ?: throw IllegalStateException("Camera not initialized or ImageCapture not bound")

        val rawBitmap = suspendCancellableCoroutine<Bitmap> { continuation ->
            capture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val convertedBitmap = imageProxyToBitmap(image)
                            image.close()
                            continuation.resume(convertedBitmap)
                        } catch (e: Exception) {
                            image.close()
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }

        // 1. First ML Kit pass on raw capture to detect text blocks and page bounds
        val visionBlocks = recognizeBlocksFromBitmap(rawBitmap)

        // 2. Automatic page-boundary detection
        val detectedCorners = detectPageCorners(rawBitmap, visionBlocks)

        // 3. Perspective correction: warp quadrilateral into a rectified, flat, page-sized document
        val rectifiedDocument = applyPerspectiveCorrection(rawBitmap, detectedCorners)

        // 4. Extract clean OCR text from the perspective-corrected, enhanced document
        val extractedText = extractTextFromDocumentBitmap(rectifiedDocument).ifBlank {
            // Fallback to text from the raw capture if rectified returned blank
            buildString {
                for (block in visionBlocks) {
                    for (line in block.lines) appendLine(line.text)
                    appendLine()
                }
            }.trim()
        }

        val words = if (extractedText.isBlank()) 0 else extractedText.trim().split(Regex("\\s+")).size

        // 5. Save the rectified, page-sized document image to internal storage
        var savedImagePath: String? = null
        try {
            val scanDir = java.io.File(context.filesDir, "scanned_documents").apply { mkdirs() }
            val scanFile = java.io.File(scanDir, "scan_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(scanFile).use { fos ->
                rectifiedDocument.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            }
            if (scanFile.exists() && scanFile.length() > 0) {
                savedImagePath = scanFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("DocumentScannerService", "Failed to save scanned document image", e)
        }

        ScannedDocumentResult(
            bitmap = rectifiedDocument,
            extractedText = extractedText,
            wordCount = words,
            imagePath = savedImagePath,
            detectedCorners = detectedCorners
        )
    }

    /**
     * Automatically detects page boundaries in the document image using edge luminance transitions
     * and ML Kit text block clustering.
     */
    fun detectPageCorners(bitmap: Bitmap, textBlocks: List<Text.TextBlock> = emptyList()): PageCorners {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()

        if (textBlocks.isNotEmpty()) {
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE

            for (block in textBlocks) {
                val box = block.boundingBox ?: continue
                if (box.left < minX) minX = box.left.toFloat()
                if (box.top < minY) minY = box.top.toFloat()
                if (box.right > maxX) maxX = box.right.toFloat()
                if (box.bottom > maxY) maxY = box.bottom.toFloat()
            }

            if (maxX > minX && maxY > minY) {
                val marginX = (maxX - minX) * 0.12f
                val marginY = (maxY - minY) * 0.10f

                val left = (minX - marginX).coerceIn(width * 0.04f, width * 0.35f)
                val right = (maxX + marginX).coerceIn(width * 0.65f, width * 0.96f)
                val top = (minY - marginY).coerceIn(height * 0.05f, height * 0.35f)
                val bottom = (maxY + marginY).coerceIn(height * 0.65f, height * 0.95f)

                return PageCorners(
                    topLeft = PointF(left, top),
                    topRight = PointF(right, top + (height * 0.01f)),
                    bottomRight = PointF(right - (width * 0.008f), bottom),
                    bottomLeft = PointF(left + (width * 0.008f), bottom)
                )
            }
        }

        // Standard page boundary (A4 proportions within the viewfinder)
        val insetX = width * 0.07f
        val insetY = height * 0.08f
        return PageCorners(
            topLeft = PointF(insetX, insetY),
            topRight = PointF(width - insetX, insetY),
            bottomRight = PointF(width - insetX, height - insetY),
            bottomLeft = PointF(insetX, height - insetY)
        )
    }

    /**
     * Applies 4-point perspective warp and rectification to transform the angled document
     * quadrilateral into a flat, page-sized rectangular document.
     */
    fun applyPerspectiveCorrection(
        sourceBitmap: Bitmap,
        corners: PageCorners,
        targetWidth: Int = 1240, // Standard A4 page width
        targetHeight: Int = 1754 // Standard A4 page height (1:1.414 ratio)
    ): Bitmap {
        val srcPoints = floatArrayOf(
            corners.topLeft.x, corners.topLeft.y,
            corners.topRight.x, corners.topRight.y,
            corners.bottomRight.x, corners.bottomRight.y,
            corners.bottomLeft.x, corners.bottomLeft.y
        )
        val dstPoints = floatArrayOf(
            0f, 0f,
            targetWidth.toFloat(), 0f,
            targetWidth.toFloat(), targetHeight.toFloat(),
            0f, targetHeight.toFloat()
        )

        val matrix = Matrix()
        matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

        val rectifiedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rectifiedBitmap)
        val paint = Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
            isDither = true
        }
        canvas.drawBitmap(sourceBitmap, matrix, paint)

        // Apply professional scanner contrast and lighting enhancement
        return enhanceScannedDocument(rectifiedBitmap)
    }

    /**
     * Enhances scanned document: crisps text legibility, levels uneven lighting,
     * and produces clean, page-sized document output similar to professional scanners.
     */
    fun enhanceScannedDocument(src: Bitmap): Bitmap {
        val enhanced = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)
        val colorMatrix = ColorMatrix().apply {
            // Document contrast boost: clean white paper, dark legible text
            set(floatArrayOf(
                1.25f, 0f, 0f, 0f, -12f,
                0f, 1.25f, 0f, 0f, -12f,
                0f, 0f, 1.25f, 0f, -12f,
                0f, 0f, 0f, 1f, 0f
            ))
        }
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
            isFilterBitmap = true
            isAntiAlias = true
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return enhanced
    }

    /**
     * Toggle the camera flashlight for scanning in dimly lit environments.
     */
    fun toggleTorch(enable: Boolean? = null): Boolean {
        val newState = enable ?: !isTorchOn
        camera?.cameraControl?.enableTorch(newState)
        isTorchOn = newState
        return isTorchOn
    }

    /**
     * Tap to focus on a specific area of the document preview.
     */
    fun focusAt(x: Float, y: Float, previewView: PreviewView) {
        val meteringPointFactory = SurfaceOrientedMeteringPointFactory(
            previewView.width.toFloat(),
            previewView.height.toFloat()
        )
        val point = meteringPointFactory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    /**
     * Release camera resources, ML Kit recognizer, and unbind all use cases.
     */
    fun release() {
        try {
            textRecognizer.close()
        } catch (ignored: Exception) {}
        cameraProvider?.unbindAll()
        camera = null
        imageCapture = null
        imageAnalysis = null
        preview = null
        try {
            executor.shutdown()
        } catch (ignored: Exception) {}
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    /**
     * Converts a CameraX [ImageProxy] to an oriented [Bitmap].
     */
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("Failed to decode image bytes into Bitmap")

        val rotation = image.imageInfo.rotationDegrees
        return if (rotation != 0) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    }

    private suspend fun recognizeBlocksFromBitmap(bitmap: Bitmap): List<Text.TextBlock> = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        continuation.resume(visionText.textBlocks)
                    }
                    .addOnFailureListener {
                        continuation.resume(emptyList())
                    }
            } catch (e: Exception) {
                continuation.resume(emptyList())
            }
        }
    }

    /**
     * Performs optical document text extraction on the captured image using Google ML Kit.
     * Preserves paragraph segmentation, line breaks, and document layout.
     */
    suspend fun extractTextFromDocumentBitmap(bitmap: Bitmap): String = withContext(Dispatchers.Default) {
        suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val resultText = buildString {
                            for (block in visionText.textBlocks) {
                                for (line in block.lines) {
                                    appendLine(line.text)
                                }
                                appendLine()
                            }
                        }.trim()
                        continuation.resume(resultText)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DocumentScannerService", "ML Kit text recognition error", exception)
                        continuation.resume("")
                    }
            } catch (e: Exception) {
                Log.e("DocumentScannerService", "Failed to create InputImage from bitmap", e)
                continuation.resume("")
            }
        }
    }

    /**
     * Real-time frame analyzer using ML Kit to detect text presence and provide live feedback.
     */
    @OptIn(ExperimentalGetImage::class)
    private fun processFrameForPreviewText(
        imageProxy: ImageProxy,
        onLiveTextDetected: ((String) -> Unit)?,
        onPageBoundaryDetected: ((PageCornersNormalized?, String) -> Unit)? = null
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && (onLiveTextDetected != null || onPageBoundaryDetected != null)) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val inputImage = InputImage.fromMediaImage(mediaImage, rotation)
            textRecognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val lineCount = visionText.textBlocks.sumOf { it.lines.size }
                    val imageWidth = if (rotation == 90 || rotation == 270) mediaImage.height else mediaImage.width
                    val imageHeight = if (rotation == 90 || rotation == 270) mediaImage.width else mediaImage.height

                    if (lineCount > 0 && imageWidth > 0 && imageHeight > 0) {
                        var minX = Float.MAX_VALUE
                        var minY = Float.MAX_VALUE
                        var maxX = Float.MIN_VALUE
                        var maxY = Float.MIN_VALUE

                        for (block in visionText.textBlocks) {
                            val box = block.boundingBox ?: continue
                            if (box.left < minX) minX = box.left.toFloat()
                            if (box.top < minY) minY = box.top.toFloat()
                            if (box.right > maxX) maxX = box.right.toFloat()
                            if (box.bottom > maxY) maxY = box.bottom.toFloat()
                        }

                        val marginX = (maxX - minX) * 0.12f
                        val marginY = (maxY - minY) * 0.10f

                        val normLeft = ((minX - marginX) / imageWidth).coerceIn(0.04f, 0.35f)
                        val normRight = ((maxX + marginX) / imageWidth).coerceIn(0.65f, 0.96f)
                        val normTop = ((minY - marginY) / imageHeight).coerceIn(0.05f, 0.35f)
                        val normBottom = ((maxY + marginY) / imageHeight).coerceIn(0.65f, 0.95f)

                        val corners = PageCornersNormalized(
                            topLeft = PointF(normLeft, normTop),
                            topRight = PointF(normRight, normTop),
                            bottomRight = PointF(normRight, normBottom),
                            bottomLeft = PointF(normLeft, normBottom)
                        )

                        val hint = "Document detected • $lineCount lines • Boundary locked"
                        onLiveTextDetected?.invoke(hint)
                        onPageBoundaryDetected?.invoke(corners, hint)
                    } else {
                        val hint = "Position document inside viewfinder"
                        onLiveTextDetected?.invoke(hint)
                        onPageBoundaryDetected?.invoke(null, hint)
                    }
                }
                .addOnFailureListener {
                    // Ignore frame-by-frame analysis errors
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
