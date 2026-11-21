package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CaptionStylePreset
import com.example.model.ClipStudioConfig
import com.example.model.FacecamShape
import com.example.model.GlowBorderOption
import com.example.model.LayoutPreset
import com.example.model.ViralClip
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.VerticalClipCanvas
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ViralClipUiState
import kotlin.math.roundToInt

private enum class StudioInspectorTab(val label: String, val icon: ImageVector) {
    LAYOUT("Layout", Icons.Filled.Layers),
    FACECAM("Facecam", Icons.Filled.Face),
    CAPTIONS("Captions", Icons.Filled.Subtitles)
}

@Composable
fun StudioScreen(
    state: ViralClipUiState,
    onSelectClip: (String) -> Unit,
    onUpdateConfig: ((ClipStudioConfig) -> ClipStudioConfig) -> Unit,
    onResetFacecam: () -> Unit,
    onTogglePayloadPreview: () -> Unit,
    rerenderJsonPreview: String,
    onRerenderAndSave: () -> Unit,
    onDownloadStudioClip: () -> Unit,
    onGoHomeToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.clips.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.widthIn(max = 460.dp),
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MovieCreation,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No Clip Selected for Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "Generate or upload a clip on the Home tab first, then customize layouts, facecam & captions here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = onGoHomeToScan,
                        border = BorderStroke(1.dp, ElectricCyan)
                    ) {
                        Text("Go to Home Hub", color = ElectricCyan)
                    }
                }
            }
        }
        return
    }

    val activeClip = state.activeStudioClip
    val config = state.studioDraftConfig
    var inspectorTab by remember { mutableStateOf(StudioInspectorTab.LAYOUT) }
    var isPreviewPlaying by remember { mutableStateOf(true) }
    var isCanvasDragging by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Main Content Area (Responsive: Side-by-side on Tablets, Stacked on Mobile)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val isWide = maxWidth >= 680.dp

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Pane: Interactive 9:16 Canvas
                    Column(
                        modifier = Modifier
                            .weight(0.45f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioClipSelectorStrip(
                            clips = state.clips,
                            activeClipId = activeClip.id,
                            onSelectClip = onSelectClip
                        )
                        VerticalClipCanvas(
                            clip = activeClip,
                            config = config,
                            isInteractive = true,
                            isPlaying = isPreviewPlaying,
                            onTogglePlay = { isPreviewPlaying = !isPreviewPlaying },
                            activeServerUrl = state.settings.activeServerUrl,
                            useRemoteVideoPlayback = !state.settings.demoMode,
                            onDragStateChange = { isCanvasDragging = it },
                            onConfigChange = onUpdateConfig,
                            modifier = Modifier
                                .weight(1f)
                                .widthIn(max = 310.dp)
                        )
                    }

                    // Right Pane: Scrollable Controls Inspector
                    Column(
                        modifier = Modifier
                            .weight(0.55f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StudioInspectorSection(
                            config = config,
                            inspectorTab = inspectorTab,
                            onSelectTab = { inspectorTab = it },
                            onUpdateConfig = onUpdateConfig,
                            onResetFacecam = onResetFacecam,
                            showPayloadPreview = state.showPayloadPreview,
                            onTogglePayloadPreview = onTogglePayloadPreview,
                            rerenderJsonPreview = rerenderJsonPreview
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(
                            state = rememberScrollState(),
                            enabled = !isCanvasDragging
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StudioClipSelectorStrip(
                        clips = state.clips,
                        activeClipId = activeClip.id,
                        onSelectClip = onSelectClip
                    )

                    // Interactive 9:16 Canvas (parent scroll automatically locked while dragging!)
                    VerticalClipCanvas(
                        clip = activeClip,
                        config = config,
                        isInteractive = true,
                        isPlaying = isPreviewPlaying,
                        onTogglePlay = { isPreviewPlaying = !isPreviewPlaying },
                        activeServerUrl = state.settings.activeServerUrl,
                        useRemoteVideoPlayback = !state.settings.demoMode,
                        onDragStateChange = { isCanvasDragging = it },
                        onConfigChange = onUpdateConfig,
                        modifier = Modifier
                            .widthIn(max = 228.dp)
                            .fillMaxWidth(0.64f)
                    )

                    Text(
                        text = when (config.layout) {
                            LayoutPreset.SPLIT -> "Tip: Drag the neon split bar or captions directly on canvas"
                            LayoutPreset.PIP, LayoutPreset.BLUR -> "Tip: Drag facecam, pull red corner to resize, or drag captions"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan.copy(alpha = 0.85f)
                    )

                    StudioInspectorSection(
                        config = config,
                        inspectorTab = inspectorTab,
                        onSelectTab = { inspectorTab = it },
                        onUpdateConfig = onUpdateConfig,
                        onResetFacecam = onResetFacecam,
                        showPayloadPreview = state.showPayloadPreview,
                        onTogglePayloadPreview = onTogglePayloadPreview,
                        rerenderJsonPreview = rerenderJsonPreview
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }

        // Sticky Bottom Studio Action Bar: Re-Render & Save + Download
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VoidBlack.copy(alpha = 0.92f))
                .border(width = 1.dp, color = GlassBorder)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeonGradientButton(
                text = if (state.isRerendering) "Rendering…" else stringResource(R.string.action_rerender_save),
                onClick = onRerenderAndSave,
                icon = Icons.Filled.MovieCreation,
                isLoading = state.isRerendering,
                modifier = Modifier
                    .weight(1.35f)
                    .height(48.dp),
                testTag = "studio_rerender_save_button"
            )

            OutlinedButton(
                onClick = onDownloadStudioClip,
                modifier = Modifier
                    .weight(0.95f)
                    .height(48.dp)
                    .testTag("studio_download_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, ElectricCyan.copy(alpha = 0.8f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElectricCyan.copy(alpha = 0.10f),
                    contentColor = ElectricCyan
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_download),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun StudioClipSelectorStrip(
    clips: List<ViralClip>,
    activeClipId: String,
    onSelectClip: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        clips.forEach { clip ->
            val selected = clip.id == activeClipId
            Row(
                modifier = Modifier
                    .testTag("studio_clip_pill_${clip.id}")
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) NeonCrimson.copy(alpha = 0.22f) else VoidBlack.copy(alpha = 0.65f)
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) NeonCrimson else GlassBorder,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${clip.viralScore}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (selected) NeonCrimson else ElectricCyan
                )
                Text(
                    text = clip.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 150.dp)
                )
            }
        }
    }
}

@Composable
private fun StudioInspectorSection(
    config: ClipStudioConfig,
    inspectorTab: StudioInspectorTab,
    onSelectTab: (StudioInspectorTab) -> Unit,
    onUpdateConfig: ((ClipStudioConfig) -> ClipStudioConfig) -> Unit,
    onResetFacecam: () -> Unit,
    showPayloadPreview: Boolean,
    onTogglePayloadPreview: () -> Unit,
    rerenderJsonPreview: String
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = ElectricCyan.copy(alpha = 0.65f),
        contentPadding = PaddingValues(14.dp)
    ) {
        // Inspector Sub-tabs: Layout / Facecam / Captions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(VoidBlack.copy(alpha = 0.72f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StudioInspectorTab.entries.forEach { tab ->
                val selected = tab == inspectorTab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("studio_tab_${tab.name.lowercase()}")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) ElectricCyan.copy(alpha = 0.20f) else Color.Transparent)
                        .border(
                            width = if (selected) 1.dp else 0.dp,
                            color = if (selected) ElectricCyan.copy(alpha = 0.7f) else Color.Transparent,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (selected) ElectricCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = DisplayFontFamily),
                        color = if (selected) TextPrimary else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (inspectorTab) {
            StudioInspectorTab.LAYOUT -> {
                // Layout Preset Selector
                Text(
                    text = "LAYOUT PRESET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LayoutPreset.entries.forEach { preset ->
                        val selected = config.layout == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("layout_preset_${preset.apiValue}")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) NeonCrimson.copy(alpha = 0.22f)
                                    else VoidBlack.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) NeonCrimson else GlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onUpdateConfig { it.copy(layout = preset) } }
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (selected) TextPrimary else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Split Screen Ratio Slider (when in Split Screen)
                if (config.layout == LayoutPreset.SPLIT) {
                    val topPct = (config.splitRatio * 100).roundToInt()
                    StudioLabeledSlider(
                        label = "Split Ratio (Facecam / Gameplay)",
                        valueText = "$topPct% / ${100 - topPct}%",
                        value = config.splitRatio,
                        valueRange = 0.22f..0.58f,
                        onValueChange = { v -> onUpdateConfig { it.copy(splitRatio = v) } },
                        accent = NeonCrimson
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Gameplay Zoom & Pan Sliders
                StudioLabeledSlider(
                    label = "Gameplay Zoom",
                    valueText = "${String.format("%.2f", config.gameplayZoom)}x",
                    value = config.gameplayZoom,
                    valueRange = 1.0f..2.2f,
                    onValueChange = { v -> onUpdateConfig { it.copy(gameplayZoom = v) } },
                    accent = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                StudioLabeledSlider(
                    label = "Gameplay Pan X",
                    valueText = "${(config.gameplayPanX * 100).roundToInt()}%",
                    value = config.gameplayPanX,
                    valueRange = -0.5f..0.5f,
                    onValueChange = { v -> onUpdateConfig { it.copy(gameplayPanX = v) } },
                    accent = ElectricCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                StudioLabeledSlider(
                    label = "Gameplay Pan Y",
                    valueText = "${(config.gameplayPanY * 100).roundToInt()}%",
                    value = config.gameplayPanY,
                    valueRange = -0.5f..0.5f,
                    onValueChange = { v -> onUpdateConfig { it.copy(gameplayPanY = v) } },
                    accent = ElectricCyan
                )
            }

            StudioInspectorTab.FACECAM -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "FACECAM SHAPE & OVERLAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Row(
                        modifier = Modifier
                            .testTag("studio_reset_facecam_button")
                            .clip(RoundedCornerShape(8.dp))
                            .background(VoidBlack.copy(alpha = 0.7f))
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .clickable(onClick = onResetFacecam)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset facecam position",
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Square vs Circle Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FacecamShape.entries.forEach { shape ->
                        val selected = config.facecamShape == shape
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("facecam_shape_${shape.apiValue}")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) ElectricCyan.copy(alpha = 0.20f) else VoidBlack.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) ElectricCyan else GlassBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onUpdateConfig { current ->
                                        // Automatically switch to Floating PIP if currently Split so user sees shape immediately
                                        val nextLayout = if (current.layout == LayoutPreset.SPLIT) LayoutPreset.PIP else current.layout
                                        current.copy(facecamShape = shape, layout = nextLayout)
                                    }
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = shape.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = DisplayFontFamily),
                                color = if (selected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live Coordinates Readout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(VoidBlack.copy(alpha = 0.65f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "POS X: ${(config.facecamX * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = ElectricCyan
                    )
                    Text(
                        text = "POS Y: ${(config.facecamY * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = ElectricCyan
                    )
                    Text(
                        text = "SCALE: ${(config.facecamScale * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                        color = NeonCrimson
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                StudioLabeledSlider(
                    label = "Facecam Scale",
                    valueText = "${(config.facecamScale * 100).roundToInt()}%",
                    value = config.facecamScale,
                    valueRange = 0.22f..0.60f,
                    onValueChange = { v -> onUpdateConfig { it.copy(facecamScale = v) } },
                    accent = NeonCrimson
                )

                if (config.facecamShape == FacecamShape.RECT) {
                    Spacer(modifier = Modifier.height(6.dp))
                    StudioLabeledSlider(
                        label = "Corner Radius",
                        valueText = "${config.facecamCornerRadiusDp.roundToInt()}dp",
                        value = config.facecamCornerRadiusDp,
                        valueRange = 0f..32f,
                        onValueChange = { v -> onUpdateConfig { it.copy(facecamCornerRadiusDp = v) } },
                        accent = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "BORDER GLOW COLOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GlowBorderOption.entries.forEach { option ->
                        val selected = config.facecamGlow == option
                        Box(
                            modifier = Modifier
                                .testTag("glow_color_${option.name.lowercase()}")
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (option == GlowBorderOption.NONE) VoidBlack else option.color
                                )
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) TextPrimary else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable { onUpdateConfig { it.copy(facecamGlow = option) } },
                            contentAlignment = Alignment.Center
                        ) {
                            if (option == GlowBorderOption.NONE) {
                                Text(
                                    text = "×",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            StudioInspectorTab.CAPTIONS -> {
                // Quick Caption Y Position Presets (Top / Middle / Bottom)
                Text(
                    text = "CAPTION POSITION (OR DRAG ON CANVAS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val positions = listOf("Top" to 0.22f, "Middle" to 0.50f, "Bottom" to 0.74f)
                    positions.forEach { (label, yVal) ->
                        val selected = kotlin.math.abs(config.captionY - yVal) < 0.08f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("caption_pos_${label.lowercase()}")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) ElectricCyan.copy(alpha = 0.22f) else VoidBlack.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) ElectricCyan else GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onUpdateConfig { it.copy(captionY = yVal) } }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Style Presets: Bold Yellow Pop, Neon Cyan, Classic White
                Text(
                    text = "CAPTION STYLE PRESET",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionStylePreset.entries.forEach { style ->
                        val selected = config.captionStyle == style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("caption_style_${style.apiValue}")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (selected) style.textColor.copy(alpha = 0.18f) else VoidBlack.copy(alpha = 0.65f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) style.textColor else GlassBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onUpdateConfig { it.copy(captionStyle = style) } }
                                .padding(horizontal = 6.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = style.textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Font Size Slider
                StudioLabeledSlider(
                    label = "Caption Font Size",
                    valueText = "${config.captionFontSize}sp",
                    value = config.captionFontSize.toFloat(),
                    valueRange = 14f..32f,
                    onValueChange = { v -> onUpdateConfig { it.copy(captionFontSize = v.roundToInt()) } },
                    accent = NeonPinkGlow
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Inline Whisper Caption Text Editor
                Text(
                    text = "FIX WHISPER TRANSCRIPT INLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = config.captionText,
                    onValueChange = { text -> onUpdateConfig { it.copy(captionText = text) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_caption_inline_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = DisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VoidBlack.copy(alpha = 0.78f),
                        unfocusedContainerColor = VoidBlack.copy(alpha = 0.6f),
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = ElectricCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Collapsible Live FastAPI JSON Coordinates Preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("studio_toggle_json_payload")
                .clip(RoundedCornerShape(10.dp))
                .background(VoidBlack.copy(alpha = 0.65f))
                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onTogglePayloadPreview)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Code,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "POST /api/rerender Payload JSON",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                    color = TextSecondary
                )
            }
            Text(
                text = if (showPayloadPreview) "HIDE" else "INSPECT",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                color = ElectricCyan
            )
        }

        AnimatedVisibility(visible = showPayloadPreview) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VoidBlack)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = rerenderJsonPreview,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = MonoFontFamily,
                        lineHeight = 16.sp
                    ),
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun StudioLabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    accent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = accent
            )
        }
        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = VoidBlack
            )
        )
    }
}
