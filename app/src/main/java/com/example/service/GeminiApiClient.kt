package com.example.service

import com.example.BuildConfig
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

class GeminiApiClient(
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
        val effectiveKey = getEffectiveApiKey(settings.apiKey)
        if (effectiveKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is required. Please set it in Settings or the AI Studio Secrets panel.")
            )
        }

        val model = settings.modelId.ifBlank { "gemini-2.5-flash" }
        val baseUrl = settings.customEndpoint.ifBlank {
            "https://generativelanguage.googleapis.com/v1beta"
        }.trimEnd('/')

        val url = "$baseUrl/models/$model:generateContent?key=$effectiveKey"

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Text part
            val textPart = JSONObject().put("text", prompt)
            partsArray.put(textPart)

            // Optional image part
            if (!imageBase64.isNullOrBlank()) {
                val imagePart = JSONObject().put(
                    "inline_data",
                    JSONObject()
                        .put("mime_type", "image/jpeg")
                        .put("data", imageBase64)
                )
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // Generation config with JSON response hint
            val genConfig = JSONObject()
                .put("temperature", 0.2)
                .put("response_mime_type", "application/json")
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                } catch (_: Exception) {
                    responseBody
                }
                return@withContext Result.failure(Exception("Gemini API Error (${response.code}): $errorMsg"))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("No candidate content returned by Gemini API."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun testConnection(settings: AISettings): Result<String> = withContext(Dispatchers.IO) {
        val effectiveKey = getEffectiveApiKey(settings.apiKey)
        if (effectiveKey.isBlank()) {
            return@withContext Result.failure(Exception("API key is blank."))
        }

        val model = settings.modelId.ifBlank { "gemini-2.5-flash" }
        val baseUrl = settings.customEndpoint.ifBlank {
            "https://generativelanguage.googleapis.com/v1beta"
        }.trimEnd('/')

        val url = "$baseUrl/models/$model:generateContent?key=$effectiveKey"

        try {
            val rootJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val content = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().put("text", "Respond with exact JSON: {\"status\":\"ok\"}"))
                        }
                        put("parts", parts)
                    }
                    put(content)
                }
                put("contents", contents)
            }

            val body = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("Connected successfully to Gemini ($model).")
            } else {
                val msg = try {
                    JSONObject(responseBody).optJSONObject("error")?.optString("message") ?: responseBody
                } catch (_: Exception) {
                    responseBody
                }
                Result.failure(Exception("HTTP ${response.code}: $msg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getEffectiveApiKey(settingsKey: String): String {
        if (settingsKey.isNotBlank()) return settingsKey.trim()
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildConfigKey = field.get(null) as? String ?: ""
            if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                System.getenv("GEMINI_API_KEY") ?: ""
            }
        } catch (_: Exception) {
            System.getenv("GEMINI_API_KEY") ?: ""
        }
    }
}
