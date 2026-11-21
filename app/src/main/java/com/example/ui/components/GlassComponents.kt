package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PersistedSettings
import com.example.model.AppTab
import com.example.model.BackendHealthState
import com.example.model.ConnectionStatus
import com.example.model.EngineMode
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.ErrorNeon
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SmokedGlass
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.ui.theme.VoltYellow

@Composable
fun ViralClipAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCrimson.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.06f),
                        radius = size.width * 0.78f
                    ),
                    radius = size.width * 0.78f,
                    center = Offset(size.width * 0.12f, size.height * 0.06f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.11f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.92f, size.height * 0.42f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.92f, size.height * 0.42f)
                )
            }
    ) {
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color? = null,
    cornerRadius: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (accentColor != null) {
        Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = 0.65f),
                GlassBorder,
                accentColor.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SmokedGlass.copy(alpha = 0.90f),
                        CarbonSurface.copy(alpha = 0.92f)
                    )
                )
            )
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun NeonGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Filled.AutoAwesome,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    gradientColors: List<Color> = listOf(NeonCrimson, NeonPinkGlow),
    testTag: String = "neon_cta_button"
) {
    val shape = RoundedCornerShape(16.dp)
    val activeBrush = if (enabled) {
        Brush.horizontalGradient(gradientColors)
    } else {
        Brush.horizontalGradient(listOf(SmokedGlass, SmokedGlass))
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .minimumInteractiveComponentSize()
            .height(54.dp)
            .clip(shape)
            .background(activeBrush)
            .border(
                width = 1.dp,
                color = if (enabled) Color.White.copy(alpha = 0.28f) else GlassBorder,
                shape = shape
            )
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = TextPrimary,
                    strokeWidth = 2.2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = DisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = if (enabled) TextPrimary else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ViralClipTopBar(
    settings: PersistedSettings,
    healthState: BackendHealthState,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "status_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val (badgeLabel, badgeColor) = when {
        settings.demoMode -> "Demo Mode" to AmberWarn
        healthState.status == ConnectionStatus.CONNECTED -> {
            val modeText = if (healthState.reportedMode.equals("cloud", ignoreCase = true) ||
                settings.engineMode == EngineMode.CLOUD_COLAB
            ) {
                "Cloud Mode"
            } else {
                "PC Mode"
            }
            val gpuSuffix = if (healthState.gpuAvailable) " • GPU" else ""
            "$modeText$gpuSuffix" to EmeraldLive
        }
        healthState.status == ConnectionStatus.CHECKING -> "Pinging…" to ElectricCyan
        healthState.status == ConnectionStatus.ERROR -> {
            val modeLabel = if (settings.engineMode == EngineMode.PC_LOCAL) "PC Offline" else "Cloud Offline"
            modeLabel to ErrorNeon
        }
        else -> {
            val modeLabel = if (settings.engineMode == EngineMode.PC_LOCAL) "PC Mode" else "Cloud Mode"
            modeLabel to ElectricCyan
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = VoidBlack.copy(alpha = 0.86f)
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Logo Brand Mark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_viralclip_logo_1795265828800),
                        contentDescription = "ViralClip X logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(listOf(NeonCrimson, ElectricCyan)),
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = TextPrimary,
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("ViralClip ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = NeonCrimson,
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("X")
                            }
                        },
                        fontSize = 21.sp,
                        letterSpacing = 0.4.sp
                    )
                }

                // Live Backend Status Badge Pill
                Row(
                    modifier = Modifier
                        .testTag("backend_status_badge")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(999.dp))
                        .background(badgeColor.copy(alpha = 0.14f))
                        .border(
                            width = 1.dp,
                            color = badgeColor.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .clickable(onClick = onStatusClick)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = pulseAlpha))
                    )
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = badgeColor,
                        maxLines = 1
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                NeonCrimson.copy(alpha = 0.40f),
                                ElectricCyan.copy(alpha = 0.40f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun ViralScoreBadge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val accent = when {
        score >= 95 -> NeonCrimson
        score >= 90 -> ElectricCyan
        else -> VoltYellow
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.72f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "$score/100 Viral",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = accent
        )
    }
}

private fun tabIcons(tab: AppTab): Pair<ImageVector, ImageVector> = when (tab) {
    AppTab.HOME -> Icons.Filled.Home to Icons.Outlined.Home
    AppTab.CLIPS -> Icons.Filled.Movie to Icons.Outlined.Movie
    AppTab.STUDIO -> Icons.Filled.Tune to Icons.Outlined.Tune
    AppTab.SETTINGS -> Icons.Filled.Settings to Icons.Outlined.Settings
}

@Composable
fun ViralClipBottomBar(
    activeTab: AppTab,
    clipCount: Int,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = VoidBlack.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(68.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val selected = tab == activeTab
                val (filledIcon, outlinedIcon) = tabIcons(tab)
                val activeColor = if (tab == AppTab.STUDIO) ElectricCyan else NeonCrimson
                val contentColor by animateColorAsState(
                    targetValue = if (selected) activeColor else TextMuted,
                    label = "tab_color"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tab_${tab.route}")
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selected) activeColor.copy(alpha = 0.16f) else Color.Transparent
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (tab == AppTab.CLIPS && clipCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = NeonCrimson,
                                        contentColor = TextPrimary
                                    ) {
                                        Text(
                                            text = clipCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selected) filledIcon else outlinedIcon,
                                    contentDescription = tab.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (selected) filledIcon else outlinedIcon,
                                contentDescription = tab.label,
                                tint = contentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = contentColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ViralClipNavigationRail(
    activeTab: AppTab,
    clipCount: Int,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.navigationBarsPadding(),
        containerColor = VoidBlack.copy(alpha = 0.94f),
        contentColor = TextPrimary
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTab.entries.forEach { tab ->
            val selected = tab == activeTab
            val (filledIcon, outlinedIcon) = tabIcons(tab)
            val activeColor = if (tab == AppTab.STUDIO) ElectricCyan else NeonCrimson
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(tab) },
                modifier = Modifier.testTag("rail_tab_${tab.route}"),
                icon = {
                    if (tab == AppTab.CLIPS && clipCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = NeonCrimson,
                                    contentColor = TextPrimary
                                ) {
                                    Text(clipCount.toString(), fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) filledIcon else outlinedIcon,
                                contentDescription = tab.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) filledIcon else outlinedIcon,
                            contentDescription = tab.label
                        )
                    }
                },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = DisplayFontFamily)
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = activeColor,
                    selectedTextColor = activeColor,
                    indicatorColor = activeColor.copy(alpha = 0.18f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
