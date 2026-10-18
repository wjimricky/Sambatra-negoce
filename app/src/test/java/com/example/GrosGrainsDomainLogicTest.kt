package com.example

import com.example.data.model.PurchaseDecisionCriteria
import com.example.data.repository.GrosGrainsRepository
import org.junit.Assert.*
import org.junit.Test

class GrosGrainsDomainLogicTest {

    private val repository = GrosGrainsRepository.getInstance()

    @Test
    fun testPurchaseRule_FirstFourCriteriaAreStrictlyEliminatory() {
        // All true except question 1 (prix de revient non calculé)
        val criteria1 = PurchaseDecisionCriteria(
            hasFullCostPriceCalculated = false,
            isTargetMarginAchievable = true,
            hasIdentifiedBuyerOrContract = true,
            isQualityControlledPrePayment = true,
            isStorageDurationSafe = true,
            isExposureWithinLimits = true,
            doesTreasuryRemainAboveTwoMonths = true,
            hasSecondSupplierAvailable = true
        )
        val result1 = repository.evaluatePurchaseDecision(criteria1)
        assertFalse("Un NON sur la question 1 doit refuser catégoriquement l'achat", result1.canProceed)
        assertEquals("REFUS D'ACHAT FORMEL", result1.verdict)

        // All 8 true
        val criteriaAll = PurchaseDecisionCriteria(
            hasFullCostPriceCalculated = true,
            isTargetMarginAchievable = true,
            hasIdentifiedBuyerOrContract = true,
            isQualityControlledPrePayment = true,
            isStorageDurationSafe = true,
            isExposureWithinLimits = true,
            doesTreasuryRemainAboveTwoMonths = true,
            hasSecondSupplierAvailable = true
        )
        val resultAll = repository.evaluatePurchaseDecision(criteriaAll)
        assertTrue("Tous critères validés doit autoriser l'achat", resultAll.canProceed)
    }

    @Test
    fun testPaddyMillingSimulation_MatchesBookPage88() {
        // Cas 1 du livre : Paddy à 1300 Ar, riz blanc à 2300 Ar, sous-produit -150 Ar, frais 80+40+milling
        val sim65 = repository.simulatePaddyMilling(
            paddyPriceAr = 1300.0,
            whiteRiceSalePriceAr = 2300.0,
            yieldPercent = 65.0
        )
        // Rendement 65% : marge ~10.3%
        assertEquals(10.3, sim65.grossMarginPercent, 0.5)

        val sim68 = repository.simulatePaddyMilling(
            paddyPriceAr = 1300.0,
            whiteRiceSalePriceAr = 2300.0,
            yieldPercent = 68.0
        )
        // Rendement 68% : marge ~14.3%
        assertEquals(14.3, sim68.grossMarginPercent, 0.5)
    }

    @Test
    fun testDiscountSimulation_MatchesBookPage37() {
        // Haricot prix revient 3151, vente 3664, remise 3%
        val sim = repository.simulateDiscount(
            costPriceAr = 3151.0,
            targetSalePriceAr = 3664.0,
            discountPercent = 3.0
        )
        // Perte de marge unitaire : 110 Ar/kg (~21.4% de la marge unitaire)
        assertEquals(110.0, sim.lostUnitMarginAr, 1.0)
        // Volume compensatoire requis : ~27.3%
        assertEquals(27.3, sim.requiredVolumeIncreasePercent, 1.0)
    }

    @Test
    fun testCreditAndUnpaidDebtCompensation_MatchesBookPage40() {
        // Impayé de 6M Ar avec marge 14% -> 42.85M Ar
        val sim = repository.simulateCredit(
            salePriceAr = 3664.0,
            creditDays = 45,
            monthlyRatePercent = 1.5,
            targetMarginPercent = 14.0,
            potentialUnpaidAmountAr = 6_000_000.0
        )
        // Coût du crédit pour 45 jours à 1.5% = 2.25% du prix
        assertEquals(2.25, sim.creditCostPercentOfPrice, 0.01)
        // Ventes compensatoires = 6M / 0.14 = 42 857 142 Ar
        assertEquals(42_857_142.0, sim.salesNeededToCompensateUnpaidAr, 100.0)
    }

    @Test
    fun testFormaterContexteMetier_ContainsEssentialGuideRules() {
        val viewModel = com.example.ui.viewmodel.ConseillerGrosGrainsViewModel()
        val contexte = viewModel.formaterContexteMetier()
        assertTrue("Le contexte doit mentionner la trésorerie en Ariary", contexte.contains("Ariary"))
        assertTrue("Le contexte doit mentionner la règle d'or d'achat/stockage/crédit", contexte.contains("La marge se gagne à l'achat"))
        assertTrue("Le contexte doit mentionner les 8 critères d'achat", contexte.contains("Grille des 8 critères"))
    }

    @Test
    fun testRoomEntityAliases_Instantiable() {
        val produit: com.example.data.local.room.Produit = com.example.data.local.room.ProduitEntity(
            id = "test_p",
            code = "TST",
            nom = "Test Produit",
            nomMalagasy = "Test Malagasy",
            type = "GRAIN",
            classeAbc = "A",
            variete = "Standard",
            cmupCostAr = 1000.0,
            prixVenteGrosAr = 1200.0,
            consoJournaliereMoyenneKg = 100.0,
            seuilHumiditeMaxPercent = 13.0,
            stockSecuriteKg = 500.0,
            pointDeCommandeKg = 1000.0,
            formatConditionnement = "Sacs 50kg",
            zoneEntrepot = "Zone A",
            risquesSpecifiques = "Aucun"
        )
        assertEquals("TST", produit.code)
    }

    @Test
    fun testZoningOptimization_GrainsAndSpicesConservationRules() {
        val audit = repository.zoningAudit.value
        assertNotNull("L'audit de zonage ne doit pas être nul", audit)
        assertTrue("L'audit doit couvrir au moins 4 zones", audit.zones.size >= 4)
        assertTrue("La file de déstockage doit classer les lots", audit.destockingQueue.isNotEmpty())

        // Vérifier que le rang 1 de déstockage existe
        val topPriority = audit.destockingQueue.first()
        assertEquals(1, topPriority.rank)
        assertTrue("Le lot prioritaire doit avoir une règle FIFO ou FEFO", topPriority.ruleApplied.contains("FIFO") || topPriority.ruleApplied.contains("FEFO"))
    }

    @Test
    fun testMonthlyReportCalculation_MarginAndLossTracking() {
        val report = repository.getMonthlyReportData("Octobre 2026")
        assertNotNull("Le rapport ne doit pas être nul", report)
        assertTrue("Le CA doit être positif", report.totalTurnoverAr > 0)
        assertTrue("La marge totale doit être positive", report.totalGrossMarginAr > 0)
        assertTrue("Le taux de marge global doit être proche de 14%", report.overallMarginPercent in 10.0..20.0)

        // Suivi des pertes : doit respecter le seuil admissible du Guide (< 1.5%)
        assertTrue("Le taux moyen de perte doit être inférieur au seuil de 1.5%", report.averageLossRatePercent < 1.5)
        assertEquals(4, report.lossTracking.size)
        assertTrue("Doit contenir la freinte hygrométrique", report.lossTracking.any { it.category.contains("hygrométrique") })
    }
}

