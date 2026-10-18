package com.example.data.logistics

import java.text.SimpleDateFormat
import java.util.*

object LogisticsOptimizer {

    val VEHICLES = listOf(
        LogisticsVehicle(
            id = "V-10T",
            name = "Camion Porteur 10 Tonnes (Mercedes Actros)",
            type = VehicleType.CAMION_10T,
            maxPayloadKg = 10000.0,
            maxVolumeM3 = 45.0,
            hasInsulatedCompartment = false,
            hasRaisedFloorPallets = true,
            hasHeavyDutyTarp = true,
            fuelCostPerKmAr = 4500.0,
            driverName = "Rabe Jean (Conducteur Émérite)"
        ),
        LogisticsVehicle(
            id = "V-3T5",
            name = "Camionnette 3.5T Isotherme (Iveco Daily)",
            type = VehicleType.CAMIONNETTE_3T5,
            maxPayloadKg = 3500.0,
            maxVolumeM3 = 18.0,
            hasInsulatedCompartment = true,
            hasRaisedFloorPallets = true,
            hasHeavyDutyTarp = true,
            fuelCostPerKmAr = 2800.0,
            driverName = "Andry Rakoto (Certifié Produits Nobles)"
        ),
        LogisticsVehicle(
            id = "V-1T2",
            name = "Pick-up Express 1.2T (Toyota Hilux)",
            type = VehicleType.PICKUP_1T2,
            maxPayloadKg = 1200.0,
            maxVolumeM3 = 6.0,
            hasInsulatedCompartment = false,
            hasRaisedFloorPallets = false,
            hasHeavyDutyTarp = true,
            fuelCostPerKmAr = 2200.0,
            driverName = "Herizo (Livreur Urbain Rapide)"
        )
    )

    val CORRIDORS = listOf(
        DeliveryCorridor(
            id = "rn2",
            name = "Axe RN2 Est (Antananarivo → Moramanga → Toamasina)",
            routeCode = "RN2",
            distanceKm = 350.0,
            typicalDurationHours = 9.5,
            roadCondition = "Route sinueuse de montagne (Falaise de Mandraka), fort dénivelé",
            climaticRisk = "Forte humidité tropicale, brouillard dense et pluies côtières",
            baselineRoadShrinkagePct = 0.75, // 0.75% freinte due à l'humidité et secousses
            recommendedDepartureHour = "04:30"
        ),
        DeliveryCorridor(
            id = "rn7",
            name = "Axe RN7 Sud (Antananarivo → Antsirabe → Ambositra → Fianarantsoa)",
            routeCode = "RN7",
            distanceKm = 410.0,
            typicalDurationHours = 8.5,
            roadCondition = "Goudronné bon état général, circulation dense",
            climaticRisk = "Fraîcheur des Hautes Terres, air sec favorable",
            baselineRoadShrinkagePct = 0.40,
            recommendedDepartureHour = "05:00"
        ),
        DeliveryCorridor(
            id = "rn4",
            name = "Axe RN4 Nord-Ouest (Antananarivo → Ankazobe → Mahajanga)",
            routeCode = "RN4",
            distanceKm = 570.0,
            typicalDurationHours = 12.0,
            roadCondition = "Longues lignes droites, forte chaleur, zones dégradées",
            climaticRisk = "Chaleur intense (>33°C), poussière et risque d'échauffement",
            baselineRoadShrinkagePct = 0.60,
            recommendedDepartureHour = "04:00"
        ),
        DeliveryCorridor(
            id = "urbain_tana",
            name = "Distribution Locale Grand Tanà (Anosibe, Analakely, Tanjombato)",
            routeCode = "URB",
            distanceKm = 35.0,
            typicalDurationHours = 3.0,
            roadCondition = "Urbain, embouteillages denses",
            climaticRisk = "Exposition aux gaz d'échappement et arrêts fréquents",
            baselineRoadShrinkagePct = 0.20,
            recommendedDepartureHour = "06:00"
        )
    )

    val INITIAL_PENDING_ORDERS = listOf(
        DeliveryClientOrder(
            id = "CMD-001",
            clientName = "Supermarché Score Ankorondrano",
            destinationCity = "Antananarivo",
            corridorId = "urbain_tana",
            productCode = "RIZ-MAK-01",
            productName = "Riz Blanc Makalioka Extra",
            isSpice = false,
            weightKg = 2500.0,
            volumeM3 = 3.6,
            moistureAtDeparturePct = 12.8,
            isPriorityFefo = false,
            specialInstructions = "Livraison quai n°2 avant 09h00"
        ),
        DeliveryClientOrder(
            id = "CMD-002",
            clientName = "Grossiste Raharison & Fils (Bazar Be)",
            destinationCity = "Toamasina",
            corridorId = "rn2",
            productCode = "RIZ-ROU-02",
            productName = "Riz Rouge de Manalalondo",
            isSpice = false,
            weightKg = 4000.0,
            volumeM3 = 5.8,
            moistureAtDeparturePct = 13.1,
            isPriorityFefo = true,
            specialInstructions = "Vérifier étanchéité bâche pluie à Mandraka"
        ),
        DeliveryClientOrder(
            id = "CMD-003",
            clientName = "Exportateur Vanille & Épices Océan Indien",
            destinationCity = "Toamasina Port",
            corridorId = "rn2",
            productCode = "POI-NOI-01",
            productName = "Poivre Noir Sauvage Voatsiperifery",
            isSpice = true,
            weightKg = 600.0,
            volumeM3 = 1.2,
            moistureAtDeparturePct = 11.4,
            isPriorityFefo = true,
            specialInstructions = "Produit de grande valeur. Compartiment isotherme obligatoire !"
        ),
        DeliveryClientOrder(
            id = "CMD-004",
            clientName = "Épicerie Fine Gourmet Tanà",
            destinationCity = "Antananarivo",
            corridorId = "urbain_tana",
            productCode = "GIR-CLO-01",
            productName = "Clous de Girofle Grade CG1",
            isSpice = true,
            weightKg = 250.0,
            volumeM3 = 0.6,
            moistureAtDeparturePct = 11.2,
            isPriorityFefo = false,
            specialInstructions = "Cartons scellés sous plastique thermo-rétractable"
        ),
        DeliveryClientOrder(
            id = "CMD-005",
            clientName = "Distributeur Centrale Sud (Andohalo)",
            destinationCity = "Fianarantsoa",
            corridorId = "rn7",
            productCode = "POI-CAP-01",
            productName = "Pois du Cap (Kabaro Calibré)",
            isSpice = false,
            weightKg = 3200.0,
            volumeM3 = 4.9,
            moistureAtDeparturePct = 12.9,
            isPriorityFefo = true,
            specialInstructions = "FEFO prioritaire - lot arrivant à 45 jours de stockage"
        ),
        DeliveryClientOrder(
            id = "CMD-006",
            clientName = "Minoterie & Aliments du Boeny",
            destinationCity = "Mahajanga",
            corridorId = "rn4",
            productCode = "MAI-GRA-01",
            productName = "Maïs Grain Jaune Trié",
            isSpice = false,
            weightKg = 5500.0,
            volumeM3 = 8.2,
            moistureAtDeparturePct = 13.0,
            isPriorityFefo = false,
            specialInstructions = "Protection anti-poussière le long de la RN4"
        )
    )

    /**
     * Planifie et optimise un trajet de livraison selon les contraintes du Guide 2026
     */
    fun buildOptimizedTrip(
        vehicleId: String,
        corridorId: String,
        selectedOrderIds: List<String>
    ): OptimizedTripPlan {
        val vehicle = VEHICLES.find { it.id == vehicleId } ?: VEHICLES[0]
        val corridor = CORRIDORS.find { it.id == corridorId } ?: CORRIDORS[0]
        val orders = INITIAL_PENDING_ORDERS.filter { selectedOrderIds.contains(it.id) }

        val totalWeight = orders.sumOf { it.weightKg }
        val totalVolume = orders.sumOf { it.volumeM3 }

        val weightUtilization = if (vehicle.maxPayloadKg > 0) (totalWeight / vehicle.maxPayloadKg) * 100.0 else 0.0
        val volumeUtilization = if (vehicle.maxVolumeM3 > 0) (totalVolume / vehicle.maxVolumeM3) * 100.0 else 0.0

        val warnings = mutableListOf<CargoCompatibilityWarning>()

        // 1. Contrôle surcharge
        if (totalWeight > vehicle.maxPayloadKg) {
            val overloadKg = totalWeight - vehicle.maxPayloadKg
            warnings.add(
                CargoCompatibilityWarning(
                    level = WarningSeverity.CRITICAL_BLOCKING,
                    title = "Surcharge Véhicule : +${overloadKg.toInt()} kg",
                    detail = "Le poids total (${totalWeight.toInt()} kg) dépasse la charge utile maximale du véhicule (${vehicle.maxPayloadKg.toInt()} kg).",
                    recommendedRemedy = "Réduire la commande ou basculer vers le Camion Porteur 10 Tonnes."
                )
            )
        }

        // 2. Règle d'or Guide 2026 : Incompatibilité Épices volatiles vs Céréales
        val hasSpices = orders.any { it.isSpice }
        val hasGrains = orders.any { !it.isSpice }

        if (hasSpices && hasGrains) {
            if (!vehicle.hasInsulatedCompartment) {
                warnings.add(
                    CargoCompatibilityWarning(
                        level = WarningSeverity.CRITICAL_BLOCKING,
                        title = "Incompatibilité Sanitaire Guide 2026 : Épices & Céréales",
                        detail = "Le Poivre Noir et Girofle émettent des composés aromatiques volatils (pipérine, eugénol). Le Riz et les Céréales absorbent avidement ces odeurs, entraînant le rejet immédiat du lot par le client destinataire.",
                        recommendedRemedy = "Utiliser la Camionnette Isotherme compartimentée, ou insérer les sacs d'épices dans une caisse hermétique scellée sous double bâche polyéthylène."
                    )
                )
            } else {
                warnings.add(
                    CargoCompatibilityWarning(
                        level = WarningSeverity.INFO,
                        title = "Séparation Compartimentée Activée",
                        detail = "La camionnette isotherme sépare physiquement les épices aromatiques des sacs de céréales neutres.",
                        recommendedRemedy = "Vérifier le joint d'étanchéité de la cloison de séparation avant départ."
                    )
                )
            }
        }

        // 3. Risque météo & humidité sur corridor
        if (corridor.routeCode == "RN2" && totalWeight > 0) {
            warnings.add(
                CargoCompatibilityWarning(
                    level = WarningSeverity.WARNING,
                    title = "Alerte Humidité RN2 (Zone Pluvieuse de Mandraka)",
                    detail = "Sur l'axe Antananarivo - Toamasina, l'humidité relative dépasse 80%. L'eau s'infiltre par dépression sous les bâches mal arrimées.",
                    recommendedRemedy = "Poser des palettes sèches >10 cm au plancher et rabattre la bâche avant sous la bâche arrière (sens inverse du vent de course)."
                )
            )
        }

        // 4. Calcul de la freinte de transport (perte au transport selon Guide 2026)
        // Freinte = Poids * (% corridor)
        val shrinkagePct = corridor.baselineRoadShrinkagePct
        val estimatedShrinkageKg = totalWeight * (shrinkagePct / 100.0)
        val avgPriceKgAr = 3200.0 // Prix moyen indicatif
        val estimatedShrinkageCostAr = estimatedShrinkageKg * avgPriceKgAr

        val transportCostAr = corridor.distanceKm * vehicle.fuelCostPerKmAr

        // Checklist de conformité départ Guide 2026
        val checklist = listOf(
            LogisticsCheckItem(
                id = "chk-1",
                title = "Contrôle d'étanchéité de la bâche (aucun trou ni déchirure)",
                guideReference = "Guide 2026 p. 48 : Protocole transport zéro infiltration"
            ),
            LogisticsCheckItem(
                id = "chk-2",
                title = "Présence de caillebotis ou palettes sèches surélevées >10 cm",
                guideReference = "Guide 2026 p. 49 : Éviter le contact direct avec le plancher humide"
            ),
            LogisticsCheckItem(
                id = "chk-3",
                title = "Pesée contradictoire et humidité mesurée au chargement (<14%)",
                guideReference = "Guide 2026 p. 50 : Règle de non-contestation de la freinte routière"
            ),
            LogisticsCheckItem(
                id = "chk-4",
                title = "Arrimage et sanglage anti-friction avec cornières de protection",
                guideReference = "Guide 2026 p. 52 : Prévention de l'éclatement des sacs de jute inférieurs"
            ),
            LogisticsCheckItem(
                id = "chk-5",
                title = "Respect de la fenêtre horaire fraîche de départ (${corridor.recommendedDepartureHour})",
                guideReference = "Guide 2026 p. 53 : Limiter l'échauffement thermique sous bâche"
            )
        )

        val tripId = "TRIP-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())}"

        return OptimizedTripPlan(
            tripId = tripId,
            vehicle = vehicle,
            corridor = corridor,
            departureTime = corridor.recommendedDepartureHour,
            orders = orders,
            totalWeightKg = totalWeight,
            totalVolumeM3 = totalVolume,
            weightUtilizationPct = Math.round(weightUtilization * 10.0) / 10.0,
            volumeUtilizationPct = Math.round(volumeUtilization * 10.0) / 10.0,
            compatibilityWarnings = warnings,
            estimatedRoadShrinkageKg = Math.round(estimatedShrinkageKg * 10.0) / 10.0,
            estimatedRoadShrinkageCostAr = estimatedShrinkageCostAr,
            transportCostAr = transportCostAr,
            lossMitigationChecklist = checklist
        )
    }
}
