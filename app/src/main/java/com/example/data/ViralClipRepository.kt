package com.example.data

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import com.example.R
import com.example.model.BackendHealthState
import com.example.model.ClipStudioConfig
import com.example.model.ConnectionStatus
import com.example.model.StreamPlatform
import com.example.model.ViralClip
import com.example.network.ApiClipDto
import com.example.network.BackendApiClient
import com.example.network.CaptionPayload
import com.example.network.FacecamPayload
import com.example.network.JobStatusResponse
import com.example.network.MediaFileHelper
import com.example.network.ProcessUrlRequest
import com.example.network.RerenderRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ViralClipRepository(
    private val context: Context,
    private val apiClient: BackendApiClient = BackendApiClient()
) {
    suspend fun checkHealth(baseUrl: String): BackendHealthState = withContext(Dispatchers.IO) {
        val start = SystemClock.elapsedRealtime()
        runCatching {
            val service = apiClient.getService(baseUrl)
            val response = service.getHealth()
            val latency = SystemClock.elapsedRealtime() - start
            val ok = response.status.equals("ok", ignoreCase = true)
            BackendHealthState(
                status = if (ok) ConnectionStatus.CONNECTED else ConnectionStatus.ERROR,
                reportedMode = response.mode,
                gpuAvailable = response.gpu,
                latencyMs = latency,
                message = if (ok) {
                    val gpuTag = if (response.gpu) "GPU Ready" else "CPU Fallback"
                    "Connected (${response.mode.uppercase()} • $gpuTag • ${latency}ms)"
                } else {
                    "Unexpected status: ${response.status}"
                }
            )
        }.getOrElse { err ->
            BackendHealthState(
                status = ConnectionStatus.ERROR,
                gpuAvailable = false,
                message = err.localizedMessage?.take(90) ?: "Cannot reach FastAPI backend"
            )
        }
    }

    suspend fun startUrlJob(baseUrl: String, url: String, isLive: Boolean): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val service = apiClient.getService(baseUrl)
                service.processUrl(ProcessUrlRequest(url = url.trim(), isLive = isLive)).jobId
            }
        }

    suspend fun startUploadJob(baseUrl: String, uri: Uri, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val service = apiClient.getService(baseUrl)
                val part = MediaFileHelper.buildMultipartFromUri(context, uri, fileName)
                service.processUpload(part).jobId
            }
        }

    suspend fun pollJobStatus(baseUrl: String, jobId: String): Result<JobStatusResponse> =
        withContext(Dispatchers.IO) {
            runCatching {
                val service = apiClient.getService(baseUrl)
                service.getJobStatus(jobId)
            }
        }

    suspend fun rerenderClip(
        baseUrl: String,
        clipId: String,
        config: ClipStudioConfig
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val service = apiClient.getService(baseUrl)
            val request = RerenderRequest(
                clipId = clipId,
                layout = config.layout.apiValue,
                facecam = FacecamPayload(
                    x = config.facecamX,
                    y = config.facecamY,
                    scale = config.facecamScale,
                    shape = config.facecamShape.apiValue
                ),
                gameplayZoom = config.gameplayZoom,
                caption = CaptionPayload(
                    y = config.captionY,
                    style = config.captionStyle.apiValue,
                    size = config.captionFontSize,
                    text = config.captionText
                )
            )
            val rawVideoUrl = service.rerenderClip(request).videoUrl
            BackendApiClient.resolveMediaUrl(baseUrl, rawVideoUrl) ?: rawVideoUrl
        }
    }

    fun formatRerenderPayloadJson(clipId: String, config: ClipStudioConfig): String =
        apiClient.formatRerenderJson(clipId, config)

    suspend fun downloadClip(clip: ViralClip, activeServerUrl: String): Result<String> {
        val json = formatRerenderPayloadJson(clip.id, clip.studioConfig)
        return MediaFileHelper.downloadClip(
            context = context,
            okHttpClient = apiClient.okHttpClient,
            clip = clip,
            activeServerUrl = activeServerUrl,
            rerenderJson = json
        )
    }

    fun mapApiClipsToDomain(
        dtos: List<ApiClipDto>,
        fallbackPlatform: StreamPlatform,
        activeServerUrl: String
    ): List<ViralClip> {
        return dtos.mapIndexed { idx, dto ->
            val useAlt = idx % 2 == 1
            ViralClip(
                id = dto.clipId ?: dto.id ?: "api_clip_${System.currentTimeMillis()}_$idx",
                title = dto.title?.takeIf { it.isNotBlank() } ?: "AI VIRAL HIGHLIGHT #${idx + 1}",
                durationSec = dto.duration ?: (24 + idx * 5),
                viralScore = (dto.viralScore ?: (96 - idx * 3)).coerceIn(60, 99),
                hypePeakLabel = "Backend GPU Render • Whisper Synced",
                sourcePlatform = StreamPlatform.entries.firstOrNull {
                    it.displayName.equals(dto.platform, ignoreCase = true)
                } ?: fallbackPlatform,
                streamerName = dto.streamer?.takeIf { it.isNotBlank() } ?: "Live Stream",
                videoUrl = BackendApiClient.resolveMediaUrl(activeServerUrl, dto.videoUrl),
                facecamRes = if (useAlt) R.drawable.img_facecam_hype_1795265838199 else R.drawable.img_facecam_cyber_1795265839367,
                gameplayRes = if (useAlt) R.drawable.img_gameplay_br_1795265849295 else R.drawable.img_gameplay_fps_1795265846214,
                studioConfig = ClipStudioConfig(
                    captionText = dto.captionText?.takeIf { it.isNotBlank() }
                        ?: "CLIP THAT RIGHT NOW! INSANE PLAY!"
                ),
                createdLabel = "Rendered live"
            )
        }
    }
}
