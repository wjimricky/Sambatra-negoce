package com.example

import com.example.data.closing.AnnualClosingEngine
import com.example.data.closing.AuditCertificationGrade
import com.example.data.local.DemoDataProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class Step5AnnualClosingAndAuditTest {

    @Test
    fun testAnnualPnlCalculation_SambatraNegoceModel() {
        val products = DemoDataProvider.initialProducts
        val lots = DemoDataProvider.initialLots
        val clients = DemoDataProvider.initialClients

        val pnl = AnnualClosingEngine.calculateAnnualPnl(
            products = products,
            lots = lots,
            clients = clients
        )

        assertEquals("Sambatra Négoce SARL", pnl.companyName)
        assertEquals("4001289345", pnl.nif)
        assertEquals(2026, pnl.fiscalYear)

        // Chiffre d'Affaires annuel et Marge Brute
        assertTrue("Le CA annuel global doit dépasser 200 000 000 Ar", pnl.grossTurnoverAr > 200_000_000.0)
        assertTrue("Le volume annuel vendu doit dépasser 50 000 kg", pnl.volumeTotalSoldKg > 50_000.0)
        assertTrue("La marge brute commerciale doit être positive", pnl.grossMarginAr > 40_000_000.0)
        assertTrue("Le taux de marge brute doit être d'au moins 15%", pnl.grossMarginRatePercent >= 15.0)

        // Coûts variables d'exploitation
        assertTrue("Le coût de stockage (2.5%/mois) doit être pris en compte", pnl.totalStorageCarryingCostAr > 0.0)
        assertTrue("La perte sur freinte réelle doit être déduite", pnl.realShrinkageLossAr > 0.0)
        assertTrue("Les frais de logistique et transport doivent être inclus", pnl.logisticsAndTransportAr > 10_000_000.0)

        // Charges fixes (Entrepôt 400m², salaires, charges générales)
        assertEquals(60_000_000.0, pnl.totalFixedExpensesAr, 0.01)

        // Résultat d'Exploitation et Résultat Net
        assertTrue("L'EBE doit être calculé", pnl.operatingIncomeAr > 0.0)
        assertTrue("Le résultat net après impôt doit être calculé", pnl.netOperatingProfitAr > 0.0)
        assertTrue("La rentabilité nette sur CA doit être positive", pnl.netProfitMarginPercent > 0.0)

        // Seuil de rentabilité (Point Mort)
        assertTrue("Le CA seuil de rentabilité doit être supérieur à 50 000 000 Ar", pnl.breakEvenTurnoverAr > 50_000_000.0)
        assertTrue("Le tonnage au point mort doit être positif", pnl.breakEvenVolumeTons > 0.0)
        assertTrue("Le point mort doit être atteint en moins de 365 jours", pnl.breakEvenDays in 1..364)

        // Décomposition par produit
        assertEquals("Les 8 commodités doivent être analysées", 8, pnl.productBreakdowns.size)
        val riceMakalioka = pnl.productBreakdowns.find { it.productName.contains("Makalioka", ignoreCase = true) }
        assertNotNull(riceMakalioka)
        assertTrue("Le riz Makalioka doit générer une contribution positive", riceMakalioka!!.grossMarginAr > 0.0)
        assertTrue("La part de marge de chaque produit doit être calculée", pnl.productBreakdowns.all { it.shareOfTotalMarginPercent >= 0.0 })
    }

    @Test
    fun testAuditScorecard_GoldenRulesCompliance() {
        val products = DemoDataProvider.initialProducts
        val lots = DemoDataProvider.initialLots
        val clients = DemoDataProvider.initialClients
        val treasuryAr = 10_000_000.0 // 2 mois de charges fixes

        val scorecard = AnnualClosingEngine.generateAnnualAuditScorecard(
            products = products,
            lots = lots,
            clients = clients,
            treasuryAr = treasuryAr
        )

        assertEquals(2026, scorecard.fiscalYear)
        assertTrue("Le score global doit être compris entre 0 et 100", scorecard.overallScore in 0..100)
        assertTrue("Le score d'audit doit être suffisant pour certification", scorecard.overallScore >= 70)
        assertNotEquals(AuditCertificationGrade.NON_CONFORME_RISQUE_ELEVE, scorecard.certificationGrade)

        // Vérification des 4 Piliers
        assertEquals("La scorecard doit comporter 4 piliers fondamentaux", 4, scorecard.pillars.size)
        assertTrue("Le pilier Stocks & Conservation doit être présent", scorecard.pillars.any { it.name.contains("Stocks") })
        assertTrue("Le pilier Trésorerie doit être présent", scorecard.pillars.any { it.name.contains("Trésorerie") })
        assertTrue("Le pilier Crédit & Recouvrement doit être présent", scorecard.pillars.any { it.name.contains("Crédit") })
        assertTrue("Le pilier Achats & Arbitrages doit être présent", scorecard.pillars.any { it.name.contains("Achats") })

        // Règles d'or vérifiées
        assertTrue("Au moins 4 règles d'or doivent être auditées", scorecard.verifiedRules.size >= 4)
        val treasuryRule = scorecard.verifiedRules.find { it.ruleTitle.contains("2 mois de trésorerie") }
        assertNotNull(treasuryRule)
        assertTrue("La règle des 2 mois avec 10M Ar doit être conforme", treasuryRule!!.isCompliant)

        val shrinkageRule = scorecard.verifiedRules.find { it.ruleTitle.contains("freinte") }
        assertNotNull(shrinkageRule)
        assertTrue("La règle de freinte riz doit être conforme", shrinkageRule!!.isCompliant)

        val speculativeRule = scorecard.verifiedRules.find { it.ruleTitle.contains("spéculatif") }
        assertNotNull(speculativeRule)
        assertTrue("La règle des 10% spéculatif max doit être conforme", speculativeRule!!.isCompliant)
    }

    @Test
    fun testBudgetForecastN1_SpeculativeCapRule() {
        val equityAr = 120_000_000.0 // Fonds propres Sambatra Négoce

        // Cas 1 : Respect du plafond (8M Ar <= 12M Ar = 10% de 120M)
        val scenarioCompliant = AnnualClosingEngine.simulateNextYearBudget(
            currentEquityAr = equityAr,
            targetGrowthPercent = 15.0,
            spicesSpeculativeBudgetAr = 8_000_000.0
        )

        assertEquals(2027, scenarioCompliant.targetYear)
        assertEquals(12_000_000.0, scenarioCompliant.maxSpeculativeAuthorizedAr, 0.01)
        assertFalse("8M Ar ne dépasse pas les 10% de fonds propres", scenarioCompliant.isSpeculativeExceeded)
        assertTrue("La marge prévisionnelle doit être supérieure à 80M Ar", scenarioCompliant.expectedGrossMarginAr > 80_000_000.0)

        // Cas 2 : Violation du plafond spéculatif (16M Ar > 12M Ar -> Alerte Cas 10)
        val scenarioExceeded = AnnualClosingEngine.simulateNextYearBudget(
            currentEquityAr = equityAr,
            targetGrowthPercent = 15.0,
            spicesSpeculativeBudgetAr = 16_000_000.0
        )

        assertTrue("16M Ar dépasse les 10% de fonds propres et doit déclencher l'alerte Cas 10", scenarioExceeded.isSpeculativeExceeded)
    }

    @Test
    fun testDataBackupAndArchiveSummary() {
        val products = DemoDataProvider.initialProducts
        val lots = DemoDataProvider.initialLots
        val clients = DemoDataProvider.initialClients
        val suppliers = DemoDataProvider.initialSuppliers

        val backup = AnnualClosingEngine.generateBackupArchiveSummary(
            products = products,
            lots = lots,
            clients = clients,
            suppliers = suppliers
        )

        assertEquals(8, backup.totalProductsExported)
        assertEquals(lots.size, backup.totalLotsExported)
        assertEquals(clients.size, backup.totalClientsExported)
        assertEquals(suppliers.size, backup.totalSuppliersExported)
        assertTrue("La valorisation globale du stock doit dépasser 100 000 000 Ar", backup.totalInventoryValueAr > 100_000_000.0)
        assertTrue("Le checksum doit débuter par SN-", backup.backupChecksum.startsWith("SN-"))
        assertTrue("Le message de statut doit certifier la clôture 2026", backup.statusMessage.contains("2026"))
    }
}
