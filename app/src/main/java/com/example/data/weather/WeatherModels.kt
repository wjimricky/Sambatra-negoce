package com.example.data.weather

import com.example.data.model.WarehouseZoneId

/**
 * Entités et modèles pour le module météo temps réel & règles de conservation Guide 2026
 */
data class WarehouseLocation(
    val id: String,
    val nameFr: String,
    val nameMg: String,
    val latitude: Double,
    val longitude: Double,
    val region: String,
    val description: String,
    val typicalRisk: String
)

data class CurrentWeatherData(
    val location: WarehouseLocation,
    val temperatureC: Double,
    val relativeHumidityPct: Double,
    val dewPointC: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val windSpeedKmh: Double,
    val conditionDescription: String,
    val recordedAt: String,
    val isLiveFromNetwork: Boolean = true
)

data class HourlyHumidityForecast(
    val hourLabel: String,
    val temperatureC: Double,
    val humidityPct: Double
)

enum class StorageRiskLevel(val labelFr: String, val labelMg: String, val severityColorHex: Long) {
    OPTIMAL("Optimal", "Tonga lafatra", 0xFF15803D), // Green
    VIGILANCE("Vigilance Humidité", "Fahamailoana", 0xFFB45309), // Amber
    DANGER_CRITIQUE("Danger Dégradation", "Loza Mitatao", 0xFFB91C1C) // Red
}

data class StorageZoneWeatherRisk(
    val zoneId: WarehouseZoneId,
    val zoneName: String,
    val mainProducts: String,
    val maxAllowedAmbientHumidityPct: Double,
    val currentAmbientHumidityPct: Double,
    val riskLevel: StorageRiskLevel,
    val degradationRisks: List<String>,
    val preventiveActions: List<String>
)

data class AerationRecommendation(
    val canOpenAerationNow: Boolean,
    val statusHeadlineFr: String,
    val reasonFr: String,
    val safeAerationWindow: String,
    val recommendedActions: List<String>
)

data class WeatherRiskAssessment(
    val weather: CurrentWeatherData,
    val hourlyForecast: List<HourlyHumidityForecast>,
    val zoneRisks: List<StorageZoneWeatherRisk>,
    val aerationRecommendation: AerationRecommendation,
    val generatedAt: String
)
