package com.example.data.logistics

/**
 * Modèles pour la planification logistique et l'optimisation des trajets (Guide 2026)
 */
enum class VehicleType(val labelFr: String) {
    CAMION_10T("Camion Porteur 10 Tonnes Bâché & Ventilé"),
    CAMIONNETTE_3T5("Camionnette 3.5T Fourgonnette Protégée"),
    PICKUP_1T2("Pick-up Express 1.2T Rapide")
}

data class LogisticsVehicle(
    val id: String,
    val name: String,
    val type: VehicleType,
    val maxPayloadKg: Double,
    val maxVolumeM3: Double,
    val hasInsulatedCompartment: Boolean,
    val hasRaisedFloorPallets: Boolean,
    val hasHeavyDutyTarp: Boolean,
    val fuelCostPerKmAr: Double,
    val driverName: String
)

data class DeliveryCorridor(
    val id: String,
    val name: String,
    val routeCode: String,
    val distanceKm: Double,
    val typicalDurationHours: Double,
    val roadCondition: String, // "Goudronné bon état", "Piste dégradée", "Route de montagne sinueuse"
    val climaticRisk: String,
    val baselineRoadShrinkagePct: Double, // Freinte de transport standard en %
    val recommendedDepartureHour: String
)

data class DeliveryClientOrder(
    val id: String,
    val clientName: String,
    val destinationCity: String,
    val corridorId: String,
    val productCode: String,
    val productName: String,
    val isSpice: Boolean,
    val weightKg: Double,
    val volumeM3: Double,
    val moistureAtDeparturePct: Double,
    val isPriorityFefo: Boolean,
    val specialInstructions: String
)

data class CargoCompatibilityWarning(
    val level: WarningSeverity, // INFO, WARNING, BLOCKING
    val title: String,
    val detail: String,
    val recommendedRemedy: String
)

enum class WarningSeverity {
    INFO,
    WARNING,
    CRITICAL_BLOCKING
}

data class OptimizedTripPlan(
    val tripId: String,
    val vehicle: LogisticsVehicle,
    val corridor: DeliveryCorridor,
    val departureTime: String,
    val orders: List<DeliveryClientOrder>,
    val totalWeightKg: Double,
    val totalVolumeM3: Double,
    val weightUtilizationPct: Double,
    val volumeUtilizationPct: Double,
    val compatibilityWarnings: List<CargoCompatibilityWarning>,
    val estimatedRoadShrinkageKg: Double,
    val estimatedRoadShrinkageCostAr: Double,
    val transportCostAr: Double,
    val lossMitigationChecklist: List<LogisticsCheckItem>
)

data class LogisticsCheckItem(
    val id: String,
    val title: String,
    val guideReference: String,
    val isChecked: Boolean = false,
    val isMandatory: Boolean = true
)
