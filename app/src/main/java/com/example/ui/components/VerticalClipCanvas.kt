package com.example.ui.components

import android.content.Intent
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.CaptionStylePreset
import com.example.model.ClipStudioConfig
import com.example.model.FacecamShape
import com.example.model.GlowBorderOption
import com.example.model.LayoutPreset
import com.example.model.ViralClip
import com.example.network.BackendApiClient
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorNeon
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class RemotePlaybackState {
    LOADING,
    READY,
    ERROR
}

/**
 * Low-level touch gesture helper that mimics web `touch-action: none` + onTouchStart/Move/End:
 * Immediately consumes pointer down & every subsequent move so parent vertical scroll containers
 * never hijack touch dragging on mobile screens.
 */
private fun Modifier.touchDragNoScroll(
    key: Any?,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDragDelta: (dx: Float, dy: Float) -> Unit
): Modifier = this.pointerInput(key) {
    awaitEachGesture {
        val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false)
        down.consume()
        onDragStart()
        try {
            var activePointerId = down.id
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == activePointerId }
                    ?: event.changes.firstOrNull { it.pressed }
                    ?: break
                activePointerId = change.id
                if (!change.pressed) {
                    change.consume()
                    break
                }
                val delta = change.positionChange()
                if (delta != Offset.Zero) {
                    change.consume()
                    onDragDelta(delta.x, delta.y)
                } else {
                    change.consume()
                }
            }
        } finally {
            onDragEnd()
        }
    }
}

@Composable
fun VerticalClipCanvas(
    clip: ViralClip,
    config: ClipStudioConfig,
    isInteractive: Boolean,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    activeServerUrl: String = "",
    useRemoteVideoPlayback: Boolean = false,
    onDragStateChange: (Boolean) -> Unit = {},
    onConfigChange: ((ClipStudioConfig) -> ClipStudioConfig) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val latestConfig by rememberUpdatedState(config)
    val latestOnConfigChange by rememberUpdatedState(onConfigChange)
    val latestOnDragStateChange by rememberUpdatedState(onDragStateChange)

    val resolvedRemoteVideoUrl = remember(clip.videoUrl, activeServerUrl, useRemoteVideoPlayback) {
        if (!useRemoteVideoPlayback) {
            null
        } else {
            BackendApiClient.resolveMediaUrl(activeServerUrl, clip.videoUrl)
        }
    }

    var retryNonce by remember(resolvedRemoteVideoUrl) { mutableIntStateOf(0) }
    var playbackState by remember(resolvedRemoteVideoUrl, retryNonce) {
        mutableStateOf(
            if (!resolvedRemoteVideoUrl.isNullOrBlank()) RemotePlaybackState.LOADING
            else RemotePlaybackState.READY
        )
    }
    var playbackErrorDetail by remember(resolvedRemoteVideoUrl, retryNonce) {
        mutableStateOf<String?>(null)
    }

    // Watchdog prevents videos from ever staying stuck on an endless black screen or spinner
    LaunchedEffect(resolvedRemoteVideoUrl, retryNonce, playbackState, isInteractive) {
        if (!isInteractive &&
            !resolvedRemoteVideoUrl.isNullOrBlank() &&
            playbackState == RemotePlaybackState.LOADING
        ) {
            delay(10_000L)
            if (playbackState == RemotePlaybackState.LOADING) {
                playbackErrorDetail = "Stream timed out while loading metadata."
                playbackState = RemotePlaybackState.ERROR
            }
        }
    }

    val words = remember(config.captionText) {
        config.captionText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }
    var activeWordIndex by remember(clip.id, words.size) { mutableIntStateOf(0) }
    var elapsedSec by remember(clip.id) { mutableIntStateOf(0) }

    LaunchedEffect(isPlaying, clip.id, words.size) {
        if (!isPlaying) return@LaunchedEffect
        var tick = 0
        while (true) {
            delay(340L)
            tick++
            if (words.isNotEmpty()) {
                activeWordIndex = (activeWordIndex + 1) % words.size
            }
            if (tick % 3 == 0) {
                elapsedSec = (elapsedSec + 1) % clip.durationSec.coerceAtLeast(1)
            }
        }
    }

    val motionTransition = rememberInfiniteTransition(label = "clip_motion")
    val kineticScale by motionTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kinetic_zoom"
    )
    val wavePhase by motionTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val effectiveKineticZoom = if (isPlaying) kineticScale else 1.0f
    val canvasShape = RoundedCornerShape(20.dp)
    val shouldUseRemoteStream = !resolvedRemoteVideoUrl.isNullOrBlank() && !isInteractive

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(canvasShape)
            .background(VoidBlack)
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        ElectricCyan.copy(alpha = if (isInteractive) 0.72f else 0.35f),
                        NeonCrimson.copy(alpha = if (isInteractive) 0.72f else 0.35f)
                    )
                ),
                shape = canvasShape
            )
    ) {
        val density = LocalDensity.current
        val canvasWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val canvasHeightPx = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

        // 1. Base Visual Layer
        // Always render artwork preview underneath while LOADING or ERROR so there's never a bare black screen
        if (!shouldUseRemoteStream || playbackState != RemotePlaybackState.READY) {
            when (config.layout) {
                LayoutPreset.SPLIT -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(config.splitRatio.coerceIn(0.20f, 0.60f))
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        ) {
                            Image(
                                painter = painterResource(id = clip.facecamRes),
                                contentDescription = "Streamer facecam preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val s = 1.03f * effectiveKineticZoom
                                        scaleX = s
                                        scaleY = s
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(VoidBlack.copy(alpha = 0.65f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        // Glowing Split Seam Divider (Touch-Draggable without scrolling parent!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isInteractive) 32.dp else 2.5.dp)
                                .then(
                                    if (isInteractive) {
                                        Modifier
                                            .testTag("studio_split_divider")
                                            .touchDragNoScroll(
                                                key = canvasHeightPx,
                                                onDragStart = { latestOnDragStateChange(true) },
                                                onDragEnd = { latestOnDragStateChange(false) },
                                                onDragDelta = { _, dy ->
                                                    val deltaRatio = dy / canvasHeightPx
                                                    latestOnConfigChange { current ->
                                                        current.copy(
                                                            splitRatio = (current.splitRatio + deltaRatio)
                                                                .coerceIn(0.22f, 0.58f)
                                                        )
                                                    }
                                                }
                                            )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCrimson, ElectricCyan, NeonCrimson)
                                        )
                                    )
                            )
                            if (isInteractive) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(VoidBlack.copy(alpha = 0.90f))
                                        .border(1.dp, ElectricCyan, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DragIndicator,
                                        contentDescription = "Drag split divider",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    val topPct = (config.splitRatio * 100).roundToInt()
                                    Text(
                                        text = "$topPct/${100 - topPct}",
                                        color = ElectricCyan,
                                        fontFamily = MonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight((1f - config.splitRatio.coerceIn(0.20f, 0.60f)).coerceAtLeast(0.2f))
                                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        ) {
                            GameplayLayer(
                                gameplayRes = clip.gameplayRes,
                                zoom = config.gameplayZoom * effectiveKineticZoom,
                                panX = config.gameplayPanX,
                                panY = config.gameplayPanY
                            )
                        }
                    }
                }

                LayoutPreset.PIP -> {
                    GameplayLayer(
                        gameplayRes = clip.gameplayRes,
                        zoom = (config.gameplayZoom * 1.15f) * effectiveKineticZoom,
                        panX = config.gameplayPanX,
                        panY = config.gameplayPanY
                    )

                    DraggableFacecamOverlay(
                        facecamRes = clip.facecamRes,
                        config = config,
                        canvasWidthPx = canvasWidthPx,
                        canvasHeightPx = canvasHeightPx,
                        isInteractive = isInteractive,
                        onDragStateChange = { latestOnDragStateChange(it) },
                        onDragDelta = { dx, dy ->
                            latestOnConfigChange { current ->
                                val newX = (current.facecamX + dx / canvasWidthPx).coerceIn(0.16f, 0.84f)
                                val newY = (current.facecamY + dy / canvasHeightPx).coerceIn(0.10f, 0.85f)
                                current.copy(facecamX = newX, facecamY = newY)
                            }
                        },
                        onResizeDelta = { dx ->
                            latestOnConfigChange { current ->
                                val newScale = (current.facecamScale + dx / canvasWidthPx).coerceIn(0.22f, 0.60f)
                                current.copy(facecamScale = newScale)
                            }
                        }
                    )
                }

                LayoutPreset.BLUR -> {
                    Image(
                        painter = painterResource(id = clip.gameplayRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(18.dp)
                            .graphicsLayer {
                                scaleX = 1.35f
                                scaleY = 1.35f
                                alpha = 0.65f
                            }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VoidBlack.copy(alpha = 0.35f))
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.5.dp,
                                color = ElectricCyan.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        GameplayLayer(
                            gameplayRes = clip.gameplayRes,
                            zoom = config.gameplayZoom * effectiveKineticZoom,
                            panX = config.gameplayPanX,
                            panY = config.gameplayPanY
                        )
                    }

                    DraggableFacecamOverlay(
                        facecamRes = clip.facecamRes,
                        config = config,
                        canvasWidthPx = canvasWidthPx,
                        canvasHeightPx = canvasHeightPx,
                        isInteractive = isInteractive,
                        onDragStateChange = { latestOnDragStateChange(it) },
                        onDragDelta = { dx, dy ->
                            latestOnConfigChange { current ->
                                val newX = (current.facecamX + dx / canvasWidthPx).coerceIn(0.16f, 0.84f)
                                val newY = (current.facecamY + dy / canvasHeightPx).coerceIn(0.10f, 0.85f)
                                current.copy(facecamX = newX, facecamY = newY)
                            }
                        },
                        onResizeDelta = { dx ->
                            latestOnConfigChange { current ->
                                val newScale = (current.facecamScale + dx / canvasWidthPx).coerceIn(0.22f, 0.56f)
                                current.copy(facecamScale = newScale)
                            }
                        }
                    )
                }
            }
        }

        // Inline TextureView + MediaPlayer for real backend MP4 clips (no black SurfaceView holes, preloads metadata & first frame)
        if (shouldUseRemoteStream && playbackState != RemotePlaybackState.ERROR) {
            key(resolvedRemoteVideoUrl, retryNonce) {
                InlineRemoteMp4Player(
                    videoUrl = resolvedRemoteVideoUrl!!,
                    isPlaying = isPlaying,
                    onMetadataReady = {
                        playbackErrorDetail = null
                        playbackState = RemotePlaybackState.READY
                    },
                    onError = { reason ->
                        playbackErrorDetail = reason
                        playbackState = RemotePlaybackState.ERROR
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            if (playbackState == RemotePlaybackState.LOADING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack.copy(alpha = 0.42f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = "Loading clip stream…",
                            color = TextPrimary,
                            fontFamily = MonoFontFamily,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Clear Fallback Overlay with Retry & Open in Browser if stream loading fails
        if (shouldUseRemoteStream && playbackState == RemotePlaybackState.ERROR) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VoidBlack.copy(alpha = 0.82f))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = "Stream playback error",
                        tint = ErrorNeon,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Video Stream Unavailable",
                        color = TextPrimary,
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = playbackErrorDetail ?: "Could not load MP4 from backend server.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = resolvedRemoteVideoUrl.orEmpty(),
                        color = TextMuted,
                        fontFamily = MonoFontFamily,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                playbackErrorDetail = null
                                playbackState = RemotePlaybackState.LOADING
                                retryNonce++
                            },
                            modifier = Modifier.testTag("clip_retry_video_${clip.id}"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ElectricCyan),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ElectricCyan.copy(alpha = 0.14f),
                                contentColor = ElectricCyan
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                resolvedRemoteVideoUrl?.let { url ->
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("clip_open_browser_${clip.id}"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NeonCrimson),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = NeonCrimson.copy(alpha = 0.14f),
                                contentColor = TextPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browser", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bottom readability vignette
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.24f)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, VoidBlack.copy(alpha = 0.80f))
                    )
                )
        )

        // 2. Top-left Source Tag
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(VoidBlack.copy(alpha = 0.72f))
                    .border(1.dp, clip.sourcePlatform.accentColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (shouldUseRemoteStream && playbackState == RemotePlaybackState.READY) {
                        "MP4 • ${clip.streamerName}"
                    } else {
                        "${clip.sourcePlatform.shortCode} • ${clip.streamerName}"
                    },
                    color = TextPrimary,
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                )
            }
        }

        // Top-right Play / Pause Control Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .testTag("canvas_play_toggle_${clip.id}")
                .size(34.dp)
                .clip(CircleShape)
                .background(VoidBlack.copy(alpha = 0.72f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable(onClick = onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause preview" else "Play preview",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // 3. Draggable Animated Whisper Auto-Caption Layer (when interactive or demo preview)
        val showOverlayCaptions = isInteractive || !shouldUseRemoteStream
        if (showOverlayCaptions) {
            val captionOffsetYPx = ((config.captionY.coerceIn(0.12f, 0.88f) - 0.5f) * canvasHeightPx).roundToInt()
            var isDraggingCaption by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(0, captionOffsetYPx) }
                    .fillMaxWidth(0.92f)
                    .then(
                        if (isInteractive) {
                            Modifier
                                .testTag("studio_draggable_caption")
                                .touchDragNoScroll(
                                    key = canvasHeightPx,
                                    onDragStart = {
                                        isDraggingCaption = true
                                        latestOnDragStateChange(true)
                                    },
                                    onDragEnd = {
                                        isDraggingCaption = false
                                        latestOnDragStateChange(false)
                                    },
                                    onDragDelta = { _, dy ->
                                        val deltaY = dy / canvasHeightPx
                                        latestOnConfigChange { current ->
                                            current.copy(
                                                captionY = (current.captionY + deltaY).coerceIn(0.12f, 0.88f)
                                            )
                                        }
                                    }
                                )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedKaraokeCaption(
                    words = words,
                    activeWordIndex = if (isPlaying) activeWordIndex else -1,
                    style = config.captionStyle,
                    fontSizeSp = config.captionFontSize,
                    showDragGuide = isInteractive,
                    isDragging = isDraggingCaption
                )
            }
        }

        // 4. Bottom Audio Hype Equalizer & Timecode Bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Canvas(
                modifier = Modifier
                    .width(72.dp)
                    .height(16.dp)
            ) {
                val barCount = 12
                val barWidth = size.width / (barCount * 1.7f)
                val gap = barWidth * 0.7f
                for (i in 0 until barCount) {
                    val wave = if (isPlaying) {
                        0.30f + 0.70f * kotlin.math.abs(sin(wavePhase + i * 0.65f))
                    } else {
                        0.28f + 0.45f * ((i * 7) % 5) / 4f
                    }
                    val barHeight = size.height * wave
                    val barColor = if (i % 3 == 0) NeonCrimson else ElectricCyan
                    drawRoundRect(
                        color = barColor.copy(alpha = 0.9f),
                        topLeft = Offset(i * (barWidth + gap), size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isInteractive) {
                    Text(
                        text = "Y:${(latestConfig.captionY * 100).roundToInt()}%",
                        color = ElectricCyan,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp
                    )
                }
                val secStr = "0:${elapsedSec.toString().padStart(2, '0')}"
                Text(
                    text = "$secStr / ${clip.formattedDuration}",
                    color = TextPrimary.copy(alpha = 0.92f),
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * Inline TextureView + MediaPlayer that plays MP4 streams cleanly inside Compose scrolling lists
 * (analogous to HTML5 <video playsInline preload="metadata">). Preloads metadata and seeks to frame 1
 * so paused clips show a real video frame instead of a black screen.
 */
@Composable
private fun InlineRemoteMp4Player(
    videoUrl: String,
    isPlaying: Boolean,
    onMetadataReady: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestIsPlaying by rememberUpdatedState(isPlaying)
    val latestOnReady by rememberUpdatedState(onMetadataReady)
    val latestOnError by rememberUpdatedState(onError)

    val playerHolder = remember { MediaPlayerHolder() }

    DisposableEffect(videoUrl) {
        onDispose {
            playerHolder.release()
        }
    }

    LaunchedEffect(isPlaying, playerHolder.isPrepared) {
        playerHolder.syncPlayback(isPlaying)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureView(ctx).apply {
                isOpaque = false
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        playerHolder.prepare(
                            context = ctx,
                            textureView = this@apply,
                            surfaceTexture = surfaceTexture,
                            videoUrl = videoUrl,
                            autoStart = { latestIsPlaying },
                            onReady = { latestOnReady() },
                            onFailure = { msg -> latestOnError(msg) }
                        )
                    }

                    override fun onSurfaceTextureSizeChanged(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        playerHolder.applyCenterCropTransform(this@apply)
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                        playerHolder.release()
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                }
            }
        },
        update = {
            playerHolder.syncPlayback(isPlaying)
        }
    )
}

private class MediaPlayerHolder {
    private var mediaPlayer: MediaPlayer? = null
    private var surface: Surface? = null
    var isPrepared by mutableStateOf(false)
        private set
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0

    fun prepare(
        context: android.content.Context,
        textureView: TextureView,
        surfaceTexture: SurfaceTexture,
        videoUrl: String,
        autoStart: () -> Boolean,
        onReady: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        release()
        val newSurface = Surface(surfaceTexture)
        surface = newSurface
        val mp = MediaPlayer()
        mediaPlayer = mp
        try {
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            mp.setSurface(newSurface)
            mp.isLooping = true
            mp.setOnVideoSizeChangedListener { _, w, h ->
                videoWidth = w
                videoHeight = h
                applyCenterCropTransform(textureView)
            }
            mp.setOnPreparedListener { prepared ->
                isPrepared = true
                videoWidth = prepared.videoWidth
                videoHeight = prepared.videoHeight
                applyCenterCropTransform(textureView)
                // Render first metadata frame immediately so paused clip never stays black
                runCatching { prepared.seekTo(1) }
                if (autoStart()) {
                    runCatching { prepared.start() }
                }
                onReady()
            }
            mp.setOnErrorListener { _, what, extra ->
                isPrepared = false
                onFailure("Playback error ($what/$extra). Verify backend server & CORS/HTTP access.")
                true
            }
            mp.setDataSource(context, Uri.parse(videoUrl))
            mp.prepareAsync()
        } catch (e: Exception) {
            isPrepared = false
            onFailure(e.localizedMessage ?: "Unable to open remote video stream.")
        }
    }

    fun applyCenterCropTransform(view: TextureView) {
        if (videoWidth <= 0 || videoHeight <= 0 || view.width <= 0 || view.height <= 0) return
        val viewWidth = view.width.toFloat()
        val viewHeight = view.height.toFloat()
        val videoAspect = videoWidth.toFloat() / videoHeight.toFloat()
        val viewAspect = viewWidth / viewHeight
        var scaleX = 1f
        var scaleY = 1f
        if (videoAspect > viewAspect) {
            scaleX = videoAspect / viewAspect
        } else {
            scaleY = viewAspect / videoAspect
        }
        val matrix = Matrix().apply {
            setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
        }
        view.setTransform(matrix)
    }

    fun syncPlayback(shouldPlay: Boolean) {
        val mp = mediaPlayer ?: return
        if (!isPrepared) return
        runCatching {
            if (shouldPlay && !mp.isPlaying) {
                mp.start()
            } else if (!shouldPlay && mp.isPlaying) {
                mp.pause()
            }
        }
    }

    fun release() {
        isPrepared = false
        runCatching {
            mediaPlayer?.stop()
        }
        runCatching {
            mediaPlayer?.release()
        }
        mediaPlayer = null
        runCatching {
            surface?.release()
        }
        surface = null
    }
}

@Composable
private fun GameplayLayer(
    gameplayRes: Int,
    zoom: Float,
    panX: Float,
    panY: Float
) {
    Image(
        painter = painterResource(id = gameplayRes),
        contentDescription = "Gameplay preview",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val clampedZoom = zoom.coerceIn(1.0f, 2.4f)
                scaleX = clampedZoom
                scaleY = clampedZoom
                translationX = panX * size.width * 0.35f
                translationY = panY * size.height * 0.35f
            }
    )
}

@Composable
private fun DraggableFacecamOverlay(
    facecamRes: Int,
    config: ClipStudioConfig,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    isInteractive: Boolean,
    onDragStateChange: (Boolean) -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onResizeDelta: (Float) -> Unit
) {
    val density = LocalDensity.current
    val latestOnDragStateChange by rememberUpdatedState(onDragStateChange)
    val latestOnDragDelta by rememberUpdatedState(onDragDelta)
    val latestOnResizeDelta by rememberUpdatedState(onResizeDelta)

    val boxWidthPx = canvasWidthPx * config.facecamScale.coerceIn(0.22f, 0.62f)
    val boxHeightPx = if (config.facecamShape == FacecamShape.CIRCLE) {
        boxWidthPx
    } else {
        boxWidthPx * 0.78f
    }

    val boxWidthDp = with(density) { boxWidthPx.toDp() }
    val boxHeightDp = with(density) { boxHeightPx.toDp() }

    val leftPx = (config.facecamX * canvasWidthPx - boxWidthPx / 2f)
        .coerceIn(4f, (canvasWidthPx - boxWidthPx - 4f).coerceAtLeast(4f))
    val topPx = (config.facecamY * canvasHeightPx - boxHeightPx / 2f)
        .coerceIn(4f, (canvasHeightPx - boxHeightPx - 4f).coerceAtLeast(4f))

    val shape: Shape = if (config.facecamShape == FacecamShape.CIRCLE) {
        CircleShape
    } else {
        RoundedCornerShape(config.facecamCornerRadiusDp.dp)
    }

    val glowColor = config.facecamGlow.color
    val borderWidth = if (config.facecamGlow == GlowBorderOption.NONE) 0.dp else 2.5.dp
    var isDraggingFacecam by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { IntOffset(leftPx.roundToInt(), topPx.roundToInt()) }
            .size(width = boxWidthDp, height = boxHeightDp)
            .then(
                if (isInteractive) {
                    Modifier
                        .testTag("studio_draggable_facecam")
                        .touchDragNoScroll(
                            key = "facecam_drag",
                            onDragStart = {
                                isDraggingFacecam = true
                                latestOnDragStateChange(true)
                            },
                            onDragEnd = {
                                isDraggingFacecam = false
                                latestOnDragStateChange(false)
                            },
                            onDragDelta = { dx, dy ->
                                latestOnDragDelta(dx, dy)
                            }
                        )
                } else Modifier
            )
    ) {
        Image(
            painter = painterResource(id = facecamRes),
            contentDescription = "Floating facecam overlay",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .then(
                    if (isDraggingFacecam) {
                        Modifier.border(3.dp, ElectricCyan, shape)
                    } else if (borderWidth > 0.dp) {
                        Modifier.border(borderWidth, glowColor, shape)
                    } else Modifier
                )
        )

        if (isInteractive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(VoidBlack.copy(alpha = 0.78f))
                    .padding(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenWith,
                    contentDescription = "Drag facecam",
                    tint = ElectricCyan,
                    modifier = Modifier.size(11.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .testTag("studio_facecam_resize_handle")
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonCrimson)
                    .border(1.5.dp, TextPrimary, CircleShape)
                    .touchDragNoScroll(
                        key = "facecam_resize",
                        onDragStart = { latestOnDragStateChange(true) },
                        onDragEnd = { latestOnDragStateChange(false) },
                        onDragDelta = { dx, dy ->
                            latestOnResizeDelta(dx + dy * 0.5f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ZoomOutMap,
                    contentDescription = "Resize facecam",
                    tint = TextPrimary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedKaraokeCaption(
    words: List<String>,
    activeWordIndex: Int,
    style: CaptionStylePreset,
    fontSizeSp: Int,
    showDragGuide: Boolean,
    isDragging: Boolean
) {
    val displayWords = if (words.isEmpty()) listOf("WHISPER AUTO-CAPTIONS") else words
    val chunkSize = 5
    val chunkStart = if (activeWordIndex >= 0) (activeWordIndex / chunkSize) * chunkSize else 0
    val visibleWords = displayWords.drop(chunkStart).take(chunkSize)

    val containerShape = RoundedCornerShape(10.dp)
    val guideBorder = when {
        isDragging -> ElectricCyan
        showDragGuide -> Color.White.copy(alpha = 0.42f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .clip(containerShape)
            .background(style.badgeBg)
            .border(
                width = if (showDragGuide) (if (isDragging) 1.5.dp else 1.dp) else 0.dp,
                color = guideBorder,
                shape = containerShape
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buildAnnotatedString {
                visibleWords.forEachIndexed { localIdx, word ->
                    val globalIdx = chunkStart + localIdx
                    val isCurrent = globalIdx == activeWordIndex
                    withStyle(
                        SpanStyle(
                            color = if (isCurrent) style.highlightColor else style.textColor,
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCurrent) (fontSizeSp + 2).sp else fontSizeSp.sp
                        )
                    ) {
                        append(word.uppercase())
                    }
                    if (localIdx < visibleWords.lastIndex) {
                        append(" ")
                    }
                }
            },
            textAlign = TextAlign.Center,
            lineHeight = (fontSizeSp + 6).sp,
            letterSpacing = 0.5.sp
        )
    }
}
