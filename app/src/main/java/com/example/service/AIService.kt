package com.example.service

import com.example.model.AISettings

interface AIService {
    suspend fun diagnose(
        prompt: String,
        imageBase64: String?,
        settings: AISettings
    ): Result<String>

    suspend fun testConnection(settings: AISettings): Result<String>
}
