package com.example.service

import com.example.model.ParsedAIResponse
import com.example.model.PossibleCause
import com.example.model.SafeTest
import org.json.JSONArray
import org.json.JSONObject

object AIResponseParser {

    /**
     * Parses raw response string into ParsedAIResponse safely.
     * Uses Android's built-in org.json for zero-dependency robust parsing.
     */
    fun parse(rawResponse: String): ParsedAIResponse {
        val cleanJson = extractJson(rawResponse)
        if (cleanJson.isBlank()) {
            return fallbackResponse(rawResponse)
        }

        return try {
            val json = JSONObject(cleanJson)

            val summary = json.optString("summary", "Problem evaluated based on symptoms.")

            val causesList = mutableListOf<PossibleCause>()
            val causesJson = json.optJSONArray("possible_causes")
            if (causesJson != null) {
                for (i in 0 until causesJson.length()) {
                    val obj = causesJson.optJSONObject(i)
                    if (obj != null) {
                        causesList.add(
                            PossibleCause(
                                name = obj.optString("name", "Unknown Cause"),
                                likelihood = obj.optString("likelihood", "medium"),
                                reason = obj.optString("reason", "Correlates with reported symptoms.")
                            )
                        )
                    }
                }
            }

            val confidence = json.optDouble("confidence", 0.65)
            val nextQuestion = if (json.has("next_question") && !json.isNull("next_question")) {
                json.optString("next_question").takeIf { it.isNotBlank() }
            } else null

            var safeTest: SafeTest? = null
            val testObj = json.optJSONObject("safe_test")
            if (testObj != null) {
                val instructions = jsonArrayToStringList(testObj.optJSONArray("instructions"))
                val observations = jsonArrayToStringList(testObj.optJSONArray("expected_observations"))
                safeTest = SafeTest(
                    title = testObj.optString("title", "Safe Visual Inspection"),
                    instructions = if (instructions.isNotEmpty()) instructions else listOf("Carefully inspect exterior without opening while powered off."),
                    expectedObservations = observations,
                    riskLevel = testObj.optString("risk_level", "low")
                )
            }

            val repairPlan = jsonArrayToStringList(json.optJSONArray("repair_plan"))
            val requiredTools = jsonArrayToStringList(json.optJSONArray("required_tools"))
            val requiredParts = jsonArrayToStringList(json.optJSONArray("required_parts"))
            val difficulty = json.optString("estimated_difficulty", "beginner")
            val estimatedTime = json.optInt("estimated_time_minutes", 30)

            val technicianRecommended = json.optBoolean("technician_recommended", false)
            val technicianReason = if (json.has("technician_reason") && !json.isNull("technician_reason")) {
                json.optString("technician_reason")
            } else null

            val safetyWarnings = jsonArrayToStringList(json.optJSONArray("safety_warnings"))

            ParsedAIResponse(
                summary = summary,
                possible_causes = causesList,
                confidence = confidence.coerceIn(0.0, 1.0),
                next_question = nextQuestion,
                safe_test = safeTest,
                repair_plan = repairPlan,
                required_tools = requiredTools,
                required_parts = requiredParts,
                estimated_difficulty = difficulty,
                estimated_time_minutes = estimatedTime,
                technician_recommended = technicianRecommended,
                technician_reason = technicianReason,
                safety_warnings = safetyWarnings
            )
        } catch (_: Exception) {
            fallbackResponse(rawResponse)
        }
    }

    private fun extractJson(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.removePrefix("```json")
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.removePrefix("```")
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.removeSuffix("```")
        }
        trimmed = trimmed.trim()

        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }
        return trimmed
    }

    private fun jsonArrayToStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val item = array.optString(i)
            if (!item.isNullOrBlank()) {
                list.add(item)
            }
        }
        return list
    }

    private fun fallbackResponse(rawText: String): ParsedAIResponse {
        return ParsedAIResponse(
            summary = rawText.take(200).ifBlank { "Symptom analysis in progress." },
            possible_causes = listOf(
                PossibleCause(
                    name = "Wear and tear or loose connector",
                    likelihood = "medium",
                    reason = "Common failure mode for these symptoms."
                )
            ),
            confidence = 0.50,
            next_question = "Does the issue occur continuously, or only intermittently?",
            safe_test = SafeTest(
                title = "External Power & Cable Inspection",
                instructions = listOf(
                    "Disconnect device from power outlet.",
                    "Inspect cables, plugs, and ports for visible fraying or dust."
                ),
                expectedObservations = listOf("Cables intact with no visible scorch marks or kinks"),
                riskLevel = "low"
            ),
            repair_plan = listOf(
                "Verify connections with a known-working outlet or cable",
                "Clean dust accumulation from air vents and ports"
            ),
            required_tools = listOf("Flashlight", "Compressed air can"),
            required_parts = emptyList(),
            estimated_difficulty = "beginner",
            estimated_time_minutes = 20,
            technician_recommended = false,
            safety_warnings = listOf("Always disconnect power before physical inspection.")
        )
    }
}
