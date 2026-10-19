package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.ClientSegment
import com.example.data.model.PaymentStatus
import com.example.ui.components.SectionTitle
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(viewModel: MainViewModel) {
    val clients by viewModel.clients.collectAsState()
    val currentTreasury by viewModel.currentTreasury.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()

    var selectedFilter by remember { mutableStateOf(0) } // 0: Tous, 1: En Retard >30j, 2: Bloqués, 3: Segments A/B, 4: Simuler Crédit
    var expandedClientId by remember { mutableStateOf<String?>(null) }
    var selectedClientForPayment by remember { mutableStateOf<Client?>(null) }
    var selectedClientForDunning by remember { mutableStateOf<Client?>(null) }
    var paymentAmountText by remember { mutableStateOf("2000000") }

    // État simulateur crédit
    var simSelectedClientId by remember { mutableStateOf(clients.firstOrNull()?.id ?: "") }
    var simCreditAmountText by remember { mutableStateOf("3000000") }
    var showWorkingCapitalDialog by remember { mutableStateOf(false) }
    var showCommercialDocDialog by remember { mutableStateOf(false) }

    val totalDebt = clients.sumOf { it.currentDebtAr }
    val debtOver30 = clients.sumOf { it.debtOver30DaysAr }
    val globalCreditCapAr = 15_000_000.0 // Plafond d'encours global fixé selon le Guide (max 30-35% du CA mensuel)
    val treasuryExposurePercent = if (currentTreasury > 0) (totalDebt / currentTreasury) * 100.0 else 0.0

    val filteredClients = when (selectedFilter) {
        1 -> clients.filter { it.debtOver30DaysAr > 0 }
        2 -> clients.filter { it.isBlockedByRule }
        3 -> clients.filter { it.segment == ClientSegment.A || it.segment == ClientSegment.B }
        else -> clients
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // En-tête Règle d'or du Guide 2026
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditScore, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "« La marge se perd au crédit. » (Guide 2026 p. 4, 37 & 44)\nLimitation stricte des encours • Solvabilité sur 25 points • Suspension immédiate en cas d'impayé.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Tableau de Bord Synthèse des Créances & Exposition Trésorerie
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tableau de Bord Créances & Trésorerie",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Surface(
                            color = if (debtOver30 > 0) Color(0xFFFEE2E2) else Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (debtOver30 > 0) "1 CLIENT EN LITIGE" else "RISQUE MAÎTRISÉ",
                                color = if (debtOver30 > 0) StatusRedDanger else StatusGreenOk,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // 4 Blocs KPI
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KpiMiniBox(
                            title = "Encours Total",
                            value = "${(totalDebt / 1_000_000.0).toString().take(4)}M Ar",
                            subtitle = "Plafond: 15M Ar",
                            color = AgroGreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        KpiMiniBox(
                            title = "Retards > 30j",
                            value = "${(debtOver30 / 1_000_000.0).toString().take(4)}M Ar",
                            subtitle = "Seuil: 0 Ar !",
                            color = StatusRedDanger,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        KpiMiniBox(
                            title = "Encours / Trésorerie",
                            value = "${String.format(java.util.Locale.US, "%.1f", treasuryExposurePercent)}%",
                            subtitle = "Alerte si > 40%",
                            color = if (treasuryExposurePercent > 40.0) StatusAmberWarning else StatusGreenOk,
                            modifier = Modifier.weight(1f)
                        )
                        KpiMiniBox(
                            title = "DSO Moyen",
                            value = "22 jours",
                            subtitle = "Cible: < 20 jours",
                            color = SpiceOrangeSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Jauge de respect du plafond global
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plafond global d'encours utilisé :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${(totalDebt / 1_000_000.0).toString().take(4)}M / 15M Ar (${String.format(java.util.Locale.US, "%.1f", (totalDebt / globalCreditCapAr) * 100.0)}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ((totalDebt / globalCreditCapAr).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (totalDebt > globalCreditCapAr) StatusRedDanger else AgroGreenPrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showWorkingCapitalDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Diagnostic BFR", fontSize = 11.sp, color = AgroGreenPrimary)
                        }
                        OutlinedButton(
                            onClick = { showCommercialDocDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bons BL & Audit", fontSize = 11.sp, color = SpiceOrangeSecondary)
                        }
                    }
                }
            }
        }

        // Alerte Proactive Immédiate sur les retards de paiement
        if (debtOver30 > 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StatusRedDanger)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = StatusRedDanger, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ALERTE TRÉSORERIE : Paiement en Retard Critique (p. 44)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StatusRedDanger
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Le client « Hôtel-Restaurant Le Tsinjo » a une facture échue de 4 200 000 Ar depuis 38 jours.\n" +
                                    "Application de la règle du guide : LIVRAISONS BLOQUÉES. Aucune nouvelle expédition à crédit autorisée tant que le solde n'est pas apuré.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF7F1D1D)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    val client = clients.find { it.id == "cli_restau_tsinjo" }
                                    selectedClientForDunning = client
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRedDanger),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Déclencher la Relance J+15", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val client = clients.find { it.id == "cli_restau_tsinjo" }
                                    selectedClientForPayment = client
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Encaisser l'Acompte", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Filtres & Barre d'onglets
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "Tous (${clients.size})",
                    1 to "Retards >30j (${clients.count { it.debtOver30DaysAr > 0 }})",
                    2 to "Bloqués (${clients.count { it.isBlockedByRule }})",
                    3 to "Segments A/B",
                    4 to "Simuler Crédit"
                ).forEach { (idx, label) ->
                    FilterChip(
                        selected = selectedFilter == idx,
                        onClick = { selectedFilter = idx },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }

        // Module Simulateur de Nouveau Crédit
        if (selectedFilter == 4) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Simulateur d'Admissibilité de Nouveau Crédit (Guide p. 44)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = "Vérifie les 3 verrous prudentiels avant d'autoriser une livraison à crédit.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text("Sélectionner le client :", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            clients.take(3).forEach { c ->
                                val isSelected = c.id == simSelectedClientId
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { simSelectedClientId = c.id },
                                    label = { Text(c.name.take(14), fontSize = 10.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = simCreditAmountText,
                            onValueChange = { simCreditAmountText = it },
                            label = { Text("Montant de la nouvelle commande à crédit (Ar)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        val simAmount = simCreditAmountText.toDoubleOrNull() ?: 3_000_000.0
                        val evalResult = viewModel.evaluateClientCreditRequest(simSelectedClientId, simAmount)

                        Surface(
                            color = if (evalResult.isAuthorized) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = evalResult.verdict,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (evalResult.isAuthorized) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                evalResult.blockingReasons.forEach { reason ->
                                    Text("❌ $reason", fontSize = 11.sp, color = Color(0xFF991B1B), lineHeight = 15.sp)
                                }
                                evalResult.recommendedConditions.forEach { cond ->
                                    Text("⚠️ Condition : $cond", fontSize = 11.sp, color = Color(0xFF92400E), lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Portefeuille Clients et Fiches de Solvabilité
        item {
            SectionTitle(
                title = if (lang == "FR") "Portefeuille Clients & Historique de Solvabilité" else "Mpanjifa & Tantara fandoavam-bola",
                subtitle = "Classification sur 25 points, encours et historique des règlements"
            )
        }

        items(filteredClients) { client ->
            val isExpanded = expandedClientId == client.id

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Ligne 1 : Segment, Nom, Score et Statut de Blocage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                color = when (client.segment) {
                                    ClientSegment.A -> AgroGreenPrimary
                                    ClientSegment.B -> SpiceOrangeSecondary
                                    ClientSegment.C -> Color.Gray
                                    ClientSegment.D -> StatusRedDanger
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = client.segment.name,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = client.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${client.contactName} • ${client.phone} • ${client.location}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Score & Solvabilité
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Score : ${client.creditScore25}/25",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = client.solvencyLabel.take(16),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    client.creditScore25 >= 21 -> StatusGreenOk
                                    client.creditScore25 >= 16 -> AgroGreenPrimary
                                    client.creditScore25 >= 12 -> StatusAmberWarning
                                    else -> StatusRedDanger
                                }
                            )
                        }
                    }

                    // Badge Règle d'or si bloqué
                    if (client.isBlockedByRule) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = StatusRedDanger, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVRAISON SUSPENDUE : Facture en retard > 30j !",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRedDanger
                                    )
                                }
                                Button(
                                    onClick = { selectedClientForPayment = client },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedDanger),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text("Apurer", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Ligne 2 : Données financières
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Dette en cours :", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${(client.currentDebtAr / 1_000_000.0).toString().take(4)}M Ar",
                                fontWeight = FontWeight.Bold,
                                color = if (client.debtOver30DaysAr > 0) StatusRedDanger else AgroGreenPrimary,
                                fontSize = 12.sp
                            )
                        }
                        Column {
                            Text("Plafond autorisé :", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(client.creditLimitAr / 1_000_000.0).toString().take(4)}M Ar", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Délai moyen :", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${client.averagePaymentDays} jours", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Part de votre CA :", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", client.shareOfTotalCaPercent)}%",
                                fontWeight = FontWeight.Bold,
                                color = if (client.shareOfTotalCaPercent > 20.0) StatusAmberWarning else StatusGreenOk,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Jauge d'utilisation du crédit
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Utilisation crédit :", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${String.format(java.util.Locale.US, "%.0f", client.creditUtilizationPercent)}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { ((client.creditUtilizationPercent / 100.0).toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp),
                            color = if (client.creditUtilizationPercent > 100.0) StatusRedDanger else AgroGreenPrimary,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bouton pour afficher l'historique de solvabilité
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { expandedClientId = if (isExpanded) null else client.id },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AgroGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "Masquer l'historique de solvabilité" else "Voir l'historique de solvabilité (${client.paymentHistory.size} factures)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AgroGreenPrimary
                            )
                        }

                        if (client.currentDebtAr > 0) {
                            OutlinedButton(
                                onClick = { selectedClientForPayment = client },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Encaisser", fontSize = 10.sp)
                            }
                        }
                    }

                    // Section Historique de Solvabilité Dépliée
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Historique des Règlements & Ponctualité (Guide p. 44)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AgroGreenPrimary
                            )

                            if (client.paymentHistory.isEmpty()) {
                                Text("Aucune facture passée enregistrée pour ce client.", fontSize = 10.sp, color = Color.Gray)
                            } else {
                                client.paymentHistory.forEach { entry ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        border = CardDefaults.outlinedCardBorder(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${entry.invoiceNumber} • ${entry.dateFacture}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Surface(
                                                    color = when (entry.statut) {
                                                        PaymentStatus.PAYE_A_TEMPS -> Color(0xFFF0FDF4)
                                                        PaymentStatus.PAYE_EN_RETARD -> Color(0xFFFFFBEB)
                                                        PaymentStatus.EN_COURS -> Color(0xFFEFF6FF)
                                                        PaymentStatus.EN_RETARD -> Color(0xFFFEF2F2)
                                                        PaymentStatus.LITIGE -> Color(0xFFFDF4FF)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = entry.statut.label,
                                                        color = when (entry.statut) {
                                                            PaymentStatus.PAYE_A_TEMPS -> StatusGreenOk
                                                            PaymentStatus.PAYE_EN_RETARD -> StatusAmberWarning
                                                            PaymentStatus.EN_COURS -> Color(0xFF1D4ED8)
                                                            PaymentStatus.EN_RETARD -> StatusRedDanger
                                                            PaymentStatus.LITIGE -> Color(0xFF9333EA)
                                                        },
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Montant : ${(entry.montantAr / 1_000_000.0).toString().take(4)}M Ar", fontSize = 10.sp)
                                                Text("Délai réel : ${entry.delaiReelJours}j (accordé: ${entry.delaiAccordeJours}j)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }

                                            if (entry.commentaire.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = entry.commentaire,
                                                    fontSize = 9.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = if (entry.statut == PaymentStatus.EN_RETARD) StatusRedDanger else Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Encaisser un règlement
    if (selectedClientForPayment != null) {
        val client = selectedClientForPayment!!
        AlertDialog(
            onDismissRequest = { selectedClientForPayment = null },
            title = { Text("Encaisser un règlement - ${client.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Dette actuelle : ${client.currentDebtAr.toInt()} Ar. Tout règlement renforce instantanément la trésorerie disponible de Sambatra Négoce.",
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = paymentAmountText,
                        onValueChange = { paymentAmountText = it },
                        label = { Text("Montant encaissé (Ar)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
                        viewModel.recordClientPayment(client.id, amount)
                        selectedClientForPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                ) {
                    Text("Valider l'encaissement")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedClientForPayment = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Protocole de Recouvrement Négocié (Guide p. 44)
    if (selectedClientForDunning != null) {
        val client = selectedClientForDunning!!
        com.example.ui.components.DebtRecoveryPlanDialog(
            client = client,
            onDismiss = { selectedClientForDunning = null },
            onPlanConfirmed = { plan ->
                selectedClientForDunning = null
            }
        )
    }

    if (showWorkingCapitalDialog) {
        com.example.ui.components.WorkingCapitalDialog(
            viewModel = viewModel,
            onDismiss = { showWorkingCapitalDialog = false }
        )
    }

    if (showCommercialDocDialog) {
        com.example.ui.components.CommercialDocumentDialog(
            viewModel = viewModel,
            onDismiss = { showCommercialDocDialog = false }
        )
    }
}

@Composable
private fun KpiMiniBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(1.dp))
            Text(text = subtitle, fontSize = 9.sp, color = Color.Gray)
        }
    }
}
