package com.example.data.finance

/**
 * Modèles financiers pour le calcul de marge bénéficiaire nette en temps réel
 * selon les normes strictes du Guide de Référence 2026 (Ch. 3, 6 & 11).
 */
data class NetMarginInputs(
    val productCode: String,
    val productName: String,
    val lotNumber: String,
    val quantityKg: Double,
    val purchasePricePerKgAr: Double, // Prix d'achat bord-champ ou fournisseur
    val sellingPricePerKgAr: Double, // Prix de vente client négocié
    val transportDistanceKm: Double, // Distance de livraison (km)
    val fuelRatePerKmAr: Double = 4500.0, // Tarif carburant/fret selon véhicule (Ar/km)
    val holdingDurationDays: Int, // Nombre de jours de stockage en entrepôt
    val dailyWarehouseCostPerTonAr: Double = 800.0, // Coût quotidien de magasinage/manutention par tonne (Ar)
    val packagingCostPerKgAr: Double = 35.0, // Coût des sacs polypropylène / sacs étanches au kg
    val observedLossPercent: Double = 0.50, // Freinte de transport & stockage constatée (%)
    val monthlyCapitalInterestRatePercent: Double = 1.5, // Coût financier d'immobilisation de trésorerie (%/mois)
    val fixedOverheadPerKgAr: Double = 60.0 // Quote-part des charges fixes de structure (loyer, salaires, balance)
)

data class NetMarginDetailedBreakdown(
    val productCode: String,
    val productName: String,
    val totalRevenueAr: Double, // Chiffre d'affaires brut
    val purchaseCostAr: Double, // Coût d'achat initial
    val transportCostAr: Double, // Fret et transport routier
    val warehouseHoldingCostAr: Double, // Frais de stockage & manutention
    val packagingCostAr: Double, // Coût des emballages
    val lossAndShrinkageCostAr: Double, // Valeur financière de la freinte
    val financialHoldingCostAr: Double, // Coût d'opportunité / financement du stock
    val fixedOverheadCostAr: Double, // Charges fixes réparties
    
    val totalCostPriceAr: Double, // Prix de revient complet rendu client (PRC)
    val costPricePerKgAr: Double, // Prix de revient au kg
    
    val grossMarginAr: Double, // Marge brute = CA - Achat
    val grossMarginPercent: Double,
    
    val netMarginAr: Double, // Marge nette = CA - PRC
    val netMarginPerKgAr: Double,
    val netMarginPercent: Double, // Marge nette en % du CA
    
    val targetMarginPercent: Double, // Marge cible Guide 2026 (ex: 14% grains, 22% épices)
    val isTargetAchieved: Boolean,
    val profitabilityRating: ProfitabilityRating,
    val strategicAdvice: String
)

enum class ProfitabilityRating(val labelFr: String, val colorHex: Long) {
    EXCELLENT("Rentabilité Supérieure", 0xFF15803D), // > 18%
    CONFORME("Conforme Guide 2026", 0xFF16A34A), // 12% - 18%
    VIGILANCE_FAIBLE("Marge Serrée / Risque", 0xFFD97706), // 6% - 12%
    DEFICITAIRE("Opération Déficitaire", 0xFFDC2626) // < 6% ou négatif
}
