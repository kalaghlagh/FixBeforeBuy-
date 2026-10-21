package com.example.service

import com.example.model.DiagnosticMessage
import com.example.model.ParsedAIResponse
import com.example.model.PossibleCause
import com.example.model.ProblemInput
import com.example.model.ProductCategory
import com.example.model.SafeTest

object OfflineDiagnosticFallback {

    fun generateLocalDiagnosis(
        input: ProblemInput,
        history: List<DiagnosticMessage>,
        previousAnswer: String? = null
    ): ParsedAIResponse {
        val desc = input.description.lowercase()
        val stepCount = history.count { it.isQuestion }

        return when (input.category) {
            ProductCategory.APPLIANCES -> diagnoseAppliances(desc, stepCount, previousAnswer)
            ProductCategory.COMPUTERS -> diagnoseComputers(desc, stepCount, previousAnswer)
            ProductCategory.ACCESSORIES -> diagnoseAccessories(desc, stepCount, previousAnswer)
            ProductCategory.EVERYDAY -> diagnoseEveryday(desc, stepCount, previousAnswer)
        }
    }

    private fun diagnoseAppliances(desc: String, stepCount: Int, answer: String?): ParsedAIResponse {
        val isDrain = desc.contains("drain") || desc.contains("pump") || desc.contains("water")
        val isNoise = desc.contains("noise") || desc.contains("rattle") || desc.contains("vibrat")

        if (stepCount == 0) {
            return ParsedAIResponse(
                summary = "Initial inspection of appliance symptoms (${if (isDrain) "Drainage/Pump issue" else "Mechanical/Electrical symptom"}).",
                possible_causes = listOf(
                    PossibleCause("Clogged debris filter or impellor obstruction", "high", "Most common cause of appliance drainage and circulation issues"),
                    PossibleCause("Worn drive belt or motor bearings", "medium", "Often causes grinding or loud mechanical vibration"),
                    PossibleCause("Faulty solenoid inlet or pressure switch", "low", "Affects water cycle sequencing")
                ),
                confidence = 0.65,
                next_question = "Do you hear an active humming sound from the motor/pump, or is it completely silent?",
                safe_test = SafeTest(
                    title = "External Drain Filter Inspection",
                    instructions = listOf(
                        "Disconnect appliance from AC wall outlet.",
                        "Place an absorbent towel under the bottom front access hatch.",
                        "Unscrew the manual coin/debris trap filter slowly and check for coins, lint, or foreign objects."
                    ),
                    expectedObservations = listOf("Clear impellor chamber that spins freely with a finger"),
                    riskLevel = "low"
                ),
                repair_plan = listOf(
                    "Remove and rinse debris trap under warm water",
                    "Check drain hose for kinks or crimps behind the unit",
                    "Re-seat rubber gasket and tighten filter cap securely"
                ),
                required_tools = listOf("Bucket or shallow pan", "Towels", "Flashlight"),
                required_parts = listOf("Replacement filter seal (if warped)"),
                estimated_difficulty = "beginner",
                estimated_time_minutes = 25,
                technician_recommended = false,
                safety_warnings = listOf("Disconnect electrical plug and shut off water supply valves before servicing.")
            )
        } else {
            val wasHumming = answer?.lowercase()?.contains("hum") == true || answer?.lowercase()?.contains("yes") == true
            return ParsedAIResponse(
                summary = "Detailed diagnosis: ${if (wasHumming) "Motor is receiving electrical power but experiencing mechanical jamming." else "Electrical circuit open or safety interlock activated."}",
                possible_causes = listOf(
                    PossibleCause(
                        if (wasHumming) "Foreign object jamming pump impellor blades" else "Blown thermal fuse or faulty door latch switch",
                        "high",
                        if (wasHumming) "Humming indicates voltage is present at motor coils" else "Silence indicates open safety circuit"
                    ),
                    PossibleCause("Failed motor run capacitor", "medium", "Prevents motor from generating starting torque")
                ),
                confidence = 0.85,
                next_question = null, // Diagnosis reached
                safe_test = SafeTest(
                    title = "Manual Shaft Rotation Test",
                    instructions = listOf(
                        "Ensure unit remains disconnected from power.",
                        "Use a pencil or insulated dowel to gently spin the plastic impellor blades.",
                        "Verify whether resistance feels smooth or locked."
                    ),
                    expectedObservations = listOf("Smooth 360-degree rotation without clicking or resistance"),
                    riskLevel = "low"
                ),
                repair_plan = listOf(
                    "Disconnect unit from power completely",
                    "Extract obstructing debris from pump housing",
                    "If pump impellor is broken off shaft, replace pump assembly",
                    "Run a short rinse test cycle to verify water evacuation"
                ),
                required_tools = listOf("Pliers", "Screwdriver set", "Multimeter (continuity test)"),
                required_parts = listOf("OEM Drain pump assembly (approx $25-$45)"),
                estimated_difficulty = "intermediate",
                estimated_time_minutes = 45,
                technician_recommended = false,
                safety_warnings = listOf("Double check power is disconnected. Clean up any spilled water before restoring electricity.")
            )
        }
    }

    private fun diagnoseComputers(desc: String, stepCount: Int, answer: String?): ParsedAIResponse {
        val isFanOrHeat = desc.contains("fan") || desc.contains("heat") || desc.contains("hot") || desc.contains("buzz")
        val isPowerOrBoot = desc.contains("black") || desc.contains("screen") || desc.contains("power") || desc.contains("boot")

        if (stepCount == 0) {
            return ParsedAIResponse(
                summary = "Evaluating computer symptoms: ${if (isFanOrHeat) "Thermal throttling and cooling subsystem" else "POST / Display initialization failure"}.",
                possible_causes = listOf(
                    PossibleCause(
                        if (isFanOrHeat) "Dust accumulation inside copper heatsink fins" else "RAM module loose or oxidized contacts",
                        "high",
                        if (isFanOrHeat) "Restricted airflow forces fans to spin at 100% duty cycle" else "Common cause of black screen with power LED on"
                    ),
                    PossibleCause("Dried-out thermal interface compound", "medium", "Degraded heat transfer from CPU die to heatpipe"),
                    PossibleCause("Corrupted BIOS/UEFI display handshake", "low", "Can be resolved by hard RTC CMOS reset")
                ),
                confidence = 0.70,
                next_question = if (isFanOrHeat) "Does the laptop shut down abruptly under gaming or heavy load?" else "Do keyboard caps lock lights or fan spin when power button is pressed?",
                safe_test = SafeTest(
                    title = if (isFanOrHeat) "Exterior Vent Airflow Check" else "Hard Reset and External Display Test",
                    instructions = listOf(
                        "Shut down computer and unplug AC charger.",
                        if (isFanOrHeat) "Feel exhaust vents while running a video; check if air feels warm or barely exiting." else "Connect HDMI cable to an external TV/monitor to test video signal."
                    ),
                    expectedObservations = listOf(
                        if (isFanOrHeat) "Strong, unobstructed warm airflow exiting vents" else "Desktop appears on secondary monitor"
                    ),
                    riskLevel = "low"
                ),
                repair_plan = listOf(
                    "Disconnect laptop battery before opening chassis",
                    "Blow out cooling fan and radiator fins with compressed air",
                    "Clean old thermal paste with 99% isopropyl alcohol and apply fresh paste"
                ),
                required_tools = listOf("Precision screwdriver (#00 Phillips / Torx)", "Plastic pry spudger", "99% Isopropyl alcohol", "Compressed air"),
                required_parts = listOf("Quality thermal paste (e.g. Arctic MX-4, $8)"),
                estimated_difficulty = "beginner",
                estimated_time_minutes = 30,
                technician_recommended = false,
                safety_warnings = listOf("Disconnect internal battery connector before touching any motherboard component. Ground yourself to prevent ESD.")
            )
        } else {
            return ParsedAIResponse(
                summary = "Confirmed diagnosis based on response symptoms.",
                possible_causes = listOf(
                    PossibleCause("Thermal dissipation bottleneck", "high", "Clear evidence of thermal resistance between chip and radiator"),
                    PossibleCause("Worn hydraulic bearing inside centrifugal fan", "medium", "Creates audible whine or high RPM oscillation")
                ),
                confidence = 0.90,
                next_question = null,
                safe_test = null,
                repair_plan = listOf(
                    "Back up critical personal files to cloud or USB drive",
                    "Remove bottom cover screws and release plastic clips",
                    "Disconnect internal battery immediately",
                    "Unscrew cooling assembly in reverse numerical order (4-3-2-1)",
                    "Wipe CPU/GPU dies clean with isopropyl alcohol until shiny",
                    "Apply pea-sized amount of thermal paste and reassemble evenly"
                ),
                required_tools = listOf("PH00 screwdriver", "Thermal paste", "Alcohol wipes", "Anti-static wrist strap"),
                required_parts = listOf("High-conductivity thermal paste ($7-$10)"),
                estimated_difficulty = "intermediate",
                estimated_time_minutes = 35,
                technician_recommended = false,
                safety_warnings = listOf("Never use conductive liquid metal paste on aluminum heatsinks. Do not over-tighten heatsink tension screws.")
            )
        }
    }

    private fun diagnoseAccessories(desc: String, stepCount: Int, answer: String?): ParsedAIResponse {
        return ParsedAIResponse(
            summary = "Peripheral troubleshooting: Signal integrity and switch contacts.",
            possible_causes = listOf(
                PossibleCause("Dust or oxidation inside microswitch contacts", "high", "Causes chatter, double clicks, or missed keystrokes"),
                PossibleCause("2.4GHz RF interference or USB 3.0 port shielding noise", "medium", "Causes erratic wireless cursor stuttering"),
                PossibleCause("Cable strain relief fatigue at bend point", "low", "Intermittent disconnection when cord moves")
            ),
            confidence = 0.80,
            next_question = if (stepCount == 0) "Does the issue persist when tested on another computer or with a different USB cable?" else null,
            safe_test = SafeTest(
                title = "Cross-Port and Cross-Device Isolation",
                instructions = listOf(
                    "Plug device directly into motherboard USB rear port (avoid unpowered hubs).",
                    "Test with wireless dongle using extension cable to place it within 30cm of device."
                ),
                expectedObservations = listOf("Immediate recovery of smooth tracking or click registration"),
                riskLevel = "low"
            ),
            repair_plan = listOf(
                "Clean optical sensor lens with a dry cotton swab",
                "Apply contact cleaner (DeoxIT or electronics isopropyl) into switch plunger",
                "Check for firmware updates on manufacturer utility"
            ),
            required_tools = listOf("Cotton swabs", "Precision screwdriver", "Contact cleaner"),
            required_parts = listOf("Replacement microswitches (Omron/Kailh, ~$3 for pack of 4)"),
            estimated_difficulty = "beginner",
            estimated_time_minutes = 20,
            technician_recommended = false,
            safety_warnings = listOf("Always unplug USB cable or remove AA/AAA batteries before servicing.")
        )
    }

    private fun diagnoseEveryday(desc: String, stepCount: Int, answer: String?): ParsedAIResponse {
        return ParsedAIResponse(
            summary = "General product troubleshooting: Mechanical alignment and electrical contacts.",
            possible_causes = listOf(
                PossibleCause("Clogged filter, intake restriction, or stuck drive gear", "high", "Physical restrictions account for over 60% of small motor failures"),
                PossibleCause("Loose electrical terminal or corroded battery clip", "medium", "Intermittent power delivery"),
                PossibleCause("Worn carbon brushes inside electric motor", "low", "Gradual loss of motor torque")
            ),
            confidence = 0.75,
            next_question = if (stepCount == 0) "Is there any burning smell or visible physical damage on the housing?" else null,
            safe_test = SafeTest(
                title = "Clean and Visual Obstruction Check",
                instructions = listOf(
                    "Power down and remove power cord/batteries.",
                    "Inspect all rotating parts, rollers, and intake paths for tangled hair, threads, or debris."
                ),
                expectedObservations = listOf("Free movement of mechanical linkages without grinding"),
                riskLevel = "low"
            ),
            repair_plan = listOf(
                "Clean out all dust filters and lint traps",
                "Apply silicone lubricant spray to mechanical plastic gears (avoid electrical contacts)",
                "Tighten any loose casing screws"
            ),
            required_tools = listOf("Scissors / seam ripper", "Silicone lubricant", "Standard screwdriver set"),
            required_parts = listOf("Replacement filters or belts (if worn)"),
            estimated_difficulty = "beginner",
            estimated_time_minutes = 25,
            technician_recommended = false,
            safety_warnings = listOf("Disconnect all power sources. Wear eye protection if dealing with spring-loaded parts.")
        )
    }
}
