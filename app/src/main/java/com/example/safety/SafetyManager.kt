package com.example.safety

data class SafetyAuditResult(
    val isSafeForDIY: Boolean,
    val hazardType: HazardType? = null,
    val warningTitle: String = "",
    val warningMessage: String = "",
    val technicianRecommendation: String = "",
    val mandatoryPrecautions: List<String> = emptyList()
)

enum class HazardType {
    HIGH_VOLTAGE,
    SWOLLEN_LITHIUM_BATTERY,
    GAS_APPLIANCE_OR_LINE,
    MICROWAVE_CAPACITOR,
    LIVE_MAINS_ELECTRICAL,
    WATER_AND_ELECTRICITY,
    SAFETY_BYPASS
}

object SafetyManager {

    private val HIGH_VOLTAGE_KEYWORDS = listOf(
        "high voltage", "power supply capacitor", "crt", "flyback",
        "opened power supply", "internal psu", "400v", "240v live", "mains shock"
    )

    private val BATTERY_HAZARD_KEYWORDS = listOf(
        "swollen battery", "bloated battery", "punctured battery", "smoking battery",
        "expanding battery", "bent battery", "lithium fire", "smell chemical battery"
    )

    private val GAS_KEYWORDS = listOf(
        "gas leak", "natural gas", "propane", "gas odor", "smell gas", "gas pipe",
        "pilot light gas", "gas valve"
    )

    private val MICROWAVE_KEYWORDS = listOf(
        "microwave capacitor", "magnetron", "open microwave", "inside microwave",
        "high voltage diode", "microwave transformer"
    )

    private val LIVE_ELECTRICAL_KEYWORDS = listOf(
        "live wire", "breaker box", "fuse box", "sparking outlet", "bare copper 120v",
        "bare copper 230v", "shocked me", "electrocution"
    )

    private val WATER_ELECTRICAL_KEYWORDS = listOf(
        "submerged while plugged", "dropped in water while charging", "wet outlet",
        "water inside power supply"
    )

    private val BYPASS_KEYWORDS = listOf(
        "bypass fuse", "bridge thermal fuse with wire", "bypass safety switch",
        "remove interlock", "defeat ground pin"
    )

    /**
     * Deterministically audits user problem descriptions or AI generated instructions.
     * If hazardous keywords or situations are detected, blocks DIY repair and returns warnings.
     */
    fun evaluateSafety(text: String): SafetyAuditResult {
        val lower = text.lowercase()

        // 1. Gas hazard
        if (GAS_KEYWORDS.any { lower.contains(it) }) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.GAS_APPLIANCE_OR_LINE,
                warningTitle = "CRITICAL HAZARD: Flammable Gas Risk",
                warningMessage = "Suspected or reported gas issues carry extreme explosion and toxic hazard risks. NEVER attempt DIY repairs on gas lines, valves, or gas burner assemblies.",
                technicianRecommendation = "Immediately turn off the main gas shutoff valve, ventilate the area, avoid turning on lights or electrical switches, and call your local gas emergency hotline or certified gas technician.",
                mandatoryPrecautions = listOf("Evacuate area if odor is strong", "Do not create electrical sparks", "Contact certified gas professionals")
            )
        }

        // 2. Microwave high-voltage capacitor hazard
        if (MICROWAVE_KEYWORDS.any { lower.contains(it) } ||
            (lower.contains("microwave") && (lower.contains("capacitor") || lower.contains("open") || lower.contains("inside") || lower.contains("magnetron") || lower.contains("voltage")))
        ) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.MICROWAVE_CAPACITOR,
                warningTitle = "CRITICAL HAZARD: Lethal Stored Voltage",
                warningMessage = "Microwave ovens contain large high-voltage capacitors (2,000V–5,000V) and magnetrons that can retain lethal electrical charges for days even when unplugged.",
                technicianRecommendation = "Never remove the outer metal housing of a microwave oven. Service must only be conducted by a certified appliance technician with high-voltage discharge equipment.",
                mandatoryPrecautions = listOf("Unplug unit immediately", "Do not open outer chassis", "Contact qualified appliance repair service")
            )
        }

        // 3. Swollen / Damaged Lithium Battery
        if (BATTERY_HAZARD_KEYWORDS.any { lower.contains(it) }) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.SWOLLEN_LITHIUM_BATTERY,
                warningTitle = "DANGER: Lithium Battery Thermal Runaway Risk",
                warningMessage = "A swollen, bloated, or punctured lithium battery is under mechanical stress and can experience violent thermal runaway, emitting toxic fumes or catching fire.",
                technicianRecommendation = "Do not puncture, squeeze, or apply heat to the battery. Store device in a non-combustible container (e.g. metal box/sand) away from flammable items and take it to an authorized electronics recycling or certified repair depot.",
                mandatoryPrecautions = listOf("Power off and disconnect from charger immediately", "Do not pierce or press the battery", "Keep in fire-safe area")
            )
        }

        // 4. Live electrical / mains shock
        if (LIVE_ELECTRICAL_KEYWORDS.any { lower.contains(it) }) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.LIVE_MAINS_ELECTRICAL,
                warningTitle = "DANGER: High Voltage Mains Exposure",
                warningMessage = "Work involving live electrical wiring, sparking wall receptacles, or service panels poses a severe risk of electric shock, electrocution, or house fires.",
                technicianRecommendation = "Switch off the corresponding circuit breaker at your main service panel. Consult a licensed master electrician.",
                mandatoryPrecautions = listOf("Turn off circuit breaker before touching", "Do not touch exposed copper conductors", "Call licensed electrician")
            )
        }

        // 5. Water + Electricity
        if (WATER_ELECTRICAL_KEYWORDS.any { lower.contains(it) }) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.WATER_AND_ELECTRICITY,
                warningTitle = "DANGER: Water & Electrical Shock Hazard",
                warningMessage = "Operating or inspecting electrical equipment with internal moisture or standing water can cause short circuits and dangerous shocks.",
                technicianRecommendation = "Disconnect power source from breaker if safe to do so. Let the unit completely dry for at least 72 hours before professional assessment.",
                mandatoryPrecautions = listOf("Do not touch with wet hands", "Cut power from breaker if plug is submerged", "Allow complete drying")
            )
        }

        // 6. Safety bypass
        if (BYPASS_KEYWORDS.any { lower.contains(it) }) {
            return SafetyAuditResult(
                isSafeForDIY = false,
                hazardType = HazardType.SAFETY_BYPASS,
                warningTitle = "WARNING: Safety Interlock Bypass Prohibited",
                warningMessage = "Bypassing thermal fuses, interlocks, or grounding pins removes protective safeguards designed to prevent fires and catastrophic failure.",
                technicianRecommendation = "Replace faulty components with OEM or certified equivalent parts only. Never bridge or jump safety fuses.",
                mandatoryPrecautions = listOf("Only use exact-spec replacements", "Never bridge safety fuses with jumper wires")
            )
        }

        // Safe for standard low-risk DIY troubleshooting
        return SafetyAuditResult(
            isSafeForDIY = true,
            warningTitle = "Standard Safety Precaution",
            warningMessage = "Always disconnect the device from mains electrical power and remove batteries before opening access covers or performing cleaning.",
            mandatoryPrecautions = listOf(
                "Disconnect power / unplug charger before disassembly",
                "Work in a well-lit, dry workspace",
                "Keep screws and fasteners organized by size"
            )
        )
    }
}
