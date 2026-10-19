package com.example.data.closing

import com.example.data.model.Client
import com.example.data.model.Product
import com.example.data.model.Supplier
import java.util.Locale

/**
 * Modèles pour la Clôture Annuelle, Compte d'Exploitation Général,
 * Seuil de Rentabilité, Audit de Performance Opérationnelle et Budget N+1.
 * Conforme au Guide 2026 : Chapitre 14 (Modèle financier) & Chapitre 16 (Bilan & Clôture).
 */

data class AnnualProfitLossStatement(
    val fiscalYear: Int,
    val companyName: String = "Sambatra Négoce SARL",
    val nif: String = "4001289345",
    val stat: String = "51901 11 2024 0 00123",
    // Chiffre d'Affaires
    val grossTurnoverAr: Double,
    val volumeTotalSoldKg: Double,
    // Coût des Marchandises Vendues (CAMV valorisé au CMUP)
    val costOfGoodsSoldAr: Double,
    // Marge Brute Commerciale
    val grossMarginAr: Double,
    val grossMarginRatePercent: Double,
    // Coûts Directs Variables d'Exploitation
    val logisticsAndTransportAr: Double,
    val totalStorageCarryingCostAr: Double, // 2.5% à 3% par mois selon Chap. 6
    val realShrinkageLossAr: Double,       // Pertes et freinte réelle constatée
    val badDebtsWriteOffAr: Double,        // Créances irrécouvrables passées en pertes
    val totalVariableExpensesAr: Double,
    // Marge sur Coûts Variables (MCV)
    val contributionMarginAr: Double,
    val contributionMarginPercent: Double,
    // Charges Fixes Annuelles (Entrepôt 400m², salaires, assurances, énergie)
    val warehouseRentAr: Double,
    val salariesAndSocialAr: Double,
    val generalAdminFixedExpensesAr: Double,
    val totalFixedExpensesAr: Double,
    // Résultat d'Exploitation (EBE / EBITDA et Résultat Net)
    val operatingIncomeAr: Double, // EBE
    val netOperatingProfitAr: Double,
    val netProfitMarginPercent: Double,
    // Seuil de Rentabilité (Point Mort)
    val breakEvenTurnoverAr: Double,
    val breakEvenVolumeTons: Double,
    val breakEvenDays: Int,
    // Détail analytique par produit
    val productBreakdowns: List<ProductProfitability>
)

data class ProductProfitability(
    val productId: String,
    val productName: String,
    val category: String, // "Grains", "Légumineuses", "Épices de rente"
    val volumeSoldKg: Double,
    val turnoverAr: Double,
    val costOfPurchasesAr: Double,
    val grossMarginAr: Double,
    val grossMarginRatePercent: Double,
    val shrinkageKg: Double,
    val shrinkageLossAr: Double,
    val netContributionAr: Double,
    val shareOfTotalMarginPercent: Double
)

enum class AuditCertificationGrade {
    EXCELLENT_CONFORME,  // Score >= 85 : Félicitations, certification sans réserve
    CONFORME_AVEC_RESERVES, // Score 70 - 84 : Conforme avec plan d'action d'amélioration
    NON_CONFORME_RISQUE_ELEVE // Score < 70 : Risque de cessation ou de perte majeure de capital
}

data class OperationalAuditScorecard(
    val fiscalYear: Int,
    val overallScore: Int, // Sur 100
    val certificationGrade: AuditCertificationGrade,
    val pillars: List<AuditPillarScore>,
    val verifiedRules: List<VerifiedGoldenRule>,
    val generalAuditorAppreciation: String,
    val mandatoryNextYearActionPlan: List<String>
)

data class AuditPillarScore(
    val name: String,
    val score: Int,
    val maxScore: Int,
    val status: String,
    val details: String
)

data class VerifiedGoldenRule(
    val ruleTitle: String,
    val referenceChapter: String,
    val expectedTarget: String,
    val achievedValue: String,
    val isCompliant: Boolean,
    val comment: String
)

data class BudgetForecastScenario(
    val targetYear: Int,
    val initialTreasuryAr: Double,
    val projectedTurnoverAr: Double,
    val expectedGrossMarginAr: Double,
    val fixedExpensesProjectedAr: Double,
    val projectedNetIncomeAr: Double,
    // Allocation des fonds propres pour la grande collecte
    val allocatedPaddyAndRiceEquityAr: Double,
    val allocatedLegumesEquityAr: Double,
    val allocatedSpicesSpeculativeEquityAr: Double,
    val maxSpeculativeAuthorizedAr: Double, // 10% des fonds propres (Chap. 15, Cas 10)
    val isSpeculativeExceeded: Boolean,
    val recommendedCreditLineNeedAr: Double
)

data class BackupArchiveSummary(
    val exportTimestamp: Long,
    val exportDateFormatted: String,
    val totalProductsExported: Int,
    val totalLotsExported: Int,
    val totalClientsExported: Int,
    val totalSuppliersExported: Int,
    val totalInventoryValueAr: Double,
    val backupChecksum: String,
    val statusMessage: String
)
