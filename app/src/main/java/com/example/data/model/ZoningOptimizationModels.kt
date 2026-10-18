package com.example.data.model

enum class WarehouseZoneId(val code: String, val displayName: String) {
    ZONE_A("ZONE_A", "Zone A - Céréales & Grands Volumes (200 m²)"),
    ZONE_B("ZONE_B", "Zone B - Épices & Produits Nobles Sécurisée (60 m²)"),
    ZONE_C("ZONE_C", "Zone C - Légumineuses & Stocks Tampons (50 m²)"),
    ZONE_Q("ZONE_Q", "Zone Q - Quarantaine & Contrôle Qualité (30 m²)")
}

data class WarehouseZoneInfo(
    val id: WarehouseZoneId,
    val name: String,
    val surfaceM2: Int,
    val maxBagsCapacity: Int,
    val currentBagsCount: Int,
    val targetHumidityMaxPercent: Double,
    val currentTemperatureC: Double,
    val currentHumidityPercent: Double,
    val isOdorIsolated: Boolean,
    val primaryRotationRule: String, // "FIFO" ou "FEFO"
    val description: String
)

data class ZoningAnomaly(
    val id: String,
    val lotNumber: String,
    val productName: String,
    val currentZone: String,
    val expectedZone: String,
    val severity: String, // "CRITIQUE", "AVERTISSEMENT", "INFO"
    val message: String,
    val guideReference: String
)

data class DestockingPriorityItem(
    val rank: Int,
    val lotNumber: String,
    val productName: String,
    val productType: String, // "GRAIN" ou "EPICE"
    val zone: String,
    val quantityKg: Double,
    val moisturePercent: Double,
    val storageDays: Int,
    val maxRecommendedStorageDays: Int,
    val ruleApplied: String, // "FEFO (Priorité Péremption/Humidité)" ou "FIFO (Premier Entré)"
    val priorityLevel: String, // "URGENT", "ÉLEVÉ", "NORMAL"
    val reason: String
)

data class ZoningMoveRecommendation(
    val lotNumber: String,
    val productName: String,
    val fromZone: String,
    val toZone: String,
    val reason: String,
    val priority: String // "IMMÉDIAT", "PLANIFIÉ"
)

data class WarehouseZoningAudit(
    val zones: List<WarehouseZoneInfo>,
    val totalLotsAnalyzed: Int,
    val totalStockKg: Double,
    val occupancyPercent: Double,
    val complianceScorePercent: Double,
    val anomalies: List<ZoningAnomaly>,
    val recommendedMoves: List<ZoningMoveRecommendation>,
    val destockingQueue: List<DestockingPriorityItem>
)
