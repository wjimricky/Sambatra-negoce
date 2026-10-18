package com.example

import com.example.data.finance.NetMarginCalculator
import com.example.data.finance.NetMarginInputs
import com.example.data.finance.ProfitabilityRating
import com.example.data.forecasting.HarvestSeasonalityEngine
import com.example.data.forecasting.ReplenishmentAlertPriority
import com.example.data.local.room.ProduitEntity
import com.example.data.local.room.StockLotEntity
import com.example.data.sustainability.CarbonCalculationInputs
import com.example.data.sustainability.CarbonFootprintCalculator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Step2BusinessLogicTest {

    @Test
    fun testNetMarginCalculator_FluctuatingCosts() {
        val inputs = NetMarginInputs(
            productCode = "RIZ",
            productName = "Riz blanc usiné Makalioka",
            lotNumber = "LOT-TEST-01",
            quantityKg = 5000.0,
            purchasePricePerKgAr = 2080.0,
            sellingPricePerKgAr = 2380.0,
            transportDistanceKm = 150.0,
            fuelRatePerKmAr = 4500.0,
            holdingDurationDays = 30,
            dailyWarehouseCostPerTonAr = 1800.0,
            observedLossPercent = 0.5
        )

        val breakdown = NetMarginCalculator.calculateNetMargin(inputs)

        // Verifications
        assertTrue("Le chiffre d'affaires doit être calculé sur le produit livré", breakdown.totalRevenueAr > 11_000_000.0)
        assertTrue("Les frais de transport doivent être calculés", breakdown.transportCostAr > 0.0)
        assertTrue("Les frais de stockage doivent être calculés", breakdown.warehouseHoldingCostAr > 0.0)
        assertTrue("La marge brute doit être positive", breakdown.grossMarginAr > 0.0)
        assertNotNull(breakdown.profitabilityRating)
    }

    @Test
    fun testCarbonFootprintCalculator_TruckDistanceAndGrains() {
        val inputs = CarbonCalculationInputs(
            productCode = "RIZ",
            productName = "Riz Blanc",
            quantityKg = 10000.0,
            transportDistanceKm = 360.0,
            vehicleType = "CAMION_10T",
            vehiclePayloadUtilizationPercent = 60.0,
            storageDurationDays = 20,
            isLocalSourcing = false
        )

        val footprint = CarbonFootprintCalculator.calculateFootprint(inputs)

        assertTrue("Les émissions de transport doivent être calculées", footprint.transportEmissionKgCo2e > 0.0)
        assertTrue("L'émission totale doit être calculée", footprint.totalEmissionKgCo2e > 0.0)
        assertNotNull(footprint.ecoScore)
        assertTrue("Des recommandations de réduction carbone doivent être proposées", footprint.carbonReductionPotentials.isNotEmpty())
    }

    @Test
    fun testHarvestSeasonalityEngine_AlaotraRiceHarvestVsLean() {
        val calendar = HarvestSeasonalityEngine.HARVEST_CALENDARS.first { it.productCode == "RIZ" }
        assertTrue("Le bassin doit inclure le Lac Alaotra", calendar.mainProductionBasin.contains("Lac Alaotra"))

        val forecasts = HarvestSeasonalityEngine.generate12MonthPriceForecast("RIZ", 2300.0)
        assertEquals(12, forecasts.size)

        // Mois 5 (Mai) est en récolte (prix plus bas)
        val mayForecast = forecasts[4]
        assertTrue("Mai doit être en période de récolte", mayForecast.isHarvestPeriod)
        assertTrue("L'indice de prix en récolte doit être inférieur ou égal à 1.0", mayForecast.priceIndex <= 1.0)

        // Mois 1 (Janvier) est en soudure (prix plus haut)
        val janForecast = forecasts[0]
        assertTrue("Janvier doit être en période de soudure", janForecast.isLeanSeason)
        assertTrue("L'indice de prix en soudure doit être supérieur à 1.0", janForecast.priceIndex > 1.0)
    }

    @Test
    fun testHarvestSeasonalityEngine_AutoReplenishmentAlerts() {
        val products = listOf(
            ProduitEntity(
                id = "P1",
                code = "RIZ",
                nom = "Riz Blanc",
                nomMalagasy = "Vary fotsy",
                type = "GRAIN",
                classeAbc = "A",
                variete = "Makalioka",
                cmupCostAr = 2080.0,
                prixVenteGrosAr = 2300.0,
                consoJournaliereMoyenneKg = 250.0,
                stockSecuriteKg = 3500.0, // 14 jours
                pointDeCommandeKg = 6000.0, // 24 jours
                seuilHumiditeMaxPercent = 13.0,
                formatConditionnement = "Sacs 50kg",
                zoneEntrepot = "ZONE_A",
                risquesSpecifiques = "Charançons"
            )
        )

        // Stock très bas : seulement 1000 kg (soit 4 jours de conso < stock de sécurité 3500 kg)
        val criticalLots = listOf(
            StockLotEntity(
                numeroLot = "LOT-CRIT-01",
                produitId = "P1",
                fournisseurId = "F1",
                nomFournisseur = "Test Fournisseur",
                regionOrigine = "Alaotra",
                dateReception = "2026-10-01",
                quantiteInitialeKg = 1000.0,
                quantiteDisponibleKg = 1000.0,
                nombreSacs = 20,
                nombreSacsPeses = 5,
                humiditeMesureePercent = 12.5,
                impuretesPercent = 0.8,
                grainsDefectueuxPercent = 0.2,
                decisionControle = "CONFORME",
                identifiantContreEchantillonScelle = "ECH-CRIT-01",
                zoneStockageEntrepot = "ZONE_A",
                coutUnitaireAchatAr = 2080.0,
                dateLimiteConservation = "2027-01-01"
            )
        )

        val alerts = HarvestSeasonalityEngine.evaluateReplenishmentAlerts(products, criticalLots)
        assertFalse(alerts.isEmpty())
        val firstAlert = alerts[0]
        assertEquals("RIZ", firstAlert.productCode)
        assertEquals(ReplenishmentAlertPriority.CRITIQUE_IMMEDIAT, firstAlert.priority)
        assertTrue("La quantité recommandée doit ramener le stock au-dessus du point de commande", firstAlert.recommendedOrderQuantityKg > 0.0)
    }
}
