package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.DiagnosisRepository
import com.example.data.repository.SettingsRepository
import com.example.model.*
import com.example.service.DiagnosticEngine
import com.example.service.GeminiApiClient
import com.example.service.OpenAICompatibleClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val diagnosisRepository = DiagnosisRepository(database.sessionDao())
    private val settingsRepository = SettingsRepository(application)
    private val diagnosticEngine = DiagnosticEngine()
    private val geminiClient = GeminiApiClient()
    private val openAIClient = OpenAICompatibleClient()

    val settings: StateFlow<AISettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AISettings()
    )

    val recentSessions: StateFlow<List<DiagnosticSession>> = diagnosisRepository.recentSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allSessions: StateFlow<List<DiagnosticSession>> = diagnosisRepository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentSession = MutableStateFlow<DiagnosticSession?>(null)
    val currentSession: StateFlow<DiagnosticSession?> = _currentSession.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _connectionTestStatus = MutableStateFlow<String?>(null)
    val connectionTestStatus: StateFlow<String?> = _connectionTestStatus.asStateFlow()

    private val _connectionTestLoading = MutableStateFlow(false)
    val connectionTestLoading: StateFlow<Boolean> = _connectionTestLoading.asStateFlow()

    fun startDiagnosis(
        category: ProductCategory,
        description: String,
        brand: String = "",
        model: String = "",
        ageYears: String = "",
        warrantyStatus: String = "Expired",
        imageUri: String? = null,
        imageBase64: String? = null
    ) {
        if (description.isBlank()) {
            _errorMessage.value = "Please provide a problem description."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val input = ProblemInput(
                    category = category,
                    description = description.trim(),
                    brand = brand.trim(),
                    model = model.trim(),
                    ageYears = ageYears.trim(),
                    warrantyStatus = warrantyStatus,
                    imageUri = imageUri,
                    imageBase64 = imageBase64
                )
                val session = diagnosticEngine.startSession(input, settings.value)
                _currentSession.value = session
                diagnosisRepository.saveSession(session)
            } catch (e: Exception) {
                _errorMessage.value = "Diagnosis error: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendAnswer(answer: String, additionalImageBase64: String? = null) {
        val session = _currentSession.value ?: return
        if (answer.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val updated = diagnosticEngine.answerQuestion(
                    session = session,
                    userAnswer = answer.trim(),
                    additionalImageBase64 = additionalImageBase64,
                    settings = settings.value
                )
                _currentSession.value = updated
                diagnosisRepository.saveSession(updated)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to evaluate answer: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleRepairStep(stepNumber: Int) {
        val session = _currentSession.value ?: return
        val plan = session.repairPlan ?: return

        val updatedSteps = plan.steps.map { step ->
            if (step.stepNumber == stepNumber) {
                step.copy(isCompleted = !step.isCompleted)
            } else step
        }

        val updatedPlan = plan.copy(steps = updatedSteps)
        val updatedSession = session.copy(repairPlan = updatedPlan, updatedAt = System.currentTimeMillis())
        _currentSession.value = updatedSession

        viewModelScope.launch {
            diagnosisRepository.saveSession(updatedSession)
        }
    }

    fun updateCostComparison(comparison: CostComparison) {
        val session = _currentSession.value ?: return
        val updated = session.copy(costComparison = comparison, updatedAt = System.currentTimeMillis())
        _currentSession.value = updated

        viewModelScope.launch {
            diagnosisRepository.saveSession(updated)
        }
    }

    fun resumeSession(session: DiagnosticSession) {
        _currentSession.value = session
    }

    fun clearCurrentSession() {
        _currentSession.value = null
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            diagnosisRepository.deleteSession(sessionId)
            if (_currentSession.value?.id == sessionId) {
                _currentSession.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            diagnosisRepository.clearAll()
            _currentSession.value = null
        }
    }

    fun saveSettings(newSettings: AISettings) {
        viewModelScope.launch {
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            settingsRepository.clearApiKey()
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionTestLoading.value = true
            _connectionTestStatus.value = null
            val current = settings.value

            val service = when (current.provider) {
                AIProvider.GEMINI -> geminiClient
                AIProvider.OPENAI -> openAIClient
            }

            val result = service.testConnection(current)
            if (result.isSuccess) {
                _connectionTestStatus.value = "SUCCESS: ${result.getOrNull()}"
            } else {
                _connectionTestStatus.value = "FAILED: ${result.exceptionOrNull()?.message}"
            }
            _connectionTestLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
