package com.example.data.model

enum class ProductType(val labelFr: String, val labelMg: String) {
    GRAIN("Grain", "Voamaina / Vary"),
    SPICE("Épice", "Zavamanitra")
}

enum class AppTab(val titleFr: String, val titleMg: String) {
    DASHBOARD("Tableau de bord", "Biraon'ny orinasa"),
    ADVISOR("Conseiller IA", "Mpanolotsaina AI"),
    STOCKS("Stocks & Lots", "Tahiry & Loko"),
    CLIENTS("Clients & Crédits", "Mpanjifa & Trosa"),
    TOOLS("Checklists & Cas", "Fitaovana & Ohatra")
}

enum class AbcClass {
    A, B, C
}

data class Product(
    val id: String,
    val code: String,
    val name: String,
    val nameMg: String,
    val type: ProductType,
    val abcClass: AbcClass,
    val currentStockKg: Double,
    val cmupCostAr: Double, // Coût Moyen Unitaire Pondéré
    val wholesalePriceAr: Double,
    val targetCoverageDays: Int,
    val dailyConsumptionKg: Double,
    val maxMoisturePercent: Double,
    val minOrderKg: Double,
    val packagingFormat: String,
    val warehouseZone: String,
    val specificRisks: String
) {
    val coverageDays: Int
        get() = if (dailyConsumptionKg > 0) (currentStockKg / dailyConsumptionKg).toInt() else 0

    val marginPercent: Double
        get() = if (wholesalePriceAr > 0) ((wholesalePriceAr - cmupCostAr) / wholesalePriceAr) * 100.0 else 0.0

    val stockValueAr: Double
        get() = currentStockKg * cmupCostAr
}

enum class QualityDecision(val label: String, val colorCode: String) {
    CONFORME("Accepté conforme", "GREEN"),
    DECOTE("Accepté avec décote", "AMBER"),
    QUARANTAINE("Mis en quarantaine", "ORANGE"),
    REFUSE("Refusé / Retourné", "RED")
}

data class BatchLot(
    val lotNumber: String, // ex: GIR-ANA-260915-03
    val productId: String,
    val productName: String,
    val supplierName: String,
    val originRegion: String, // ex: Analanjirofo, Alaotra-Mangoro, Vakinankaratra
    val receptionDate: String,
    val quantityKg: Double,
    val netReceivedKg: Double,
    val moisturePercent: Double,
    val impuritiesPercent: Double,
    val bagsCount: Int,
    val weighedBagsCount: Int, // au moins 10% pesés contradictoirement
    val decision: QualityDecision,
    val sealedCounterSampleId: String, // Contre-échantillon scellé obligatoire
    val warehouseZone: String,
    val storageDays: Int,
    val unitCostAr: Double,
    val observations: String
)

enum class ClientSegment(val label: String, val description: String) {
    A("Segment A - Stratégique", "Gros volumes réguliers, paiement fiable. Visite régulière et contrat dédié."),
    B("Segment B - Régulier", "Volumes moyens, paiement correct. Offre standard et relance planifiée."),
    C("Segment C - Occasionnel", "Petits lots, vente au comptoir, paiement comptant obligatoire."),
    D("Segment D - À Risque", "Retards et litiges. Exiger paiement avant livraison, plafond réduit.")
}

enum class PaymentStatus(val label: String, val colorCode: String) {
    PAYE_A_TEMPS("Payé à l'échéance", "GREEN"),
    PAYE_EN_RETARD("Payé avec retard", "AMBER"),
    EN_COURS("En cours non échu", "BLUE"),
    EN_RETARD("Échu en retard", "RED"),
    LITIGE("Litige / Contentieux", "PURPLE")
}

data class PaymentHistoryEntry(
    val id: String,
    val invoiceNumber: String,
    val dateFacture: String,
    val montantAr: Double,
    val delaiAccordeJours: Int,
    val delaiReelJours: Int,
    val datePaiement: String?,
    val statut: PaymentStatus,
    val commentaire: String = ""
)

data class Client(
    val id: String,
    val name: String,
    val segment: ClientSegment,
    val contactName: String,
    val phone: String,
    val location: String,
    val monthlyVolumeKg: Double,
    val monthlyCaAr: Double,
    val creditLimitAr: Double,
    val currentDebtAr: Double,
    val debtOver30DaysAr: Double,
    val averagePaymentDays: Int,
    val creditScore25: Int, // Grille de notation sur 25 points (p. 43-44)
    val paymentHistory: List<PaymentHistoryEntry> = emptyList(),
    val isCreditBlockedManually: Boolean = false
) {
    val shareOfTotalCaPercent: Double
        get() = (monthlyCaAr / 40_000_000.0) * 100.0 // Règle max 20-25%

    val creditUtilizationPercent: Double
        get() = if (creditLimitAr > 0) (currentDebtAr / creditLimitAr) * 100.0 else if (currentDebtAr > 0) 100.0 else 0.0

    val isOverLimit: Boolean
        get() = currentDebtAr > creditLimitAr && creditLimitAr > 0

    // Règle d'or du Guide 2026 (p. 44) : Livraison suspendue si retard > 15j ou plafond dépassé
    val isBlockedByRule: Boolean
        get() = isCreditBlockedManually || debtOver30DaysAr > 0 || isOverLimit || (segment == ClientSegment.D && currentDebtAr > 0)

    val solvencyLabel: String
        get() = when {
            creditScore25 >= 21 -> "Excellente (21-25/25)"
            creditScore25 >= 16 -> "Bonne (16-20/25)"
            creditScore25 >= 12 -> "Moyenne / Vigilance (12-15/25)"
            else -> "Risque Élevé / Comptant (< 12/25)"
        }
}

data class Supplier(
    val id: String,
    val name: String,
    val region: String,
    val mainProducts: String,
    val reliabilityScore5: Double, // Grille de sélection sur 5 (p. 17)
    val complianceRatePercent: Double, // % de lots conformes
    val currentAdvancesAr: Double, // Avances en cours (p. 19)
    val phone: String,
    val isPrimarySource: Boolean
)
