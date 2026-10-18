package com.example

import com.example.data.ai.CommodityPriceForecast
import com.example.data.ai.GeminiAdvisorService
import com.example.data.ai.LotStorageRequirement
import com.example.data.ai.PredictivePriceEngine
import com.example.data.ai.SmartStorageRecommender
import com.example.data.model.WarehouseZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Step3AiAndPredictiveLogicTest {

    @Test
    fun testPredictivePriceEngine_DataIntegrity() {
        val riceForecast = PredictivePriceEngine.getForecast("RIZ_MAK")
        assertNotNull(riceForecast)
        assertEquals("Lac Alaotra (Ambatondrazaka)", riceForecast.productionBasin)
        assertTrue("Le prix en soudure doit être supérieur au prix en récolte", riceForecast.leanPeakPriceAr > riceForecast.harvestFloorPriceAr)
        assertTrue("Le gain net par kg doit être positif après déduction des frais", riceForecast.netExpectedGainPerKgAr > 0.0)
        assertEquals(12, riceForecast.monthlyPriceProjections.size)

        val cloveForecast = PredictivePriceEngine.getForecast("GIR_CLOU")
        assertEquals("Analanjirofo (Fénérive-Est, Sainte-Marie)", cloveForecast.productionBasin)
        assertTrue(cloveForecast.leanPeakPriceAr > 30000.0)
    }

    @Test
    fun testPredictivePriceEngine_ArbitrageCalculation() {
        val forecast = PredictivePriceEngine.getForecast("HAR_ROUGE")
        val projection = PredictivePriceEngine.calculateProjectedArbitrage(
            forecast = forecast,
            volumeTons = 5.0,
            plannedHoldingMonths = 5
        )

        assertTrue(projection.grossSaleRevenueAr > projection.totalBuyCostAr)
        assertTrue("Les frais de détention doivent être calculés", projection.totalHoldingCostAr > 0.0)
        assertTrue("La freinte doit être calculée", projection.totalLossCostAr > 0.0)
        assertTrue("Le gain net doit être substantiel sur l'arbitrage haricot", projection.netGainAr > 1_000_000.0)
        assertTrue("Le projet d'arbitrage doit être viable", projection.isViable)
    }

    @Test
    fun testSmartStorage_AromaticSpicesStrictlyIsolatedInZoneB() {
        // Girofle (épice odorante noble)
        val cloveLot = LotStorageRequirement(
            lotNumber = "GIR-ANA-260915-03",
            productCode = "GIR",
            productName = "Girofle Clous CG3 Trié",
            category = "Épices",
            quantityKg = 2000.0,
            bagsCount = 40,
            moisturePercent = 11.5,
            maxMoistureTolerancePercent = 12.0,
            abcClass = "A",
            hasStrongAromaticOdor = true,
            isQualityCompliant = true
        )

        val rec = SmartStorageRecommender.recommendStorageSlot(cloveLot)

        assertEquals("Les épices odorantes doivent être impérativement stockées en Zone B", WarehouseZoneId.ZONE_B, rec.recommendedZone)
        assertTrue("L'isolation des odeurs doit être conforme", rec.odorSegregationCompliant)
        assertTrue("La zone B doit être sécurisée sous clé", rec.zoneLabel.contains("Zone B"))
        assertTrue("La rotation doit être FEFO pour les épices", rec.rotationStrategy.contains("FEFO"))
        assertTrue("L'explication IA doit mentionner le transfert d'odeur vers le riz", rec.aiReasoningExplanation.contains("céréales") || rec.aiReasoningExplanation.contains("odeur"))
    }

    @Test
    fun testSmartStorage_ExcessiveMoistureRoutedToQuarantineZoneQ() {
        // Riz avec humidité anormale (15.5% > seuil max 13.5%)
        val humidRiceLot = LotStorageRequirement(
            lotNumber = "RIZ-ALA-261012-09",
            productCode = "RIZ",
            productName = "Riz blanc usiné",
            category = "Céréales",
            quantityKg = 4000.0,
            bagsCount = 80,
            moisturePercent = 15.5, // EXCÈS CRITIQUE
            maxMoistureTolerancePercent = 13.5,
            abcClass = "A",
            hasStrongAromaticOdor = false,
            isQualityCompliant = false // Signalé non conforme
        )

        val rec = SmartStorageRecommender.recommendStorageSlot(humidRiceLot)

        assertEquals("Un lot trop humide ou non conforme doit aller en Zone Q", WarehouseZoneId.ZONE_Q, rec.recommendedZone)
        assertNotNull("Une alerte de risque sanitaire d'humidité doit être générée", rec.sanitaryRiskWarning)
        assertTrue("Empilage réduit en quarantaine", rec.stackingHeightMaxLayers <= 6)
    }

    @Test
    fun testSmartStorage_CerealsRoutedToZoneAWithStackingStandards() {
        val riceLot = LotStorageRequirement(
            lotNumber = "RIZ-ALA-260920-01",
            productCode = "RIZ",
            productName = "Riz blanc usiné Makalioka",
            category = "Céréales",
            quantityKg = 10000.0,
            bagsCount = 200,
            moisturePercent = 12.5,
            maxMoistureTolerancePercent = 13.5,
            abcClass = "A",
            hasStrongAromaticOdor = false,
            isQualityCompliant = true
        )

        val rec = SmartStorageRecommender.recommendStorageSlot(riceLot)

        assertEquals(WarehouseZoneId.ZONE_A, rec.recommendedZone)
        assertEquals("Palettes de 10 cm minimum", 10, rec.palletClearanceCm)
        assertEquals("Espacement de 50 cm des murs", 50, rec.wallSpacingCm)
        assertEquals("10 sacs de haut max pour céréales", 10, rec.stackingHeightMaxLayers)
    }

    @Test
    fun testGeminiAdvisorService_OfflineAndSearchGroundedResponses() = runBlocking {
        val service = GeminiAdvisorService()
        
        // Requête météo avec recherche web
        val weatherResp = service.askAdvisor(
            userQuery = "Quelle météo sur la RN2 et quel risque d'humidité pour mon riz ?",
            enterpriseContext = "Sambatra Négoce, Tana",
            enableGoogleSearch = true
        )
        assertNotNull(weatherResp)
        assertTrue(weatherResp.replyText.contains("Hygrométrie") || weatherResp.replyText.contains("météo") || weatherResp.replyText.contains("RN2"))
        
        // Requête prix et arbitrage
        val priceResp = service.askAdvisor(
            userQuery = "Prévision prix du riz et quand revendre en soudure ?",
            enterpriseContext = "Sambatra Négoce",
            enableGoogleSearch = false
        )
        assertTrue(priceResp.replyText.contains("Soudure") || priceResp.replyText.contains("prix") || priceResp.replyText.contains("Guide 2026"))
    }
}
