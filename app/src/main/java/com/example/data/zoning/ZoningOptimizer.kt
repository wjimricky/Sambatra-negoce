package com.example.data.zoning

import com.example.data.model.*

object ZoningOptimizer {

    /**
     * Algorithme d'optimisation du zonage et de déstockage FIFO/FEFO
     * selon les règles de conservation des grains et épices du Guide 2026 (Chapitres 5 & 6, p. 25-32).
     */
    fun performZoningAndDestockingAudit(
        lots: List<BatchLot>,
        products: List<Product>
    ): WarehouseZoningAudit {
        val productMap = products.associateBy { it.id }
        val productByCode = products.associateBy { it.code }

        val anomalies = mutableListOf<ZoningAnomaly>()
        val moves = mutableListOf<ZoningMoveRecommendation>()
        val destockingList = mutableListOf<DestockingPriorityItem>()

        var totalStockKg = 0.0
        var compliantLotsCount = 0

        // Définition des 4 zones officielles Sambatra Négoce
        val zoneA = WarehouseZoneInfo(
            id = WarehouseZoneId.ZONE_A,
            name = "Zone A - Céréales & Grands Volumes",
            surfaceM2 = 200,
            maxBagsCapacity = 800,
            currentBagsCount = 0,
            targetHumidityMaxPercent = 14.0,
            currentTemperatureC = 22.5,
            currentHumidityPercent = 13.2,
            isOdorIsolated = false,
            primaryRotationRule = "FIFO (Premier Entré, Premier Sorti)",
            description = "Palettes bois surélevées de 15 cm, allée centrale de 1,5 m, écart 50 cm des murs. Empilage max 10 sacs."
        )

        val zoneB = WarehouseZoneInfo(
            id = WarehouseZoneId.ZONE_B,
            name = "Zone B - Épices & Produits Nobles Sécurisée",
            surfaceM2 = 60,
            maxBagsCapacity = 200,
            currentBagsCount = 0,
            targetHumidityMaxPercent = 12.5,
            currentTemperatureC = 20.0,
            currentHumidityPercent = 11.8,
            isOdorIsolated = true,
            primaryRotationRule = "FEFO (Premier Périmé / Risque Humidité, Premier Sorti)",
            description = "Local clos, sec, obscurité contrôlée pour préserver la pipérine et l'eugénol. Isolation totale des odeurs étrangères."
        )

        val zoneC = WarehouseZoneInfo(
            id = WarehouseZoneId.ZONE_C,
            name = "Zone C - Légumineuses & Céréales Secondaires",
            surfaceM2 = 50,
            maxBagsCapacity = 300,
            currentBagsCount = 0,
            targetHumidityMaxPercent = 13.5,
            currentTemperatureC = 23.0,
            currentHumidityPercent = 13.4,
            isOdorIsolated = false,
            primaryRotationRule = "FIFO / FEFO Hybride (Surveillance Bruches > 45 jours)",
            description = "Ventilation dynamique, contrôle piégeage insectes (bruches du haricot et charançons du maïs)."
        )

        val zoneQ = WarehouseZoneInfo(
            id = WarehouseZoneId.ZONE_Q,
            name = "Zone Q - Quarantaine & Contrôle Qualité",
            surfaceM2 = 30,
            maxBagsCapacity = 100,
            currentBagsCount = 0,
            targetHumidityMaxPercent = 14.0,
            currentTemperatureC = 24.0,
            currentHumidityPercent = 14.5,
            isOdorIsolated = false,
            primaryRotationRule = "Traitement ou Retour Fournisseur",
            description = "Lots sous séquestre après contrôle en 5 étapes (humidité excessive, décotes, attente contre-expertise)."
        )

        var bagsA = 0
        var bagsB = 0
        var bagsC = 0
        var bagsQ = 0

        for (lot in lots) {
            totalStockKg += lot.netReceivedKg
            val prod = productMap[lot.productId] ?: productByCode[lot.lotNumber.take(3)]
            val isSpice = prod?.type == ProductType.SPICE || lot.lotNumber.startsWith("POI") || lot.lotNumber.startsWith("GIR")
            val isBean = lot.lotNumber.startsWith("HAR") || lot.lotNumber.startsWith("PDC")
            val isMaize = lot.lotNumber.startsWith("MAI")

            // Déterminer la zone cible selon le guide
            val expectedZone = when {
                lot.decision == QualityDecision.QUARANTAINE || lot.decision == QualityDecision.REFUSE -> "Zone Quarantaine"
                lot.moisturePercent > 14.2 -> "Zone Quarantaine"
                isSpice -> "Zone Épices"
                isBean || isMaize -> "Zone Légumineuses"
                else -> "Zone Grains"
            }

            // Comptage des sacs par zone actuelle
            val currentZoneStr = lot.warehouseZone.lowercase()
            when {
                currentZoneStr.contains("épice") || currentZoneStr.contains("epice") -> bagsB += lot.bagsCount
                currentZoneStr.contains("quarantaine") -> bagsQ += lot.bagsCount
                currentZoneStr.contains("allée b") || currentZoneStr.contains("allée c") || currentZoneStr.contains("légumineuse") -> bagsC += lot.bagsCount
                else -> bagsA += lot.bagsCount
            }

            // Vérification des anomalies de zonage (Guide p. 25-27)
            var hasAnomaly = false

            // Règle 1 : Épices obligatoirement en Zone B sécurisée
            if (isSpice && !currentZoneStr.contains("épice") && !currentZoneStr.contains("epice")) {
                hasAnomaly = true
                anomalies.add(
                    ZoningAnomaly(
                        id = "anom_spice_${lot.lotNumber}",
                        lotNumber = lot.lotNumber,
                        productName = lot.productName,
                        currentZone = lot.warehouseZone,
                        expectedZone = "Zone B - Épices Sécurisée",
                        severity = "CRITIQUE",
                        message = "Épice stockée hors du local sécurisé ! Risque de perte d'arômes, absorption d'odeurs et vol.",
                        guideReference = "Guide p. 27 : « Les épices nobles exigent un local fermé, sec, obscur et étanche aux odeurs concurrentes. »"
                    )
                )
                moves.add(
                    ZoningMoveRecommendation(
                        lotNumber = lot.lotNumber,
                        productName = lot.productName,
                        fromZone = lot.warehouseZone,
                        toZone = "Zone B - Épices Sécurisée",
                        reason = "Mise en sécurité hygrométrique et aromatique immédiate",
                        priority = "IMMÉDIAT"
                    )
                )
            }

            // Règle 2 : Humidité trop élevée pour la zone
            val maxHumid = if (isSpice) 12.5 else 14.0
            if (lot.moisturePercent > maxHumid && !currentZoneStr.contains("quarantaine")) {
                hasAnomaly = true
                anomalies.add(
                    ZoningAnomaly(
                        id = "anom_humid_${lot.lotNumber}",
                        lotNumber = lot.lotNumber,
                        productName = lot.productName,
                        currentZone = lot.warehouseZone,
                        expectedZone = "Zone Q - Quarantaine / Aération",
                        severity = "CRITIQUE",
                        message = "Humidité mesurée à ${lot.moisturePercent}% (seuil max admissible : $maxHumid%). Risque d'échauffement de pile et moisissures.",
                        guideReference = "Guide p. 26 : « Tout lot au-dessus de 14% doit être aéré en urgence ou placé en quarantaine. »"
                    )
                )
            }

            // Règle 3 : Séjour prolongé sur légumineuses (Haricots > 45 jours => danger bruches)
            if (isBean && lot.storageDays >= 40) {
                hasAnomaly = true
                anomalies.add(
                    ZoningAnomaly(
                        id = "anom_bruche_${lot.lotNumber}",
                        lotNumber = lot.lotNumber,
                        productName = lot.productName,
                        currentZone = lot.warehouseZone,
                        expectedZone = "Zone C - Tête de File Déstockage",
                        severity = "AVERTISSEMENT",
                        message = "Lot en stock depuis ${lot.storageDays} jours. Seuil critique de 45-60 jours atteint : risque d'éclosion de bruches.",
                        guideReference = "Guide p. 30 : « Haricots : au-delà de 60 jours, le risque de perforation par les bruches détruit la valeur marchande. »"
                    )
                )
            }

            if (!hasAnomaly) {
                compliantLotsCount++
            }

            // Règle d'affectation pour le Déstockage (FIFO vs FEFO)
            val maxDays = when {
                isSpice -> 90
                isBean -> 60
                isMaize -> 45
                else -> 90
            }

            // Score de priorité de déstockage : plus le score est élevé, plus le lot doit sortir vite
            val isUrgentFefo = isSpice || lot.moisturePercent >= maxHumid || (isBean && lot.storageDays >= 35)
            val score = if (isUrgentFefo) {
                // FEFO : priorité à l'humidité et au vieillissement
                (lot.storageDays.toDouble() / maxDays.toDouble()) * 60.0 + (lot.moisturePercent * 3.0)
            } else {
                // FIFO : priorité aux jours d'ancienneté
                (lot.storageDays.toDouble() / maxDays.toDouble()) * 50.0
            }

            val rule = if (isUrgentFefo) "FEFO (Risque Humidité / DLUO)" else "FIFO (Rotation Régulière)"
            val priorityLevel = when {
                score >= 60.0 -> "URGENT"
                score >= 35.0 -> "ÉLEVÉ"
                else -> "NORMAL"
            }

            val reason = when {
                isSpice -> "Épice à rotation FEFO stricte : préserver les composés volatils avant dégradation."
                isBean && lot.storageDays >= 35 -> "Haricot approchant le seuil d'infestation : à déstocker sous 7 jours."
                lot.moisturePercent >= maxHumid -> "Humidité haute (${lot.moisturePercent}%) : sortie prioritaire requise."
                else -> "Rotation FIFO standard : lot reçu le ${lot.receptionDate} (${lot.storageDays} jours)."
            }

            destockingList.add(
                DestockingPriorityItem(
                    rank = 0, // Sera calculé après tri
                    lotNumber = lot.lotNumber,
                    productName = lot.productName,
                    productType = if (isSpice) "EPICE" else "GRAIN",
                    zone = lot.warehouseZone,
                    quantityKg = lot.netReceivedKg,
                    moisturePercent = lot.moisturePercent,
                    storageDays = lot.storageDays,
                    maxRecommendedStorageDays = maxDays,
                    ruleApplied = rule,
                    priorityLevel = priorityLevel,
                    reason = reason
                )
            )
        }

        // Trier la file de déstockage du plus urgent au moins urgent
        val sortedDestockingQueue = destockingList
            .sortedWith(
                compareByDescending<DestockingPriorityItem> { it.priorityLevel == "URGENT" }
                    .thenByDescending { it.priorityLevel == "ÉLEVÉ" }
                    .thenByDescending { it.storageDays }
                    .thenByDescending { it.moisturePercent }
            )
            .mapIndexed { index, item ->
                item.copy(rank = index + 1)
            }

        val totalBags = bagsA + bagsB + bagsC + bagsQ
        val maxTotalBags = zoneA.maxBagsCapacity + zoneB.maxBagsCapacity + zoneC.maxBagsCapacity + zoneQ.maxBagsCapacity
        val occupancy = if (maxTotalBags > 0) (totalBags.toDouble() / maxTotalBags.toDouble()) * 100.0 else 0.0

        val complianceScore = if (lots.isNotEmpty()) (compliantLotsCount.toDouble() / lots.size.toDouble()) * 100.0 else 100.0

        val updatedZones = listOf(
            zoneA.copy(currentBagsCount = bagsA),
            zoneB.copy(currentBagsCount = bagsB),
            zoneC.copy(currentBagsCount = bagsC),
            zoneQ.copy(currentBagsCount = bagsQ)
        )

        return WarehouseZoningAudit(
            zones = updatedZones,
            totalLotsAnalyzed = lots.size,
            totalStockKg = totalStockKg,
            occupancyPercent = occupancy,
            complianceScorePercent = complianceScore,
            anomalies = anomalies,
            recommendedMoves = moves,
            destockingQueue = sortedDestockingQueue
        )
    }
}
