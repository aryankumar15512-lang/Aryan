package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ViralClip
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.VerticalClipCanvas
import com.example.ui.components.ViralScoreBadge
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ErrorNeon
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.SmokedGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ViralClipUiState

@Composable
fun ClipsScreen(
    state: ViralClipUiState,
    onDownloadClip: (ViralClip) -> Unit,
    onEditClipInStudio: (String) -> Unit,
    onDeleteClip: (String) -> Unit,
    onGoHomeToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedClips = state.clips
    var playingClipId by remember { mutableStateOf(displayedClips.firstOrNull()?.id) }
    var clipPendingDelete by remember { mutableStateOf<ViralClip?>(null) }

    // Quick clean delete confirmation dialog
    clipPendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { clipPendingDelete = null },
            containerColor = SmokedGlass,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = {
                Text(
                    text = "Delete Clip?",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Remove \"${pending.title}\" from your Clips feed?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (playingClipId == pending.id) playingClipId = null
                        onDeleteClip(pending.id)
                        clipPendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_clip_button")
                ) {
                    Text(
                        text = "Delete",
                        color = ErrorNeon,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { clipPendingDelete = null },
                    modifier = Modifier.testTag("cancel_delete_clip_button")
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary
                    )
                }
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .testTag("clips_feed_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Clean Uncluttered Header (no extra layout filter chips)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.clips_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${displayedClips.size} AI-detected vertical clips ready for Shorts & Reels",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    if (displayedClips.isNotEmpty()) {
                        val avgScore = displayedClips.map { it.viralScore }.average().toInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidBlack.copy(alpha = 0.72f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "AVG $avgScore",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }

            if (displayedClips.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MovieFilter,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(42.dp)
                            )
                            Text(
                                text = "No clips yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Paste a live stream link, VOD URL, or upload a video from the Home tab to auto-find viral clips.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = onGoHomeToScan,
                                border = BorderStroke(1.dp, ElectricCyan)
                            ) {
                                Text("Go to Input Hub", color = ElectricCyan)
                            }
                        }
                    }
                }
            } else {
                items(displayedClips, key = { it.id }) { clip ->
                    ViralClipFeedCard(
                        clip = clip,
                        isPlaying = playingClipId == clip.id,
                        isDownloading = state.downloadingClipId == clip.id,
                        activeServerUrl = state.settings.activeServerUrl,
                        useRemoteVideoPlayback = !state.settings.demoMode,
                        onTogglePlay = {
                            playingClipId = if (playingClipId == clip.id) null else clip.id
                        },
                        onDownload = { onDownloadClip(clip) },
                        onEdit = { onEditClipInStudio(clip.id) },
                        onRequestDelete = { clipPendingDelete = clip }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun ViralClipFeedCard(
    clip: ViralClip,
    isPlaying: Boolean,
    isDownloading: Boolean,
    activeServerUrl: String,
    useRemoteVideoPlayback: Boolean,
    onTogglePlay: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit,
    onRequestDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("clip_card_${clip.id}"),
        accentColor = if (clip.viralScore >= 95) NeonCrimson else ElectricCyan.copy(alpha = 0.65f),
        contentPadding = PaddingValues(14.dp)
    ) {
        // Top Metadata Row: Viral Score + Duration + Delete (clean, no layout preset badges)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ViralScoreBadge(score = clip.viralScore)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VoidBlack.copy(alpha = 0.72f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = clip.formattedDuration,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = TextPrimary
                    )
                }

                // Delete Clip Button
                Box(
                    modifier = Modifier
                        .testTag("clip_delete_button_${clip.id}")
                        .minimumInteractiveComponentSize()
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorNeon.copy(alpha = 0.12f))
                        .border(1.dp, ErrorNeon.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onRequestDelete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Delete clip",
                        tint = ErrorNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Centered 9:16 Vertical Video Preview Player
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            VerticalClipCanvas(
                clip = clip,
                config = clip.studioConfig,
                isInteractive = false,
                isPlaying = isPlaying,
                onTogglePlay = onTogglePlay,
                activeServerUrl = activeServerUrl,
                useRemoteVideoPlayback = useRemoteVideoPlayback,
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .fillMaxWidth(0.76f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Catchy Auto-Generated Title & Hype Trigger Metadata (no layout tags)
        Text(
            text = clip.title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = clip.hypePeakLabel,
                style = MaterialTheme.typography.bodySmall,
                color = ElectricCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = clip.createdLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Row: Download (Primary Neon) & Edit (Opens Studio)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonGradientButton(
                text = if (isDownloading) "Downloading…" else stringResource(R.string.action_download),
                onClick = onDownload,
                icon = Icons.Filled.Download,
                isLoading = isDownloading,
                modifier = Modifier
                    .weight(1.25f)
                    .height(48.dp),
                testTag = "clip_download_button_${clip.id}"
            )

            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("clip_edit_button_${clip.id}"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.78f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElectricCyan.copy(alpha = 0.10f),
                    contentColor = ElectricCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_edit),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
