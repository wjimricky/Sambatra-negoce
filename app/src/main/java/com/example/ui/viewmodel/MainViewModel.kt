package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAdvisorService
import com.example.data.model.*
import com.example.data.repository.GrosGrainsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date()),
    val isGroundedWithGoogleSearch: Boolean = false,
    val searchSources: List<com.example.data.ai.WebSourceCitation> = emptyList()
)

class MainViewModel(
    private val repository: GrosGrainsRepository = GrosGrainsRepository.getInstance(),
    private val advisorService: GeminiAdvisorService = GeminiAdvisorService()
) : ViewModel() {

    private val _isGoogleSearchEnabled = MutableStateFlow(true)
    val isGoogleSearchEnabled: StateFlow<Boolean> = _isGoogleSearchEnabled.asStateFlow()

    fun toggleGoogleSearch() {
        _isGoogleSearchEnabled.value = !_isGoogleSearchEnabled.value
    }

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    val products: StateFlow<List<Product>> = repository.products
    val lots: StateFlow<List<BatchLot>> = repository.lots
    val clients: StateFlow<List<Client>> = repository.clients
    val suppliers: StateFlow<List<Supplier>> = repository.suppliers
    val kpis: StateFlow<List<DashboardKpi>> = repository.kpis
    val purchaseChecklist: StateFlow<List<ChecklistItem>> = repository.purchaseChecklist
    val receptionChecklist: StateFlow<List<ChecklistItem>> = repository.receptionChecklist
    val shippingChecklist: StateFlow<List<ChecklistItem>> = repository.shippingChecklist
    val currentTreasury: StateFlow<Double> = repository.currentTreasury
    val selectedLanguage: StateFlow<String> = repository.selectedLanguage
    val syncState: StateFlow<com.example.data.sync.SyncUiState> = repository.syncState
    val zoningAudit: StateFlow<com.example.data.model.WarehouseZoningAudit> = repository.zoningAudit

    fun initRoom(context: android.content.Context) {
        repository.initializeRoomAndSync(context)
    }

    fun updateLotWarehouseZone(lotNumber: String, newZone: String) {
        repository.updateLotWarehouseZone(lotNumber, newZone)
    }

    fun triggerManualSync() {
        repository.triggerManualSync()
    }

    fun toggleOfflineSimulation() {
        repository.toggleOfflineSimulation()
    }

    fun getMonthlyReportData(monthName: String = "Octobre 2026"): com.example.data.report.MonthlyReportData {
        return repository.getMonthlyReportData(monthName)
    }

    fun generateMonthlyPdfReport(context: android.content.Context, monthName: String = "Octobre 2026"): java.io.File {
        return repository.generateMonthlyPdfReport(context, monthName)
    }

    fun shareMonthlyPdfReport(context: android.content.Context, monthName: String = "Octobre 2026") {
        val file = repository.generateMonthlyPdfReport(context, monthName)
        com.example.data.report.PdfReportGenerator.sharePdf(context, file)
    }

    fun openMonthlyPdfReport(context: android.content.Context, monthName: String = "Octobre 2026") {
        val file = repository.generateMonthlyPdfReport(context, monthName)
        com.example.data.report.PdfReportGenerator.openPdf(context, file)
    }

    val mondayAlerts: StateFlow<List<AdvisorAlert>> = MutableStateFlow(repository.getMondayMorningAlerts())
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.getMondayMorningAlerts())

    // Chat with AI Advisor
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Salama ! Je suis le Conseiller GrosGrains, votre expert virtuel basé sur le guide 2026. Comment puis-je vous aider aujourd'hui ? Vous pouvez me poser une question ou tester l'un de nos simulateurs de scénario.",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAdvisorThinking = MutableStateFlow(false)
    val isAdvisorThinking: StateFlow<Boolean> = _isAdvisorThinking.asStateFlow()

    // Scénarios & Grille d'achat
    private val _purchaseCriteria = MutableStateFlow(
        PurchaseDecisionCriteria(
            hasFullCostPriceCalculated = true,
            isTargetMarginAchievable = true,
            hasIdentifiedBuyerOrContract = true,
            isQualityControlledPrePayment = true,
            isStorageDurationSafe = true,
            isExposureWithinLimits = true,
            doesTreasuryRemainAboveTwoMonths = true,
            hasSecondSupplierAvailable = true
        )
    )
    val purchaseCriteria: StateFlow<PurchaseDecisionCriteria> = _purchaseCriteria.asStateFlow()

    private val _purchaseEvaluation = MutableStateFlow(repository.evaluatePurchaseDecision(_purchaseCriteria.value))
    val purchaseEvaluation: StateFlow<PurchaseEvaluationResult> = _purchaseEvaluation.asStateFlow()

    // Dialog & Simulation States
    private val _showReceptionDialog = MutableStateFlow(false)
    val showReceptionDialog: StateFlow<Boolean> = _showReceptionDialog.asStateFlow()

    private val _showPurchaseSimulator = MutableStateFlow(false)
    val showPurchaseSimulator: StateFlow<Boolean> = _showPurchaseSimulator.asStateFlow()

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun toggleLanguage() {
        val next = if (selectedLanguage.value == "FR") "MG" else "FR"
        repository.setLanguage(next)
    }

    fun setShowReceptionDialog(show: Boolean) {
        _showReceptionDialog.value = show
    }

    fun setShowPurchaseSimulator(show: Boolean) {
        _showPurchaseSimulator.value = show
    }

    fun updatePurchaseCriteria(
        q1: Boolean? = null,
        q2: Boolean? = null,
        q3: Boolean? = null,
        q4: Boolean? = null,
        q5: Boolean? = null,
        q6: Boolean? = null,
        q7: Boolean? = null,
        q8: Boolean? = null
    ) {
        val curr = _purchaseCriteria.value
        val updated = curr.copy(
            hasFullCostPriceCalculated = q1 ?: curr.hasFullCostPriceCalculated,
            isTargetMarginAchievable = q2 ?: curr.isTargetMarginAchievable,
            hasIdentifiedBuyerOrContract = q3 ?: curr.hasIdentifiedBuyerOrContract,
            isQualityControlledPrePayment = q4 ?: curr.isQualityControlledPrePayment,
            isStorageDurationSafe = q5 ?: curr.isStorageDurationSafe,
            isExposureWithinLimits = q6 ?: curr.isExposureWithinLimits,
            doesTreasuryRemainAboveTwoMonths = q7 ?: curr.doesTreasuryRemainAboveTwoMonths,
            hasSecondSupplierAvailable = q8 ?: curr.hasSecondSupplierAvailable
        )
        _purchaseCriteria.value = updated
        _purchaseEvaluation.value = repository.evaluatePurchaseDecision(updated)
    }

    fun askAdvisor(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(text = query, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            _isAdvisorThinking.value = true
            val contextInfo = buildEnterpriseContextString()
            val answer = advisorService.askAdvisor(
                userQuery = query,
                enterpriseContext = contextInfo,
                enableGoogleSearch = _isGoogleSearchEnabled.value
            )
            _chatMessages.value = _chatMessages.value + ChatMessage(
                text = answer.replyText,
                isUser = false,
                isGroundedWithGoogleSearch = answer.isGroundedWithGoogleSearch,
                searchSources = answer.webSourceLinks
            )
            _isAdvisorThinking.value = false
        }
    }

    private fun buildEnterpriseContextString(): String {
        val treasury = currentTreasury.value
        val prodCount = products.value.size
        val lotsCount = lots.value.size
        return "Entreprise: Sambatra Négoce (Antananarivo). Trésorerie actuelle: ${treasury.toInt()} Ar (seuil 2 mois: 10M Ar). Produits actifs: $prodCount. Lots en stock: $lotsCount. Maïs en sur-stock (51j de couverture). Client Restaurant Le Tsinjo en retard de 4.2M Ar (>30 jours)."
    }

    fun registerNewBatch(
        productCode: String,
        originRegion: String,
        supplierName: String,
        quantityKg: Double,
        moisturePercent: Double,
        impuritiesPercent: Double,
        bagsCount: Int,
        weighedBagsCount: Int,
        decision: QualityDecision,
        unitCostAr: Double,
        observations: String
    ): BatchLot {
        val batch = repository.registerNewBatch(
            productCode = productCode,
            originRegion = originRegion,
            supplierName = supplierName,
            quantityKg = quantityKg,
            moisturePercent = moisturePercent,
            impuritiesPercent = impuritiesPercent,
            bagsCount = bagsCount,
            weighedBagsCount = weighedBagsCount,
            decision = decision,
            unitCostAr = unitCostAr,
            observations = observations
        )
        _showReceptionDialog.value = false
        return batch
    }

    fun toggleChecklist(type: String, id: String) {
        repository.toggleChecklist(type, id)
    }

    fun recordClientPayment(clientId: String, amountAr: Double) {
        repository.recordPayment(clientId, amountAr)
    }

    // Proxy helper methods for simulation
    fun simulatePaddyMilling(
        paddyPriceAr: Double,
        whiteRiceSalePriceAr: Double,
        yieldPercent: Double
    ) = repository.simulatePaddyMilling(paddyPriceAr, whiteRiceSalePriceAr, yieldPercent)

    fun simulateDiscount(
        costPriceAr: Double,
        targetSalePriceAr: Double,
        discountPercent: Double
    ) = repository.simulateDiscount(costPriceAr, targetSalePriceAr, discountPercent)

    fun simulateCredit(
        salePriceAr: Double,
        creditDays: Int,
        monthlyRatePercent: Double,
        targetMarginPercent: Double,
        potentialUnpaidAmountAr: Double
    ) = repository.simulateCredit(salePriceAr, creditDays, monthlyRatePercent, targetMarginPercent, potentialUnpaidAmountAr)

    fun simulateImpactFinancierAchatOuStockage(
        montantAchatAr: Double,
        dureeStockageMois: Double = 1.0,
        tauxPortageFinancierMensuel: Double = 1.5,
        tauxFreinteMensuel: Double = 1.0,
        fraisMagasinageManutentionAr: Double = 0.0,
        prixVentePrevisionnelAr: Double = 0.0,
        chargesFixesMensuellesAr: Double = 5_000_000.0
    ): com.example.data.model.SimulationImpactFinancier {
        val conseillerVm = ConseillerGrosGrainsViewModel(repository = repository, geminiService = advisorService)
        return conseillerVm.simulerImpactFinancierAchatOuStockage(
            montantAchatAr, dureeStockageMois, tauxPortageFinancierMensuel,
            tauxFreinteMensuel, fraisMagasinageManutentionAr, prixVentePrevisionnelAr, chargesFixesMensuellesAr
        )
    }

    data class CreditAuthorizationResult(
        val isAuthorized: Boolean,
        val verdict: String,
        val blockingReasons: List<String>,
        val recommendedConditions: List<String>,
        val currentTreasuryAr: Double,
        val resultingExposurePercent: Double
    )

    fun evaluateClientCreditRequest(clientId: String, newCreditAmountAr: Double): CreditAuthorizationResult {
        val client = clients.value.find { it.id == clientId }
            ?: return CreditAuthorizationResult(false, "Client introuvable", listOf("Client non répertorié"), emptyList(), currentTreasury.value, 0.0)

        val treasury = currentTreasury.value
        val totalCurrentDebts = clients.value.sumOf { it.currentDebtAr }
        val newTotalDebt = totalCurrentDebts + newCreditAmountAr
        val exposurePct = if (treasury > 0) (newTotalDebt / treasury) * 100.0 else 100.0

        val blocking = mutableListOf<String>()
        val conditions = mutableListOf<String>()

        // Règle 1 : Aucun impayé ou retard > 15j
        if (client.debtOver30DaysAr > 0) {
            blocking.add("RÈGLE ABSOLUE VIOLÉE : Facture en retard de ${(client.debtOver30DaysAr/1_000_000.0).toString().take(4)}M Ar (>30 jours). Aucune nouvelle livraison à crédit autorisée (Guide p. 44).")
        }

        // Règle 2 : Plafond d'encours individuel
        val newClientDebt = client.currentDebtAr + newCreditAmountAr
        if (newClientDebt > client.creditLimitAr && client.creditLimitAr > 0) {
            blocking.add("DÉPASSEMENT DE PLAFOND : Le nouvel encours (${(newClientDebt/1_000_000.0).toString().take(4)}M Ar) dépasse le plafond autorisé de ${(client.creditLimitAr/1_000_000.0).toString().take(4)}M Ar basé sur sa note de ${client.creditScore25}/25.")
        }

        // Règle 3 : Segment C / D
        if (client.segment == ClientSegment.D) {
            blocking.add("SEGMENT D (À RISQUE) : Historique de litiges. Paiement intégral avant livraison obligatoire.")
        } else if (client.segment == ClientSegment.C && newCreditAmountAr > 500_000.0) {
            conditions.add("Segment C : Vente au comptoir recommandée. Exiger au moins 70% d'acompte à la commande.")
        }

        // Règle 4 : Plafond global d'encours vs trésorerie
        if (exposurePct > 50.0) {
            conditions.add("Vigilance Trésorerie : L'encours global représenterait ${String.format(java.util.Locale.US, "%.1f", exposurePct)}% de la trésorerie disponible (> seuil max de 40-50%). Risque d'illiquidité.")
        }

        val isAuth = blocking.isEmpty()
        val verdict = if (isAuth) {
            if (conditions.isEmpty()) "CRÉDIT ACCORDÉ SANS RÉSERVE (Conforme Guide 2026)" else "CRÉDIT ACCORDÉ AVEC CONDITIONS SUSPENSIVES"
        } else {
            "CRÉDIT REFUSÉ STRICTEMENT (Règles prudentielles du Guide p. 4, 37, 44)"
        }

        return CreditAuthorizationResult(
            isAuthorized = isAuth,
            verdict = verdict,
            blockingReasons = blocking,
            recommendedConditions = conditions,
            currentTreasuryAr = treasury,
            resultingExposurePercent = exposurePct
        )
    }
}

