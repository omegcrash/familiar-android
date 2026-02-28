package com.omegcrash.familiar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "familiar_settings")

class PreferencesStore(private val context: Context) {

    companion object {
        val API_PROVIDER = stringPreferencesKey("api_provider")
        val API_KEY = stringPreferencesKey("api_key")
        val OLLAMA_URL = stringPreferencesKey("ollama_url")
        val AGENT_NAME = stringPreferencesKey("agent_name")
        val MODEL_NAME = stringPreferencesKey("model_name")
        val SETUP_COMPLETE = stringPreferencesKey("setup_complete")
        val CONNECTION_MODE = stringPreferencesKey("connection_mode") // "local" or "remote"
        val SERVER_URL = stringPreferencesKey("server_url")
        val REMOTE_API_KEY = stringPreferencesKey("remote_api_key")
        val PERSONA = stringPreferencesKey("persona")
        val OWNER_PIN = stringPreferencesKey("owner_pin")
        val BRIEFING_ENABLED = stringPreferencesKey("briefing_enabled")
    }

    val isSetupComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SETUP_COMPLETE] == "true"
    }

    val connectionMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CONNECTION_MODE] ?: "local"
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SERVER_URL] ?: "http://127.0.0.1:5000"
    }

    val remoteApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[REMOTE_API_KEY] ?: ""
    }

    fun getEnvVars(): Flow<Map<String, String>> = context.dataStore.data.map { prefs ->
        buildMap {
            prefs[API_PROVIDER]?.let { put("FAMILIAR_LLM_PROVIDER", it) }
            prefs[API_KEY]?.let { apiKey ->
                when (prefs[API_PROVIDER]) {
                    "anthropic" -> put("ANTHROPIC_API_KEY", apiKey)
                    "openai" -> put("OPENAI_API_KEY", apiKey)
                    "gemini" -> put("GEMINI_API_KEY", apiKey)
                }
            }
            prefs[OLLAMA_URL]?.let { put("OLLAMA_URL", it) }
            prefs[AGENT_NAME]?.let { put("FAMILIAR_AGENT_NAME", it) }
            prefs[MODEL_NAME]?.let { put("FAMILIAR_MODEL", it) }
            prefs[PERSONA]?.let { put("FAMILIAR_PERSONA", it) }
            prefs[OWNER_PIN]?.takeIf { it.isNotBlank() }?.let {
                put("FAMILIAR_OWNER_PIN_RAW", it)
            }
            prefs[BRIEFING_ENABLED]?.let { put("FAMILIAR_BRIEFING_ENABLED", it) }
        }
    }

    suspend fun saveSetup(
        provider: String,
        apiKey: String,
        ollamaUrl: String,
        agentName: String,
        modelName: String,
        connectionMode: String = "local",
        serverUrl: String = "",
        remoteApiKey: String = "",
        persona: String = "hospitality",
        ownerPin: String = "",
        briefingEnabled: Boolean = true,
    ) {
        context.dataStore.edit { prefs ->
            prefs[API_PROVIDER] = provider
            prefs[API_KEY] = apiKey
            prefs[OLLAMA_URL] = ollamaUrl
            prefs[AGENT_NAME] = agentName
            prefs[MODEL_NAME] = modelName
            prefs[CONNECTION_MODE] = connectionMode
            prefs[SERVER_URL] = serverUrl
            prefs[REMOTE_API_KEY] = remoteApiKey
            prefs[PERSONA] = persona
            prefs[OWNER_PIN] = ownerPin
            prefs[BRIEFING_ENABLED] = if (briefingEnabled) "true" else "false"
            prefs[SETUP_COMPLETE] = "true"
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
