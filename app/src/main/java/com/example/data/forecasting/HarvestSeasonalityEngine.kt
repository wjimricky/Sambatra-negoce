package com.example.data.forecasting

import com.example.data.local.room.ProduitEntity
import com.example.data.local.room.StockLotEntity

/**
 * Moteur de prévision et saisonnalité des récoltes à Madagascar (Guide 2026, Ch. 4 & 6).
 * Génère des alertes automatisées de réapprovisionnement basées sur la conjonction
 * des niveaux de stock physiques et des cycles agronomiques (récolte vs soudure).
 */
object HarvestSeasonalityEngine {

    val HARVEST_CALENDARS = listOf(
        CropHarvestCalendar(
            productCode = "RIZ",
            productName = "Riz blanc usiné (Makalioka / Tsipala)",
            mainProductionBasin = "Lac Alaotra (Ambatondrazaka) & Marovoay",
            harvestStartMonth = 5, // Mai
            harvestEndMonth = 7, // Juillet
            harvestPeakMonth = 6,
            leanSeasonStartMonth = 11, // Novembre à Mars (Soudure / Maitso ahitra)
            leanSeasonEndMonth = 3,
            seasonalPriceDipPercent = -15.0, // -15% à la récolte
            seasonalPriceSurgePercent = 28.0, // +28% en pic de soudure
            postHarvestMoistureRisk = "Humidité initiale élevée des paddy battus sous la pluie (>15%). Séchage solaire impératif."
        ),
        CropHarvestCalendar(
            productCode = "HAR",
            productName = "Haricot sec (Tsipoy / Lingot)",
            mainProductionBasin = "Vakinankaratra (Betafo, Antsirabe)",
            harvestStartMonth = 6,
            harvestEndMonth = 8,
            harvestPeakMonth = 7,
            leanSeasonStartMonth = 12,
            leanSeasonEndMonth = 4,
            seasonalPriceDipPercent = -18.0,
            seasonalPriceSurgePercent = 25.0,
            postHarvestMoistureRisk = "Vulnérabilité aux bruches si le taux d'humidité dépasse 13.5% après 60 jours."
        ),
        CropHarvestCalendar(
            productCode = "MAI",
            productName = "Maïs grain sec",
            mainProductionBasin = "Moyen-Ouest (Tsiroanomandidy) & Vakinankaratra",
            harvestStartMonth = 5,
            harvestEndMonth = 7,
            harvestPeakMonth = 6,
            leanSeasonStartMonth = 12,
            leanSeasonEndMonth = 3,
            seasonalPriceDipPercent = -20.0,
            seasonalPriceSurgePercent = 30.0,
            postHarvestMoistureRisk = "Risque d'aflatoxines si la ventilation est interrompue en saison humide."
        ),
        CropHarvestCalendar(
            productCode = "PDC",
            productName = "Pois du Cap (Kabaro)",
            mainProductionBasin = "Sud-Ouest (Morombe, Tuléar)",
            harvestStartMonth = 9,
            harvestEndMonth = 11,
            harvestPeakMonth = 10,
            leanSeasonStartMonth = 1,
            leanSeasonEndMonth = 5,
            seasonalPriceDipPercent = -14.0,
            seasonalPriceSurgePercent = 22.0,
            postHarvestMoistureRisk = "Coupures fréquentes de la RN7 par les pluies durant la soudure."
        ),
        CropHarvestCalendar(
            productCode = "GIR",
            productName = "Clous de Girofle (Grade CG1)",
            mainProductionBasin = "Côte Est (Analanjirofo, Fénérive-Est, Mananara)",
            harvestStartMonth = 10,
            harvestEndMonth = 12,
            harvestPeakMonth = 11,
            leanSeasonStartMonth = 4,
            leanSeasonEndMonth = 8,
            seasonalPriceDipPercent = -12.0,
            seasonalPriceSurgePercent = 20.0,
            postHarvestMoistureRisk = "Pluies tropicales de la côte Est à l'époque de la cueillette. Risque de clous noircis humides."
        ),
        CropHarvestCalendar(
            productCode = "POI",
            productName = "Poivre noir & sauvage (Voatsiperifery)",
            mainProductionBasin = "Analanjirofo & Forêts humides de l'Est",
            harvestStartMonth = 6,
            harvestEndMonth = 9,
            harvestPeakMonth = 7,
            leanSeasonStartMonth = 1,
            leanSeasonEndMonth = 5,
            seasonalPriceDipPercent = -10.0,
            seasonalPriceSurgePercent = 18.0,
            postHarvestMoistureRisk = "Forte hygroscopicité : absorbe l'humidité ambiante maritime à Toamasina."
        )
    )

    /**
     * Calcule la courbe saisonnière des prix sur 12 mois pour un produit donné
     */
    fun generate12MonthPriceForecast(
        productCode: String,
        nominalPriceAr: Double
    ): List<SeasonalPriceTrendForecast> {
        val calendar = HARVEST_CALENDARS.find { it.productCode == productCode }
            ?: HARVEST_CALENDARS[0]

        val months = listOf(
            "Jan" to 1, "Fév" to 2, "Mar" to 3, "Avr" to 4,
            "Mai" to 5, "Juin" to 6, "Juil" to 7, "Août" to 8,
            "Sept" to 9, "Oct" to 10, "Nov" to 11, "Déc" to 12
        )

        return months.map { (label, monthNum) ->
            val isHarvest = if (calendar.harvestStartMonth <= calendar.harvestEndMonth) {
                monthNum in calendar.harvestStartMonth..calendar.harvestEndMonth
            } else {
                monthNum >= calendar.harvestStartMonth || monthNum <= calendar.harvestEndMonth
            }

            val isLean = if (calendar.leanSeasonStartMonth <= calendar.leanSeasonEndMonth) {
                monthNum in calendar.leanSeasonStartMonth..calendar.leanSeasonEndMonth
            } else {
                monthNum >= calendar.leanSeasonStartMonth || monthNum <= calendar.leanSeasonEndMonth
            }

            val priceIndex = when {
                monthNum == calendar.harvestPeakMonth -> 1.0 + (calendar.seasonalPriceDipPercent / 100.0)
                isHarvest -> 1.0 + (calendar.seasonalPriceDipPercent * 0.7 / 100.0)
                isLean -> 1.0 + (calendar.seasonalPriceSurgePercent / 100.0)
                else -> 1.0
            }

            SeasonalPriceTrendForecast(
                monthLabel = label,
                monthNumber = monthNum,
                priceIndex = Math.round(priceIndex * 100.0) / 100.0,
                forecastedPriceAr = Math.round(nominalPriceAr * priceIndex).toDouble(),
                isHarvestPeriod = isHarvest,
                isLeanSeason = isLean
            )
        }
    }

    /**
     * Analyse dynamique des besoins de réapprovisionnement automatique
     */
    fun evaluateReplenishmentAlerts(
        products: List<ProduitEntity>,
        lots: List<StockLotEntity>,
        currentMonth: Int = 10 // Octobre
    ): List<AutoReplenishmentAlert> {
        val alerts = mutableListOf<AutoReplenishmentAlert>()

        for (product in products) {
            val productLots = lots.filter { it.produitId == product.id && !it.estBloqueQuarantaine }
            val totalDispoKg = productLots.sumOf { it.quantiteDisponibleKg }
            val cmj = if (product.consoJournaliereMoyenneKg > 0) product.consoJournaliereMoyenneKg else 100.0
            val coverageDays = (totalDispoKg / cmj).toInt()

            val calendar = HARVEST_CALENDARS.find { it.productCode == product.code }

            val isHarvestTime = calendar?.let {
                if (it.harvestStartMonth <= it.harvestEndMonth) currentMonth in it.harvestStartMonth..it.harvestEndMonth
                else currentMonth >= it.harvestStartMonth || currentMonth <= it.harvestEndMonth
            } ?: false

            val isLeanSeasonSoon = calendar?.let {
                val monthsUntilLean = (it.leanSeasonStartMonth - currentMonth + 12) % 12
                monthsUntilLean in 0..2
            } ?: false

            val priority = when {
                totalDispoKg <= product.stockSecuriteKg -> ReplenishmentAlertPriority.CRITIQUE_IMMEDIAT
                totalDispoKg <= product.pointDeCommandeKg -> ReplenishmentAlertPriority.CRITIQUE_IMMEDIAT
                isHarvestTime -> ReplenishmentAlertPriority.OPPORTUNITE_RECOLTE
                isLeanSeasonSoon -> ReplenishmentAlertPriority.ANTICIPATION_SOUDURE
                else -> ReplenishmentAlertPriority.OPTIMAL
            }

            // Calcul de la commande recommandée selon la formule Guide 2026 :
            // Commande = (Couverture cible x CMJ) - Stock disponible actuel
            val targetStockKg = product.couvertureCibleJours * cmj
            val recommendedQtyKg = maxOf(0.0, targetStockKg - totalDispoKg)

            val seasonalContext = when {
                isHarvestTime -> "Pleine récolte dans le bassin (${calendar?.mainProductionBasin}). Prix d'achat au plus bas saisonnier (${calendar?.seasonalPriceDipPercent}%)."
                isLeanSeasonSoon -> "La période de soudure débute dans moins de 45 jours. Flambée historique prévisible de +${calendar?.seasonalPriceSurgePercent}%."
                else -> "Période intermédiaire de cours stables. Consommation nominale de ${cmj.toInt()} kg/jour."
            }

            val strategicAction = when (priority) {
                ReplenishmentAlertPriority.CRITIQUE_IMMEDIAT ->
                    "Lancer d'urgence un bon de commande de ${recommendedQtyKg.toInt()} kg auprès de ${product.nom} sous 48h (Délai réassort: ${product.delaiApprovisionnementJours}j)."
                ReplenishmentAlertPriority.OPPORTUNITE_RECOLTE ->
                    "Reconstituer le stock stratégique à bas prix. Commander ${recommendedQtyKg.toInt()} kg pour maximiser la marge nette annuelle."
                ReplenishmentAlertPriority.ANTICIPATION_SOUDURE ->
                    "Sécuriser un volume tampon de ${recommendedQtyKg.toInt()} kg avant la hausse des cours de soudure."
                ReplenishmentAlertPriority.OPTIMAL ->
                    "Couverture satisfaisante (${coverageDays} jours restants). Prochain réapprovisionnement programmé le mois prochain."
            }

            alerts.add(
                AutoReplenishmentAlert(
                    id = "ALERT-${product.code}",
                    productCode = product.code,
                    productName = product.nom,
                    priority = priority,
                    currentStockKg = totalDispoKg,
                    reorderPointKg = product.pointDeCommandeKg,
                    safetyStockKg = product.stockSecuriteKg,
                    remainingCoverageDays = coverageDays,
                    recommendedOrderQuantityKg = Math.round(recommendedQtyKg).toDouble(),
                    estimatedOrderCostAr = Math.round(recommendedQtyKg * product.cmupCostAr).toDouble(),
                    seasonalContext = seasonalContext,
                    strategicAction = strategicAction
                )
            )
        }

        // Tri : les urgences immédiates en tête
        return alerts.sortedBy { it.priority.ordinal }
    }
}
