package com.example.model

enum class AIProvider(val displayName: String) {
    GEMINI("Google Gemini API"),
    OPENAI("OpenAI-Compatible / Custom Endpoint")
}

data class AISettings(
    val provider: AIProvider = AIProvider.GEMINI,
    val apiKey: String = "",
    val modelId: String = "gemini-2.5-flash",
    val customEndpoint: String = "",
    val appTheme: String = "system", // "system", "light", "dark"
    val language: String = "en" // "en", "ar", "fr"
)
