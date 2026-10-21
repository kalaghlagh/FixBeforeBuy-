package com.example.service

import com.example.model.AISettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OpenAICompatibleClient(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIService {

    override suspend fun diagnose(
        prompt: String,
        imageBase64: String?,
        settings: AISettings
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = settings.apiKey.trim()
        val endpoint = settings.customEndpoint.ifBlank {
            "https://api.openai.com/v1/chat/completions"
        }
        val model = settings.modelId.ifBlank { "gpt-4o-mini" }

        try {
            val root = JSONObject()
            root.put("model", model)
            val messages = JSONArray()

            val userMsg = JSONObject()
            userMsg.put("role", "user")

            if (!imageBase64.isNullOrBlank()) {
                val contentArray = JSONArray()
                contentArray.put(JSONObject().put("type", "text").put("text", prompt))
                contentArray.put(
                    JSONObject().put("type", "image_url").put(
                        "image_url",
                        JSONObject().put("url", "data:image/jpeg;base64,$imageBase64")
                    )
                )
                userMsg.put("content", contentArray)
            } else {
                userMsg.put("content", prompt)
            }

            messages.put(userMsg)
            root.put("messages", messages)
            root.put("temperature", 0.2)

            val body = root.toString().toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder().url(endpoint).post(body)
            if (apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $apiKey")
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error (${response.code}): $responseBody"))
            }

            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val content = choices.getJSONObject(0).optJSONObject("message")?.optString("content")
                if (!content.isNullOrBlank()) {
                    return@withContext Result.success(content)
                }
            }

            Result.failure(Exception("Empty message content returned from provider."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(settings: AISettings): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = settings.customEndpoint.ifBlank {
            "https://api.openai.com/v1/chat/completions"
        }
        val model = settings.modelId.ifBlank { "gpt-4o-mini" }
        val apiKey = settings.apiKey.trim()

        try {
            val root = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    put(JSONObject().put("role", "user").put("content", "ping"))
                })
                put("max_tokens", 5)
            }

            val body = root.toString().toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url(endpoint).post(body).apply {
                if (apiKey.isNotBlank()) header("Authorization", "Bearer $apiKey")
            }.build()

            val res = client.newCall(req).execute()
            if (res.isSuccessful) {
                Result.success("Connected to endpoint ($model).")
            } else {
                Result.failure(Exception("HTTP ${res.code}: ${res.body?.string().orEmpty().take(150)}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
