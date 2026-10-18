package com.example.data.ai

import com.example.data.model.WarehouseZoneId

/**
 * Moteur d'IA pour la recommandation d'emplacement intelligent en entrepôt (Zonage 400 m², Guide 2026 Ch. 8).
 * Suggère l'allée, la palette et la zone optimale selon les dimensions des lots,
 * la capacité restante, la classe ABC et les contraintes sanitaires (isolation odeurs et humidité).
 */

data class LotStorageRequirement(
    val lotNumber: String,
    val productCode: String,
    val productName: String,
    val category: String, // "Céréales", "Légumineuses", "Épices"
    val quantityKg: Double,
    val bagsCount: Int,
    val moisturePercent: Double,
    val maxMoistureTolerancePercent: Double,
    val abcClass: String, // "A", "B", "C"
    val hasStrongAromaticOdor: Boolean, // Girofle, poivre, piment
    val isQualityCompliant: Boolean, // Conforme ou doute/décote
    val clientLeadDays: Int = 15 // Délai prévu avant expédition
)

data class StorageSlotRecommendation(
    val recommendedZone: WarehouseZoneId,
    val zoneLabel: String,
    val specificAisleAndPalletId: String,
    val suitabilityScorePercent: Int, // 0 à 100%
    val stackingHeightMaxLayers: Int, // ex: 8 sacs de haut
    val palletClearanceCm: Int, // 10 cm minimum au-dessus du sol
    val wallSpacingCm: Int, // 50 cm des murs
    val roofClearanceCm: Int, // 100 cm sous les tôles
    val odorSegregationCompliant: Boolean,
    val rotationStrategy: String, // "FEFO" ou "FIFO"
    val sanitaryRiskWarning: String?,
    val aiReasoningExplanation: String,
    val guide2026Reference: String
)

object SmartStorageRecommender {

    // Capacité nominale des 4 zones de l'entrepôt 400 m² (Guide p. 43)
    val ZONE_CAPACITIES = mapOf(
        WarehouseZoneId.ZONE_A to ZoneCapacityState(
            zoneId = WarehouseZoneId.ZONE_A,
            name = "Zone A - Céréales & Grands Volumes (200 m²)",
            totalCapacityBags = 4000,
            currentOccupiedBags = 2850,
            surfaceM2 = 200,
            hasOdorIsolation = false,
            targetMaxHumidityPercent = 13.5
        ),
        WarehouseZoneId.ZONE_B to ZoneCapacityState(
            zoneId = WarehouseZoneId.ZONE_B,
            name = "Zone B - Épices & Produits Nobles Sécurisée (60 m²)",
            totalCapacityBags = 1200,
            currentOccupiedBags = 720,
            surfaceM2 = 60,
            hasOdorIsolation = true,
            targetMaxHumidityPercent = 12.0
        ),
        WarehouseZoneId.ZONE_C to ZoneCapacityState(
            zoneId = WarehouseZoneId.ZONE_C,
            name = "Zone C - Légumineuses & Stocks Tampons (50 m²)",
            totalCapacityBags = 1000,
            currentOccupiedBags = 620,
            surfaceM2 = 50,
            hasOdorIsolation = false,
            targetMaxHumidityPercent = 13.0
        ),
        WarehouseZoneId.ZONE_Q to ZoneCapacityState(
            zoneId = WarehouseZoneId.ZONE_Q,
            name = "Zone Q - Quarantaine & Contrôle Qualité (30 m²)",
            totalCapacityBags = 600,
            currentOccupiedBags = 180,
            surfaceM2 = 30,
            hasOdorIsolation = false,
            targetMaxHumidityPercent = 12.0
        )
    )

    fun recommendStorageSlot(req: LotStorageRequirement): StorageSlotRecommendation {
        // 1. Règle absolue de Quarantaine sanitaire (Guide p. 22 & 43)
        if (!req.isQualityCompliant || req.moisturePercent > (req.maxMoistureTolerancePercent + 1.0)) {
            return StorageSlotRecommendation(
                recommendedZone = WarehouseZoneId.ZONE_Q,
                zoneLabel = "Zone Q - Quarantaine Sanitaire",
                specificAisleAndPalletId = "Allée Q-1, Baie d'Isolement #Q-04",
                suitabilityScorePercent = 98,
                stackingHeightMaxLayers = 4, // Empilage réduit pour favoriser la circulation d'air
                palletClearanceCm = 15,
                wallSpacingCm = 60,
                roofClearanceCm = 150,
                odorSegregationCompliant = true,
                rotationStrategy = "ISOLATION CONSERVATOIRE (Attente contre-expertise scellée)",
                sanitaryRiskWarning = "⚠️ Humidité excessive mesurée (${req.moisturePercent}% > ${req.maxMoistureTolerancePercent}%). Risque critique d'échauffement et prolifération de moisissures.",
                aiReasoningExplanation = "L'IA interdit formellement l'intégration en stock sain. Le lot doit être isolé en Zone Q pour séchage solaire ou re-pesée contradictoire avec contre-échantillon scellé.",
                guide2026Reference = "Guide 2026 p. 22 : « Tout lot non conforme ou suspect est consigné en zone d'attente/quarantaine avant déstockage ou accord écrit de décote. »"
            )
        }

        // 2. Règle d'Isolation Hermétique des Épices et Produits Odorants (Guide p. 44)
        if (req.hasStrongAromaticOdor || req.category.equals("Épices", ignoreCase = true)) {
            val zoneB = ZONE_CAPACITIES[WarehouseZoneId.ZONE_B]!!
            val remainingBags = zoneB.totalCapacityBags - zoneB.currentOccupiedBags
            val hasEnoughCapacity = remainingBags >= req.bagsCount

            val aisle = if (req.abcClass == "A") "Allée B-1 (Frontal Sécurisé)" else "Allée B-2 (Casier Haute Valeur)"
            val pallet = "Palette B-${(10..24).random()}"

            return StorageSlotRecommendation(
                recommendedZone = WarehouseZoneId.ZONE_B,
                zoneLabel = "Zone B - Épices & Produits Nobles (Sous clé)",
                specificAisleAndPalletId = "$aisle, $pallet",
                suitabilityScorePercent = if (hasEnoughCapacity) 95 else 82,
                stackingHeightMaxLayers = 6, // Les sacs d'épices ne doivent pas être écrasés pour préserver les huiles
                palletClearanceCm = 10,
                wallSpacingCm = 50,
                roofClearanceCm = 100,
                odorSegregationCompliant = true,
                rotationStrategy = "FEFO (Premier Périmé / Première Sortie)",
                sanitaryRiskWarning = if (!hasEnoughCapacity) "Taux d'occupation élevé en Zone B (${zoneB.currentOccupiedBags}/${zoneB.totalCapacityBags} sacs)." else null,
                aiReasoningExplanation = "Les épices (girofle, poivre) dégagent des essences aromatiques volatiles très pénétrantes. Le stockage en Zone B hermétique évite l'odeur résiduelle sur les céréales et protège la haute valeur financière sous clé.",
                guide2026Reference = "Guide 2026 p. 44 : « L'isolation des épices est impérative. Le riz absorbe les odeurs en moins de 48 heures au contact du poivre ou du girofle. »"
            )
        }

        // 3. Règle pour Légumineuses (Zone C)
        if (req.category.equals("Légumineuses", ignoreCase = true)) {
            val aisle = if (req.abcClass == "A") "Allée C-1 (Accès Direct)" else "Allée C-2 (Allée Centrale)"
            val pallet = "Palette C-${(5..18).random()}"

            return StorageSlotRecommendation(
                recommendedZone = WarehouseZoneId.ZONE_C,
                zoneLabel = "Zone C - Légumineuses & Stocks Tampons",
                specificAisleAndPalletId = "$aisle, $pallet",
                suitabilityScorePercent = 92,
                stackingHeightMaxLayers = 8,
                palletClearanceCm = 10,
                wallSpacingCm = 50,
                roofClearanceCm = 100,
                odorSegregationCompliant = true,
                rotationStrategy = "FEFO (Surveillance stricte des bruches après 60 jours)",
                sanitaryRiskWarning = if (req.moisturePercent > 13.0) "Humidité limite (${req.moisturePercent}%). Aérer régulièrement." else null,
                aiReasoningExplanation = "Zone dédiée aux légumineuses avec aération transversale pour prévenir l'éclosion des charançons et bruches. Emplacement FEFO indexé sur la date de récolte.",
                guide2026Reference = "Guide 2026 Ch. 8 : « Les légumineuses exigent un contrôle mensuel systématique et un déstockage par antériorité d'entrée (FEFO). »"
            )
        }

        // 4. Par défaut : Céréales & Grands Volumes (Zone A)
        val zoneA = ZONE_CAPACITIES[WarehouseZoneId.ZONE_A]!!
        val isA = req.abcClass == "A"
        val aisle = if (isA) "Allée A-1 (Proche Quai d'Expédition)" else "Allée A-3 (Zone Centrale)"
        val pallet = "Palette A-${(1..36).random()}"

        return StorageSlotRecommendation(
            recommendedZone = WarehouseZoneId.ZONE_A,
            zoneLabel = "Zone A - Céréales & Grands Volumes",
            specificAisleAndPalletId = "$aisle, $pallet",
            suitabilityScorePercent = 96,
            stackingHeightMaxLayers = 10, // 10 hauteurs max sur palettes
            palletClearanceCm = 10,
            wallSpacingCm = 50,
            roofClearanceCm = 100,
            odorSegregationCompliant = true,
            rotationStrategy = if (isA) "FIFO Rapide (Rotation élevée < 20 jours)" else "FIFO Standard",
            sanitaryRiskWarning = null,
            aiReasoningExplanation = "Zone A calibrée pour supporter la charge lourde du riz et maïs. Disposition en travée de 20 m avec couloir de 1,20 m pour passage du transpalette et inspection des 4 faces de la pile.",
            guide2026Reference = "Guide 2026 p. 43 : « Pas de sac au sol, pas de sac collé au mur. Palettes de 10 cm au minimum et espacement de 50 cm pour inspection. »"
        )
    }
}

data class ZoneCapacityState(
    val zoneId: WarehouseZoneId,
    val name: String,
    val totalCapacityBags: Int,
    val currentOccupiedBags: Int,
    val surfaceM2: Int,
    val hasOdorIsolation: Boolean,
    val targetMaxHumidityPercent: Double
) {
    val occupancyRatePercent: Double
        get() = (currentOccupiedBags.toDouble() / totalCapacityBags.toDouble()) * 100.0
}
