package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.InputMode
import com.example.model.ProcessingJobState
import com.example.model.ProcessingStep
import com.example.model.StreamPlatform
import com.example.model.UploadedVideoInfo
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.ErrorNeon
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.SmokedGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ViralClipUiState

@Composable
fun HomeScreen(
    state: ViralClipUiState,
    onSelectInputMode: (InputMode) -> Unit,
    onUrlChange: (String) -> Unit,
    onPlatformQuickFill: (StreamPlatform) -> Unit,
    onVideoPicked: (android.net.Uri?) -> Unit,
    onClearUpload: () -> Unit,
    onStartScan: () -> Unit,
    onCancelScan: () -> Unit,
    onOpenClipsFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Zero-permission Android Photo Picker for MP4/MOV videos (Google Play compliant)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        onVideoPicked(uri)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Broadcast Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCrimson.copy(alpha = 0.16f))
                            .border(1.dp, NeonCrimson.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "3-IN-1 UNIVERSAL HUB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = MonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonCrimson
                        )
                    }
                    Text(
                        text = if (state.settings.demoMode) "INSTANT DEMO READY" else "GPU ENGINE ARMED",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = ElectricCyan
                    )
                }
                Text(
                    text = "Drop Any Stream. Get Viral Shorts.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "AI automatically detects hype spikes, Whisper captions & facecam framing for YouTube Shorts and Reels.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // 3-in-1 Universal Input Card
            GlassCard(
                accentColor = if (state.inputMode == InputMode.UPLOAD_VIDEO) ElectricCyan else NeonCrimson,
                contentPadding = PaddingValues(16.dp)
            ) {
                // Mode Selector Segmented Tabs
                InputModeSegmentedBar(
                    selectedMode = state.inputMode,
                    onSelectMode = onSelectInputMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = state.inputMode,
                    label = "input_mode_content"
                ) { mode ->
                    when (mode) {
                        InputMode.LIVE_STREAM, InputMode.VOD_URL -> {
                            UrlInputHubSection(
                                mode = mode,
                                urlInput = state.urlInput,
                                detectedPlatform = state.detectedPlatform,
                                onUrlChange = onUrlChange,
                                onPlatformQuickFill = onPlatformQuickFill
                            )
                        }

                        InputMode.UPLOAD_VIDEO -> {
                            UploadVideoHubSection(
                                uploadedInfo = state.selectedUpload,
                                onPickVideo = {
                                    videoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                    )
                                },
                                onClearUpload = onClearUpload
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Big Neon CTA Button
                NeonGradientButton(
                    text = if (state.processingState.isRunning) {
                        stringResource(R.string.cta_scanning_clips)
                    } else {
                        stringResource(R.string.cta_auto_find_clips)
                    },
                    onClick = onStartScan,
                    isLoading = state.processingState.isRunning,
                    icon = Icons.Filled.AutoAwesome,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "auto_find_viral_clips_button"
                )
            }

            // Live 5-Step Processing Tracker
            AnimatedVisibility(
                visible = state.processingState.isRunning ||
                    state.processingState.completedClipCount != null ||
                    state.processingState.errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                LiveProcessingTrackerCard(
                    processingState = state.processingState,
                    onCancel = onCancelScan,
                    onOpenClips = onOpenClipsFeed
                )
            }

            // Quick Ready Feed Preview Banner when idle
            if (!state.processingState.isRunning && state.clips.isNotEmpty()) {
                GlassCard(
                    accentColor = ElectricCyan.copy(alpha = 0.7f),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${state.clips.size} Viral Clips Ready",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Top score ${state.clips.maxOf { it.viralScore }}/100 • Ready for Shorts & Reels",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(
                            modifier = Modifier
                                .testTag("home_view_clips_shortcut")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricCyan.copy(alpha = 0.16f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenClipsFeed)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Open Clips",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricCyan
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open clips feed",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun InputModeSegmentedBar(
    selectedMode: InputMode,
    onSelectMode: (InputMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(VoidBlack.copy(alpha = 0.75f))
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        InputMode.entries.forEach { mode ->
            val selected = mode == selectedMode
            val icon = when (mode) {
                InputMode.LIVE_STREAM -> Icons.Filled.LiveTv
                InputMode.VOD_URL -> Icons.Filled.PlayCircleFilled
                InputMode.UPLOAD_VIDEO -> Icons.Filled.CloudUpload
            }
            val activeAccent = if (mode == InputMode.UPLOAD_VIDEO) ElectricCyan else NeonCrimson

            Row(
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_mode_${mode.name.lowercase()}")
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) activeAccent.copy(alpha = 0.22f) else Color.Transparent
                    )
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = if (selected) activeAccent.copy(alpha = 0.7f) else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectMode(mode) }
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) activeAccent else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    ),
                    color = if (selected) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UrlInputHubSection(
    mode: InputMode,
    urlInput: String,
    detectedPlatform: StreamPlatform?,
    onUrlChange: (String) -> Unit,
    onPlatformQuickFill: (StreamPlatform) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stream_url_input"),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    text = if (mode == InputMode.LIVE_STREAM) {
                        stringResource(R.string.url_hint_live)
                    } else {
                        stringResource(R.string.url_hint_vod)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    tint = detectedPlatform?.accentColor ?: ElectricCyan
                )
            },
            trailingIcon = {
                if (urlInput.isNotEmpty()) {
                    IconButton(onClick = { onUrlChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear URL",
                            tint = TextSecondary
                        )
                    }
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = MonoFontFamily,
                color = TextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VoidBlack.copy(alpha = 0.72f),
                unfocusedContainerColor = VoidBlack.copy(alpha = 0.56f),
                focusedBorderColor = detectedPlatform?.accentColor ?: NeonCrimson,
                unfocusedBorderColor = GlassBorder,
                cursorColor = ElectricCyan
            )
        )

        // Platform Icon Badges Row (YouTube, Twitch, Kick, Facebook, Rumble, X)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "SUPPORTED PLATFORMS (TAP TO AUTO-FILL SAMPLE)",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextMuted
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StreamPlatform.entries.forEach { platform ->
                    val isMatched = detectedPlatform == platform
                    Row(
                        modifier = Modifier
                            .testTag("platform_badge_${platform.shortCode.lowercase()}")
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isMatched) platform.accentColor.copy(alpha = 0.24f)
                                else VoidBlack.copy(alpha = 0.65f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isMatched) platform.accentColor else platform.accentColor.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPlatformQuickFill(platform) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(platform.accentColor)
                        )
                        Text(
                            text = platform.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isMatched) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Zero manual timestamps note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricCyan.copy(alpha = 0.08f))
                .border(1.dp, ElectricCyan.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.zero_timestamps_note),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun UploadVideoHubSection(
    uploadedInfo: UploadedVideoInfo?,
    onPickVideo: () -> Unit,
    onClearUpload: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_video_dropzone")
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            ElectricCyan.copy(alpha = 0.10f),
                            VoidBlack.copy(alpha = 0.75f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    color = if (uploadedInfo != null) EmeraldLive.copy(alpha = 0.7f) else ElectricCyan.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onPickVideo)
                .padding(horizontal = 18.dp, vertical = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uploadedInfo == null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.16f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = "Select video from gallery",
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.upload_drop_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = DisplayFontFamily),
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.upload_drop_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldLive.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VideoFile,
                                contentDescription = null,
                                tint = EmeraldLive,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = uploadedInfo.fileName,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${uploadedInfo.sizeMb} • Ready for AI Hype Scan",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLive
                            )
                        }
                    }
                    IconButton(
                        onClick = onClearUpload,
                        modifier = Modifier.testTag("clear_uploaded_video_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove selected video",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveProcessingTrackerCard(
    processingState: ProcessingJobState,
    onCancel: () -> Unit,
    onOpenClips: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (processingState.progress / 100f).coerceIn(0f, 1f),
        animationSpec = tween(250),
        label = "pipeline_progress"
    )
    val isDone = !processingState.isRunning && processingState.completedClipCount != null
    val hasError = processingState.errorMessage != null

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_processing_tracker"),
        accentColor = when {
            hasError -> ErrorNeon
            isDone -> EmeraldLive
            else -> ElectricCyan
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        hasError -> "Pipeline Interrupted"
                        isDone -> "Viral Clips Ready!"
                        else -> "AI Pipeline Running"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = when {
                        hasError -> ErrorNeon
                        isDone -> EmeraldLive
                        else -> TextPrimary
                    }
                )
                if (processingState.jobId != null) {
                    Text(
                        text = "JOB #${processingState.jobId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
            Text(
                text = "${processingState.progress}%",
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = MonoFontFamily),
                color = if (isDone) EmeraldLive else ElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = if (isDone) EmeraldLive else NeonCrimson,
            trackColor = VoidBlack,
            strokeCap = StrokeCap.Round
        )

        if (hasError) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = ErrorNeon,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = processingState.errorMessage ?: "Unknown error",
                    style = MaterialTheme.typography.bodySmall,
                    color = ErrorNeon
                )
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProcessingStep.entries.forEach { step ->
                    val completed = isDone || step.index < processingState.currentStepIndex
                    val active = processingState.isRunning && step.index == processingState.currentStepIndex

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when {
                            completed -> Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Completed",
                                tint = EmeraldLive,
                                modifier = Modifier.size(19.dp)
                            )
                            active -> CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                            else -> Icon(
                                imageVector = Icons.Filled.RadioButtonUnchecked,
                                contentDescription = "Pending",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${step.index + 1}. ${step.title}",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (active || completed) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = when {
                                    active -> ElectricCyan
                                    completed -> TextPrimary
                                    else -> TextMuted
                                }
                            )
                            if (active) {
                                Text(
                                    text = step.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (processingState.isRunning) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel_scan_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel Processing",
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else if (isDone) {
            NeonGradientButton(
                text = "View ${processingState.completedClipCount} New Viral Clips",
                onClick = onOpenClips,
                icon = Icons.AutoMirrored.Filled.ArrowForward,
                gradientColors = listOf(ElectricCyan, NeonPinkGlow),
                modifier = Modifier.fillMaxWidth(),
                testTag = "tracker_open_clips_button"
            )
        }
    }
}
