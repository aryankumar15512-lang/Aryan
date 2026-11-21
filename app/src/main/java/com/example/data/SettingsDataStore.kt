package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.EngineMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.viralClipDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "viralclip_x_settings"
)

data class PersistedSettings(
    val demoMode: Boolean = true,
    val engineMode: EngineMode = EngineMode.PC_LOCAL,
    val pcServerUrl: String = DEFAULT_PC_URL,
    val cloudServerUrl: String = DEFAULT_CLOUD_URL
) {
    val activeServerUrl: String
        get() = stripTrailingSlashes(
            when (engineMode) {
                EngineMode.PC_LOCAL -> pcServerUrl
                EngineMode.CLOUD_COLAB -> cloudServerUrl
            }
        )

    companion object {
        const val DEFAULT_PC_URL = "http://192.168.1.5:8000"
        const val DEFAULT_CLOUD_URL = "https://viralclip-gpu.trycloudflare.com"

        fun stripTrailingSlashes(url: String): String {
            val trimmed = url.trim()
            if (trimmed.equals("http://", ignoreCase = true) ||
                trimmed.equals("https://", ignoreCase = true)
            ) {
                return trimmed
            }
            return trimmed.replace(Regex("/+$"), "")
        }
    }
}

class SettingsDataStore(private val context: Context) {
    private object Keys {
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val ENGINE_MODE = stringPreferencesKey("engine_mode")
        val PC_SERVER_URL = stringPreferencesKey("pc_server_url")
        val CLOUD_SERVER_URL = stringPreferencesKey("cloud_server_url")
    }

    val settingsFlow: Flow<PersistedSettings> = context.viralClipDataStore.data.map { prefs ->
        val modeStr = prefs[Keys.ENGINE_MODE] ?: EngineMode.PC_LOCAL.apiValue
        val mode = EngineMode.entries.firstOrNull { it.apiValue == modeStr } ?: EngineMode.PC_LOCAL
        val demoMode = prefs[Keys.DEMO_MODE] ?: true
        val rawPc = prefs[Keys.PC_SERVER_URL] ?: PersistedSettings.DEFAULT_PC_URL
        val rawCloud = prefs[Keys.CLOUD_SERVER_URL] ?: PersistedSettings.DEFAULT_CLOUD_URL
        PersistedSettings(
            demoMode = demoMode,
            engineMode = mode,
            pcServerUrl = if (!demoMode) PersistedSettings.stripTrailingSlashes(rawPc) else rawPc,
            cloudServerUrl = if (!demoMode) PersistedSettings.stripTrailingSlashes(rawCloud) else rawCloud
        )
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.viralClipDataStore.edit { prefs ->
            prefs[Keys.DEMO_MODE] = enabled
            if (!enabled) {
                prefs[Keys.PC_SERVER_URL]?.let {
                    prefs[Keys.PC_SERVER_URL] = PersistedSettings.stripTrailingSlashes(it)
                }
                prefs[Keys.CLOUD_SERVER_URL]?.let {
                    prefs[Keys.CLOUD_SERVER_URL] = PersistedSettings.stripTrailingSlashes(it)
                }
            }
        }
    }

    suspend fun setEngineMode(mode: EngineMode) {
        context.viralClipDataStore.edit { it[Keys.ENGINE_MODE] = mode.apiValue }
    }

    suspend fun setPcServerUrl(url: String) {
        context.viralClipDataStore.edit { it[Keys.PC_SERVER_URL] = url }
    }

    suspend fun setCloudServerUrl(url: String) {
        context.viralClipDataStore.edit { it[Keys.CLOUD_SERVER_URL] = url }
    }
}
