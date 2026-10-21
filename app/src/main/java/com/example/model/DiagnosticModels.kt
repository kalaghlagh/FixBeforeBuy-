package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ProblemInput(
    val category: ProductCategory = ProductCategory.APPLIANCES,
    val description: String = "",
    val brand: String = "",
    val model: String = "",
    val ageYears: String = "",
    val warrantyStatus: String = "Expired",
    val imageUri: String? = null,
    val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class PossibleCause(
    val name: String,
    val likelihood: String, // "high", "medium", "low"
    val reason: String
)

@JsonClass(generateAdapter = true)
data class SafeTest(
    val title: String = "",
    val instructions: List<String> = emptyList(),
    val expectedObservations: List<String> = emptyList(),
    val riskLevel: String = "low" // "low", "medium", "high"
)

@JsonClass(generateAdapter = true)
data class RepairStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class RepairPlan(
    val title: String = "",
    val steps: List<RepairStep> = emptyList(),
    val requiredTools: List<String> = emptyList(),
    val requiredParts: List<String> = emptyList(),
    val estimatedDifficulty: String = "beginner", // "beginner", "intermediate", "advanced", "professional_only"
    val estimatedTimeMinutes: Int = 30
)

@JsonClass(generateAdapter = true)
data class CostComparison(
    val partsCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val extraRepairCost: Double = 0.0,
    val replacementCost: Double = 0.0,
    val extraPurchaseCost: Double = 0.0,
    val repairUsefulLifeYears: Double = 2.0,
    val replaceUsefulLifeYears: Double = 5.0
) {
    val totalRepairCost: Double get() = partsCost + laborCost + extraRepairCost
    val totalReplacementCost: Double get() = replacementCost + extraPurchaseCost
    val immediateSavings: Double get() = totalReplacementCost - totalRepairCost
    val isRepairEconomical: Boolean get() = immediateSavings > 0

    val annualCostRepair: Double get() =
        if (repairUsefulLifeYears > 0) totalRepairCost / repairUsefulLifeYears else totalRepairCost
    val annualCostReplace: Double get() =
        if (replaceUsefulLifeYears > 0) totalReplacementCost / replaceUsefulLifeYears else totalReplacementCost
}

@JsonClass(generateAdapter = true)
data class DiagnosticMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "user", "assistant", "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickOptions: List<String> = emptyList(),
    val isQuestion: Boolean = false
)

@JsonClass(generateAdapter = true)
data class DiagnosticSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: ProductCategory,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val problemInput: ProblemInput,
    val summary: String = "",
    val possibleCauses: List<PossibleCause> = emptyList(),
    val confidence: Double = 0.0,
    val nextQuestion: String? = null,
    val safeTest: SafeTest? = null,
    val repairPlan: RepairPlan? = null,
    val technicianRecommended: Boolean = false,
    val technicianReason: String? = null,
    val safetyWarnings: List<String> = emptyList(),
    val messages: List<DiagnosticMessage> = emptyList(),
    val costComparison: CostComparison = CostComparison(),
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class ParsedAIResponse(
    val summary: String = "",
    val possible_causes: List<PossibleCause> = emptyList(),
    val confidence: Double = 0.0,
    val next_question: String? = null,
    val safe_test: SafeTest? = null,
    val repair_plan: List<String> = emptyList(),
    val required_tools: List<String> = emptyList(),
    val required_parts: List<String> = emptyList(),
    val estimated_difficulty: String = "beginner",
    val estimated_time_minutes: Int = 30,
    val technician_recommended: Boolean = false,
    val technician_reason: String? = null,
    val safety_warnings: List<String> = emptyList()
)
