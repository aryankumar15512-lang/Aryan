package com.example.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.PlatformFacebook
import com.example.ui.theme.PlatformKick
import com.example.ui.theme.PlatformRumble
import com.example.ui.theme.PlatformTwitch
import com.example.ui.theme.PlatformX
import com.example.ui.theme.PlatformYouTube
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VoltYellow

enum class AppTab(val route: String, val label: String) {
    HOME("home", "Home"),
    CLIPS("clips", "Clips"),
    STUDIO("studio", "Studio"),
    SETTINGS("settings", "Settings")
}

enum class InputMode(val label: String) {
    LIVE_STREAM("Live Stream"),
    VOD_URL("Video / VOD"),
    UPLOAD_VIDEO("Upload Video")
}

enum class StreamPlatform(
    val displayName: String,
    val shortCode: String,
    val accentColor: Color,
    val domains: List<String>,
    val sampleLiveUrl: String,
    val sampleVodUrl: String
) {
    YOUTUBE(
        displayName = "YouTube",
        shortCode = "YT",
        accentColor = PlatformYouTube,
        domains = listOf("youtube.com", "youtu.be"),
        sampleLiveUrl = "https://www.youtube.com/live/ace-clutch-ranked-stream",
        sampleVodUrl = "https://www.youtube.com/watch?v=9xViralFPSClutch"
    ),
    TWITCH(
        displayName = "Twitch",
        shortCode = "TW",
        accentColor = PlatformTwitch,
        domains = listOf("twitch.tv"),
        sampleLiveUrl = "https://www.twitch.tv/nova_fps_live",
        sampleVodUrl = "https://www.twitch.tv/videos/2298471920"
    ),
    KICK(
        displayName = "Kick",
        shortCode = "KK",
        accentColor = PlatformKick,
        domains = listOf("kick.com"),
        sampleLiveUrl = "https://kick.com/vortexgaming",
        sampleVodUrl = "https://kick.com/video/8849201-ranked-finals"
    ),
    FACEBOOK(
        displayName = "Facebook",
        shortCode = "FB",
        accentColor = PlatformFacebook,
        domains = listOf("facebook.com", "fb.gg", "fb.watch"),
        sampleLiveUrl = "https://fb.gg/v/pro-esports-live",
        sampleVodUrl = "https://www.facebook.com/gaming/videos/774829104"
    ),
    RUMBLE(
        displayName = "Rumble",
        shortCode = "RM",
        accentColor = PlatformRumble,
        domains = listOf("rumble.com"),
        sampleLiveUrl = "https://rumble.com/c/ProArenaLive",
        sampleVodUrl = "https://rumble.com/v5clutch-ranked-highlight.html"
    ),
    X(
        displayName = "X",
        shortCode = "X",
        accentColor = PlatformX,
        domains = listOf("x.com", "twitter.com"),
        sampleLiveUrl = "https://x.com/i/broadcasts/1v5FinalZone",
        sampleVodUrl = "https://x.com/cyberpro/status/184920184710"
    );

    companion object {
        fun detectFromUrl(url: String): StreamPlatform? {
            val lower = url.trim().lowercase()
            if (lower.isEmpty()) return null
            return entries.firstOrNull { platform ->
                platform.domains.any { domain -> lower.contains(domain) }
            }
        }
    }
}

enum class LayoutPreset(val apiValue: String, val label: String) {
    SPLIT("split", "Split Screen"),
    PIP("pip", "Floating PIP"),
    BLUR("blur", "Blurred BG");

    companion object {
        fun fromApi(value: String?): LayoutPreset =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: SPLIT
    }
}

enum class FacecamShape(val apiValue: String, val label: String) {
    RECT("rect", "Square"),
    CIRCLE("circle", "Circle");

    companion object {
        fun fromApi(value: String?): FacecamShape =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: RECT
    }
}

enum class CaptionStylePreset(
    val apiValue: String,
    val label: String,
    val textColor: Color,
    val highlightColor: Color,
    val badgeBg: Color
) {
    BOLD_YELLOW_POP(
        apiValue = "bold_yellow_pop",
        label = "Bold Yellow Pop",
        textColor = VoltYellow,
        highlightColor = NeonCrimson,
        badgeBg = Color(0xCC07080D)
    ),
    NEON_CYAN(
        apiValue = "neon_cyan",
        label = "Neon Cyan",
        textColor = ElectricCyan,
        highlightColor = TextPrimary,
        badgeBg = Color(0xCC061C24)
    ),
    CLASSIC_WHITE(
        apiValue = "classic_white",
        label = "Classic White",
        textColor = TextPrimary,
        highlightColor = VoltYellow,
        badgeBg = Color(0xB8000000)
    );

    companion object {
        fun fromApi(value: String?): CaptionStylePreset =
            entries.firstOrNull { it.apiValue.equals(value, ignoreCase = true) } ?: BOLD_YELLOW_POP
    }
}

enum class GlowBorderOption(val label: String, val color: Color) {
    CYAN("Electric Cyan", ElectricCyan),
    CRIMSON("Neon Pink", NeonCrimson),
    YELLOW("Volt Yellow", VoltYellow),
    PURPLE("Cyber Purple", CyberPurple),
    WHITE("Pure White", TextPrimary),
    NONE("No Border", Color.Transparent)
}

data class ClipStudioConfig(
    val layout: LayoutPreset = LayoutPreset.SPLIT,
    val splitRatio: Float = 0.35f,          // 0.20f..0.60f (facecam top %)
    val facecamX: Float = 0.64f,            // normalized 0f..1f center X inside canvas
    val facecamY: Float = 0.17f,            // normalized 0f..1f center Y inside canvas
    val facecamScale: Float = 0.34f,        // normalized width fraction 0.20f..0.62f
    val facecamShape: FacecamShape = FacecamShape.RECT,
    val facecamCornerRadiusDp: Float = 14f, // 0f..32f
    val facecamGlow: GlowBorderOption = GlowBorderOption.CYAN,
    val gameplayZoom: Float = 1.15f,        // 1.0f..2.2f
    val gameplayPanX: Float = 0f,           // -0.5f..0.5f
    val gameplayPanY: Float = 0f,           // -0.5f..0.5f
    val captionY: Float = 0.68f,            // normalized 0.12f..0.88f
    val captionStyle: CaptionStylePreset = CaptionStylePreset.BOLD_YELLOW_POP,
    val captionFontSize: Int = 22,          // 14..34 sp
    val captionText: String = "NO WAY I JUST HIT THAT 1v5 CLUTCH!"
)

data class ViralClip(
    val id: String,
    val title: String,
    val durationSec: Int,
    val viralScore: Int,
    val hypePeakLabel: String,
    val sourcePlatform: StreamPlatform,
    val streamerName: String,
    val videoUrl: String? = null,
    val localVideoUri: String? = null,
    @DrawableRes val facecamRes: Int,
    @DrawableRes val gameplayRes: Int,
    val studioConfig: ClipStudioConfig,
    val isEdited: Boolean = false,
    val isRerendered: Boolean = false,
    val createdLabel: String = "Just now"
) {
    val formattedDuration: String
        get() {
            val mins = durationSec / 60
            val secs = durationSec % 60
            return "$mins:${secs.toString().padStart(2, '0')}"
        }
}

enum class ProcessingStep(val index: Int, val title: String, val detail: String) {
    CAPTURING(0, "Capturing Stream / Video", "Ingesting source frames & lossless audio track"),
    DETECTING_HYPE(1, "Detecting Hype & Audio Spikes", "Scanning decibel surges, chat velocity & killfeed peaks"),
    WHISPER_CAPTIONS(2, "Generating Whisper Auto-Captions", "Transcribing word-level timestamps with high accuracy"),
    AUTO_FRAMING(3, "Auto-Framing Facecam & Gameplay", "Tracking face bounding box & locking 9:16 dynamic split"),
    RENDERING(4, "Rendering Clips", "Encoding 60fps vertical Shorts with animated captions")
}

data class ProcessingJobState(
    val isRunning: Boolean = false,
    val jobId: String? = null,
    val currentStepIndex: Int = 0,
    val currentStepLabel: String = "",
    val progress: Int = 0, // 0..100
    val completedClipCount: Int? = null,
    val errorMessage: String? = null
)

enum class EngineMode(val apiValue: String, val title: String, val subtitle: String) {
    PC_LOCAL(
        apiValue = "pc",
        title = "PC Mode (Local Wi-Fi)",
        subtitle = "Connect directly to your RTX gaming PC FastAPI server over LAN"
    ),
    CLOUD_COLAB(
        apiValue = "cloud",
        title = "Cloud Mode (Google Colab)",
        subtitle = "Connect to a free T4/L4 GPU Colab notebook via Cloudflare Tunnel"
    )
}

enum class ConnectionStatus {
    DEMO,
    IDLE,
    CHECKING,
    CONNECTED,
    ERROR
}

data class BackendHealthState(
    val status: ConnectionStatus = ConnectionStatus.DEMO,
    val reportedMode: String? = null,
    val gpuAvailable: Boolean = false,
    val latencyMs: Long? = null,
    val message: String = "Demo Mode Active • Simulated AI Engine"
)

data class UploadedVideoInfo(
    val uriString: String,
    val fileName: String,
    val sizeMb: String
)

enum class ClipFilter(val label: String) {
    TOP_VIRAL("Top Viral"),
    ALL("All Clips"),
    EDITED("Edited")
}
