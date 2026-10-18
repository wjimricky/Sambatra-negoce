package com.example.data.sustainability

/**
 * Calculateur d'impact carbone basé sur les distances de transport, types de véhicules
 * et caractéristiques agronomiques des grains et épices (Guide de Référence 2026, Durabilité).
 */
object CarbonFootprintCalculator {

    // Facteurs d'émission transport routier (kg CO2e par tonne.km)
    private const val FACTOR_CAMION_10T = 0.140
    private const val FACTOR_CAMIONNETTE_3T5 = 0.280
    private const val FACTOR_PICKUP_1T2 = 0.520

    // Empreinte stockage & séchage par kg de produit
    private fun getStorageEmissionFactor(productCode: String): Double {
        return when {
            productCode.startsWith("RIZ") -> 0.035 // Céréale nécessitant aération mécanique
            productCode.startsWith("MAI") -> 0.030
            productCode.startsWith("HAR") || productCode.startsWith("PDC") -> 0.015 // Légumineuses fixatrices d'azote
            productCode.startsWith("GIR") || productCode.startsWith("POI") -> 0.020 // Épices séchées au soleil
            else -> 0.025
        }
    }

    // Benchmark Guide 2026 (cible max en g CO2e / kg livré)
    private fun getTargetBenchmark(productCode: String): Double {
        return when {
            productCode.startsWith("RIZ") -> 95.0
            productCode.startsWith("HAR") || productCode.startsWith("PDC") -> 65.0
            productCode.startsWith("GIR") || productCode.startsWith("POI") -> 120.0
            else -> 90.0
        }
    }

    fun calculateFootprint(inputs: CarbonCalculationInputs): CarbonFootprintAssessment {
        val qtyKg = inputs.quantityKg
        val qtyTons = qtyKg / 1000.0

        // 1. Émissions Transport Routier
        val vehicleFactor = when (inputs.vehicleType) {
            "CAMION_10T" -> FACTOR_CAMION_10T
            "CAMIONNETTE_3T5" -> FACTOR_CAMIONNETTE_3T5
            "PICKUP_1T2" -> FACTOR_PICKUP_1T2
            else -> FACTOR_CAMION_10T
        }

        // Pénalité si le camion voyage à vide ou sous-chargé
        val loadFactor = if (inputs.vehiclePayloadUtilizationPercent > 0) {
            (100.0 / inputs.vehiclePayloadUtilizationPercent).coerceIn(1.0, 2.0)
        } else 1.0

        val transportEmissionKg = qtyTons * inputs.transportDistanceKm * vehicleFactor * loadFactor

        // 2. Émissions Stockage & Aération (Guide p. 48)
        val storageRatePerMonth = getStorageEmissionFactor(inputs.productCode)
        val storageDurationMonths = inputs.storageDurationDays / 30.0
        val storageEmissionKg = qtyKg * storageRatePerMonth * (0.8 + 0.2 * storageDurationMonths)

        // 3. Émissions Conditionnement / Emballage
        val packagingEmissionKg = qtyKg * 0.008

        val totalEmissionKg = transportEmissionKg + storageEmissionKg + packagingEmissionKg

        // Intensité carbone par kilogramme livré en grammes de CO2e
        val intensityGramsPerKg = if (qtyKg > 0) (totalEmissionKg / qtyKg) * 1000.0 else 0.0

        // Eco-Score Grade
        val grade = when {
            intensityGramsPerKg < 40.0 -> EcoScoreGrade.GRADE_A
            intensityGramsPerKg < 80.0 -> EcoScoreGrade.GRADE_B
            intensityGramsPerKg < 140.0 -> EcoScoreGrade.GRADE_C
            intensityGramsPerKg < 200.0 -> EcoScoreGrade.GRADE_D
            else -> EcoScoreGrade.GRADE_E
        }

        val benchmark = getTargetBenchmark(inputs.productCode)
        val isCompliant = intensityGramsPerKg <= benchmark

        // Pistes d'optimisation carbone concrètes Guide 2026
        val recommendations = mutableListOf<CarbonReductionRecommendation>()

        if (inputs.vehicleType == "PICKUP_1T2" && qtyKg >= 2000.0) {
            val potentialSavings = (FACTOR_PICKUP_1T2 - FACTOR_CAMIONNETTE_3T5) * qtyTons * inputs.transportDistanceKm
            recommendations.add(
                CarbonReductionRecommendation(
                    title = "Massification en Camionnette 3.5T ou Porteur 10T",
                    potentialSavingsKgCo2e = Math.round(potentialSavings * 10.0) / 10.0,
                    actionGuide2026 = "Remplacer les navettes fragmentées par un seul transport groupé (-45% de CO2 sur l'axe)."
                )
            )
        }

        if (inputs.vehiclePayloadUtilizationPercent < 80.0) {
            val emptyMilesSavings = transportEmissionKg * 0.20
            recommendations.add(
                CarbonReductionRecommendation(
                    title = "Optimisation du Remplissage & Fret Retour",
                    potentialSavingsKgCo2e = Math.round(emptyMilesSavings * 10.0) / 10.0,
                    actionGuide2026 = "Combiner la livraison avec un retour de collecte de paddy ou d'épices pour éliminer les kilomètres à vide."
                )
            )
        }

        if (inputs.storageDurationDays > 60) {
            val holdingSavings = storageEmissionKg * 0.30
            recommendations.add(
                CarbonReductionRecommendation(
                    title = "Accélération de la Rotation FEFO",
                    potentialSavingsKgCo2e = Math.round(holdingSavings * 10.0) / 10.0,
                    actionGuide2026 = "Écouler les lots avant 45 jours de stockage pour réduire la ventilation électrique forcée."
                )
            )
        }

        return CarbonFootprintAssessment(
            productCode = inputs.productCode,
            productName = inputs.productName,
            totalEmissionKgCo2e = Math.round(totalEmissionKg * 10.0) / 10.0,
            emissionPerKgDeliveredGramsCo2e = Math.round(intensityGramsPerKg * 10.0) / 10.0,
            transportEmissionKgCo2e = Math.round(transportEmissionKg * 10.0) / 10.0,
            storageAndDryingEmissionKgCo2e = Math.round(storageEmissionKg * 10.0) / 10.0,
            packagingEmissionKgCo2e = Math.round(packagingEmissionKg * 10.0) / 10.0,
            ecoScore = grade,
            carbonBenchmarkGuide2026 = benchmark,
            isCompliantWith2026Target = isCompliant,
            carbonReductionPotentials = recommendations
        )
    }
}
