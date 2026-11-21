package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppTab
import com.example.ui.components.ViralClipAtmosphere
import com.example.ui.components.ViralClipBottomBar
import com.example.ui.components.ViralClipNavigationRail
import com.example.ui.components.ViralClipTopBar
import com.example.ui.screens.ClipsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SmokedGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.ViralClipXTheme
import com.example.viewmodel.ViralClipViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            ViralClipXTheme {
                ViralClipXApp()
            }
        }
    }
}

@Composable
fun ViralClipXApp(
    viewModel: ViralClipViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.consumeSnackbar()
        }
    }

    // Intercept system back on secondary tabs to return cleanly to Home
    BackHandler(enabled = state.activeTab != AppTab.HOME) {
        viewModel.selectTab(AppTab.HOME)
    }

    ViralClipAtmosphere {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 720.dp

            Scaffold(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    ViralClipTopBar(
                        settings = state.settings,
                        healthState = state.healthState,
                        onStatusClick = { viewModel.selectTab(AppTab.SETTINGS) }
                    )
                },
                bottomBar = {
                    if (!useNavRail) {
                        ViralClipBottomBar(
                            activeTab = state.activeTab,
                            clipCount = state.clips.size,
                            onSelectTab = viewModel::selectTab
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = SmokedGlass,
                            contentColor = TextPrimary,
                            actionColor = ElectricCyan,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavRail) {
                        ViralClipNavigationRail(
                            activeTab = state.activeTab,
                            clipCount = state.clips.size,
                            onSelectTab = viewModel::selectTab
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Crossfade(
                            targetState = state.activeTab,
                            label = "viralclip_tab_transition"
                        ) { tab ->
                            when (tab) {
                                AppTab.HOME -> HomeScreen(
                                    state = state,
                                    onSelectInputMode = viewModel::selectInputMode,
                                    onUrlChange = viewModel::updateUrlInput,
                                    onPlatformQuickFill = viewModel::fillSampleUrlForPlatform,
                                    onVideoPicked = viewModel::onVideoPicked,
                                    onClearUpload = viewModel::clearSelectedUpload,
                                    onStartScan = viewModel::startAutoFindViralClips,
                                    onCancelScan = viewModel::cancelProcessing,
                                    onOpenClipsFeed = { viewModel.selectTab(AppTab.CLIPS) }
                                )

                                AppTab.CLIPS -> ClipsScreen(
                                    state = state,
                                    onDownloadClip = viewModel::downloadClip,
                                    onEditClipInStudio = viewModel::openClipInStudio,
                                    onDeleteClip = viewModel::deleteClip,
                                    onGoHomeToScan = { viewModel.selectTab(AppTab.HOME) }
                                )

                                AppTab.STUDIO -> StudioScreen(
                                    state = state,
                                    onSelectClip = viewModel::openClipInStudio,
                                    onUpdateConfig = viewModel::updateStudioConfig,
                                    onResetFacecam = viewModel::resetFacecamPosition,
                                    onTogglePayloadPreview = viewModel::togglePayloadPreview,
                                    rerenderJsonPreview = viewModel.getCurrentRerenderJson(),
                                    onRerenderAndSave = viewModel::rerenderAndSaveStudioClip,
                                    onDownloadStudioClip = viewModel::downloadActiveStudioClip,
                                    onGoHomeToScan = { viewModel.selectTab(AppTab.HOME) }
                                )

                                AppTab.SETTINGS -> SettingsScreen(
                                    state = state,
                                    onToggleDemoMode = viewModel::setDemoMode,
                                    onSelectEngineMode = viewModel::setEngineMode,
                                    onUpdatePcUrl = viewModel::updatePcServerUrl,
                                    onUpdateCloudUrl = viewModel::updateCloudServerUrl,
                                    onTestConnection = viewModel::testBackendConnection
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
