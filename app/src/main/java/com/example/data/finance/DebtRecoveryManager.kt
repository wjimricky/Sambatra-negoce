package com.example.data.finance

import com.example.data.model.Client
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DunningLevel {
    STAGE_1_COURTEOUS_CALL, // J+3 à J+7 : Rappel courtois & relevé de compte
    STAGE_2_WARNING_LETTER,  // J+8 à J+15 : Avertissement & préavis d'arrêt des livraisons
    STAGE_3_BLOCK_DELIVERIES, // J+16 à J+30 : Blocage strict des expéditions & mise en demeure
    STAGE_4_SETTLEMENT_PLAN,  // J > 30 : Protocole d'accord transactionnel & apurement échelonné
    STAGE_5_LEGAL_DISPUTE    // J > 45 : Transmission dossier contentieux / avocat
}

data class AgingBucket(
    val bucketLabel: String,
    val daysRange: String,
    val totalAmountAr: Double,
    val countInvoices: Int,
    val riskLevel: String, // "NORMAL", "MODÉRÉ", "CRITIQUE"
    val affectedClients: List<String>
)

data class ClientAgingAnalysis(
    val clientId: String,
    val clientName: String,
    val totalDebtAr: Double,
    val currentDue0To15DaysAr: Double,
    val overdue16To30DaysAr: Double,
    val overdueMoreThan30DaysAr: Double,
    val creditLimitAr: Double,
    val creditScore25: Int,
    val recommendedDunningLevel: DunningLevel,
    val isDeliveryBlocked: Boolean,
    val dunningActionTitle: String,
    val dunningActionDescription: String
)

data class SettlementInstallment(
    val installmentNumber: Int,
    val dueDateFormatted: String,
    val amountAr: Double,
    val percentageOfDebt: Double,
    val status: String // "À ÉCHOIR", "PAYÉ"
)

data class DebtSettlementPlan(
    val agreementReference: String,
    val clientId: String,
    val clientName: String,
    val totalDebtAr: Double,
    val immediateUpfrontDepositAr: Double,
    val immediateUpfrontPercentage: Double,
    val remainingBalanceAr: Double,
    val installmentsCount: Int,
    val installments: List<SettlementInstallment>,
    val resumptionOfDeliveriesCondition: String,
    val formalAgreementLegalNotice: String
)

object DebtRecoveryManager {

    /**
     * Analyse l'ancienneté des créances pour l'ensemble du portefeuille clients.
     */
    fun analyzeAgingBalances(clients: List<Client>): List<ClientAgingAnalysis> {
        return clients.map { client ->
            val overdueMore30 = client.debtOver30DaysAr
            val total = client.currentDebtAr
            // Répartition selon historique
            val overdue16To30 = if (total > overdueMore30) {
                if (client.averagePaymentDays > 15 && overdueMore30 == 0.0) total * 0.35 else 0.0
            } else 0.0
            val current0To15 = (total - overdueMore30 - overdue16To30).coerceAtLeast(0.0)

            val dunningLevel = when {
                overdueMore30 > 0.0 -> DunningLevel.STAGE_4_SETTLEMENT_PLAN
                overdue16To30 > 0.0 -> DunningLevel.STAGE_2_WARNING_LETTER
                total > client.creditLimitAr && client.creditLimitAr > 0 -> DunningLevel.STAGE_2_WARNING_LETTER
                total > 0 -> DunningLevel.STAGE_1_COURTEOUS_CALL
                else -> DunningLevel.STAGE_1_COURTEOUS_CALL
            }

            val blocked = overdueMore30 > 0 || (total > client.creditLimitAr && client.creditLimitAr > 0)

            val actionTitle = when (dunningLevel) {
                DunningLevel.STAGE_1_COURTEOUS_CALL -> "Suivi d'échéance normale"
                DunningLevel.STAGE_2_WARNING_LETTER -> "Relance amiable & Avertissement de suspension"
                DunningLevel.STAGE_3_BLOCK_DELIVERIES -> "Mise en demeure formelle & Suspension immédiate"
                DunningLevel.STAGE_4_SETTLEMENT_PLAN -> "Protocole d'accord d'apurement avec acompte 30%"
                DunningLevel.STAGE_5_LEGAL_DISPUTE -> "Transmission contentieux"
            }

            val actionDesc = when (dunningLevel) {
                DunningLevel.STAGE_1_COURTEOUS_CALL -> "Facture dans les délais contractuels accordés."
                DunningLevel.STAGE_2_WARNING_LETTER -> "Relancer par téléphone sous 48h. Rappeler la date d'échéance contractuelle."
                DunningLevel.STAGE_3_BLOCK_DELIVERIES -> "Bloquer toute préparation de commande. Envoyer la lettre de mise en demeure."
                DunningLevel.STAGE_4_SETTLEMENT_PLAN -> "Exiger un versement d'acompte immédiat d'au moins 30%. Échelonner le solde sur 3 semaines maximum. Aucune livraison sauf au comptoir."
                DunningLevel.STAGE_5_LEGAL_DISPUTE -> "Engager les poursuites avec le contre-échantillon scellé et les bons de livraison signés."
            }

            ClientAgingAnalysis(
                clientId = client.id,
                clientName = client.name,
                totalDebtAr = total,
                currentDue0To15DaysAr = current0To15,
                overdue16To30DaysAr = overdue16To30,
                overdueMoreThan30DaysAr = overdueMore30,
                creditLimitAr = client.creditLimitAr,
                creditScore25 = client.creditScore25,
                recommendedDunningLevel = dunningLevel,
                isDeliveryBlocked = blocked,
                dunningActionTitle = actionTitle,
                dunningActionDescription = actionDesc
            )
        }
    }

    /**
     * Génère un plan d'apurement d'échéancier transactionnel conforme à la règle du Guide p. 44.
     */
    fun generateSettlementPlan(
        client: Client,
        upfrontDepositPercent: Double = 30.0,
        installmentsCount: Int = 3
    ): DebtSettlementPlan {
        val totalDebt = client.currentDebtAr
        val upfrontDepositAr = Math.round(totalDebt * (upfrontDepositPercent / 100.0)).toDouble()
        val remainingBalanceAr = totalDebt - upfrontDepositAr

        val singleInstallmentAmount = if (installmentsCount > 0) {
            Math.round(remainingBalanceAr / installmentsCount).toDouble()
        } else remainingBalanceAr

        val today = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val installments = (1..installmentsCount).map { i ->
            val dueTimestamp = today + (i * 7L * 24L * 3600L * 1000L) // Échéance hebdomadaire
            SettlementInstallment(
                installmentNumber = i,
                dueDateFormatted = dateFormat.format(Date(dueTimestamp)),
                amountAr = singleInstallmentAmount,
                percentageOfDebt = (singleInstallmentAmount / totalDebt) * 100.0,
                status = "À ÉCHOIR"
            )
        }

        val refNumber = "PROT-APUR-26-${client.id.takeLast(4).uppercase(Locale.US)}"

        return DebtSettlementPlan(
            agreementReference = refNumber,
            clientId = client.id,
            clientName = client.name,
            totalDebtAr = totalDebt,
            immediateUpfrontDepositAr = upfrontDepositAr,
            immediateUpfrontPercentage = upfrontDepositPercent,
            remainingBalanceAr = remainingBalanceAr,
            installmentsCount = installmentsCount,
            installments = installments,
            resumptionOfDeliveriesCondition = "Livraisons à crédit strictement suspendues. Réapprovisionnement autorisé uniquement au comptoir en espèces/mobile money avec une majoration de 10% affectée à l'apurement de la dette.",
            formalAgreementLegalNotice = "Fait à Antananarivo en deux exemplaires originaux. Le non-respect d'une seule échéance rendra immédiatement exigible l'intégralité de la dette sans mise en demeure préalable (Guide de référence 2026, p. 44)."
        )
    }

    /**
     * Rédige le courrier de mise en demeure officiel.
     */
    fun buildFormalDunningLetter(
        client: Client,
        companyName: String = "Sambatra Négoce SARL",
        companyNif: String = "4001289345",
        companyStat: String = "46301 11 2026 0 10234"
    ): String {
        val dateToday = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(Date())
        return """
            ========================================================================
            MISE EN DEMEURE AVANT POURSUITES CONTENTIEUSES
            $companyName • NIF : $companyNif • STAT : $companyStat
            Siège social : Rue Pasteur, Anosibe / Antananarivo - Madagascar
            ========================================================================

            Antananarivo, le $dateToday

            À l'attention de la Direction :
            ${client.name}
            ${client.contactName}
            Tél : ${client.phone}
            Localisation : ${client.location}

            Objet : MISE EN DEMEURE DE PAYER SOUS HUITAINE - Réf. Dossier : LIT-${client.id.uppercase(Locale.US)}

            Madame, Monsieur,

            Sauf erreur ou omission de notre part, nous constatons qu'en dépit de nos précédentes relances amiables, votre compte client présente à ce jour un solde débiteur impayé de :

            >>> ${String.format(Locale.US, "%,.0f", client.currentDebtAr).replace(',', ' ')} ARIARY (MGA) <<<
            Dont ${String.format(Locale.US, "%,.0f", client.debtOver30DaysAr).replace(',', ' ')} Ariary de factures échues depuis plus de 30 jours.

            En application des clauses générales de vente de Sambatra Négoce SARL et des dispositions légales en vigueur :

            1. L'ENSEMBLE DE VOS COMMANDES ET LIVRAISONS EST STRICTEMENT BLOQUÉ à compter de la réception de la présente.
            2. Nous vous mettons en demeure de régulariser la somme principale ci-dessus sous un délai impératif de HUIT (8) JOURS OUVRÉS, par virement bancaire ou versement direct à notre caisse centrale.

            À défaut de règlement intégral ou de signature d'un protocole d'apurement transactionnel avec versement d'acompte dans ce délai, nous serons contraints de transmettre sans autre préavis votre dossier à notre conseil juridique aux fins de recouvrement forcé par voie d'injonction de payer.

            Dans l'attente de votre prompt règlement, nous vous prions d'agréer, Madame, Monsieur, l'expression de nos salutations distinguées.

            La Direction Financière & Recouvrement
            Sambatra Négoce SARL
        """.trimIndent()
    }
}
