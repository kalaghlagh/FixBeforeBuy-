package com.example.service

import com.example.model.AISettings
import com.example.model.DiagnosticMessage
import com.example.model.DiagnosticSession
import com.example.model.ParsedAIResponse
import com.example.model.ProblemInput
import com.example.model.RepairPlan
import com.example.model.RepairStep
import com.example.safety.SafetyManager

class DiagnosticEngine(
    private val geminiClient: GeminiApiClient = GeminiApiClient(),
    private val openAIClient: OpenAICompatibleClient = OpenAICompatibleClient()
) {

    suspend fun startSession(
        input: ProblemInput,
        settings: AISettings
    ): DiagnosticSession {
        // Step 1: Deterministic Safety Evaluation
        val safetyResult = SafetyManager.evaluateSafety(input.description)
        if (!safetyResult.isSafeForDIY) {
            val safetyMsg = DiagnosticMessage(
                sender = "system",
                text = "⚠️ ${safetyResult.warningTitle}\n\n${safetyResult.warningMessage}\n\n👉 Recommendation: ${safetyResult.technicianRecommendation}",
                isQuestion = false
            )
            return DiagnosticSession(
                title = input.description.take(40).ifBlank { "Repair Diagnostic" },
                category = input.category,
                problemInput = input,
                summary = safetyResult.warningTitle,
                confidence = 0.99,
                technicianRecommended = true,
                technicianReason = safetyResult.warningMessage,
                safetyWarnings = safetyResult.mandatoryPrecautions + listOf(safetyResult.warningMessage),
                messages = listOf(safetyMsg),
                isCompleted = true
            )
        }

        // Step 2: Query AI or Offline Heuristic Fallback
        val parsed = executeInference(input, emptyList(), null, settings)

        // Step 3: Build session
        val initialMessages = mutableListOf<DiagnosticMessage>()
        initialMessages.add(
            DiagnosticMessage(
                sender = "assistant",
                text = "${parsed.summary}\n\nConfidence: ${(parsed.confidence * 100).toInt()}%",
                isQuestion = false
            )
        )

        if (!parsed.next_question.isNullOrBlank()) {
            initialMessages.add(
                DiagnosticMessage(
                    sender = "assistant",
                    text = parsed.next_question,
                    isQuestion = true,
                    quickOptions = listOf("Yes", "No", "Not sure")
                )
            )
        }

        val repairPlan = if (parsed.repair_plan.isNotEmpty()) {
            RepairPlan(
                title = "Recommended Repair Procedure",
                steps = parsed.repair_plan.mapIndexed { idx, step ->
                    RepairStep(stepNumber = idx + 1, title = "Step ${idx + 1}", description = step)
                },
                requiredTools = parsed.required_tools,
                requiredParts = parsed.required_parts,
                estimatedDifficulty = parsed.estimated_difficulty,
                estimatedTimeMinutes = parsed.estimated_time_minutes
            )
        } else null

        return DiagnosticSession(
            title = input.description.take(40).ifBlank { "Repair Diagnostic" },
            category = input.category,
            problemInput = input,
            summary = parsed.summary,
            possibleCauses = parsed.possible_causes,
            confidence = parsed.confidence,
            nextQuestion = parsed.next_question,
            safeTest = parsed.safe_test,
            repairPlan = repairPlan,
            technicianRecommended = parsed.technician_recommended,
            technicianReason = parsed.technician_reason,
            safetyWarnings = parsed.safety_warnings + safetyResult.mandatoryPrecautions,
            messages = initialMessages,
            isCompleted = parsed.next_question.isNullOrBlank()
        )
    }

    suspend fun answerQuestion(
        session: DiagnosticSession,
        userAnswer: String,
        additionalImageBase64: String?,
        settings: AISettings
    ): DiagnosticSession {
        // Audit answer for hazards
        val answerSafety = SafetyManager.evaluateSafety(userAnswer)
        if (!answerSafety.isSafeForDIY) {
            val updatedMessages = session.messages.toMutableList().apply {
                add(DiagnosticMessage(sender = "user", text = userAnswer))
                add(
                    DiagnosticMessage(
                        sender = "system",
                        text = "⚠️ ${answerSafety.warningTitle}\n\n${answerSafety.warningMessage}\n\n👉 Recommendation: ${answerSafety.technicianRecommendation}"
                    )
                )
            }
            return session.copy(
                technicianRecommended = true,
                technicianReason = answerSafety.warningMessage,
                messages = updatedMessages,
                safetyWarnings = session.safetyWarnings + answerSafety.mandatoryPrecautions,
                isCompleted = true,
                updatedAt = System.currentTimeMillis()
            )
        }

        val updatedMessages = session.messages.toMutableList().apply {
            add(DiagnosticMessage(sender = "user", text = userAnswer))
        }

        val parsed = executeInference(
            input = session.problemInput.copy(
                imageBase64 = additionalImageBase64 ?: session.problemInput.imageBase64
            ),
            history = updatedMessages,
            previousAnswer = userAnswer,
            settings = settings
        )

        updatedMessages.add(
            DiagnosticMessage(
                sender = "assistant",
                text = "${parsed.summary}\n\nUpdated Confidence: ${(parsed.confidence * 100).toInt()}%"
            )
        )

        if (!parsed.next_question.isNullOrBlank()) {
            updatedMessages.add(
                DiagnosticMessage(
                    sender = "assistant",
                    text = parsed.next_question,
                    isQuestion = true,
                    quickOptions = listOf("Yes", "No", "Not sure")
                )
            )
        }

        val updatedPlan = if (parsed.repair_plan.isNotEmpty()) {
            RepairPlan(
                title = "Recommended Repair Procedure",
                steps = parsed.repair_plan.mapIndexed { idx, step ->
                    RepairStep(stepNumber = idx + 1, title = "Step ${idx + 1}", description = step)
                },
                requiredTools = parsed.required_tools,
                requiredParts = parsed.required_parts,
                estimatedDifficulty = parsed.estimated_difficulty,
                estimatedTimeMinutes = parsed.estimated_time_minutes
            )
        } else session.repairPlan

        return session.copy(
            summary = parsed.summary,
            possibleCauses = if (parsed.possible_causes.isNotEmpty()) parsed.possible_causes else session.possibleCauses,
            confidence = parsed.confidence,
            nextQuestion = parsed.next_question,
            safeTest = parsed.safe_test ?: session.safeTest,
            repairPlan = updatedPlan,
            technicianRecommended = parsed.technician_recommended,
            technicianReason = parsed.technician_reason,
            safetyWarnings = (session.safetyWarnings + parsed.safety_warnings).distinct(),
            messages = updatedMessages,
            isCompleted = parsed.next_question.isNullOrBlank(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private suspend fun executeInference(
        input: ProblemInput,
        history: List<DiagnosticMessage>,
        previousAnswer: String?,
        settings: AISettings
    ): ParsedAIResponse {
        val prompt = PromptBuilder.buildDiagnosticPrompt(input, history, previousAnswer)

        val service: AIService = when (settings.provider) {
            com.example.model.AIProvider.GEMINI -> geminiClient
            com.example.model.AIProvider.OPENAI -> openAIClient
        }

        val result = service.diagnose(prompt, input.imageBase64, settings)
        return if (result.isSuccess && !result.getOrNull().isNullOrBlank()) {
            val parsed = AIResponseParser.parse(result.getOrNull()!!)
            // Second layer of deterministic safety check on AI instructions
            val aiInstructions = parsed.repair_plan.joinToString(" ") + " " + parsed.summary
            val planSafety = SafetyManager.evaluateSafety(aiInstructions)
            if (!planSafety.isSafeForDIY) {
                parsed.copy(
                    technician_recommended = true,
                    technician_reason = planSafety.warningMessage,
                    safety_warnings = parsed.safety_warnings + listOf(planSafety.warningMessage)
                )
            } else {
                parsed
            }
        } else {
            // Local heuristic fallback engine
            OfflineDiagnosticFallback.generateLocalDiagnosis(input, history, previousAnswer)
        }
    }
}
