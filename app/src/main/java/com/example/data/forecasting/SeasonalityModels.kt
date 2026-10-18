package com.example.data.forecasting

/**
 * Modèles pour le calendrier de saisonnalité des récoltes et prévisions d'approvisionnement (Guide 2026).
 */
data class CropHarvestCalendar(
    val productCode: String,
    val productName: String,
    val mainProductionBasin: String, // ex: "Lac Alaotra (Ambatondrazaka)", "Analanjirofo (Fénérive-Est)"
    val harvestStartMonth: Int, // 1 to 12
    val harvestEndMonth: Int,
    val harvestPeakMonth: Int,
    val leanSeasonStartMonth: Int, // Début de période de soudure (pénurie/prix hauts)
    val leanSeasonEndMonth: Int,
    val seasonalPriceDipPercent: Double, // Baisse moyenne du cours à la récolte (%)
    val seasonalPriceSurgePercent: Double, // Hausse moyenne du cours en soudure (%)
    val postHarvestMoistureRisk: String // Risque d'humidité à la récolte
)

enum class ReplenishmentAlertPriority(val labelFr: String, val colorHex: Long) {
    CRITIQUE_IMMEDIAT("Urgence Immédiate (< 7 jours)", 0xFFDC2626),
    OPPORTUNITE_RECOLTE("Opportunité Achat Récolte", 0xFF15803D),
    ANTICIPATION_SOUDURE("Anticipation Soudure (30 jours)", 0xFFD97706),
    OPTIMAL("Stock & Couverture Conformes", 0xFF16A34A)
}

data class AutoReplenishmentAlert(
    val id: String,
    val productCode: String,
    val productName: String,
    val priority: ReplenishmentAlertPriority,
    val currentStockKg: Double,
    val reorderPointKg: Double,
    val safetyStockKg: Double,
    val remainingCoverageDays: Int,
    val recommendedOrderQuantityKg: Double,
    val estimatedOrderCostAr: Double,
    val seasonalContext: String,
    val strategicAction: String
)

data class SeasonalPriceTrendForecast(
    val monthLabel: String,
    val monthNumber: Int,
    val priceIndex: Double, // 1.0 = base nominale, 0.85 = récolte, 1.30 = soudure
    val forecastedPriceAr: Double,
    val isHarvestPeriod: Boolean,
    val isLeanSeason: Boolean
)
