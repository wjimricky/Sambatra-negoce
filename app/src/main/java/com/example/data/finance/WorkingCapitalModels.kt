package com.example.data.finance

import com.example.data.model.Client
import com.example.data.model.Product
import com.example.data.model.Supplier
import java.util.Locale

enum class WorkingCapitalStatus {
    HEALTHY,    // BFR < 45 jours, Trésorerie > 2 mois
    VIGILANCE,  // BFR 45-55 jours ou Trésorerie 1.5 - 2 mois
    CRITICAL    // BFR > 55 jours ou Trésorerie < 1.5 mois
}

data class WorkingCapitalDiagnosis(
    val stockValueAr: Double,
    val accountsReceivableAr: Double,
    val supplierPayablesAr: Double,
    val workingCapitalAr: Double, // (Stocks + Créances) - Dettes
    val monthlyTurnoverAr: Double,
    val workingCapitalDaysOfSales: Double, // (BFR / CA mensuel) * 30
    val targetWorkingCapitalDays: Double = 45.0, // Norme Guide 2026 : < 50 jours
    val treasuryAvailableAr: Double,
    val monthlyFixedChargesAr: Double,
    val fixedChargesCoverageMonths: Double, // Trésorerie / Charges fixes
    val targetCoverageMonths: Double = 2.0, // Règle d'or : min 2 mois (10M Ar)
    val status: WorkingCapitalStatus,
    val statusMessage: String,
    val keyRecommendations: List<String>
)

data class CashFlowStressScenario(
    val scenarioName: String,
    val commodityName: String,
    val purchaseVolumeTons: Double,
    val unitPriceAr: Double,
    val purchaseCostAr: Double,
    val upfrontCashPercentage: Double,
    val cashOutflowImmediateAr: Double,
    val resultingTreasuryAr: Double,
    val resultingCoverageMonths: Double,
    val isIlliquidityRisk: Boolean,
    val alertMessage: String,
    val mitigationStrategy: String
)

object WorkingCapitalEngine {

    const val DEFAULT_FIXED_CHARGES_AR = 5_000_000.0 // 5M Ar/mois (loyer, salaires, énergie)
    const val DEFAULT_MONTHLY_TURNOVER_AR = 41_200_000.0 // CA modèle Sambatra Négoce

    /**
     * Calcule le diagnostic complet du BFR et de la trésorerie selon le Chapitre 12 & 15 du Guide 2026.
     */
    fun calculateWorkingCapital(
        products: List<Product>,
        clients: List<Client>,
        suppliers: List<Supplier>,
        currentTreasuryAr: Double,
        monthlyFixedChargesAr: Double = DEFAULT_FIXED_CHARGES_AR,
        monthlyTurnoverAr: Double = DEFAULT_MONTHLY_TURNOVER_AR
    ): WorkingCapitalDiagnosis {
        // 1. Valorisation du stock au CMUP
        val stockValueAr = products.sumOf { it.currentStockKg * it.cmupCostAr }

        // 2. Créances clients totales en cours
        val receivablesAr = clients.sumOf { it.currentDebtAr }

        // 3. Dettes fournisseurs (avances à recouvrer déduites ou factures à régler)
        val payablesAr = suppliers.sumOf { it.currentAdvancesAr }

        // 4. BFR d'exploitation = (Stocks + Créances clients) - Dettes fournisseurs
        val bfrAr = (stockValueAr + receivablesAr) - payablesAr

        // 5. BFR en jours de Chiffre d'Affaires
        val bfrDays = if (monthlyTurnoverAr > 0) (bfrAr / monthlyTurnoverAr) * 30.0 else 0.0

        // 6. Couverture des charges fixes par la trésorerie disponible
        val coverageMonths = if (monthlyFixedChargesAr > 0) currentTreasuryAr / monthlyFixedChargesAr else 0.0

        val status = when {
            coverageMonths < 1.8 || bfrDays > 50.0 -> WorkingCapitalStatus.CRITICAL
            coverageMonths < 2.0 || bfrDays > 45.0 -> WorkingCapitalStatus.VIGILANCE
            else -> WorkingCapitalStatus.HEALTHY
        }

        val statusMessage = when (status) {
            WorkingCapitalStatus.CRITICAL -> "ALERTE TRÉSORERIE : Trésorerie (${String.format(Locale.US, "%.1f", coverageMonths)} mois) sous le seuil d'alerte des 2 mois. BFR élevé à ${bfrDays.toInt()} jours."
            WorkingCapitalStatus.VIGILANCE -> "VIGILANCE : Trésorerie proche du coussin de sécurité. Surveiller les encaissements clients."
            WorkingCapitalStatus.HEALTHY -> "SITUATION SAINE : Couverture de charges (${String.format(Locale.US, "%.1f", coverageMonths)} mois) et BFR maîtrisés selon les normes 2026."
        }

        val recs = mutableListOf<String>()
        if (coverageMonths < 2.0) {
            val deficit = (2.0 * monthlyFixedChargesAr) - currentTreasuryAr
            recs.add("Reconstituer d'urgence la réserve de sécurité de 10 000 000 Ar (déficit de ${String.format(Locale.US, "%,.0f", deficit).replace(',', ' ')} Ar).")
        }
        val overdueClients = clients.filter { it.debtOver30DaysAr > 0 }
        if (overdueClients.isNotEmpty()) {
            val totalOverdue = overdueClients.sumOf { it.debtOver30DaysAr }
            recs.add("Recouvrer prioritairement les ${String.format(Locale.US, "%,.0f", totalOverdue).replace(',', ' ')} Ar de créances à plus de 30 jours (bloquer toute nouvelle livraison).")
        }
        if (bfrDays > 45.0) {
            recs.add("Accélérer la rotation des stocks les plus lourds (ex: maïs et paddy) pour libérer du cash d'exploitation.")
        }
        recs.add("Règle d'or p. 81 : Ne jamais financer un stock spéculatif de soudure avec la trésorerie des charges fixes.")

        return WorkingCapitalDiagnosis(
            stockValueAr = stockValueAr,
            accountsReceivableAr = receivablesAr,
            supplierPayablesAr = payablesAr,
            workingCapitalAr = bfrAr,
            monthlyTurnoverAr = monthlyTurnoverAr,
            workingCapitalDaysOfSales = bfrDays,
            treasuryAvailableAr = currentTreasuryAr,
            monthlyFixedChargesAr = monthlyFixedChargesAr,
            fixedChargesCoverageMonths = coverageMonths,
            status = status,
            statusMessage = statusMessage,
            keyRecommendations = recs
        )
    }

    /**
     * Simule l'impact d'une décision d'achat ou de campagne sur la trésorerie nette.
     */
    fun simulatePurchaseCashStress(
        scenarioName: String,
        commodityName: String,
        purchaseVolumeTons: Double,
        unitPriceAr: Double,
        upfrontCashPercentage: Double,
        currentTreasuryAr: Double,
        monthlyFixedChargesAr: Double = DEFAULT_FIXED_CHARGES_AR
    ): CashFlowStressScenario {
        val totalCost = purchaseVolumeTons * 1000.0 * unitPriceAr
        val upfrontPaid = totalCost * (upfrontCashPercentage / 100.0)
        val resultingTreasury = currentTreasuryAr - upfrontPaid
        val coverageMonths = if (monthlyFixedChargesAr > 0) resultingTreasury / monthlyFixedChargesAr else 0.0

        val isRisk = resultingTreasury < (2.0 * monthlyFixedChargesAr)
        val alert = if (isRisk) {
            "RISQUE D'ASPHYXIE TRÉSORERIE : Après paiement comptant de ${String.format(Locale.US, "%,.0f", upfrontPaid).replace(',', ' ')} Ar, la trésorerie chute à ${String.format(Locale.US, "%,.0f", resultingTreasury).replace(',', ' ')} Ar (${String.format(Locale.US, "%.1f", coverageMonths)} mois de charges fixes, seuil minimal = 2.0 mois)."
        } else {
            "Capacité d'achat validée : La réserve de sécurité de 2 mois de charges fixes reste préservée après décaissement."
        }

        val strategy = if (isRisk) {
            "Négocier un acompte maximal de 30% à la pesée et le solde à 15 jours, ou s'adosser à un contrat de vente ferme adossé."
        } else {
            "Procéder à l'achat tout en maintenant la veille stricte sur les échéances clients du mois."
        }

        return CashFlowStressScenario(
            scenarioName = scenarioName,
            commodityName = commodityName,
            purchaseVolumeTons = purchaseVolumeTons,
            unitPriceAr = unitPriceAr,
            purchaseCostAr = totalCost,
            upfrontCashPercentage = upfrontCashPercentage,
            cashOutflowImmediateAr = upfrontPaid,
            resultingTreasuryAr = resultingTreasury,
            resultingCoverageMonths = coverageMonths,
            isIlliquidityRisk = isRisk,
            alertMessage = alert,
            mitigationStrategy = strategy
        )
    }
}
