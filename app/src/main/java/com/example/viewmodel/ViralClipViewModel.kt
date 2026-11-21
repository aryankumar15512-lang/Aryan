package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DemoClipCatalog
import com.example.data.PersistedSettings
import com.example.data.SettingsDataStore
import com.example.data.ViralClipRepository
import com.example.model.AppTab
import com.example.model.BackendHealthState
import com.example.model.ClipFilter
import com.example.model.ClipStudioConfig
import com.example.model.ConnectionStatus
import com.example.model.EngineMode
import com.example.model.InputMode
import com.example.model.ProcessingJobState
import com.example.model.ProcessingStep
import com.example.model.StreamPlatform
import com.example.model.UploadedVideoInfo
import com.example.model.ViralClip
import com.example.network.MediaFileHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ViralClipUiState(
    val activeTab: AppTab = AppTab.HOME,
    val inputMode: InputMode = InputMode.LIVE_STREAM,
    val urlInput: String = "",
    val detectedPlatform: StreamPlatform? = null,
    val selectedUpload: UploadedVideoInfo? = null,
    val processingState: ProcessingJobState = ProcessingJobState(),
    val clips: List<ViralClip> = DemoClipCatalog.initialDemoClips,
    val clipFilter: ClipFilter = ClipFilter.TOP_VIRAL,
    val selectedStudioClipId: String = DemoClipCatalog.initialDemoClips.first().id,
    val studioDraftConfig: ClipStudioConfig = DemoClipCatalog.initialDemoClips.first().studioConfig,
    val isRerendering: Boolean = false,
    val showPayloadPreview: Boolean = false,
    val downloadingClipId: String? = null,
    val settings: PersistedSettings = PersistedSettings(),
    val healthState: BackendHealthState = BackendHealthState(),
    val snackbarMessage: String? = null
) {
    val activeStudioClip: ViralClip
        get() = clips.firstOrNull { it.id == selectedStudioClipId }
            ?: clips.firstOrNull()
            ?: DemoClipCatalog.initialDemoClips.first()

    val filteredClips: List<ViralClip>
        get() = when (clipFilter) {
            ClipFilter.TOP_VIRAL -> clips.sortedByDescending { it.viralScore }
            ClipFilter.ALL -> clips
            ClipFilter.EDITED -> clips.filter { it.isEdited || it.isRerendered }
        }
}

class ViralClipViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsStore = SettingsDataStore(application.applicationContext)
    private val repository = ViralClipRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(ViralClipUiState())
    val uiState: StateFlow<ViralClipUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            settingsStore.settingsFlow.collect { persisted ->
                _uiState.update { current ->
                    val updatedHealth = if (persisted.demoMode) {
                        BackendHealthState(
                            status = ConnectionStatus.DEMO,
                            message = "Demo Mode Active • Simulated AI Engine"
                        )
                    } else if (current.healthState.status == ConnectionStatus.DEMO) {
                        BackendHealthState(
                            status = ConnectionStatus.IDLE,
                            message = "Tap Test Connection to verify FastAPI server"
                        )
                    } else {
                        current.healthState
                    }
                    current.copy(settings = persisted, healthState = updatedHealth)
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectInputMode(mode: InputMode) {
        _uiState.update { it.copy(inputMode = mode) }
    }

    fun updateUrlInput(url: String) {
        _uiState.update {
            it.copy(
                urlInput = url,
                detectedPlatform = StreamPlatform.detectFromUrl(url)
            )
        }
    }

    fun fillSampleUrlForPlatform(platform: StreamPlatform) {
        val currentMode = _uiState.value.inputMode
        val sample = if (currentMode == InputMode.LIVE_STREAM) {
            platform.sampleLiveUrl
        } else {
            platform.sampleVodUrl
        }
        _uiState.update {
            it.copy(
                urlInput = sample,
                detectedPlatform = platform,
                snackbarMessage = "Loaded sample ${platform.displayName} link"
            )
        }
    }

    fun onVideoPicked(uri: Uri?) {
        if (uri == null) return
        val info = MediaFileHelper.queryVideoInfo(getApplication(), uri)
        _uiState.update {
            it.copy(
                selectedUpload = info,
                inputMode = InputMode.UPLOAD_VIDEO,
                snackbarMessage = "Selected ${info.fileName} (${info.sizeMb})"
            )
        }
    }

    fun clearSelectedUpload() {
        _uiState.update { it.copy(selectedUpload = null) }
    }

    fun startAutoFindViralClips() {
        val state = _uiState.value
        if (state.processingState.isRunning) return

        val mode = state.inputMode
        if (mode != InputMode.UPLOAD_VIDEO && state.urlInput.isBlank()) {
            if (state.settings.demoMode) {
                // In Demo Mode, auto-fill a realistic Twitch live URL so users can test in 1 tap
                val defaultUrl = if (mode == InputMode.LIVE_STREAM) {
                    StreamPlatform.TWITCH.sampleLiveUrl
                } else {
                    StreamPlatform.YOUTUBE.sampleVodUrl
                }
                _uiState.update {
                    it.copy(
                        urlInput = defaultUrl,
                        detectedPlatform = StreamPlatform.detectFromUrl(defaultUrl)
                    )
                }
            } else {
                _uiState.update {
                    it.copy(snackbarMessage = "Paste a YouTube, Twitch, Kick, Facebook, Rumble, or X link first")
                }
                return
            }
        }

        if (mode == InputMode.UPLOAD_VIDEO && state.selectedUpload == null) {
            if (!state.settings.demoMode) {
                _uiState.update {
                    it.copy(snackbarMessage = "Select an MP4 or MOV video from your gallery first")
                }
                return
            }
        }

        scanJob?.cancel()
        if (_uiState.value.settings.demoMode) {
            runDemoScanPipeline()
        } else {
            runLiveBackendPipeline()
        }
    }

    fun cancelProcessing() {
        scanJob?.cancel()
        _uiState.update {
            it.copy(
                processingState = ProcessingJobState(),
                snackbarMessage = "Clipping job cancelled"
            )
        }
    }

    private fun runDemoScanPipeline() {
        scanJob = viewModelScope.launch {
            val state = _uiState.value
            val platform = state.detectedPlatform ?: StreamPlatform.TWITCH
            val sourceLabel = when (state.inputMode) {
                InputMode.UPLOAD_VIDEO -> state.selectedUpload?.fileName?.substringBeforeLast('.') ?: "Gallery Upload"
                InputMode.LIVE_STREAM -> "${platform.displayName} Live"
                InputMode.VOD_URL -> "${platform.displayName} VOD"
            }

            _uiState.update {
                it.copy(
                    processingState = ProcessingJobState(
                        isRunning = true,
                        jobId = "demo_${System.currentTimeMillis() % 9999}",
                        currentStepIndex = 0,
                        currentStepLabel = ProcessingStep.CAPTURING.title,
                        progress = 4
                    )
                )
            }

            val stepTargets = listOf(18, 42, 66, 86, 100)
            for (step in ProcessingStep.entries) {
                val startPct = if (step.index == 0) 4 else stepTargets[step.index - 1]
                val endPct = stepTargets[step.index]
                val ticks = 5
                for (t in 1..ticks) {
                    delay(210L)
                    val pct = startPct + ((endPct - startPct) * t) / ticks
                    _uiState.update {
                        it.copy(
                            processingState = it.processingState.copy(
                                isRunning = true,
                                currentStepIndex = step.index,
                                currentStepLabel = step.title,
                                progress = pct
                            )
                        )
                    }
                }
            }

            val newBatch = DemoClipCatalog.generateDemoScanBatch(
                sourcePlatform = platform,
                sourceLabel = sourceLabel,
                localVideoUri = state.selectedUpload?.uriString
            )

            _uiState.update { current ->
                val merged = (newBatch + current.clips).take(12)
                current.copy(
                    clips = merged,
                    processingState = ProcessingJobState(
                        isRunning = false,
                        currentStepIndex = 4,
                        currentStepLabel = "Completed",
                        progress = 100,
                        completedClipCount = newBatch.size
                    ),
                    snackbarMessage = "Found ${newBatch.size} new viral gaming clips!"
                )
            }
        }
    }

    private fun runLiveBackendPipeline() {
        scanJob = viewModelScope.launch {
            val state = _uiState.value
            val baseUrl = state.settings.activeServerUrl
            val platform = state.detectedPlatform ?: StreamPlatform.YOUTUBE

            _uiState.update {
                it.copy(
                    processingState = ProcessingJobState(
                        isRunning = true,
                        currentStepIndex = 0,
                        currentStepLabel = ProcessingStep.CAPTURING.title,
                        progress = 5
                    )
                )
            }

            val jobIdResult = if (state.inputMode == InputMode.UPLOAD_VIDEO) {
                val upload = state.selectedUpload
                if (upload == null) {
                    Result.failure(IllegalStateException("No video selected"))
                } else {
                    repository.startUploadJob(baseUrl, Uri.parse(upload.uriString), upload.fileName)
                }
            } else {
                repository.startUrlJob(
                    baseUrl = baseUrl,
                    url = state.urlInput,
                    isLive = state.inputMode == InputMode.LIVE_STREAM
                )
            }

            val jobId = jobIdResult.getOrElse { err ->
                _uiState.update {
                    it.copy(
                        processingState = ProcessingJobState(
                            isRunning = false,
                            errorMessage = err.localizedMessage ?: "Failed to submit job to backend"
                        ),
                        snackbarMessage = "Backend error: ${err.localizedMessage ?: "Connection failed"}"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(processingState = it.processingState.copy(jobId = jobId, progress = 10))
            }

            // Poll GET /api/status/{job_id}
            while (true) {
                delay(1200L)
                val poll = repository.pollJobStatus(baseUrl, jobId)
                val status = poll.getOrElse { err ->
                    _uiState.update {
                        it.copy(
                            processingState = ProcessingJobState(
                                isRunning = false,
                                errorMessage = err.localizedMessage ?: "Lost connection while polling"
                            ),
                            snackbarMessage = "Status check failed: ${err.localizedMessage}"
                        )
                    }
                    return@launch
                }

                val stepIdx = mapStepIndex(status.step, status.progress)
                if (status.status.equals("done", ignoreCase = true)) {
                    val mapped = repository.mapApiClipsToDomain(
                        dtos = status.clips.orEmpty(),
                        fallbackPlatform = platform,
                        activeServerUrl = baseUrl
                    )
                    _uiState.update { current ->
                        val updatedClips = if (mapped.isNotEmpty()) mapped + current.clips else current.clips
                        val firstClip = updatedClips.firstOrNull()
                        current.copy(
                            clips = updatedClips,
                            selectedStudioClipId = firstClip?.id ?: current.selectedStudioClipId,
                            studioDraftConfig = firstClip?.studioConfig ?: current.studioDraftConfig,
                            processingState = ProcessingJobState(
                                isRunning = false,
                                jobId = jobId,
                                currentStepIndex = 4,
                                currentStepLabel = "Completed",
                                progress = 100,
                                completedClipCount = mapped.size
                            ),
                            snackbarMessage = "Server rendered ${mapped.size} viral clips!"
                        )
                    }
                    return@launch
                } else if (status.status.equals("error", ignoreCase = true)) {
                    _uiState.update {
                        it.copy(
                            processingState = ProcessingJobState(
                                isRunning = false,
                                jobId = jobId,
                                errorMessage = status.error ?: "Backend failed during processing"
                            )
                        )
                    }
                    return@launch
                } else {
                    _uiState.update {
                        it.copy(
                            processingState = ProcessingJobState(
                                isRunning = true,
                                jobId = jobId,
                                currentStepIndex = stepIdx,
                                currentStepLabel = status.step.ifBlank { ProcessingStep.entries[stepIdx].title },
                                progress = status.progress.coerceIn(0, 99)
                            )
                        )
                    }
                }
            }
        }
    }

    private fun mapStepIndex(stepText: String, progress: Int): Int {
        val lower = stepText.lowercase()
        return when {
            lower.contains("captur") || lower.contains("download") -> 0
            lower.contains("hype") || lower.contains("audio") || lower.contains("spike") -> 1
            lower.contains("whisper") || lower.contains("caption") -> 2
            lower.contains("fram") || lower.contains("face") -> 3
            lower.contains("render") || lower.contains("encod") -> 4
            progress < 20 -> 0
            progress < 45 -> 1
            progress < 70 -> 2
            progress < 88 -> 3
            else -> 4
        }
    }

    fun setClipFilter(filter: ClipFilter) {
        _uiState.update { it.copy(clipFilter = filter) }
    }

    fun deleteClip(clipId: String) {
        val target = _uiState.value.clips.firstOrNull { it.id == clipId } ?: return
        _uiState.update { current ->
            val remaining = current.clips.filterNot { it.id == clipId }
            val nextSelected = if (current.selectedStudioClipId == clipId) {
                remaining.firstOrNull()
            } else {
                remaining.firstOrNull { it.id == current.selectedStudioClipId } ?: remaining.firstOrNull()
            }
            current.copy(
                clips = remaining,
                selectedStudioClipId = nextSelected?.id ?: "",
                studioDraftConfig = nextSelected?.studioConfig ?: current.studioDraftConfig,
                snackbarMessage = "Deleted '${target.title}'"
            )
        }
    }

    fun openClipInStudio(clipId: String) {
        val clip = _uiState.value.clips.firstOrNull { it.id == clipId } ?: return
        _uiState.update {
            it.copy(
                selectedStudioClipId = clip.id,
                studioDraftConfig = clip.studioConfig,
                activeTab = AppTab.STUDIO
            )
        }
    }

    fun updateStudioConfig(transform: (ClipStudioConfig) -> ClipStudioConfig) {
        _uiState.update { current ->
            current.copy(studioDraftConfig = transform(current.studioDraftConfig))
        }
    }

    fun resetFacecamPosition() {
        updateStudioConfig {
            it.copy(
                facecamX = 0.68f,
                facecamY = 0.16f,
                facecamScale = 0.34f,
                gameplayZoom = 1.15f,
                gameplayPanX = 0f,
                gameplayPanY = 0f
            )
        }
    }

    fun togglePayloadPreview() {
        _uiState.update { it.copy(showPayloadPreview = !it.showPayloadPreview) }
    }

    fun getCurrentRerenderJson(): String {
        val state = _uiState.value
        return repository.formatRerenderPayloadJson(state.activeStudioClip.id, state.studioDraftConfig)
    }

    fun rerenderAndSaveStudioClip() {
        val state = _uiState.value
        if (state.isRerendering) return

        val clip = state.activeStudioClip
        val config = state.studioDraftConfig

        viewModelScope.launch {
            _uiState.update { it.copy(isRerendering = true) }

            if (state.settings.demoMode) {
                delay(900L)
                _uiState.update { current ->
                    val updatedList = current.clips.map { item ->
                        if (item.id == clip.id) {
                            item.copy(
                                studioConfig = config,
                                isEdited = true,
                                isRerendered = true
                            )
                        } else item
                    }
                    current.copy(
                        clips = updatedList,
                        isRerendering = false,
                        snackbarMessage = "Re-rendered & saved '${clip.title}' (Demo GPU)"
                    )
                }
            } else {
                val result = repository.rerenderClip(
                    baseUrl = state.settings.activeServerUrl,
                    clipId = clip.id,
                    config = config
                )
                result.fold(
                    onSuccess = { videoUrl ->
                        _uiState.update { current ->
                            val updatedList = current.clips.map { item ->
                                if (item.id == clip.id) {
                                    item.copy(
                                        studioConfig = config,
                                        videoUrl = videoUrl,
                                        isEdited = true,
                                        isRerendered = true
                                    )
                                } else item
                            }
                            current.copy(
                                clips = updatedList,
                                isRerendering = false,
                                snackbarMessage = "Backend re-rendered clip successfully!"
                            )
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(
                                isRerendering = false,
                                snackbarMessage = "Re-render failed: ${err.localizedMessage ?: "Server unreachable"}"
                            )
                        }
                    }
                )
            }
        }
    }

    fun downloadClip(clip: ViralClip) {
        if (_uiState.value.downloadingClipId != null) return
        val activeServerUrl = _uiState.value.settings.activeServerUrl
        viewModelScope.launch {
            _uiState.update { it.copy(downloadingClipId = clip.id) }
            val result = repository.downloadClip(clip, activeServerUrl)
            _uiState.update { current ->
                current.copy(
                    downloadingClipId = null,
                    snackbarMessage = result.fold(
                        onSuccess = { it },
                        onFailure = { "Download error: ${it.localizedMessage}" }
                    )
                )
            }
        }
    }

    fun downloadActiveStudioClip() {
        val state = _uiState.value
        val updatedClip = state.activeStudioClip.copy(studioConfig = state.studioDraftConfig)
        downloadClip(updatedClip)
    }

    fun setDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setDemoMode(enabled)
            if (enabled) {
                _uiState.update {
                    it.copy(
                        healthState = BackendHealthState(
                            status = ConnectionStatus.DEMO,
                            message = "Demo Mode Active • Simulated AI Engine"
                        ),
                        snackbarMessage = "Demo Mode enabled — instant clips & Studio ready"
                    )
                }
            } else {
                // Ensure active URLs have trailing slashes stripped immediately when Demo Mode is OFF
                val currentSettings = _uiState.value.settings
                val cleanPc = PersistedSettings.stripTrailingSlashes(currentSettings.pcServerUrl)
                val cleanCloud = PersistedSettings.stripTrailingSlashes(currentSettings.cloudServerUrl)
                settingsStore.setPcServerUrl(cleanPc)
                settingsStore.setCloudServerUrl(cleanCloud)
                testBackendConnection()
            }
        }
    }

    fun setEngineMode(mode: EngineMode) {
        viewModelScope.launch {
            settingsStore.setEngineMode(mode)
        }
    }

    fun updatePcServerUrl(url: String) {
        viewModelScope.launch {
            val cleaned = if (!_uiState.value.settings.demoMode) {
                PersistedSettings.stripTrailingSlashes(url)
            } else {
                url
            }
            settingsStore.setPcServerUrl(cleaned)
        }
    }

    fun updateCloudServerUrl(url: String) {
        viewModelScope.launch {
            val cleaned = if (!_uiState.value.settings.demoMode) {
                PersistedSettings.stripTrailingSlashes(url)
            } else {
                url
            }
            settingsStore.setCloudServerUrl(cleaned)
        }
    }

    fun testBackendConnection() {
        val state = _uiState.value
        val activeUrl = PersistedSettings.stripTrailingSlashes(state.settings.activeServerUrl)
        viewModelScope.launch {
            // Persist sanitized server URL without trailing slash
            if (state.settings.engineMode == EngineMode.PC_LOCAL) {
                settingsStore.setPcServerUrl(activeUrl)
            } else {
                settingsStore.setCloudServerUrl(activeUrl)
            }
            _uiState.update {
                it.copy(
                    healthState = BackendHealthState(
                        status = ConnectionStatus.CHECKING,
                        message = "Pinging $activeUrl/api/health…"
                    )
                )
            }
            val health = repository.checkHealth(activeUrl)
            if (health.status == ConnectionStatus.CONNECTED && state.settings.demoMode) {
                settingsStore.setDemoMode(false)
            }
            _uiState.update {
                it.copy(
                    healthState = health,
                    snackbarMessage = health.message
                )
            }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
