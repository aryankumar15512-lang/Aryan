package com.example.data

import com.example.R
import com.example.model.CaptionStylePreset
import com.example.model.ClipStudioConfig
import com.example.model.FacecamShape
import com.example.model.GlowBorderOption
import com.example.model.LayoutPreset
import com.example.model.StreamPlatform
import com.example.model.ViralClip

object DemoClipCatalog {

    val initialDemoClips: List<ViralClip> = listOf(
        ViralClip(
            id = "clip_ace_98",
            title = "1v5 INSANE ACE CLUTCH AT 1 HP!",
            durationSec = 34,
            viralScore = 98,
            hypePeakLabel = "+18dB Mic Peak • 5x Killfeed",
            sourcePlatform = StreamPlatform.TWITCH,
            streamerName = "NovaFPS",
            facecamRes = R.drawable.img_facecam_cyber_1795265839367,
            gameplayRes = R.drawable.img_gameplay_fps_1795265846214,
            studioConfig = ClipStudioConfig(
                layout = LayoutPreset.SPLIT,
                splitRatio = 0.36f,
                facecamX = 0.68f,
                facecamY = 0.16f,
                facecamScale = 0.34f,
                facecamShape = FacecamShape.RECT,
                facecamCornerRadiusDp = 14f,
                facecamGlow = GlowBorderOption.CYAN,
                gameplayZoom = 1.18f,
                captionY = 0.68f,
                captionStyle = CaptionStylePreset.BOLD_YELLOW_POP,
                captionFontSize = 23,
                captionText = "NO WAY I JUST HIT THAT 1v5 ACE AT ONE HP!"
            ),
            createdLabel = "2m ago"
        ),
        ViralClip(
            id = "clip_storm_95",
            title = "FINAL CIRCLE MID-AIR STORM SNIPE",
            durationSec = 27,
            viralScore = 95,
            hypePeakLabel = "Chat Velocity 4.2x • Headshot",
            sourcePlatform = StreamPlatform.YOUTUBE,
            streamerName = "KairoLive",
            facecamRes = R.drawable.img_facecam_hype_1795265838199,
            gameplayRes = R.drawable.img_gameplay_br_1795265849295,
            studioConfig = ClipStudioConfig(
                layout = LayoutPreset.PIP,
                splitRatio = 0.35f,
                facecamX = 0.72f,
                facecamY = 0.16f,
                facecamScale = 0.34f,
                facecamShape = FacecamShape.CIRCLE,
                facecamCornerRadiusDp = 24f,
                facecamGlow = GlowBorderOption.CRIMSON,
                gameplayZoom = 1.25f,
                captionY = 0.74f,
                captionStyle = CaptionStylePreset.NEON_CYAN,
                captionFontSize = 22,
                captionText = "BRO DID YOU SEE THAT MID-AIR STORM SHOT?!"
            ),
            createdLabel = "6m ago"
        ),
        ViralClip(
            id = "clip_flick_92",
            title = "0.2s IMPOSSIBLE WALLBANG FLICK",
            durationSec = 19,
            viralScore = 92,
            hypePeakLabel = "Whisper Keyword: 'CLIP THAT'",
            sourcePlatform = StreamPlatform.KICK,
            streamerName = "VortexGG",
            facecamRes = R.drawable.img_facecam_cyber_1795265839367,
            gameplayRes = R.drawable.img_gameplay_fps_1795265846214,
            studioConfig = ClipStudioConfig(
                layout = LayoutPreset.SPLIT,
                splitRatio = 0.32f,
                facecamX = 0.28f,
                facecamY = 0.16f,
                facecamScale = 0.32f,
                facecamShape = FacecamShape.RECT,
                facecamCornerRadiusDp = 12f,
                facecamGlow = GlowBorderOption.YELLOW,
                gameplayZoom = 1.32f,
                gameplayPanX = 0.08f,
                captionY = 0.64f,
                captionStyle = CaptionStylePreset.BOLD_YELLOW_POP,
                captionFontSize = 24,
                captionText = "CHAT CLIP THAT RIGHT NOW! MY HAND IS SHAKING!"
            ),
            createdLabel = "14m ago"
        ),
        ViralClip(
            id = "clip_clutch_89",
            title = "ROOFTOP 300 IQ BAIT FOR THE WIN",
            durationSec = 41,
            viralScore = 89,
            hypePeakLabel = "Laughter Burst • Victory Audio",
            sourcePlatform = StreamPlatform.X,
            streamerName = "RiftRunner",
            facecamRes = R.drawable.img_facecam_hype_1795265838199,
            gameplayRes = R.drawable.img_gameplay_br_1795265849295,
            studioConfig = ClipStudioConfig(
                layout = LayoutPreset.BLUR,
                splitRatio = 0.35f,
                facecamX = 0.72f,
                facecamY = 0.17f,
                facecamScale = 0.30f,
                facecamShape = FacecamShape.RECT,
                facecamCornerRadiusDp = 16f,
                facecamGlow = GlowBorderOption.PURPLE,
                gameplayZoom = 1.05f,
                captionY = 0.78f,
                captionStyle = CaptionStylePreset.CLASSIC_WHITE,
                captionFontSize = 21,
                captionText = "HE FELL FOR THE FAKE RELOAD EVERY SINGLE TIME!"
            ),
            createdLabel = "22m ago"
        )
    )

    fun generateDemoScanBatch(
        sourcePlatform: StreamPlatform,
        sourceLabel: String,
        localVideoUri: String? = null
    ): List<ViralClip> {
        val stamp = System.currentTimeMillis() % 10000
        return listOf(
            ViralClip(
                id = "scan_${stamp}_1",
                title = "INSANE QUAD-KILL IN FINAL 5 SECONDS!",
                durationSec = 29,
                viralScore = 97,
                hypePeakLabel = "+21dB Hype Spike • $sourceLabel",
                sourcePlatform = sourcePlatform,
                streamerName = sourceLabel,
                localVideoUri = localVideoUri,
                facecamRes = R.drawable.img_facecam_cyber_1795265839367,
                gameplayRes = R.drawable.img_gameplay_fps_1795265846214,
                studioConfig = ClipStudioConfig(
                    layout = LayoutPreset.SPLIT,
                    splitRatio = 0.35f,
                    facecamGlow = GlowBorderOption.CRIMSON,
                    captionStyle = CaptionStylePreset.BOLD_YELLOW_POP,
                    captionFontSize = 23,
                    captionText = "WHAT JUST HAPPENED?! FOUR DOWN IN TWO SECONDS!"
                ),
                createdLabel = "Just scanned"
            ),
            ViralClip(
                id = "scan_${stamp}_2",
                title = "ZERO-SHIELD ROOFTOP ESCAPE CLUTCH",
                durationSec = 36,
                viralScore = 94,
                hypePeakLabel = "AI Face Surprise Peak • $sourceLabel",
                sourcePlatform = sourcePlatform,
                streamerName = sourceLabel,
                localVideoUri = localVideoUri,
                facecamRes = R.drawable.img_facecam_hype_1795265838199,
                gameplayRes = R.drawable.img_gameplay_br_1795265849295,
                studioConfig = ClipStudioConfig(
                    layout = LayoutPreset.PIP,
                    facecamShape = FacecamShape.CIRCLE,
                    facecamGlow = GlowBorderOption.CYAN,
                    captionStyle = CaptionStylePreset.NEON_CYAN,
                    captionFontSize = 22,
                    captionText = "HOW AM I STILL ALIVE?! ZERO SHIELDS LET'S GO!"
                ),
                createdLabel = "Just scanned"
            )
        )
    }
}
