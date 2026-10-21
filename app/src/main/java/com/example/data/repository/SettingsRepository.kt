package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AIProvider
import com.example.model.AISettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.flow.catch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fix_before_buy_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val PROVIDER = stringPreferencesKey("ai_provider")
        val API_KEY = stringPreferencesKey("api_key")
        val MODEL_ID = stringPreferencesKey("model_id")
        val CUSTOM_ENDPOINT = stringPreferencesKey("custom_endpoint")
        val THEME = stringPreferencesKey("app_theme")
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    val settingsFlow: Flow<AISettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
        val providerStr = prefs[PreferencesKeys.PROVIDER] ?: AIProvider.GEMINI.name
        val provider = try {
            AIProvider.valueOf(providerStr)
        } catch (_: Exception) {
            AIProvider.GEMINI
        }
        val apiKey = prefs[PreferencesKeys.API_KEY] ?: ""
        val modelId = prefs[PreferencesKeys.MODEL_ID] ?: "gemini-2.5-flash"
        val endpoint = prefs[PreferencesKeys.CUSTOM_ENDPOINT] ?: ""
        val theme = prefs[PreferencesKeys.THEME] ?: "system"
        val language = prefs[PreferencesKeys.LANGUAGE] ?: "en"

        AISettings(
            provider = provider,
            apiKey = apiKey,
            modelId = modelId,
            customEndpoint = endpoint,
            appTheme = theme,
            language = language
        )
    }

    suspend fun updateSettings(settings: AISettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROVIDER] = settings.provider.name
            prefs[PreferencesKeys.API_KEY] = settings.apiKey
            prefs[PreferencesKeys.MODEL_ID] = settings.modelId
            prefs[PreferencesKeys.CUSTOM_ENDPOINT] = settings.customEndpoint
            prefs[PreferencesKeys.THEME] = settings.appTheme
            prefs[PreferencesKeys.LANGUAGE] = settings.language
        }
    }

    suspend fun clearApiKey() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.API_KEY] = ""
        }
    }
}
