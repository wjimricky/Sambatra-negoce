package com.example.data.finance

/**
 * Moteur de calcul de marge bénéficiaire nette en temps réel (Guide de Référence 2026, Ch. 3 & 11).
 * Intègre les coûts d'achat, frais logistiques fluctuants, durée de magasinage, freintes
 * et coût d'immobilisation de la trésorerie.
 */
object NetMarginCalculator {

    fun calculateNetMargin(inputs: NetMarginInputs): NetMarginDetailedBreakdown {
        val qty = inputs.quantityKg
        val weightInTons = qty / 1000.0

        // 1. Chiffre d'Affaires Brut (en tenant compte de la perte de volume vendable due à la freinte)
        val sellableQtyKg = qty * (1.0 - (inputs.observedLossPercent / 100.0))
        val totalRevenueAr = sellableQtyKg * inputs.sellingPricePerKgAr

        // 2. Coût d'Achat Brut
        val purchaseCostAr = qty * inputs.purchasePricePerKgAr

        // 3. Frais de Transport / Logistique Fluctuants
        val transportCostAr = inputs.transportDistanceKm * inputs.fuelRatePerKmAr

        // 4. Coût d'Entreposage & Magasinage
        val warehouseCostAr = weightInTons * inputs.dailyWarehouseCostPerTonAr * inputs.holdingDurationDays

        // 5. Coût des Emballages et Conditionnement
        val packagingCostAr = qty * inputs.packagingCostPerKgAr

        // 6. Valeur financière de la Freinte (perte de matière)
        val lostWeightKg = qty * (inputs.observedLossPercent / 100.0)
        val lossCostAr = lostWeightKg * inputs.purchasePricePerKgAr

        // 7. Coût financier d'Immobilisation du Capital (Trésorerie engagée x durée x taux)
        val capitalEngagedAr = purchaseCostAr + transportCostAr
        val monthlyRate = inputs.monthlyCapitalInterestRatePercent / 100.0
        val dailyRate = monthlyRate / 30.0
        val financialCostAr = capitalEngagedAr * dailyRate * inputs.holdingDurationDays

        // 8. Quote-part des Charges Fixes de Structure
        val fixedOverheadCostAr = qty * inputs.fixedOverheadPerKgAr

        // 9. Prix de Revient Complet (PRC)
        val totalCostPriceAr = purchaseCostAr + transportCostAr + warehouseCostAr +
                packagingCostAr + lossCostAr + financialCostAr + fixedOverheadCostAr

        val costPricePerKgAr = if (sellableQtyKg > 0) totalCostPriceAr / sellableQtyKg else 0.0

        // 10. Marges Brute et Nette
        val grossMarginAr = totalRevenueAr - purchaseCostAr
        val grossMarginPercent = if (totalRevenueAr > 0) (grossMarginAr / totalRevenueAr) * 100.0 else 0.0

        val netMarginAr = totalRevenueAr - totalCostPriceAr
        val netMarginPerKgAr = if (sellableQtyKg > 0) netMarginAr / sellableQtyKg else 0.0
        val netMarginPercent = if (totalRevenueAr > 0) (netMarginAr / totalRevenueAr) * 100.0 else 0.0

        // 11. Comparaison avec la cible Guide 2026
        val isSpice = inputs.productCode.startsWith("GIR") || inputs.productCode.startsWith("POI") ||
                inputs.productCode.startsWith("GIN") || inputs.productCode.startsWith("CUR")
        val targetMarginPercent = if (isSpice) 20.0 else 14.0

        val rating = when {
            netMarginPercent >= 18.0 -> ProfitabilityRating.EXCELLENT
            netMarginPercent >= targetMarginPercent -> ProfitabilityRating.CONFORME
            netMarginPercent >= 6.0 -> ProfitabilityRating.VIGILANCE_FAIBLE
            else -> ProfitabilityRating.DEFICITAIRE
        }

        val advice = when (rating) {
            ProfitabilityRating.EXCELLENT ->
                "Opération très lucrative. Les frais de rotation rapide (${inputs.holdingDurationDays}j) préservent la marge nette."
            ProfitabilityRating.CONFORME ->
                "Conforme à la cible du Guide 2026 (${targetMarginPercent}%). Les coûts logistiques et de stockage sont bien maîtrisés."
            ProfitabilityRating.VIGILANCE_FAIBLE ->
                "Marge nette sous tension (${String.format("%.1f", netMarginPercent)}%). Le fret routier (${String.format("%,d", transportCostAr.toLong())} Ar) ou la durée de stockage pèsent lourdement."
            ProfitabilityRating.DEFICITAIRE ->
                "ALERTE VENTE À PERTE : Le prix de vente (${inputs.sellingPricePerKgAr.toInt()} Ar) ne couvre pas le prix de revient complet (${costPricePerKgAr.toInt()} Ar/kg). Réviser le prix ou massifier le transport."
        }

        return NetMarginDetailedBreakdown(
            productCode = inputs.productCode,
            productName = inputs.productName,
            totalRevenueAr = totalRevenueAr,
            purchaseCostAr = purchaseCostAr,
            transportCostAr = transportCostAr,
            warehouseHoldingCostAr = warehouseCostAr,
            packagingCostAr = packagingCostAr,
            lossAndShrinkageCostAr = lossCostAr,
            financialHoldingCostAr = financialCostAr,
            fixedOverheadCostAr = fixedOverheadCostAr,
            totalCostPriceAr = totalCostPriceAr,
            costPricePerKgAr = Math.round(costPricePerKgAr * 10.0) / 10.0,
            grossMarginAr = grossMarginAr,
            grossMarginPercent = Math.round(grossMarginPercent * 10.0) / 10.0,
            netMarginAr = netMarginAr,
            netMarginPerKgAr = Math.round(netMarginPerKgAr * 10.0) / 10.0,
            netMarginPercent = Math.round(netMarginPercent * 10.0) / 10.0,
            targetMarginPercent = targetMarginPercent,
            isTargetAchieved = netMarginPercent >= targetMarginPercent,
            profitabilityRating = rating,
            strategicAdvice = advice
        )
    }
}
