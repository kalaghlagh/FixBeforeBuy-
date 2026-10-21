package com.example

import com.example.model.CostComparison
import com.example.model.ProblemInput
import com.example.model.ProductCategory
import com.example.safety.HazardType
import com.example.safety.SafetyManager
import com.example.service.AIResponseParser
import com.example.service.OfflineDiagnosticFallback
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FixBeforeBuyUnitTest {

    @Test
    fun testCostCalculation_repairFavorable() {
        val cost = CostComparison(
            partsCost = 45.0,
            laborCost = 25.0,
            extraRepairCost = 10.0,
            replacementCost = 350.0,
            extraPurchaseCost = 30.0,
            repairUsefulLifeYears = 2.0,
            replaceUsefulLifeYears = 5.0
        )

        assertEquals(80.0, cost.totalRepairCost, 0.001)
        assertEquals(380.0, cost.totalReplacementCost, 0.001)
        assertEquals(300.0, cost.immediateSavings, 0.001)
        assertTrue(cost.isRepairEconomical)
        assertEquals(40.0, cost.annualCostRepair, 0.001)
        assertEquals(76.0, cost.annualCostReplace, 0.001)
    }

    @Test
    fun testCostCalculation_replacementFavorable() {
        val cost = CostComparison(
            partsCost = 180.0,
            laborCost = 90.0,
            extraRepairCost = 30.0,
            replacementCost = 200.0,
            extraPurchaseCost = 10.0
        )

        assertEquals(300.0, cost.totalRepairCost, 0.001)
        assertEquals(210.0, cost.totalReplacementCost, 0.001)
        assertEquals(-90.0, cost.immediateSavings, 0.001)
        assertFalse(cost.isRepairEconomical)
    }

    @Test
    fun testSafetyManager_microwaveCapacitorHazard() {
        val result = SafetyManager.evaluateSafety("I want to open my microwave to check the high voltage capacitor")
        assertFalse(result.isSafeForDIY)
        assertEquals(HazardType.MICROWAVE_CAPACITOR, result.hazardType)
        assertTrue(result.warningTitle.contains("CRITICAL HAZARD"))
    }

    @Test
    fun testSafetyManager_swollenBatteryHazard() {
        val result = SafetyManager.evaluateSafety("My tablet back cover is popping open from a swollen battery")
        assertFalse(result.isSafeForDIY)
        assertEquals(HazardType.SWOLLEN_LITHIUM_BATTERY, result.hazardType)
        assertTrue(result.warningMessage.contains("thermal runaway"))
    }

    @Test
    fun testSafetyManager_gasLeakHazard() {
        val result = SafetyManager.evaluateSafety("I smell a natural gas leak near the stove valve")
        assertFalse(result.isSafeForDIY)
        assertEquals(HazardType.GAS_APPLIANCE_OR_LINE, result.hazardType)
    }

    @Test
    fun testSafetyManager_safeProblem() {
        val result = SafetyManager.evaluateSafety("My desktop computer keyboard keys are sticky with dust")
        assertTrue(result.isSafeForDIY)
        assertNull(result.hazardType)
        assertTrue(result.mandatoryPrecautions.isNotEmpty())
    }

    @Test
    fun testAIResponseParser_validJson() {
        val json = """
        {
            "summary": "Laptop fan bearing friction",
            "possible_causes": [
                {
                    "name": "Dust obstruction",
                    "likelihood": "high",
                    "reason": "Air vents blocked"
                }
            ],
            "confidence": 0.85,
            "next_question": "Does it spin during boot?",
            "safe_test": {
                "title": "Airflow test",
                "instructions": ["Feel vent air"],
                "expected_observations": ["Warm air exiting"],
                "risk_level": "low"
            },
            "repair_plan": ["Clean vents", "Replace thermal paste"],
            "required_tools": ["Screwdriver", "Compressed air"],
            "required_parts": ["Thermal paste"],
            "estimated_difficulty": "beginner",
            "estimated_time_minutes": 25,
            "technician_recommended": false,
            "safety_warnings": ["Unplug before opening"]
        }
        """.trimIndent()

        val parsed = AIResponseParser.parse(json)
        assertEquals("Laptop fan bearing friction", parsed.summary)
        assertEquals(1, parsed.possible_causes.size)
        assertEquals("Dust obstruction", parsed.possible_causes[0].name)
        assertEquals(0.85, parsed.confidence, 0.001)
        assertEquals("Does it spin during boot?", parsed.next_question)
        assertNotNull(parsed.safe_test)
        assertEquals("Airflow test", parsed.safe_test?.title)
        assertEquals(2, parsed.repair_plan.size)
        assertFalse(parsed.technician_recommended)
    }

    @Test
    fun testAIResponseParser_markdownWrappedJson() {
        val wrapped = """
        ```json
        {
            "summary": "Washing machine pump lint filter blocked",
            "possible_causes": [],
            "confidence": 0.70
        }
        ```
        """.trimIndent()

        val parsed = AIResponseParser.parse(wrapped)
        assertEquals("Washing machine pump lint filter blocked", parsed.summary)
        assertEquals(0.70, parsed.confidence, 0.001)
    }

    @Test
    fun testAIResponseParser_malformedJsonNeverCrashes() {
        val malformed = "This is not JSON at all, maybe network error: 504 Gateway Timeout"
        val parsed = AIResponseParser.parse(malformed)
        assertNotNull(parsed)
        assertTrue(parsed.summary.isNotBlank())
        assertTrue(parsed.possible_causes.isNotEmpty())
    }

    @Test
    fun testOfflineDiagnosticFallback_allCategories() {
        val appliances = OfflineDiagnosticFallback.generateLocalDiagnosis(
            ProblemInput(category = ProductCategory.APPLIANCES, description = "Water won't drain"),
            emptyList()
        )
        assertNotNull(appliances.safe_test)
        assertTrue(appliances.possible_causes.isNotEmpty())

        val computers = OfflineDiagnosticFallback.generateLocalDiagnosis(
            ProblemInput(category = ProductCategory.COMPUTERS, description = "Loud buzzing fan and overheating"),
            emptyList()
        )
        assertNotNull(computers.repair_plan)
        assertTrue(computers.required_tools.isNotEmpty())

        val accessories = OfflineDiagnosticFallback.generateLocalDiagnosis(
            ProblemInput(category = ProductCategory.ACCESSORIES, description = "Mouse double clicking"),
            emptyList()
        )
        assertTrue(accessories.possible_causes.isNotEmpty())

        val everyday = OfflineDiagnosticFallback.generateLocalDiagnosis(
            ProblemInput(category = ProductCategory.EVERYDAY, description = "Vacuum cleaner lost suction"),
            emptyList()
        )
        assertTrue(everyday.possible_causes.isNotEmpty())
    }

    @Test
    fun testScreenNavigation_itemsNotNull() {
        val items = com.example.ui.navigation.Screen.bottomNavItems
        assertEquals(4, items.size)
        items.forEach { screen ->
            assertNotNull("Screen in bottomNavItems must not be null", screen)
            assertTrue("Route must not be empty", screen.route.isNotBlank())
            assertTrue("Title must not be empty", screen.title.isNotBlank())
        }
    }
}
