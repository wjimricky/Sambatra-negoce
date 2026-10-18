package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAdvisorService
import com.example.data.model.AdvisorAlert
import com.example.data.model.KpiStatus
import com.example.data.model.PurchaseDecisionCriteria
import com.example.data.model.PurchaseEvaluationResult
import com.example.data.repository.GrosGrainsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.model.NiveauRisqueAchat
import com.example.data.model.SimulationImpactFinancier

data class MessageConseil(
    val id: String = java.util.UUID.randomUUID().toString(),
    val contenu: String,
    val estUtilisateur: Boolean,
    val heure: String = SimpleDateFormat("HH:mm", Locale.US).format(Date()),
    val regleGuideAssociee: String? = null
)

data class ConseillerUiState(
    val messages: List<MessageConseil> = emptyList(),
    val estEnTrainDeReflechir: Boolean = false,
    val alertesPrioritaires: List<AdvisorAlert> = emptyList(),
    val dernierDiagnostic: String? = null,
    val evaluationAchat: PurchaseEvaluationResult? = null,
    val simulationAchatStockage: SimulationImpactFinancier? = null
)

/**
 * ViewModel dédié pour le 'Conseiller GrosGrains'.
 * Initialise le client Gemini et prépare les fonctions d'envoi du contexte métier
 * (données de stock, trésorerie disponible, retards créances) pour générer des recommandations
 * strictement alignées sur le Guide de référence pour entrepreneurs (Édition octobre 2026).
 */
class ConseillerGrosGrainsViewModel(
    private val repository: GrosGrainsRepository = GrosGrainsRepository.getInstance(),
    private val geminiService: GeminiAdvisorService = GeminiAdvisorService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ConseillerUiState(
            messages = listOf(
                MessageConseil(
                    contenu = "Salama tompoko ! Je suis le Conseiller GrosGrains, votre consultant permanent fondé sur le Guide de référence pour entrepreneurs d'octobre 2026.\n\nPosez-moi une question sur vos achats, vos crédits clients, vos stocks de grains ou vos épices.",
                    estUtilisateur = false,
                    regleGuideAssociee = "Guide 2026 : « Ce qui n'est pas mesuré n'est pas géré »"
                )
            ),
            alertesPrioritaires = repository.getMondayMorningAlerts()
        )
    )
    val uiState: StateFlow<ConseillerUiState> = _uiState.asStateFlow()

    /**
     * Envoie la question avec l'état dynamique de l'entreprise
     * (trésorerie en mois de charges, couverture des stocks de grains/épices, créances en retard)
     * au client Gemini pour obtenir une recommandation d'expert contextualisée.
     */
    fun envoyerContexteMetierEtConsulter(questionUtilisateur: String) {
        if (questionUtilisateur.isBlank()) return

        val msgUser = MessageConseil(
            contenu = questionUtilisateur,
            estUtilisateur = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + msgUser,
            estEnTrainDeReflechir = true
        )

        viewModelScope.launch {
            // Assembler le contexte d'exploitation réel de l'entreprise
            val contexteMetier = genererContexteExploitation()

            // Appel au client Gemini (avec fallback sur le moteur heuristique hors-ligne du livre)
            val reponse = geminiService.askAdvisor(
                userQuery = questionUtilisateur,
                enterpriseContext = contexteMetier
            )

            val msgConseiller = MessageConseil(
                contenu = reponse.replyText,
                estUtilisateur = false,
                regleGuideAssociee = extraireRegleCle(questionUtilisateur)
            )

            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + msgConseiller,
                estEnTrainDeReflechir = false,
                dernierDiagnostic = "Analyse effectuée le ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(Date())}"
            )
        }
    }

    /**
     * Formate le contexte métier complet (état des stocks de grains et d'épices, trésorerie disponible,
     * BFR, créances clients et seuils de risque) pour l'API Gemini.
     */
    fun formaterContexteMetier(): String {
        return genererContexteExploitation()
    }

    /**
     * Surcharge paramétrable pour formater n'importe quelle situation d'exploitation d'un grossiste.
     */
    fun formaterContexteMetier(
        stocksDescription: String,
        tresorerieDisponibleAr: Double,
        chargesFixesMensuellesAr: Double,
        creancesTotalesAr: Double,
        creancesEnRetard30jAr: Double,
        margeCiblePercent: Double = 14.0
    ): String {
        val moisCharges = if (chargesFixesMensuellesAr > 0) tresorerieDisponibleAr / chargesFixesMensuellesAr else 0.0
        val alerteTresorerie = if (moisCharges < 2.0) "ATTENTION : Trésorerie inférieure au seuil de sécurité des 2 mois de charges fixes !" else "Trésorerie sécurisée (>= 2 mois)"

        return """
            === CONTEXTE D'EXPLOITATION GROSGRAINS (GUIDE OCTOBRE 2026) ===
            • Trésorerie disponible : ${tresorerieDisponibleAr.toInt()} Ariary ($moisCharges mois de charges fixes)
            • Statut de sécurité trésorerie : $alerteTresorerie (Seuil minimal : ${(chargesFixesMensuellesAr * 2).toInt()} Ar)
            • Situation des stocks :
              $stocksDescription
            • Créances clients totales : ${creancesTotalesAr.toInt()} Ar
            • Créances en retard > 30 jours : ${creancesEnRetard30jAr.toInt()} Ar
            • Marge brute sur coût variable cible : $margeCiblePercent%
            
            PRINCIPES À APPLIQUER OBLIGATOIREMENT :
            - « La marge se gagne à l'achat, se protège au stockage et se perd au crédit. »
            - Grille des 8 critères : 4 questions éliminatoires (si un NON -> refus net).
            - Stock de sécurité = (Conso max x délai max) - (Conso moy x délai moy).
            - Règle des 10% des fonds propres pour tout achat sur produit volatil.
        """.trimIndent()
    }

    /**
     * Génère la chaîne de contexte métier enrichie des données de stock,
     * de trésorerie et d'encours clients de Sambatra Négoce.
     */
    fun genererContexteExploitation(): String {
        val tresorerieAr = repository.currentTreasury.value
        val produits = repository.products.value
        val clients = repository.clients.value
        val lots = repository.lots.value

        val stockTotalValo = produits.sumOf { it.stockValueAr }
        val creancesTotales = clients.sumOf { it.currentDebtAr }
        val creancesEnRetard30j = clients.sumOf { it.debtOver30DaysAr }
        val moisChargesCouverts = tresorerieAr / 5_000_000.0 // 5M Ar/mois de charges fixes

        val stocksEnAlerte = produits.filter { it.coverageDays > it.targetCoverageDays + 10 }
            .joinToString { "${it.name}: ${it.coverageDays}j de couverture (cible: ${it.targetCoverageDays}j)" }

        val resumeStocks = produits.joinToString("\n              ") {
            "- ${it.name} [Classe ${it.abcClass}]: ${it.currentStockKg.toInt()} kg (${it.coverageDays}j de couverture, CMUP: ${it.cmupCostAr.toInt()} Ar/kg)"
        }

        return formaterContexteMetier(
            stocksDescription = resumeStocks + if (stocksEnAlerte.isNotBlank()) "\n              [ALERTE SUR-COUVERTURE] : $stocksEnAlerte" else "",
            tresorerieDisponibleAr = tresorerieAr,
            chargesFixesMensuellesAr = 5_000_000.0,
            creancesTotalesAr = creancesTotales,
            creancesEnRetard30jAr = creancesEnRetard30j,
            margeCiblePercent = 14.0
        )
    }

    /**
     * Évalue un projet d'achat de lot selon la grille stricte de 8 critères du Chapitre 15 (p. 95-96).
     */
    fun evaluerAchatSelonGrille(criteria: PurchaseDecisionCriteria) {
        val result = repository.evaluatePurchaseDecision(criteria)
        _uiState.value = _uiState.value.copy(evaluationAchat = result)
    }

    /**
     * Simule l'impact financier d'un achat important ou d'une période de stockage prolongée,
     * en utilisant les données de trésorerie actuelles de Sambatra Négoce et les règles de risque du Guide 2026 :
     * - Seuil impératif de 2 mois de charges fixes de trésorerie (Guide p. 36)
     * - Coût de portage financier de 1.5%/mois (Guide p. 30)
     * - Taux de freinte et pertes physiques au magasin (Guide p. 29)
     * - Calcul de l'érosion du taux de marge brute
     */
    fun simulerImpactFinancierAchatOuStockage(
        montantAchatAr: Double,
        dureeStockageMois: Double = 1.0,
        tauxPortageFinancierMensuel: Double = 1.5,
        tauxFreinteMensuel: Double = 1.0,
        fraisMagasinageManutentionAr: Double = 0.0,
        prixVentePrevisionnelAr: Double = 0.0,
        chargesFixesMensuellesAr: Double = 5_000_000.0
    ): SimulationImpactFinancier {
        val tresoActuelle = repository.currentTreasury.value
        val tresoResiduelle = tresoActuelle - montantAchatAr
        val moisCharges = if (chargesFixesMensuellesAr > 0) tresoResiduelle / chargesFixesMensuellesAr else 0.0
        val seuil2Mois = chargesFixesMensuellesAr * 2.0

        val coutPortage = montantAchatAr * (tauxPortageFinancierMensuel / 100.0) * dureeStockageMois
        val coutFreinte = montantAchatAr * (tauxFreinteMensuel / 100.0) * dureeStockageMois
        val surcoutTotal = coutPortage + coutFreinte + fraisMagasinageManutentionAr
        val coutRevientTotal = montantAchatAr + surcoutTotal
        val surcoutPct = if (montantAchatAr > 0) (surcoutTotal / montantAchatAr) * 100.0 else 0.0

        val margeInitiale = if (prixVentePrevisionnelAr > 0) prixVentePrevisionnelAr - montantAchatAr else 0.0
        val margeFinale = if (prixVentePrevisionnelAr > 0) prixVentePrevisionnelAr - coutRevientTotal else 0.0
        val margeFinalePct = if (prixVentePrevisionnelAr > 0) (margeFinale / prixVentePrevisionnelAr) * 100.0 else 0.0
        val erosionMarge = if (margeInitiale > 0) ((margeInitiale - margeFinale) / margeInitiale) * 100.0 else 0.0

        val niveauRisque = when {
            tresoResiduelle < 0 -> NiveauRisqueAchat.CRITIQUE
            moisCharges < 1.0 -> NiveauRisqueAchat.CRITIQUE
            montantAchatAr > (tresoActuelle * 0.70) -> NiveauRisqueAchat.CRITIQUE
            moisCharges < 2.0 -> NiveauRisqueAchat.ELEVE
            dureeStockageMois > 3.0 && surcoutPct > 6.0 -> NiveauRisqueAchat.MODERE
            montantAchatAr > (tresoActuelle * 0.40) -> NiveauRisqueAchat.MODERE
            else -> NiveauRisqueAchat.FAIBLE
        }

        val diagnosticTreso = when {
            tresoResiduelle < 0 -> "RUPTURE IMMÉDIATE : Cet achat dépasse votre trésorerie disponible de ${(-tresoResiduelle).toInt()} Ar !"
            moisCharges < 1.0 -> "DANGER MORTEL : Il ne vous restera que ${String.format(Locale.US, "%.1f", moisCharges)} mois de charges fixes (< 1 mois critique)."
            moisCharges < 2.0 -> "VIGILANCE FORTE : Trésorerie résiduelle de ${String.format(Locale.US, "%.1f", moisCharges)} mois de charges, sous le seuil impératif de 2 mois (10M Ar)."
            else -> "TRÉSORERIE SÉCURISÉE : Reste ${(tresoResiduelle / 1_000_000.0).toString().take(4)}M Ar soit ${String.format(Locale.US, "%.1f", moisCharges)} mois de charges fixes."
        }

        val conseils = mutableListOf<String>()
        if (moisCharges < 2.0) {
            conseils.add("Règle p. 36 : « Ne jamais descendre sous 2 mois de charges fixes de trésorerie disponible ». Réduire le volume d'achat ou négocier un paiement échelonné.")
        }
        if (dureeStockageMois >= 2.0) {
            conseils.add("Règle p. 30 : Le portage financier (1,5%/mois) et la freinte grugent ${String.format(Locale.US, "%.1f", surcoutPct)}% de surcoût sur ce lot (${surcoutTotal.toInt()} Ar).")
        }
        if (montantAchatAr > (tresoActuelle * 0.35)) {
            conseils.add("Règle p. 94 : Éviter d'immobiliser plus de 30% à 35% de la trésorerie sur un achat non pré-vendu par contrat.")
        }
        if (prixVentePrevisionnelAr > 0 && margeFinalePct < 14.0) {
            conseils.add("Alerte Marge (p. 37) : La marge nette tombe à ${String.format(Locale.US, "%.1f", margeFinalePct)}% (< cible 14%). La durée de stockage absorbe votre rentabilité.")
        }
        if (dureeStockageMois > 3.0) {
            conseils.add("Règle p. 95 : Stockage > 90 jours interdit sans contrat ferme d'enlèvement (risque d'altération et de retournement des cours).")
        }
        if (conseils.isEmpty()) {
            conseils.add("Opération saine et équilibrée selon les règles du Guide 2026. Veiller à la stricte application du contrôle qualité en 5 étapes.")
        }

        val synthese = when (niveauRisque) {
            NiveauRisqueAchat.CRITIQUE -> "REFUS FORMEL : L'achat asphyxie la trésorerie de Sambatra Négoce. Règle des 4 questions éliminatoires activée."
            NiveauRisqueAchat.ELEVE -> "OPÉRATION SOUS CONDITION : Négocier un acompte client de 50% ou étaler le règlement fournisseur pour conserver 2 mois de charges."
            NiveauRisqueAchat.MODERE -> "ACHAT ACCEPTABLE : Durée de stockage à surveiller étroitement. Ne pas dépasser ${dureeStockageMois.toInt()} mois."
            NiveauRisqueAchat.FAIBLE -> "FEU VERT CONSEILLER : Capacité de trésorerie préservée (>= 2 mois de charges), marge protégée."
        }

        val simulation = SimulationImpactFinancier(
            montantAchatAr = montantAchatAr,
            dureeStockageMois = dureeStockageMois,
            tauxPortageMensuelPercent = tauxPortageFinancierMensuel,
            tauxFreinteMensuelPercent = tauxFreinteMensuel,
            fraisMagasinageManutentionAr = fraisMagasinageManutentionAr,
            chargesFixesMensuellesAr = chargesFixesMensuellesAr,
            tresorerieInitialeAr = tresoActuelle,
            tresorerieResiduelleAr = tresoResiduelle,
            moisChargesResiduels = moisCharges,
            seuilSecurite2MoisAr = seuil2Mois,
            coutPortageFinancierAr = coutPortage,
            coutFreinteEtPertesAr = coutFreinte,
            coutTotalSurcoutStockageAr = surcoutTotal,
            coutRevientCompletAchatAr = coutRevientTotal,
            surcoutPourcentage = surcoutPct,
            prixVentePrevisionnelAr = prixVentePrevisionnelAr,
            margeBruteInitialeAr = margeInitiale,
            margeBruteFinaleAr = margeFinale,
            margeBruteFinalePercent = margeFinalePct,
            erosionMargePercent = erosionMarge,
            niveauRisque = niveauRisque,
            diagnosticTreso = diagnosticTreso,
            conseilsRisqueGuide = conseils,
            syntheseRecommandation = synthese
        )

        _uiState.value = _uiState.value.copy(simulationAchatStockage = simulation)
        return simulation
    }

    private fun extraireRegleCle(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("achat") || q.contains("lot") -> "Guide p. 95 : 4 critères éliminatoires avant tout achat"
            q.contains("riz") || q.contains("paddy") -> "Guide p. 88 : Le rendement de décorticage décide de la marge"
            q.contains("remise") -> "Guide p. 37 : Une remise de 3% exige +27% de volume pour compenser"
            q.contains("crédit") || q.contains("retard") -> "Guide p. 4 : La marge se perd au crédit"
            q.contains("vanille") -> "Guide p. 94 : Règle des 10% des fonds propres sur produit volatil"
            else -> "Guide 2026 : Chapitre 12 (Tableau de bord et 15 indicateurs)"
        }
    }
}

