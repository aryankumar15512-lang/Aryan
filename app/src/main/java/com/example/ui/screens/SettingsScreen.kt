package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ConnectionStatus
import com.example.model.EngineMode
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonGradientButton
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.DisplayFontFamily
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.ErrorNeon
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonPinkGlow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ViralClipUiState

@Composable
fun SettingsScreen(
    state: ViralClipUiState,
    onToggleDemoMode: (Boolean) -> Unit,
    onSelectEngineMode: (EngineMode) -> Unit,
    onUpdatePcUrl: (String) -> Unit,
    onUpdateCloudUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = state.settings
    val health = state.healthState

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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Switch seamlessly between your local RTX PC, a Google Colab GPU tunnel, or instant Demo Mode.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // 1. Demo Mode Toggle Card (ON by default when no backend is connected)
            GlassCard(
                accentColor = if (settings.demoMode) AmberWarn else GlassBorder,
                contentPadding = PaddingValues(16.dp)
            ) {
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
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberWarn.copy(alpha = 0.16f))
                                .border(1.dp, AmberWarn.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Science,
                                contentDescription = null,
                                tint = AmberWarn,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.settings_demo_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = DisplayFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(R.string.settings_demo_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = settings.demoMode,
                        onCheckedChange = onToggleDemoMode,
                        modifier = Modifier.testTag("settings_demo_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = VoidBlack,
                            checkedTrackColor = AmberWarn,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = VoidBlack
                        )
                    )
                }
            }

            // 2. PC + Cloud Hybrid Engine Switcher
            GlassCard(
                accentColor = ElectricCyan,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "HYBRID FASTAPI ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EngineMode.entries.forEach { mode ->
                        val selected = settings.engineMode == mode
                        val accent = if (mode == EngineMode.PC_LOCAL) NeonCrimson else ElectricCyan
                        val icon = if (mode == EngineMode.PC_LOCAL) Icons.Filled.Computer else Icons.Filled.Cloud

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("engine_mode_${mode.apiValue}")
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected) accent.copy(alpha = 0.16f) else VoidBlack.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = if (selected) 1.5.dp else 1.dp,
                                    color = if (selected) accent else GlassBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectEngineMode(mode) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accent.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = DisplayFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = mode.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (selected) accent else TextMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(accent)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Active Server URL Input Box
                val isPcMode = settings.engineMode == EngineMode.PC_LOCAL
                Text(
                    text = if (isPcMode) "LOCAL PC FASTAPI URL (WI-FI LAN)" else "COLAB / CLOUDFLARE TUNNEL URL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = if (isPcMode) settings.pcServerUrl else settings.cloudServerUrl,
                    onValueChange = { url ->
                        if (isPcMode) onUpdatePcUrl(url) else onUpdateCloudUrl(url)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_server_url_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = if (isPcMode) "http://192.168.1.5:8000" else "https://xxxx.trycloudflare.com",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = MonoFontFamily),
                            color = TextMuted
                        )
                    },
                    supportingText = if (!settings.demoMode) {
                        {
                            Text(
                                text = "Active endpoint origin: ${settings.activeServerUrl}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily),
                                color = ElectricCyan.copy(alpha = 0.85f)
                            )
                        }
                    } else null,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = MonoFontFamily,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = VoidBlack.copy(alpha = 0.78f),
                        unfocusedContainerColor = VoidBlack.copy(alpha = 0.58f),
                        focusedBorderColor = if (isPcMode) NeonCrimson else ElectricCyan,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = ElectricCyan
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Test Connection CTA Button
                NeonGradientButton(
                    text = if (health.status == ConnectionStatus.CHECKING) {
                        "Testing /api/health…"
                    } else {
                        stringResource(R.string.settings_test_connection)
                    },
                    onClick = onTestConnection,
                    icon = Icons.Filled.NetworkCheck,
                    isLoading = health.status == ConnectionStatus.CHECKING,
                    gradientColors = listOf(ElectricCyan, NeonPinkGlow),
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_test_connection_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Health Status Telemetry Strip
                val statusColor = when (health.status) {
                    ConnectionStatus.CONNECTED -> EmeraldLive
                    ConnectionStatus.ERROR -> ErrorNeon
                    ConnectionStatus.CHECKING -> ElectricCyan
                    ConnectionStatus.DEMO -> AmberWarn
                    ConnectionStatus.IDLE -> TextSecondary
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_health_status")
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = when (health.status) {
                            ConnectionStatus.CONNECTED -> Icons.Filled.CheckCircle
                            ConnectionStatus.ERROR -> Icons.Filled.ErrorOutline
                            else -> Icons.Filled.Memory
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = health.message,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusColor
                        )
                        if (health.latencyMs != null) {
                            Text(
                                text = "Endpoint: GET ${settings.activeServerUrl.trimEnd('/')}/api/health",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // 3. FastAPI Integration Contract Summary Card
            GlassCard(
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Api,
                        contentDescription = null,
                        tint = NeonCrimson,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_api_contract),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val endpoints = listOf(
                    "GET" to "/api/health" to "Health, mode & GPU telemetry",
                    "POST" to "/api/process-url" to "Auto-scan live stream or VOD URL",
                    "POST" to "/api/process-upload" to "Multipart gallery MP4/MOV upload",
                    "GET" to "/api/status/{job_id}" to "Live 5-step progress & clip results",
                    "POST" to "/api/rerender" to "Exact Studio layout & caption coords"
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    endpoints.forEach { (methodPath, desc) ->
                        val (method, path) = methodPath
                        val methodColor = if (method == "GET") ElectricCyan else NeonCrimson
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(VoidBlack.copy(alpha = 0.6f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = methodColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(methodColor.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = path,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = MonoFontFamily,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
