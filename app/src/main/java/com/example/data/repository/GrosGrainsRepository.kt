package com.example.data.repository

import android.content.Context
import com.example.data.local.DemoDataProvider
import com.example.data.local.room.*
import com.example.data.model.*
import com.example.data.report.MonthlyReportData
import com.example.data.report.PdfReportGenerator
import com.example.data.sync.ConnectivityObserver
import com.example.data.sync.NetworkConnectivityObserver
import com.example.data.sync.OfflineSyncManager
import com.example.data.sync.SyncUiState
import com.example.data.zoning.ZoningOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GrosGrainsRepository private constructor() {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private var roomDatabase: GrosGrainsDatabase? = null
    private var syncManager: OfflineSyncManager? = null

    private val _syncState = MutableStateFlow(SyncUiState())
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private val _products = MutableStateFlow(DemoDataProvider.initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _lots = MutableStateFlow(DemoDataProvider.initialLots)
    val lots: StateFlow<List<BatchLot>> = _lots.asStateFlow()

    private val _clients = MutableStateFlow(DemoDataProvider.initialClients)
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _suppliers = MutableStateFlow(DemoDataProvider.initialSuppliers)
    val suppliers: StateFlow<List<Supplier>> = _suppliers.asStateFlow()

    private val _kpis = MutableStateFlow(DemoDataProvider.getDashboardKpis())
    val kpis: StateFlow<List<DashboardKpi>> = _kpis.asStateFlow()

    private val _purchaseChecklist = MutableStateFlow(DemoDataProvider.checklistPurchase)
    val purchaseChecklist: StateFlow<List<ChecklistItem>> = _purchaseChecklist.asStateFlow()

    private val _receptionChecklist = MutableStateFlow(DemoDataProvider.checklistReception)
    val receptionChecklist: StateFlow<List<ChecklistItem>> = _receptionChecklist.asStateFlow()

    private val _shippingChecklist = MutableStateFlow(DemoDataProvider.checklistShipping)
    val shippingChecklist: StateFlow<List<ChecklistItem>> = _shippingChecklist.asStateFlow()

    private val _currentTreasury = MutableStateFlow(DemoDataProvider.currentTreasuryAr)
    val currentTreasury: StateFlow<Double> = _currentTreasury.asStateFlow()

    // Langue actuelle : "FR" (Français) ou "MG" (Malagasy)
    private val _selectedLanguage = MutableStateFlow("FR")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    // Audit d'optimisation du zonage et déstockage FIFO/FEFO
    val zoningAudit: StateFlow<WarehouseZoningAudit> = combine(_lots, _products) { lots, prods ->
        ZoningOptimizer.performZoningAndDestockingAudit(lots, prods)
    }.stateIn(
        repositoryScope,
        SharingStarted.Eagerly,
        ZoningOptimizer.performZoningAndDestockingAudit(_lots.value, _products.value)
    )

    fun initializeRoomAndSync(context: Context) {
        if (roomDatabase == null) {
            val db = GrosGrainsDatabase.getDatabase(context.applicationContext, repositoryScope)
            roomDatabase = db
            val connObserver = NetworkConnectivityObserver(context.applicationContext)
            val sm = OfflineSyncManager(db, connObserver, repositoryScope)
            syncManager = sm

            repositoryScope.launch {
                sm.uiState.collect { state ->
                    _syncState.value = state
                }
            }
        }
    }

    fun setLanguage(lang: String) {
        _selectedLanguage.value = lang
    }

    // --- ALGORITHMES DÉCISIONNELS TIRÉS DU LIVRE (Octobre 2026) ---

    /**
     * Grille de décision d'achat en 8 critères (Chapitre 15, p. 95-96)
     * Règle absolue : au moins un "NON" sur les 4 premières questions => NE PAS ACHETER !
     * Un "NON" sur l'une des 4 suivantes => Réduire le lot ou différer.
     */
    fun evaluatePurchaseDecision(criteria: PurchaseDecisionCriteria): PurchaseEvaluationResult {
        val conditions = mutableListOf<String>()

        if (!criteria.hasFullCostPriceCalculated) {
            conditions.add("Bloquant : Le prix de revient complet rendu entrepôt (achat + fret + pertes + financement) n'a pas été calculé !")
        }
        if (!criteria.isTargetMarginAchievable) {
            conditions.add("Bloquant : La marge cible n'est pas atteignable avec le prix de marché réaliste.")
        }
        if (!criteria.hasIdentifiedBuyerOrContract) {
            conditions.add("Bloquant : Aucun débouché ou contrat client préalable identifié. Risque de stock dormant mortel.")
        }
        if (!criteria.isQualityControlledPrePayment) {
            conditions.add("Bloquant : La qualité (humidité, impuretés, toxines) n'est pas contrôlée avant le paiement.")
        }

        if (!criteria.firstFourValid) {
            return PurchaseEvaluationResult(
                verdict = "REFUS D'ACHAT FORMEL",
                canProceed = false,
                requiredConditions = conditions,
                bookReference = "Guide p. 96 : « Au moins un NON sur les quatre premières questions : NE PAS ACHETER. »"
            )
        }

        // Vérification des questions 5 à 8
        if (!criteria.isStorageDurationSafe) {
            conditions.add("Prudence : La durée de stockage dépasse la limite de conservation du produit (ex: bruches sur haricots après 60j).")
        }
        if (!criteria.isExposureWithinLimits) {
            conditions.add("Prudence : Dépassement de la règle des 10% des fonds propres pour produit volatil (vanille) ou 30% de la trésorerie.")
        }
        if (!criteria.doesTreasuryRemainAboveTwoMonths) {
            conditions.add("Prudence : L'achat ferait chuter la trésorerie sous le seuil critique de 2 mois de charges fixes (10M Ar).")
        }
        if (!criteria.hasSecondSupplierAvailable) {
            conditions.add("Prudence : Vous n'avez pas de 2e fournisseur qualifié en secours (vulnérabilité de rupture).")
        }

        if (conditions.isNotEmpty()) {
            return PurchaseEvaluationResult(
                verdict = "ACHAT CONDITIONNÉ : RÉDUIRE LE VOLUME OU DIFFÉRER",
                canProceed = false,
                requiredConditions = conditions,
                bookReference = "Guide p. 96 : « Un NON sur les questions 5 à 8 : réduire le lot ou différer jusqu'à levée du risque. »"
            )
        }

        return PurchaseEvaluationResult(
            verdict = "ACHAT VALIDÉ CONFORME AUX RÈGLES DU GUIDE",
            canProceed = true,
            requiredConditions = listOf("Tous les 8 critères de prudence et rentabilité sont cochés avec succès !"),
            bookReference = "Guide p. 95-96 : Grille de décision validée pour exécution."
        )
    }

    /**
     * Cas 1 : Simulation Décorticage Paddy -> Riz Blanc (p. 88)
     */
    data class MillingSimulation(
        val paddyRequiredKg: Double,
        val paddyCostAr: Double,
        val millingCostAr: Double,
        val transportCostAr: Double,
        val lossesCostAr: Double,
        val byProductCreditAr: Double,
        val totalCostPerKgWhiteRiceAr: Double,
        val grossMarginAr: Double,
        val grossMarginPercent: Double,
        val recommendation: String
    )

    fun simulatePaddyMilling(
        paddyPriceAr: Double = 1300.0,
        whiteRiceSalePriceAr: Double = 2300.0,
        yieldPercent: Double = 65.0, // 60%, 65% ou 68%
        byProductCreditAr: Double = 150.0, // Son et brisures
        millingCostPerKgPaddyAr: Double = 60.0,
        transportCostAr: Double = 80.0,
        lossesCostAr: Double = 40.0
    ): MillingSimulation {
        val paddyNeededKg = 1.0 / (yieldPercent / 100.0)
        val paddyCost = paddyNeededKg * paddyPriceAr
        val millingCost = paddyNeededKg * millingCostPerKgPaddyAr
        val totalCost = paddyCost + millingCost + transportCostAr + lossesCostAr - byProductCreditAr
        val marginAr = whiteRiceSalePriceAr - totalCost
        val marginPercent = (marginAr / whiteRiceSalePriceAr) * 100.0

        val reco = when {
            marginPercent >= 14.0 -> "Opération hautement rentable (Marge ${String.format(Locale.US, "%.1f", marginPercent)}% >= 14%). Négocier le lot avec livraison échelonnée."
            marginPercent >= 10.0 -> "Rentable mais marge modérée. Exiger un essai réel sur 1 tonne et s'assurer que les sous-produits sont vendus fermes."
            else -> "Alerte : Rendement trop faible (${yieldPercent}%) ! Prix de revient trop proche du prix de vente. Revoir le prix du paddy sous 1 200 Ar ou renoncer."
        }

        return MillingSimulation(
            paddyRequiredKg = paddyNeededKg,
            paddyCostAr = paddyCost,
            millingCostAr = millingCost,
            transportCostAr = transportCostAr,
            lossesCostAr = lossesCostAr,
            byProductCreditAr = byProductCreditAr,
            totalCostPerKgWhiteRiceAr = totalCost,
            grossMarginAr = marginAr,
            grossMarginPercent = marginPercent,
            recommendation = reco
        )
    }

    /**
     * Cas 7.3 : Effet d'une remise et coût caché (p. 37)
     */
    data class DiscountSimulation(
        val originalPriceAr: Double,
        val discountedPriceAr: Double,
        val initialMarginPercent: Double,
        val newMarginPercent: Double,
        val lostUnitMarginAr: Double,
        val percentUnitMarginLost: Double,
        val requiredVolumeIncreasePercent: Double,
        val bookWarning: String
    )

    fun simulateDiscount(
        costPriceAr: Double = 3151.0,
        targetSalePriceAr: Double = 3664.0,
        discountPercent: Double = 3.0
    ): DiscountSimulation {
        val discountedPrice = targetSalePriceAr * (1.0 - (discountPercent / 100.0))
        val initialMarginAr = targetSalePriceAr - costPriceAr
        val newMarginAr = discountedPrice - costPriceAr
        val initialMarginPercent = (initialMarginAr / targetSalePriceAr) * 100.0
        val newMarginPercent = (newMarginAr / discountedPrice) * 100.0
        val lostMarginAr = initialMarginAr - newMarginAr
        val percentLost = (lostMarginAr / initialMarginAr) * 100.0

        // Volume supplémentaire requis pour compenser : initialMargin / newMargin - 1
        val volIncrease = if (newMarginAr > 0) ((initialMarginAr / newMarginAr) - 1.0) * 100.0 else 999.0

        val warning = "Une remise de ${String.format(Locale.US, "%.1f", discountPercent)}% supprime ${String.format(Locale.US, "%.1f", percentLost)}% de votre marge unitaire ! Il vous faut vendre +${String.format(Locale.US, "%.1f", volIncrease)}% de volume pour compenser."

        return DiscountSimulation(
            originalPriceAr = targetSalePriceAr,
            discountedPriceAr = discountedPrice,
            initialMarginPercent = initialMarginPercent,
            newMarginPercent = newMarginPercent,
            lostUnitMarginAr = lostMarginAr,
            percentUnitMarginLost = percentLost,
            requiredVolumeIncreasePercent = volIncrease,
            bookWarning = warning
        )
    }

    /**
     * Cas 7.3 & 8.4 : Coût du crédit client et compensation impayé (p. 37 & 40)
     */
    data class CreditCostSimulation(
        val salePriceAr: Double,
        val creditDays: Int,
        val monthlyInterestRate: Double,
        val creditCostAr: Double,
        val creditCostPercentOfPrice: Double,
        val marginErosionPercent: Double,
        val salesNeededToCompensateUnpaidAr: Double
    )

    fun simulateCredit(
        salePriceAr: Double = 3664.0,
        creditDays: Int = 45,
        monthlyRatePercent: Double = 1.5,
        targetMarginPercent: Double = 14.0,
        potentialUnpaidAmountAr: Double = 4_200_000.0
    ): CreditCostSimulation {
        val months = creditDays / 30.0
        val creditCostPercent = (monthlyRatePercent * months)
        val creditCostAr = salePriceAr * (creditCostPercent / 100.0)
        val marginErosion = (creditCostPercent / targetMarginPercent) * 100.0
        val salesNeeded = potentialUnpaidAmountAr / (targetMarginPercent / 100.0)

        return CreditCostSimulation(
            salePriceAr = salePriceAr,
            creditDays = creditDays,
            monthlyInterestRate = monthlyRatePercent,
            creditCostAr = creditCostAr,
            creditCostPercentOfPrice = creditCostPercent,
            marginErosionPercent = marginErosion,
            salesNeededToCompensateUnpaidAr = salesNeeded
        )
    }

    /**
     * Diagnostic du Lundi Matin (p. 71 de Sambatra Négoce)
     * Détecte les 3 voyants rouges et fournit les décisions immédiates.
     */
    fun getMondayMorningAlerts(): List<AdvisorAlert> {
        val alerts = mutableListOf<AdvisorAlert>()

        // 1. Couverture Maïs
        val mais = _products.value.find { it.code == "MAI" }
        if (mais != null && mais.coverageDays > 45) {
            alerts.add(
                AdvisorAlert(
                    id = "alert_mais_overstock",
                    title = "Sur-stockage critique : Maïs à ${mais.coverageDays} jours (Cible 35j)",
                    description = "Le stock actuel de ${mais.currentStockKg.toInt()} kg immobilise 35M Ar de trésorerie avec risque de charançons et moisissures.",
                    rationaleBook = "Chapitre 6 & 12.6 : Tout stock de rotation dépassant 15 jours la cible asphyxie le BFR et augmente la freinte.",
                    recommendedActions = listOf(
                        "Geler immédiatement tout nouvel achat de maïs",
                        "Lancer une offre promotionnelle ciblée aux clients de Segment B (Détaillants Isotry)",
                        "Vérifier l'étanchéité et l'aération de l'allée C dans l'entrepôt"
                    ),
                    priority = KpiStatus.RED,
                    sourceChapter = "Chapitre 6 (Gestion des stocks) & Chapitre 12 (Tableau de bord)"
                )
            )
        }

        // 2. Créance Client en retard
        val clientTsinjo = _clients.value.find { it.debtOver30DaysAr > 0 }
        if (clientTsinjo != null) {
            alerts.add(
                AdvisorAlert(
                    id = "alert_debt_overdue",
                    title = "Créance à risque : ${clientTsinjo.name} (4 200 000 Ar > 30 jours)",
                    description = "Ce retard de paiement représente 21% de l'encours total et entame directement le fonds de roulement.",
                    rationaleBook = "Chapitre 8.4 : À J+15/30, sans réaction immédiate avec échéancier écrit, la probabilité de recouvrement chute de 40%.",
                    recommendedActions = listOf(
                        "Suspendre toute nouvelle livraison à crédit immédiatement",
                        "Visiter ou appeler le gérant (Chef Patrick Rakoto) avec proposition d'échéancier",
                        "Exiger 50% d'acompte à la commande sur les prochaines livraisons"
                    ),
                    priority = KpiStatus.RED,
                    sourceChapter = "Chapitre 8 (Crédit client & Recouvrement)"
                )
            )
        }

        // 3. Trésorerie sous les 2 mois de charges fixes
        val treasury = _currentTreasury.value
        val twoMonthsTarget = DemoDataProvider.TREASURY_TARGET_SAFETY_AR
        if (treasury < twoMonthsTarget) {
            alerts.add(
                AdvisorAlert(
                    id = "alert_treasury_low",
                    title = "Trésorerie sous le seuil de sécurité : ${(treasury / 1_000_000.0)}M Ar (1.8 mois)",
                    description = "La trésorerie disponible est inférieure aux 2 mois de charges fixes de sécurité (10 000 000 Ar requis).",
                    rationaleBook = "Chapitre 7.6 & 10.7 : En dessous de 2 mois de charges, geler les achats spéculatifs et reporter les investissements matériels.",
                    recommendedActions = listOf(
                        "Geler les achats de stock spéculatifs ou d'opportunité",
                        "Reporter de 60 jours tout achat de matériel ou équipement non vital",
                        "Accélérer les encaissements en accordant 1.5% d'escompte pour paiement comptant"
                    ),
                    priority = KpiStatus.RED,
                    sourceChapter = "Chapitre 7 (Trésorerie) & Chapitre 14 (Modèle financier)"
                )
            )
        }

        return alerts
    }

    // Gestion des Lots : Réception en 5 étapes et enregistrement
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
        val dateFormat = SimpleDateFormat("yyMMdd", Locale.US)
        val dateDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val today = Date()
        val seq = (_lots.value.size + 1).toString().padStart(2, '0')
        val lotNumber = "$productCode-${originRegion.take(3).uppercase()}-${dateFormat.format(today)}-$seq"
        val sampleId = "ECH-$lotNumber-SC"

        val prod = _products.value.find { it.code == productCode }
        val prodName = prod?.name ?: "Produit $productCode"
        val zone = prod?.warehouseZone ?: "Zone Réception"

        val newLot = BatchLot(
            lotNumber = lotNumber,
            productId = prod?.id ?: "prod_unknown",
            productName = prodName,
            supplierName = supplierName,
            originRegion = originRegion,
            receptionDate = dateDisplay.format(today),
            quantityKg = quantityKg,
            netReceivedKg = quantityKg * (1.0 - (impuritiesPercent / 100.0)),
            moisturePercent = moisturePercent,
            impuritiesPercent = impuritiesPercent,
            bagsCount = bagsCount,
            weighedBagsCount = weighedBagsCount,
            decision = decision,
            sealedCounterSampleId = sampleId,
            warehouseZone = zone,
            storageDays = 0,
            unitCostAr = unitCostAr,
            observations = observations
        )

        _lots.value = listOf(newLot) + _lots.value

        // Mettre à jour le stock du produit si accepté ou décote
        if (decision == QualityDecision.CONFORME || decision == QualityDecision.DECOTE) {
            _products.value = _products.value.map { p ->
                if (p.code == productCode) {
                    val newStock = p.currentStockKg + newLot.netReceivedKg
                    val newCmup = ((p.currentStockKg * p.cmupCostAr) + (newLot.netReceivedKg * unitCostAr)) / newStock
                    p.copy(currentStockKg = newStock, cmupCostAr = newCmup)
                } else p
            }
        }

        // Persistance Room locale immédiate et mise en file de synchronisation
        repositoryScope.launch {
            roomDatabase?.let { db ->
                val entity = StockLotEntity(
                    numeroLot = newLot.lotNumber,
                    produitId = newLot.productId,
                    fournisseurId = "fourn_direct",
                    nomFournisseur = newLot.supplierName,
                    regionOrigine = newLot.originRegion,
                    dateReception = newLot.receptionDate,
                    quantiteInitialeKg = newLot.quantityKg,
                    quantiteDisponibleKg = newLot.netReceivedKg,
                    nombreSacs = newLot.bagsCount,
                    nombreSacsPeses = newLot.weighedBagsCount,
                    humiditeMesureePercent = newLot.moisturePercent,
                    impuretesPercent = newLot.impuritiesPercent,
                    decisionControle = newLot.decision.name,
                    identifiantContreEchantillonScelle = newLot.sealedCounterSampleId,
                    zoneStockageEntrepot = newLot.warehouseZone,
                    coutUnitaireAchatAr = newLot.unitCostAr,
                    dateLimiteConservation = newLot.receptionDate,
                    statutConservation = if (newLot.decision == QualityDecision.QUARANTAINE) "QUARANTAINE" else "OPTIMAL",
                    temperatureMesureeC = 23.0,
                    estBloqueQuarantaine = (newLot.decision == QualityDecision.QUARANTAINE || newLot.decision == QualityDecision.REFUSE),
                    codeQrTracabilite = "QR-LOT-${newLot.lotNumber}",
                    observationsControle = newLot.observations
                )
                db.stockLotDao().insertLot(entity)

                // Enregistrement automatique du mouvement d'entrée en stock
                val mvt = MouvementInventaireEntity(
                    numeroLot = newLot.lotNumber,
                    produitId = newLot.productId,
                    typeMouvement = "ENTREE_ACHAT",
                    quantiteKg = newLot.netReceivedKg,
                    valeurUnitaireAr = newLot.unitCostAr,
                    valeurTotaleAr = newLot.netReceivedKg * newLot.unitCostAr,
                    dateMouvement = "${newLot.receptionDate} 10:00",
                    numeroPieceRef = "BE-${newLot.lotNumber.takeLast(6)}",
                    tiersConcerne = newLot.supplierName,
                    motifOuCause = "Réception conforme protocole 5 étapes (Guide 2026)",
                    visaResponsable = "Magasinier Réception"
                )
                db.mouvementInventaireDao().insertMouvement(mvt)
            }
            syncManager?.enqueueOperation(
                entityType = "STOCK_LOT",
                entityId = newLot.lotNumber,
                action = "INSERT",
                payloadJson = "{\"lotNumber\":\"${newLot.lotNumber}\",\"productCode\":\"$productCode\",\"netReceivedKg\":${newLot.netReceivedKg},\"decision\":\"${newLot.decision}\"}"
            )
        }

        return newLot
    }

    // Déplacement de lot dans une autre zone d'entrepôt (Zonage & Réaffectation)
    fun updateLotWarehouseZone(lotNumber: String, newZone: String) {
        _lots.value = _lots.value.map { lot ->
            if (lot.lotNumber == lotNumber) {
                lot.copy(warehouseZone = newZone)
            } else lot
        }

        repositoryScope.launch {
            roomDatabase?.stockLotDao()?.updateZone(lotNumber, newZone)
            syncManager?.enqueueOperation(
                entityType = "ZONING_MOVE",
                entityId = lotNumber,
                action = "UPDATE",
                payloadJson = "{\"lotNumber\":\"$lotNumber\",\"newZone\":\"$newZone\"}"
            )
        }
    }

    // Toggle Checklist items
    fun toggleChecklist(type: String, itemId: String) {
        when (type) {
            "purchase" -> {
                _purchaseChecklist.value = _purchaseChecklist.value.map {
                    if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
                }
            }
            "reception" -> {
                _receptionChecklist.value = _receptionChecklist.value.map {
                    if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
                }
            }
            "shipping" -> {
                _shippingChecklist.value = _shippingChecklist.value.map {
                    if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
                }
            }
        }
    }

    // Ajustement de trésorerie (ex: encaissement reçu de Le Tsinjo)
    fun recordPayment(clientId: String, amountAr: Double) {
        _clients.value = _clients.value.map { c ->
            if (c.id == clientId) {
                val newDebt = maxOf(0.0, c.currentDebtAr - amountAr)
                val newOverdue = maxOf(0.0, c.debtOver30DaysAr - amountAr)
                c.copy(currentDebtAr = newDebt, debtOver30DaysAr = newOverdue)
            } else c
        }
        _currentTreasury.value += amountAr
        _kpis.value = DemoDataProvider.getDashboardKpis()

        repositoryScope.launch {
            syncManager?.enqueueOperation(
                entityType = "CLIENT_PAYMENT",
                entityId = clientId,
                action = "UPDATE",
                payloadJson = "{\"clientId\":\"$clientId\",\"amountPaidAr\":$amountAr,\"newTreasury\":${_currentTreasury.value}}"
            )
        }
    }

    // Méthodes de synchronisation locale Room
    fun triggerManualSync() {
        syncManager?.forceSyncNow()
    }

    fun toggleOfflineSimulation() {
        syncManager?.toggleOfflineSimulation()
    }

    // Méthodes de reporting mensuel PDF (KPI, Marges et Pertes)
    fun getMonthlyReportData(monthName: String = "Octobre 2026"): MonthlyReportData {
        return PdfReportGenerator.buildMonthlyReportData(
            monthName = monthName,
            products = _products.value,
            lots = _lots.value,
            currentTreasury = _currentTreasury.value
        )
    }

    fun generateMonthlyPdfReport(context: Context, monthName: String = "Octobre 2026"): File {
        val data = getMonthlyReportData(monthName)
        return PdfReportGenerator.generatePdfDocument(context, data)
    }

    companion object {
        @Volatile
        private var instance: GrosGrainsRepository? = null

        fun getInstance(): GrosGrainsRepository {
            return instance ?: synchronized(this) {
                instance ?: GrosGrainsRepository().also { instance = it }
            }
        }
    }
}
