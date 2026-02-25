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
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)  // LLM responses can be slow
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun chat(message: String): Result<ChatResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val body = gson.toJson(mapOf("message" to message)).toRequestBody(jsonType)
            val request = Request.Builder()
                .url("$baseUrl/api/chat")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Chat failed: ${response.code}")
            gson.fromJson(response.body?.string(), ChatResponse::class.java)
        }
    }

    suspend fun getStatus(): Result<JsonObject> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url("$baseUrl/api/status").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Status failed: ${response.code}")
            gson.fromJson(response.body?.string(), JsonObject::class.java)
        }
    }

    suspend fun getMemory(): Result<JsonObject> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url("$baseUrl/api/memory").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Memory failed: ${response.code}")
            gson.fromJson(response.body?.string(), JsonObject::class.java)
        }
    }

    suspend fun getSkills(): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url("$baseUrl/api/skills").get().build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) error("Skills failed: ${response.code}")
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(response.body?.string(), type)
        }
    }

    suspend fun isHealthy(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/api/status").get().build()
            client.newCall(request).execute().isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}

data class ChatResponse(
    val response: String = "",
    val tool_calls: List<ToolCall>? = null,
)

data class ToolCall(
    val skill: String = "",
    val args: Map<String, String>? = null,
    val result: String? = null,
)
