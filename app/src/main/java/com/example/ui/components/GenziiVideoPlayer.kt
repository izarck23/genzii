package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale

private const val DEFAULT_SAMPLE_VIDEO_URL =
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

/**
 * High-performance, production-ready video player supporting both Landscape and Portrait orientations,
 * auto-rotation, manual fullscreen orientation toggle, interactive seek scrubber, time indicators,
 * rewind/forward 10s, and clean lifecycle management.
 */
@Composable
fun GenziiVideoPlayer(
    localFilePath: String,
    videoTitle: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    allowFullscreenToggle: Boolean = true,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val configuration = LocalConfiguration.current
    val isSystemLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isUserFullscreen by remember { mutableStateOf(false) }
    val isLandscape = isSystemLandscape || isUserFullscreen

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(1) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekProgress by remember { mutableFloatStateOf(0f) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    // Resolve playable video source
    val resolvedSource = remember(localFilePath) {
        when {
            localFilePath.isBlank() -> DEFAULT_SAMPLE_VIDEO_URL
            localFilePath.startsWith("content://") ||
                    localFilePath.startsWith("http://") ||
                    localFilePath.startsWith("https://") -> localFilePath
            File(localFilePath).exists() -> localFilePath
            else -> DEFAULT_SAMPLE_VIDEO_URL
        }
    }

    // Auto-hide controls overlay after 3.5s of inactivity while playing
    LaunchedEffect(controlsVisible, isPlaying, isSeeking) {
        if (controlsVisible && isPlaying && !isSeeking) {
            delay(3500)
            controlsVisible = false
        }
    }

    // Polling loop for playback progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                try {
                    if (!isSeeking && vv.isPlaying) {
                        currentPositionMs = vv.currentPosition
                        val dur = vv.duration
                        if (dur > 0) durationMs = dur
                    }
                } catch (_: Exception) {}
            }
            delay(250)
        }
    }

    // Restore portrait orientation when disposing player
    DisposableEffect(Unit) {
        onDispose {
            try {
                videoViewRef?.stopPlayback()
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } catch (_: Exception) {}
        }
    }

    fun toggleOrientation() {
        val targetLandscape = !isLandscape
        isUserFullscreen = targetLandscape
        activity?.requestedOrientation = if (targetLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    fun seekToOffset(offsetMs: Int) {
        videoViewRef?.let { vv ->
            try {
                val newPos = (vv.currentPosition + offsetMs).coerceIn(0, durationMs)
                vv.seekTo(newPos)
                currentPositionMs = newPos
                controlsVisible = true
            } catch (_: Exception) {}
        }
    }

    fun togglePlayPause() {
        videoViewRef?.let { vv ->
            try {
                if (vv.isPlaying) {
                    vv.pause()
                    isPlaying = false
                } else {
                    vv.start()
                    isPlaying = true
                }
                controlsVisible = true
            } catch (_: Exception) {}
        }
    }

    fun toggleMute() {
        isMuted = !isMuted
        mediaPlayerRef?.let { mp ->
            try {
                val vol = if (isMuted) 0f else 1f
                mp.setVolume(vol, vol)
            } catch (_: Exception) {}
        }
    }

    // Outer Container
    Box(
        modifier = modifier
            .then(
                if (isLandscape) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                controlsVisible = !controlsVisible
            },
        contentAlignment = Alignment.Center
    ) {
        // Native VideoView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    videoViewRef = this
                    if (resolvedSource.startsWith("content://") ||
                        resolvedSource.startsWith("http://") ||
                        resolvedSource.startsWith("https://")
                    ) {
                        setVideoURI(Uri.parse(resolvedSource))
                    } else {
                        setVideoPath(resolvedSource)
                    }

                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        durationMs = if (mp.duration > 0) mp.duration else 1
                        isBuffering = false
                        mp.isLooping = false
                        if (isMuted) mp.setVolume(0f, 0f)
                        start()
                        isPlaying = true
                    }

                    setOnCompletionListener {
                        isPlaying = false
                        currentPositionMs = durationMs
                        controlsVisible = true
                    }

                    setOnErrorListener { _, _, _ ->
                        isBuffering = false
                        isPlaying = false
                        true // Suppress default error alert
                    }
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            onReset = { vv ->
                try {
                    vv.stopPlayback()
                } catch (_: Throwable) {}
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering Indicator
        if (isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFEC4899),
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Control Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (onClose != null || isLandscape) {
                            IconButton(
                                onClick = {
                                    if (isLandscape && !isSystemLandscape) {
                                        toggleOrientation()
                                    } else {
                                        onClose?.invoke()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Text(
                            text = videoTitle.ifBlank { "Video Media" },
                            color = Color.White,
                            fontSize = if (isLandscape) 15.sp else 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onToggleFavorite != null) {
                            IconButton(
                                onClick = { onToggleFavorite() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (isFavorite) Color(0xFFFBBF24) else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        if (onShare != null) {
                            IconButton(
                                onClick = { onShare() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Mute button
                        IconButton(
                            onClick = { toggleMute() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                contentDescription = if (isMuted) "Unmute" else "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (allowFullscreenToggle) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Rotate / Fullscreen Button
                            IconButton(
                                onClick = { toggleOrientation() },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEC4899).copy(alpha = 0.8f))
                            ) {
                                Icon(
                                    imageVector = if (isLandscape) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = if (isLandscape) "Exit Fullscreen" else "Fullscreen Landscape",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Center Playback Controls (Rewind 10s, Big Play/Pause, Forward 10s)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 36.dp else 20.dp)
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { seekToOffset(-10000) },
                        modifier = Modifier
                            .size(if (isLandscape) 52.dp else 42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Replay 10s",
                            tint = Color.White,
                            modifier = Modifier.size(if (isLandscape) 30.dp else 24.dp)
                        )
                    }

                    // Main Play/Pause Button
                    IconButton(
                        onClick = { togglePlayPause() },
                        modifier = Modifier
                            .size(if (isLandscape) 68.dp else 56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEC4899))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(if (isLandscape) 40.dp else 34.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { seekToOffset(10000) },
                        modifier = Modifier
                            .size(if (isLandscape) 52.dp else 42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(if (isLandscape) 30.dp else 24.dp)
                        )
                    }
                }

                // Bottom Scrubber Bar & Time
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = if (isLandscape) 14.dp else 8.dp)
                ) {
                    // Timeline Slider
                    Slider(
                        value = if (isSeeking) seekProgress else (currentPositionMs.toFloat() / durationMs.coerceAtLeast(1)),
                        onValueChange = { progress ->
                            isSeeking = true
                            seekProgress = progress
                        },
                        onValueChangeFinished = {
                            val targetMs = (seekProgress * durationMs).toInt().coerceIn(0, durationMs)
                            videoViewRef?.seekTo(targetMs)
                            currentPositionMs = targetMs
                            isSeeking = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFEC4899),
                            activeTrackColor = Color(0xFFEC4899),
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentMs = if (isSeeking) (seekProgress * durationMs).toInt() else currentPositionMs
                        Text(
                            text = "${formatDurationMs(currentMs)} / ${formatDurationMs(durationMs)}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isLandscape) "Landscape Mode" else "Portrait",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ScreenRotation,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { toggleOrientation() }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
