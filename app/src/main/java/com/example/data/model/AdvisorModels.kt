package com.example.data.model

enum class KpiStatus {
    GREEN, AMBER, RED
}

data class DashboardKpi(
    val id: String,
    val domain: String, // Ventes, Marge, Stock, Qualité, Crédit, Trésorerie, Logistique, Équipe
    val title: String,
    val valueDisplay: String,
    val targetDisplay: String,
    val alertThreshold: String,
    val status: KpiStatus,
    val explanation: String
)

data class AdvisorAlert(
    val id: String,
    val title: String,
    val description: String,
    val rationaleBook: String, // Justification tirée du livre
    val recommendedActions: List<String>,
    val priority: KpiStatus,
    val sourceChapter: String
)

data class PurchaseDecisionCriteria(
    val hasFullCostPriceCalculated: Boolean, // Q1
    val isTargetMarginAchievable: Boolean,    // Q2
    val hasIdentifiedBuyerOrContract: Boolean,// Q3
    val isQualityControlledPrePayment: Boolean,// Q4
    val isStorageDurationSafe: Boolean,       // Q5
    val isExposureWithinLimits: Boolean,      // Q6 (<10% equity for volatile, <30% treasury)
    val doesTreasuryRemainAboveTwoMonths: Boolean, // Q7 (>= 2 mois de charges)
    val hasSecondSupplierAvailable: Boolean   // Q8
) {
    val firstFourValid: Boolean
        get() = hasFullCostPriceCalculated && isTargetMarginAchievable &&
                hasIdentifiedBuyerOrContract && isQualityControlledPrePayment

    val allValid: Boolean
        get() = firstFourValid && isStorageDurationSafe && isExposureWithinLimits &&
                doesTreasuryRemainAboveTwoMonths && hasSecondSupplierAvailable
}

data class PurchaseEvaluationResult(
    val verdict: String,
    val canProceed: Boolean,
    val requiredConditions: List<String>,
    val bookReference: String
)

data class CaseStudy(
    val caseNumber: Int,
    val title: String,
    val product: String,
    val problem: String,
    val numbers: String,
    val wrongAction: String,
    val whatShouldBeDone: String,
    val keyLesson: String,
    val chapter: String
)

data class ChecklistItem(
    val id: String,
    val category: String,
    val text: String,
    val isChecked: Boolean = false,
    val note: String = ""
)

enum class NiveauRisqueAchat(val label: String, val couleur: String) {
    FAIBLE("Risque Faible - Achat Recommandé", "GREEN"),
    MODERE("Risque Modéré - Surveillance Requise", "AMBER"),
    ELEVE("Risque Élevé - Conditions Strictes", "ORANGE"),
    CRITIQUE("Risque Critique - Achat Déconseillé", "RED")
}

data class SimulationImpactFinancier(
    val montantAchatAr: Double,
    val dureeStockageMois: Double,
    val tauxPortageMensuelPercent: Double,
    val tauxFreinteMensuelPercent: Double,
    val fraisMagasinageManutentionAr: Double,
    val chargesFixesMensuellesAr: Double,
    val tresorerieInitialeAr: Double,
    val tresorerieResiduelleAr: Double,
    val moisChargesResiduels: Double,
    val seuilSecurite2MoisAr: Double,
    val coutPortageFinancierAr: Double,
    val coutFreinteEtPertesAr: Double,
    val coutTotalSurcoutStockageAr: Double,
    val coutRevientCompletAchatAr: Double,
    val surcoutPourcentage: Double,
    val prixVentePrevisionnelAr: Double,
    val margeBruteInitialeAr: Double,
    val margeBruteFinaleAr: Double,
    val margeBruteFinalePercent: Double,
    val erosionMargePercent: Double,
    val niveauRisque: NiveauRisqueAchat,
    val diagnosticTreso: String,
    val conseilsRisqueGuide: List<String>,
    val syntheseRecommandation: String
)
