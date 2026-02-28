package com.omegcrash.familiar.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class FamiliarClient(
    private val baseUrl: String = "http://127.0.0.1:5000",
    private val apiKey: String = "",
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // LLM responses can be slow
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    /** Build a GET request with auth header. */
    private fun authGet(path: String): Request =
        Request.Builder()
            .url("$baseUrl$path")
            .addHeader("X-API-Key", apiKey)
            .get()
            .build()

    /** Build a POST request with auth header and JSON body. */
    private fun authPost(path: String, body: Any): Request =
        Request.Builder()
            .url("$baseUrl$path")
            .addHeader("X-API-Key", apiKey)
            .post(gson.toJson(body).toRequestBody(jsonType))
            .build()

    suspend fun chat(message: String): Result<ChatResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authPost("/api/chat", mapOf("message" to message))
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Chat failed: ${response.code}")
            gson.fromJson(response.body?.string(), ChatResponse::class.java)
        }
    }

    suspend fun getStatus(): Result<JsonObject> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/status")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Status failed: ${response.code}")
            gson.fromJson(response.body?.string(), JsonObject::class.java)
        }
    }

    suspend fun getMemory(): Result<JsonObject> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/memory")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Memory failed: ${response.code}")
            gson.fromJson(response.body?.string(), JsonObject::class.java)
        }
    }

    suspend fun getSkills(): Result<SkillsResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/skills")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Skills failed: ${response.code}")
            gson.fromJson(response.body?.string(), SkillsResponse::class.java)
        }
    }

    suspend fun getConnectStatus(): Result<ConnectStatus> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/connect/status")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Connect status failed: ${response.code}")
            gson.fromJson(response.body?.string(), ConnectStatus::class.java)
        }
    }

    suspend fun getTools(): Result<ToolsResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/tools")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Tools failed: ${response.code}")
            gson.fromJson(response.body?.string(), ToolsResponse::class.java)
        }
    }

    suspend fun getConfig(): Result<AgentConfig> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authGet("/api/config")
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Config failed: ${response.code}")
            gson.fromJson(response.body?.string(), AgentConfig::class.java)
        }
    }

    suspend fun toggleSkill(name: String): Result<SkillToggleResult> = withContext(Dispatchers.IO) {
        runCatching {
            val request = authPost("/api/skills/$name/toggle", emptyMap<String, String>())
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Toggle failed: ${response.code}")
            gson.fromJson(response.body?.string(), SkillToggleResult::class.java)
        }
    }

    suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = authGet("/api/status")
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}

// ── Chat models ─────────────────────────────────────────────

data class ChatResponse(
    val response: String = "",
    val tool_calls: List<ToolCall>? = null,
)

data class ToolCall(
    val skill: String = "",
    val args: Map<String, String>? = null,
    val result: String? = null,
    val category: String? = null,
)

// ── Connect / Services models ───────────────────────────────

data class ServiceInfo(
    val name: String = "",
    val display_name: String = "",
    val category: String = "",
    val connected: Boolean = false,
    val detail: String? = null,
    val connect_command: String = "",
)

data class ConnectStatus(
    val services: List<ServiceInfo> = emptyList(),
    val categories: List<String> = emptyList(),
    val active_provider: String = "",
)

// ── Tools models ────────────────────────────────────────────

data class ToolInfo(
    val name: String = "",
    val description: String = "",
    val category: String = "",
)

data class ToolsResponse(
    val tools: List<ToolInfo> = emptyList(),
)

// ── Skills models ───────────────────────────────────────────

data class SkillDetail(
    val name: String = "",
    val enabled: Boolean = true,
)

data class SkillsResponse(
    val skills: List<SkillDetail> = emptyList(),
)

data class SkillToggleResult(
    val enabled: Boolean = false,
)

// ── Config model ────────────────────────────────────────────

data class AgentConfig(
    val agent_name: String = "",
    val provider: String = "",
    val memory_enabled: Boolean = false,
    val skills_enabled: Boolean = false,
    val scheduler_enabled: Boolean = false,
)
