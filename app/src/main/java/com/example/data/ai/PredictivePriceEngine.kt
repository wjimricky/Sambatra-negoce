package com.example.data.ai

import java.text.NumberFormat
import java.util.Locale

/**
 * Modèles et moteur d'analyse prédictive des prix du marché pour grains et épices (Guide 2026).
 * Anticipe les fluctuations saisonnières (récolte vs soudure) et identifie
 * les fenêtres optimales d'achat et de revente avec calcul du gain net après coûts de stockage.
 */

data class MonthlyPriceDataPoint(
    val monthNumber: Int, // 1 to 12
    val monthName: String,
    val estimatedPriceAr: Double,
    val isHarvestPeriod: Boolean,
    val isLeanSeason: Boolean,
    val volatilityPercent: Double
)

data class CommodityPriceForecast(
    val productCode: String,
    val productName: String,
    val category: String, // "Céréales", "Légumineuses", "Épices"
    val productionBasin: String,
    val currentSpotPriceAr: Double,
    val harvestFloorPriceAr: Double,
    val leanPeakPriceAr: Double,
    val harvestMonthName: String,
    val leanPeakMonthName: String,
    val monthlyPriceProjections: List<MonthlyPriceDataPoint>,
    
    // Analyse financière de l'opportunité d'arbitrage / stockage
    val recommendedStorageMonths: Int,
    val estimatedHoldingCostPerKgAr: Double, // Magasinage + coût d'immobilisation financière
    val estimatedRoadAndStorageLossPerKgAr: Double, // Freinte constatée
    val netExpectedGainPerKgAr: Double, // Gain net = Pic soudure - Creux récolte - Frais stockage - Freinte
    val netReturnOnInvestmentPercent: Double, // ROI net
    val optimalPurchaseWindow: String,
    val optimalSellingWindow: String,
    val strategicActionAdvice: String,
    val riskLevel: PriceRiskLevel
)

enum class PriceRiskLevel(val labelFr: String, val colorHex: Long) {
    FAIBLE("Faible Risque • Valeur Sûre", 0xFF15803D),
    MODERE("Modéré • Surveiller Humidité", 0xFF16A34A),
    VOLATILE("Volatile • Arbitrage Actif", 0xFFD97706),
    TRES_SPECULATIF("Très Spéculatif • Plafonné 10%", 0xFFDC2626)
}

object PredictivePriceEngine {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.FRANCE)

    val COMMODITIES_DATA: List<CommodityPriceForecast> = listOf(
        CommodityPriceForecast(
            productCode = "RIZ_MAK",
            productName = "Riz Blanc Usiné Makalioka",
            category = "Céréales",
            productionBasin = "Lac Alaotra (Ambatondrazaka)",
            currentSpotPriceAr = 2300.0,
            harvestFloorPriceAr = 1950.0, // Mai - Juillet
            leanPeakPriceAr = 2650.0, // Janvier - Février
            harvestMonthName = "Juin (Grande Récolte)",
            leanPeakMonthName = "Janvier (Pic de Soudure)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 2650.0, false, true, 4.0),
                MonthlyPriceDataPoint(2, "Fév", 2600.0, false, true, 4.5),
                MonthlyPriceDataPoint(3, "Mar", 2450.0, false, false, 5.0),
                MonthlyPriceDataPoint(4, "Avr", 2250.0, false, false, 6.0),
                MonthlyPriceDataPoint(5, "Mai", 2050.0, true, false, 7.0),
                MonthlyPriceDataPoint(6, "Juin", 1950.0, true, false, 5.0),
                MonthlyPriceDataPoint(7, "Juil", 2000.0, true, false, 4.0),
                MonthlyPriceDataPoint(8, "Août", 2100.0, false, false, 3.5),
                MonthlyPriceDataPoint(9, "Sep", 2200.0, false, false, 3.0),
                MonthlyPriceDataPoint(10, "Oct", 2350.0, false, false, 4.0),
                MonthlyPriceDataPoint(11, "Nov", 2500.0, false, true, 5.0),
                MonthlyPriceDataPoint(12, "Déc", 2600.0, false, true, 4.5)
            ),
            recommendedStorageMonths = 6,
            estimatedHoldingCostPerKgAr = 160.0, // 6 mois de stockage + capitaux
            estimatedRoadAndStorageLossPerKgAr = 25.0, // ~1% perte
            netExpectedGainPerKgAr = 515.0, // 2650 - 1950 - 160 - 25 = 515 Ar/kg
            netReturnOnInvestmentPercent = 26.4,
            optimalPurchaseWindow = "Fin Mai à Début Juillet (pendant les battages)",
            optimalSellingWindow = "Décembre à Février (soudure rizicole)",
            strategicActionAdvice = "Acheter en volume dès l'ouverture des couloirs de battage. Exiger taux d'humidité <= 13.0%. Vente par tranches de 20% dès décembre.",
            riskLevel = PriceRiskLevel.FAIBLE
        ),
        CommodityPriceForecast(
            productCode = "HAR_ROUGE",
            productName = "Haricot Sec Rouge (Tsipoy)",
            category = "Légumineuses",
            productionBasin = "Vakinankaratra (Betafo, Antsirabe)",
            currentSpotPriceAr = 3660.0,
            harvestFloorPriceAr = 2950.0, // Juillet
            leanPeakPriceAr = 4300.0, // Janvier - Mars
            harvestMonthName = "Juillet (Récolte des Hautes Terres)",
            leanPeakMonthName = "Février (Pénurie avant saison)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 4150.0, false, true, 6.0),
                MonthlyPriceDataPoint(2, "Fév", 4300.0, false, true, 5.5),
                MonthlyPriceDataPoint(3, "Mar", 4100.0, false, true, 7.0),
                MonthlyPriceDataPoint(4, "Avr", 3700.0, false, false, 8.0),
                MonthlyPriceDataPoint(5, "Mai", 3400.0, false, false, 7.5),
                MonthlyPriceDataPoint(6, "Juin", 3100.0, true, false, 6.0),
                MonthlyPriceDataPoint(7, "Juil", 2950.0, true, false, 5.0),
                MonthlyPriceDataPoint(8, "Août", 3050.0, true, false, 4.5),
                MonthlyPriceDataPoint(9, "Sep", 3300.0, false, false, 4.0),
                MonthlyPriceDataPoint(10, "Oct", 3600.0, false, false, 5.0),
                MonthlyPriceDataPoint(11, "Nov", 3850.0, false, true, 6.0),
                MonthlyPriceDataPoint(12, "Déc", 4050.0, false, true, 5.5)
            ),
            recommendedStorageMonths = 5,
            estimatedHoldingCostPerKgAr = 210.0,
            estimatedRoadAndStorageLossPerKgAr = 40.0,
            netExpectedGainPerKgAr = 1100.0, // 4300 - 2950 - 210 - 40 = 1100 Ar/kg
            netReturnOnInvestmentPercent = 37.2,
            optimalPurchaseWindow = "Fin Juin à Début Août",
            optimalSellingWindow = "Janvier à Mars",
            strategicActionAdvice = "Opportunité d'arbitrage forte. Traitement préventif contre les bruches obligatoire à J+15 de stockage.",
            riskLevel = PriceRiskLevel.MODERE
        ),
        CommodityPriceForecast(
            productCode = "MAI_SEC",
            productName = "Maïs Grain Jaune Sec",
            category = "Céréales",
            productionBasin = "Moyen-Ouest (Tsiroanomandidy)",
            currentSpotPriceAr = 1550.0,
            harvestFloorPriceAr = 1200.0, // Juin
            leanPeakPriceAr = 1850.0, // Novembre - Décembre (provendiers)
            harvestMonthName = "Juin (Grande Récolte)",
            leanPeakMonthName = "Décembre (Demande forte provenderie)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 1750.0, false, true, 6.0),
                MonthlyPriceDataPoint(2, "Fév", 1650.0, false, false, 7.0),
                MonthlyPriceDataPoint(3, "Mar", 1550.0, false, false, 6.5),
                MonthlyPriceDataPoint(4, "Avr", 1400.0, false, false, 8.0),
                MonthlyPriceDataPoint(5, "Mai", 1280.0, true, false, 7.0),
                MonthlyPriceDataPoint(6, "Juin", 1200.0, true, false, 6.0),
                MonthlyPriceDataPoint(7, "Juil", 1250.0, true, false, 5.0),
                MonthlyPriceDataPoint(8, "Août", 1350.0, false, false, 4.5),
                MonthlyPriceDataPoint(9, "Sep", 1480.0, false, false, 5.0),
                MonthlyPriceDataPoint(10, "Oct", 1620.0, false, false, 5.5),
                MonthlyPriceDataPoint(11, "Nov", 1780.0, false, true, 6.0),
                MonthlyPriceDataPoint(12, "Déc", 1850.0, false, true, 5.0)
            ),
            recommendedStorageMonths = 4,
            estimatedHoldingCostPerKgAr = 120.0,
            estimatedRoadAndStorageLossPerKgAr = 30.0,
            netExpectedGainPerKgAr = 500.0,
            netReturnOnInvestmentPercent = 41.6,
            optimalPurchaseWindow = "Juin à Juillet",
            optimalSellingWindow = "Novembre à Janvier",
            strategicActionAdvice = "Livraisons directes aux usines de provende. Attention au seuil d'humidité max 13.0% pour éviter l'aflatoxine.",
            riskLevel = PriceRiskLevel.MODERE
        ),
        CommodityPriceForecast(
            productCode = "GIR_CLOU",
            productName = "Girofle Clous CG3 Trié Extra",
            category = "Épices",
            productionBasin = "Analanjirofo (Fénérive-Est, Sainte-Marie)",
            currentSpotPriceAr = 28500.0,
            harvestFloorPriceAr = 24000.0, // Octobre - Novembre
            leanPeakPriceAr = 34500.0, // Mai - Juin (demande export)
            harvestMonthName = "Novembre (Campagne Girofle)",
            leanPeakMonthName = "Mai (Plein cours export)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 27000.0, false, false, 8.0),
                MonthlyPriceDataPoint(2, "Fév", 28000.0, false, false, 8.5),
                MonthlyPriceDataPoint(3, "Mar", 30000.0, false, false, 9.0),
                MonthlyPriceDataPoint(4, "Avr", 32500.0, false, false, 10.0),
                MonthlyPriceDataPoint(5, "Mai", 34500.0, false, true, 11.0),
                MonthlyPriceDataPoint(6, "Juin", 34000.0, false, true, 10.0),
                MonthlyPriceDataPoint(7, "Juil", 31000.0, false, false, 9.0),
                MonthlyPriceDataPoint(8, "Août", 28000.0, false, false, 8.0),
                MonthlyPriceDataPoint(9, "Sep", 26000.0, false, false, 7.5),
                MonthlyPriceDataPoint(10, "Oct", 24800.0, true, false, 7.0),
                MonthlyPriceDataPoint(11, "Nov", 24000.0, true, false, 6.5),
                MonthlyPriceDataPoint(12, "Déc", 25500.0, true, false, 7.0)
            ),
            recommendedStorageMonths = 6,
            estimatedHoldingCostPerKgAr = 1200.0, // Stockage sécurisé sous clé + assurance
            estimatedRoadAndStorageLossPerKgAr = 450.0, // Freinte aromatique & dessiccation
            netExpectedGainPerKgAr = 8850.0, // 34500 - 24000 - 1200 - 450 = 8850 Ar/kg
            netReturnOnInvestmentPercent = 36.8,
            optimalPurchaseWindow = "Octobre à Décembre (début de campagne)",
            optimalSellingWindow = "Avril à Juin (tension pré-hivernale)",
            strategicActionAdvice = "Contrôler rigoureusement le taux de tiges (< 5%). Stocker exclusivement en Zone B hermétique. Marge nette élevée.",
            riskLevel = PriceRiskLevel.VOLATILE
        ),
        CommodityPriceForecast(
            productCode = "POI_NOIR",
            productName = "Poivre Noir Sauvage Voatsiperifery",
            category = "Épices",
            productionBasin = "Corridor Forestier Est (Moramanga, Anosibe An'ala)",
            currentSpotPriceAr = 22000.0,
            harvestFloorPriceAr = 18500.0, // Juillet - Août
            leanPeakPriceAr = 27000.0, // Février - Mars
            harvestMonthName = "Août (Récolte forestière)",
            leanPeakMonthName = "Février (Pénurie niche)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 25000.0, false, true, 9.0),
                MonthlyPriceDataPoint(2, "Fév", 27000.0, false, true, 8.5),
                MonthlyPriceDataPoint(3, "Mar", 26000.0, false, true, 8.0),
                MonthlyPriceDataPoint(4, "Avr", 24000.0, false, false, 7.5),
                MonthlyPriceDataPoint(5, "Mai", 22500.0, false, false, 7.0),
                MonthlyPriceDataPoint(6, "Juin", 20500.0, false, false, 7.0),
                MonthlyPriceDataPoint(7, "Juil", 19000.0, true, false, 6.5),
                MonthlyPriceDataPoint(8, "Août", 18500.0, true, false, 6.0),
                MonthlyPriceDataPoint(9, "Sep", 19500.0, true, false, 6.5),
                MonthlyPriceDataPoint(10, "Oct", 21000.0, false, false, 7.0),
                MonthlyPriceDataPoint(11, "Nov", 22500.0, false, false, 8.0),
                MonthlyPriceDataPoint(12, "Déc", 23800.0, false, false, 8.5)
            ),
            recommendedStorageMonths = 6,
            estimatedHoldingCostPerKgAr = 900.0,
            estimatedRoadAndStorageLossPerKgAr = 350.0,
            netExpectedGainPerKgAr = 7250.0,
            netReturnOnInvestmentPercent = 39.1,
            optimalPurchaseWindow = "Juillet à Septembre",
            optimalSellingWindow = "Janvier à Mars",
            strategicActionAdvice = "Produit de gastronomie haut de gamme. Conditionnement en sacs sous vide étanches pour conserver les huiles essentielles.",
            riskLevel = PriceRiskLevel.VOLATILE
        ),
        CommodityPriceForecast(
            productCode = "VAN_BOURBON",
            productName = "Vanille Noire Gourmet Bourbon",
            category = "Épices",
            productionBasin = "SAVA (Antalaha, Sambava)",
            currentSpotPriceAr = 180000.0,
            harvestFloorPriceAr = 140000.0, // Octobre
            leanPeakPriceAr = 240000.0, // Mai
            harvestMonthName = "Octobre (Campagne officielle)",
            leanPeakMonthName = "Mai (Contrats internationaux)",
            monthlyPriceProjections = listOf(
                MonthlyPriceDataPoint(1, "Jan", 195000.0, false, false, 15.0),
                MonthlyPriceDataPoint(2, "Fév", 210000.0, false, false, 16.0),
                MonthlyPriceDataPoint(3, "Mar", 225000.0, false, true, 18.0),
                MonthlyPriceDataPoint(4, "Avr", 235000.0, false, true, 19.0),
                MonthlyPriceDataPoint(5, "Mai", 240000.0, false, true, 20.0),
                MonthlyPriceDataPoint(6, "Juin", 220000.0, false, false, 18.0),
                MonthlyPriceDataPoint(7, "Juil", 200000.0, false, false, 16.0),
                MonthlyPriceDataPoint(8, "Août", 180000.0, false, false, 15.0),
                MonthlyPriceDataPoint(9, "Sep", 160000.0, false, false, 14.0),
                MonthlyPriceDataPoint(10, "Oct", 140000.0, true, false, 12.0),
                MonthlyPriceDataPoint(11, "Nov", 155000.0, true, false, 13.0),
                MonthlyPriceDataPoint(12, "Déc", 175000.0, false, false, 14.0)
            ),
            recommendedStorageMonths = 6,
            estimatedHoldingCostPerKgAr = 8000.0,
            estimatedRoadAndStorageLossPerKgAr = 4000.0,
            netExpectedGainPerKgAr = 88000.0, // 240 000 - 140 000 - 8000 - 4000 = 88 000 Ar/kg
            netReturnOnInvestmentPercent = 62.8,
            optimalPurchaseWindow = "Octobre à Novembre (avec contrat client adossé)",
            optimalSellingWindow = "Avril à Juin",
            strategicActionAdvice = "ATTENTION : Règle impérative des 10% des fonds propres (Guide p. 94-95). Ne jamais spéculer sans acheteur ferme déjà contractualisé.",
            riskLevel = PriceRiskLevel.TRES_SPECULATIF
        )
    )

    fun getForecast(productCode: String): CommodityPriceForecast {
        return COMMODITIES_DATA.find { it.productCode.equals(productCode, ignoreCase = true) }
            ?: COMMODITIES_DATA[0]
    }

    /**
     * Calcule le gain financier net projeté pour un tonnage donné
     */
    fun calculateProjectedArbitrage(
        forecast: CommodityPriceForecast,
        volumeTons: Double,
        plannedHoldingMonths: Int
    ): ArbitrageProjectionResult {
        val volumeKg = volumeTons * 1000.0
        val totalBuyCostAr = volumeKg * forecast.harvestFloorPriceAr
        val grossSaleRevenueAr = volumeKg * forecast.leanPeakPriceAr
        
        // Coûts proportionnels au temps de stockage
        val adjustedHoldingCostPerKg = (forecast.estimatedHoldingCostPerKgAr / forecast.recommendedStorageMonths) * plannedHoldingMonths
        val totalHoldingCostAr = volumeKg * adjustedHoldingCostPerKg
        val totalShrinkageLossAr = volumeKg * forecast.estimatedRoadAndStorageLossPerKgAr
        
        val totalInvestmentCostAr = totalBuyCostAr + totalHoldingCostAr + totalShrinkageLossAr
        val netGainAr = grossSaleRevenueAr - totalInvestmentCostAr
        val roiPercent = if (totalInvestmentCostAr > 0) (netGainAr / totalInvestmentCostAr) * 100.0 else 0.0

        return ArbitrageProjectionResult(
            productCode = forecast.productCode,
            productName = forecast.productName,
            volumeTons = volumeTons,
            holdingMonths = plannedHoldingMonths,
            totalBuyCostAr = totalBuyCostAr,
            grossSaleRevenueAr = grossSaleRevenueAr,
            totalHoldingCostAr = totalHoldingCostAr,
            totalLossCostAr = totalShrinkageLossAr,
            netGainAr = netGainAr,
            roiPercent = roiPercent,
            isViable = netGainAr > 0 && roiPercent >= 15.0
        )
    }
}

data class ArbitrageProjectionResult(
    val productCode: String,
    val productName: String,
    val volumeTons: Double,
    val holdingMonths: Int,
    val totalBuyCostAr: Double,
    val grossSaleRevenueAr: Double,
    val totalHoldingCostAr: Double,
    val totalLossCostAr: Double,
    val netGainAr: Double,
    val roiPercent: Double,
    val isViable: Boolean
)
