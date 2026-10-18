package com.example.data.report

data class MonthlyReportKpi(
    val title: String,
    val value: String,
    val target: String,
    val status: String, // "EXCELLENT", "CONFORME", "VIGILANCE"
    val comment: String
)

data class MarginAnalysisRow(
    val productCode: String,
    val productName: String,
    val lotNumber: String,
    val quantitySoldKg: Double,
    val purchasePriceAr: Double,
    val freightAndStorageCostAr: Double,
    val fullCostPriceAr: Double,
    val salePriceAr: Double,
    val unitMarginAr: Double,
    val totalMarginAr: Double,
    val marginPercent: Double,
    val isLocomotive: Boolean
)

data class LossTrackingRow(
    val category: String, // "Freinte hygrométrique (séchage)", "Pertes de triage / vannage", "Brisures & manipulation", "Écarts d'inventaire"
    val product: String,
    val quantityLostKg: Double,
    val lossValueAr: Double,
    val realLossRatePercent: Double,
    val maxAllowedRatePercent: Double,
    val isCompliant: Boolean,
    val explanation: String
)

data class MonthlyReportData(
    val monthTitle: String,
    val period: String,
    val generationDate: String,
    val companyName: String,
    val totalTurnoverAr: Double,
    val totalGrossMarginAr: Double,
    val overallMarginPercent: Double,
    val totalLossValueAr: Double,
    val totalLossKg: Double,
    val averageLossRatePercent: Double,
    val treasuryAvailableAr: Double,
    val overdueDebtsAr: Double,
    val executiveKpis: List<MonthlyReportKpi>,
    val marginBreakdown: List<MarginAnalysisRow>,
    val lossTracking: List<LossTrackingRow>,
    val recommendations: List<String>
)
