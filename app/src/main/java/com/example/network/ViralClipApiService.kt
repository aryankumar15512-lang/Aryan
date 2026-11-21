package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String = "ok",
    @Json(name = "mode") val mode: String = "pc",
    @Json(name = "gpu") val gpu: Boolean = true
)

@JsonClass(generateAdapter = true)
data class ProcessUrlRequest(
    @Json(name = "url") val url: String,
    @Json(name = "is_live") val isLive: Boolean
)

@JsonClass(generateAdapter = true)
data class JobResponse(
    @Json(name = "job_id") val jobId: String
)

@JsonClass(generateAdapter = true)
data class ApiClipDto(
    @Json(name = "clip_id") val clipId: String? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "viral_score") val viralScore: Int? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "caption_text") val captionText: String? = null,
    @Json(name = "platform") val platform: String? = null,
    @Json(name = "streamer") val streamer: String? = null
)

@JsonClass(generateAdapter = true)
data class JobStatusResponse(
    @Json(name = "status") val status: String, // "processing" | "done" | "error"
    @Json(name = "step") val step: String = "",
    @Json(name = "progress") val progress: Int = 0,
    @Json(name = "clips") val clips: List<ApiClipDto>? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class FacecamPayload(
    @Json(name = "x") val x: Float,
    @Json(name = "y") val y: Float,
    @Json(name = "scale") val scale: Float,
    @Json(name = "shape") val shape: String // "rect" | "circle"
)

@JsonClass(generateAdapter = true)
data class CaptionPayload(
    @Json(name = "y") val y: Float,
    @Json(name = "style") val style: String,
    @Json(name = "size") val size: Int,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class RerenderRequest(
    @Json(name = "clip_id") val clipId: String,
    @Json(name = "layout") val layout: String, // "split" | "pip" | "blur"
    @Json(name = "facecam") val facecam: FacecamPayload,
    @Json(name = "gameplay_zoom") val gameplayZoom: Float,
    @Json(name = "caption") val caption: CaptionPayload
)

@JsonClass(generateAdapter = true)
data class RerenderResponse(
    @Json(name = "video_url") val videoUrl: String
)

interface ViralClipApiService {
    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/process-url")
    suspend fun processUrl(@Body request: ProcessUrlRequest): JobResponse

    @Multipart
    @POST("api/process-upload")
    suspend fun processUpload(@Part file: MultipartBody.Part): JobResponse

    @GET("api/status/{job_id}")
    suspend fun getJobStatus(@Path("job_id") jobId: String): JobStatusResponse

    @POST("api/rerender")
    suspend fun rerenderClip(@Body request: RerenderRequest): RerenderResponse
}
