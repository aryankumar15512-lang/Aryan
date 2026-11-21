package com.example.network

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.data.PersistedSettings
import com.example.model.ClipStudioConfig
import com.example.model.UploadedVideoInfo
import com.example.model.ViralClip
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.source
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

class BackendApiClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    @Volatile
    private var cachedBaseUrl: String? = null

    @Volatile
    private var cachedService: ViralClipApiService? = null

    fun normalizeBaseUrlForRetrofit(rawUrl: String): String {
        val clean = sanitizeServerOrigin(rawUrl)
        require(clean.isNotBlank()) { "Server URL cannot be empty." }
        return "$clean/"
    }

    fun getService(rawBaseUrl: String): ViralClipApiService {
        val normalized = normalizeBaseUrlForRetrofit(rawBaseUrl)
        val existing = cachedService
        if (existing != null && cachedBaseUrl == normalized) {
            return existing
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val created = retrofit.create(ViralClipApiService::class.java)
        cachedBaseUrl = normalized
        cachedService = created
        return created
    }

    fun formatRerenderJson(clipId: String, config: ClipStudioConfig): String {
        val payload = RerenderRequest(
            clipId = clipId,
            layout = config.layout.apiValue,
            facecam = FacecamPayload(
                x = round2(config.facecamX),
                y = round2(config.facecamY),
                scale = round2(config.facecamScale),
                shape = config.facecamShape.apiValue
            ),
            gameplayZoom = round2(config.gameplayZoom),
            caption = CaptionPayload(
                y = round2(config.captionY),
                style = config.captionStyle.apiValue,
                size = config.captionFontSize,
                text = config.captionText
            )
        )
        return moshi.adapter(RerenderRequest::class.java).indent("  ").toJson(payload)
    }

    private fun round2(value: Float): Float =
        kotlin.math.round(value * 100f) / 100f

    companion object {
        fun sanitizeServerOrigin(rawUrl: String): String {
            val stripped = PersistedSettings.stripTrailingSlashes(rawUrl)
            if (stripped.isBlank()) return ""
            return if (stripped.startsWith("http://", ignoreCase = true) ||
                stripped.startsWith("https://", ignoreCase = true)
            ) {
                stripped
            } else {
                "http://$stripped"
            }
        }

        /**
         * Resolves a backend video_url against the active Server URL.
         * e.g. serverUrl="http://192.168.1.5:8000/" and videoUrl="/clips/abc.mp4"
         * -> "http://192.168.1.5:8000/clips/abc.mp4"
         */
        fun resolveMediaUrl(activeServerUrl: String, videoUrl: String?): String? {
            val raw = videoUrl?.trim().orEmpty()
            if (raw.isEmpty()) return null
            if (raw.startsWith("http://", ignoreCase = true) ||
                raw.startsWith("https://", ignoreCase = true)
            ) {
                return raw
            }
            val origin = sanitizeServerOrigin(activeServerUrl)
            if (origin.isBlank()) return raw
            val path = if (raw.startsWith("/")) raw else "/$raw"
            return "$origin$path"
        }
    }
}

object MediaFileHelper {
    fun queryVideoInfo(context: Context, uri: Uri): UploadedVideoInfo {
        var name = "gaming_clip_${System.currentTimeMillis() % 10000}.mp4"
        var sizeBytes = 0L
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) {
                    cursor.getString(nameIdx)?.takeIf { it.isNotBlank() }?.let { name = it }
                }
                if (sizeIdx >= 0) {
                    sizeBytes = cursor.getLong(sizeIdx)
                }
            }
        }
        val sizeMb = if (sizeBytes > 0L) {
            String.format(Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0))
        } else {
            "Local Video"
        }
        return UploadedVideoInfo(
            uriString = uri.toString(),
            fileName = name,
            sizeMb = sizeMb
        )
    }

    fun buildMultipartFromUri(context: Context, uri: Uri, fileName: String): MultipartBody.Part {
        val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
        val requestBody = object : RequestBody() {
            override fun contentType() = mimeType.toMediaTypeOrNull()

            override fun writeTo(sink: BufferedSink) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    sink.writeAll(inputStream.source())
                } ?: throw IllegalStateException("Unable to read selected video stream")
            }
        }
        return MultipartBody.Part.createFormData("file", fileName, requestBody)
    }

    /**
     * Cleanly downloads the real MP4 video from the resolved backend URL (or copies local uploaded MP4)
     * directly into the device's Downloads/ViralClipX/ directory using Scoped Storage MediaStore.
     */
    suspend fun downloadClip(
        context: Context,
        okHttpClient: OkHttpClient,
        clip: ViralClip,
        activeServerUrl: String,
        rerenderJson: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeName = clip.title
                .lowercase(Locale.US)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .take(36)
                .ifBlank { "viralclip_${clip.id}" }

            // 1. Real Backend MP4 Download (handles relative paths like /clips/abc.mp4 cleanly)
            val resolvedUrl = BackendApiClient.resolveMediaUrl(activeServerUrl, clip.videoUrl)
            if (!resolvedUrl.isNullOrBlank() &&
                (resolvedUrl.startsWith("http://", ignoreCase = true) ||
                    resolvedUrl.startsWith("https://", ignoreCase = true))
            ) {
                val mp4FileName = "ViralClipX_${safeName}.mp4"
                val request = Request.Builder().url(resolvedUrl).get().build()
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("Download failed (HTTP ${response.code}) from $resolvedUrl")
                    }
                    val body = response.body
                        ?: throw IllegalStateException("Empty response body from backend")
                    val bytesWritten = saveStreamToDownloads(
                        context = context,
                        fileName = mp4FileName,
                        mimeType = "video/mp4",
                        inputStream = body.byteStream()
                    )
                    if (bytesWritten <= 0L) {
                        throw IllegalStateException("Downloaded MP4 file was empty")
                    }
                    val mbStr = formatBytes(bytesWritten)
                    return@runCatching "Downloaded $mp4FileName ($mbStr) to Downloads/ViralClipX"
                }
            }

            // 2. Local Gallery Upload Copy as MP4 (e.g., user uploaded local MP4 in Demo Mode)
            val localUriStr = clip.localVideoUri
            if (!localUriStr.isNullOrBlank()) {
                val mp4FileName = "ViralClipX_${safeName}.mp4"
                val input = context.contentResolver.openInputStream(Uri.parse(localUriStr))
                if (input != null) {
                    input.use { stream ->
                        val bytesWritten = saveStreamToDownloads(
                            context = context,
                            fileName = mp4FileName,
                            mimeType = "video/mp4",
                            inputStream = stream
                        )
                        val mbStr = formatBytes(bytesWritten)
                        return@runCatching "Saved $mp4FileName ($mbStr) to Downloads/ViralClipX"
                    }
                }
            }

            // 3. Offline Demo Sample Fallback (no remote backend MP4 yet)
            val exportFileName = "ViralClipX_${safeName}_studio.json"
            saveStreamToDownloads(
                context = context,
                fileName = exportFileName,
                mimeType = "application/json",
                inputStream = rerenderJson.byteInputStream(Charsets.UTF_8)
            )
            "Demo Mode: saved render spec ($exportFileName). Connect PC/Cloud backend to download MP4."
        }
    }

    private fun saveStreamToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        inputStream: InputStream
    ): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ViralClipX")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Unable to create file in Downloads")

            var totalBytes = 0L
            try {
                resolver.openOutputStream(uri)?.use { out ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = inputStream.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        totalBytes += read
                    }
                    out.flush()
                } ?: throw IllegalStateException("Unable to write to Downloads stream")

                val doneValues = ContentValues().apply {
                    put(MediaStore.Downloads.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
                return totalBytes
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
        } else {
            val baseDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val dir = File(baseDir, "ViralClipX").apply { mkdirs() }
            val outFile = File(dir, fileName)
            var totalBytes = 0L
            FileOutputStream(outFile).use { out ->
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    out.write(buffer, 0, read)
                    totalBytes += read
                }
                out.flush()
            }
            return totalBytes
        }
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
