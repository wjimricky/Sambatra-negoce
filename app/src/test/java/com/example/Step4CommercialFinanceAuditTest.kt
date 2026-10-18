package com.example

import com.example.data.finance.*
import com.example.data.local.DemoDataProvider
import com.example.data.report.CommercialDocumentGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Step4CommercialFinanceAuditTest {

    @Test
    fun testWorkingCapitalCalculation_SambatraNegoceModel() {
        val products = DemoDataProvider.initialProducts
        val clients = DemoDataProvider.initialClients
        val suppliers = DemoDataProvider.initialSuppliers
        val treasuryAr = 9_000_000.0 // 1.8 mois de charges fixes

        val diagnosis = WorkingCapitalEngine.calculateWorkingCapital(
            products = products,
            clients = clients,
            suppliers = suppliers,
            currentTreasuryAr = treasuryAr
        )

        // Validation de la valorisation du stock (> 100M Ar)
        assertTrue("La valeur du stock valorisée au CMUP doit dépasser 100M Ar", diagnosis.stockValueAr > 100_000_000.0)

        // Validation des créances clients
        assertTrue("Les créances clients doivent être positives", diagnosis.accountsReceivableAr > 5_000_000.0)

        // BFR d'exploitation
        assertTrue("Le BFR d'exploitation doit être calculé", diagnosis.workingCapitalAr > 0.0)
        assertTrue("Le BFR en jours de CA doit être calculé", diagnosis.workingCapitalDaysOfSales > 0.0)

        // Règle d'or des 2 mois de charges fixes (5M x 2 = 10M Ar)
        assertEquals("La cible de couverture doit être de 2 mois", 2.0, diagnosis.targetCoverageMonths, 0.01)
        assertEquals(1.8, diagnosis.fixedChargesCoverageMonths, 0.01)
        assertEquals(
            "La trésorerie inférieure à 2 mois doit déclencher le statut CRITICAL",
            WorkingCapitalStatus.CRITICAL,
            diagnosis.status
        )

        // Recommandations
        assertTrue("Les recommandations doivent mentionner la réserve de 10M Ar", diagnosis.keyRecommendations.any { it.contains("10 000 000 Ar") })
        assertTrue("Les recommandations doivent cibler les créances à plus de 30 jours", diagnosis.keyRecommendations.any { it.contains("30 jours") })
    }

    @Test
    fun testWorkingCapital_CashStressSimulation() {
        val currentTreasury = 9_000_000.0

        // Simulation d'un achat massif de 20 tonnes de paddy à 1 350 Ar avec 70% d'acompte immédiat
        val stressScenario = WorkingCapitalEngine.simulatePurchaseCashStress(
            scenarioName = "Achat Récolte Paddy Alaotra",
            commodityName = "Paddy Makalioka",
            purchaseVolumeTons = 20.0,
            unitPriceAr = 1350.0,
            upfrontCashPercentage = 70.0,
            currentTreasuryAr = currentTreasury
        )

        assertEquals("Coût total : 20t * 1000 * 1350 = 27 000 000 Ar", 27_000_000.0, stressScenario.purchaseCostAr, 0.01)
        assertEquals("Décaissement immédiat 70% = 18 900 000 Ar", 18_900_000.0, stressScenario.cashOutflowImmediateAr, 0.01)
        assertTrue("Le décaissement supérieur à la trésorerie doit déclencher un risque d'illiquidité", stressScenario.isIlliquidityRisk)
        assertTrue("Le message d'alerte doit mentionner l'asphyxie trésorerie", stressScenario.alertMessage.contains("ASPHYXIE TRÉSORERIE"))
        assertTrue("La stratégie doit recommander un acompte réduit", stressScenario.mitigationStrategy.contains("acompte"))
    }

    @Test
    fun testDebtRecovery_AgingAnalysisAndSettlementPlan() {
        val clients = DemoDataProvider.initialClients
        val agingAnalyses = DebtRecoveryManager.analyzeAgingBalances(clients)

        assertFalse(agingAnalyses.isEmpty())

        // Client litige Restaurant Le Tsinjo (dette > 30 jours)
        val tsinjo = agingAnalyses.find { it.clientId == "cli_restau_tsinjo" }
        assertNotNull(tsinjo)
        assertTrue("Le client avec impayé > 30j doit avoir les livraisons bloquées", tsinjo!!.isDeliveryBlocked)
        assertEquals(
            "Le protocole transactionnel doit être recommandé pour retard > 30j",
            DunningLevel.STAGE_4_SETTLEMENT_PLAN,
            tsinjo.recommendedDunningLevel
        )

        // Génération du plan d'apurement avec 30% d'acompte et 3 tranches
        val tsinjoClient = clients.first { it.id == "cli_restau_tsinjo" }
        val plan = DebtRecoveryManager.generateSettlementPlan(
            client = tsinjoClient,
            upfrontDepositPercent = 30.0,
            installmentsCount = 3
        )

        assertEquals(4_200_000.0, plan.totalDebtAr, 0.01)
        assertEquals(1_260_000.0, plan.immediateUpfrontDepositAr, 0.01) // 30% de 4.2M
        assertEquals(2_940_000.0, plan.remainingBalanceAr, 0.01)
        assertEquals(3, plan.installments.size)
        assertEquals(980_000.0, plan.installments[0].amountAr, 0.01) // 2.94M / 3 = 980 000 Ar
        assertTrue("La condition de réapprovisionnement doit exiger le comptoir", plan.resumptionOfDeliveriesCondition.contains("comptoir"))

        // Courrier officiel de mise en demeure
        val letter = DebtRecoveryManager.buildFormalDunningLetter(tsinjoClient)
        assertTrue("La lettre doit contenir la mention MISE EN DEMEURE", letter.contains("MISE EN DEMEURE"))
        assertTrue("La lettre doit mentionner le montant", letter.contains("4 200 000"))
        assertTrue("La lettre doit mentionner le délai de 8 jours", letter.contains("HUIT (8) JOURS OUVRÉS"))
        assertTrue("La lettre doit mentionner le NIF de Sambatra", letter.contains("4001289345"))
    }

    @Test
    fun testCommercialDocuments_DeliveryNoteAndInventoryAudit() {
        val client = DemoDataProvider.initialClients.first()
        val products = DemoDataProvider.initialProducts
        val lots = DemoDataProvider.initialLots

        // Bon de Livraison
        val blData = CommercialDocumentGenerator.createSampleDeliveryNote(client, products, lots)
        assertNotNull(blData.deliveryNumber)
        assertTrue("Le BL doit contenir la plaque du camion", blData.vehiclePlate.contains("TAB"))
        assertTrue("Le BL doit contenir le numéro de scellé", blData.sealNumber.startsWith("SC-"))
        assertTrue("Le BL doit avoir au moins un article", blData.items.isNotEmpty())
        assertTrue("Le BL doit contenir la règle de pesée contradictoire", blData.notesAndReservations.isNotEmpty())
        assertTrue("Le poids total net doit être calculé", blData.totalNetWeightKg > 0.0)

        // Fiche d'inventaire et contrôle de freinte
        val auditData = CommercialDocumentGenerator.createSampleMonthlyAuditData(products)
        assertNotNull(auditData.inventoryNumber)
        assertFalse(auditData.lines.isEmpty())

        // Vérification de la freinte
        assertTrue("Le taux global de freinte doit être inférieur au plafond de 1.20%", auditData.overallShrinkageRatePercent <= 1.20)
        assertTrue("La conclusion de l'audit doit certifier la conformité", auditData.auditConclusion.contains("Conforme"))
        val riceLine = auditData.lines.first { it.productCode == "RIZ" }
        assertTrue("L'écart sur le riz doit être qualifié de freinte normale", riceLine.isVarianceNormal)
    }
}
