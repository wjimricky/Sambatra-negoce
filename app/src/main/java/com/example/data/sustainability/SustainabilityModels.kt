package com.example.data.sustainability

/**
 * Modèles pour le calculateur d'impact carbone et durabilité (Guide 2026).
 */
data class CarbonCalculationInputs(
    val productCode: String,
    val productName: String,
    val quantityKg: Double,
    val transportDistanceKm: Double,
    val vehicleType: String, // "CAMION_10T", "CAMIONNETTE_3T5", "PICKUP_1T2"
    val vehiclePayloadUtilizationPercent: Double = 85.0, // Taux de remplissage de la benne
    val storageDurationDays: Int = 30,
    val isLocalSourcing: Boolean = true // Provenance locale (< 150 km) vs longue distance
)

data class CarbonFootprintAssessment(
    val productCode: String,
    val productName: String,
    val totalEmissionKgCo2e: Double, // Émission totale en kg équivalent CO2
    val emissionPerKgDeliveredGramsCo2e: Double, // Intensité carbone en g CO2e / kg
    val transportEmissionKgCo2e: Double,
    val storageAndDryingEmissionKgCo2e: Double,
    val packagingEmissionKgCo2e: Double,
    
    val ecoScore: EcoScoreGrade, // A (Très bas carbone), B, C, D, E
    val carbonBenchmarkGuide2026: Double, // Objectif cible max (g CO2e/kg)
    val isCompliantWith2026Target: Boolean,
    
    val carbonReductionPotentials: List<CarbonReductionRecommendation>
)

enum class EcoScoreGrade(val letter: String, val label: String, val colorHex: Long) {
    GRADE_A("A", "Très Faible Empreinte (< 40g/kg)", 0xFF15803D),
    GRADE_B("B", "Empreinte Maîtrisée (40-80g/kg)", 0xFF16A34A),
    GRADE_C("C", "Moyenne Nationale (80-140g/kg)", 0xFFD97706),
    GRADE_D("D", "Élevée (140-200g/kg)", 0xFFEA580C),
    GRADE_E("E", "Critique (> 200g/kg)", 0xFFDC2626)
}

data class CarbonReductionRecommendation(
    val title: String,
    val potentialSavingsKgCo2e: Double,
    val actionGuide2026: String
)
