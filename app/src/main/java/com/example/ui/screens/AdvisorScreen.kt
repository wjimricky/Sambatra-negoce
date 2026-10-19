package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QualityDecision
import com.example.ui.components.SectionTitle
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun AdvisorScreen(viewModel: MainViewModel) {
    val lang by viewModel.selectedLanguage.collectAsState()
    var selectedSubTab by remember { mutableStateOf(0) }
    var showPriceAnalysisDialog by remember { mutableStateOf(false) }
    var showSmartStorageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // En-tête Conseiller GrosGrains
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SpiceOrangeSecondary,
                    shape = CircleShape,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "FR") "Conseiller GrosGrains & IA Prédictive" else "Mpanolotsaina GrosGrains & AI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = if (lang == "FR") "Google Search • Prédiction Prix • Zonage IA (Guide 2026)" else "Google Search • Faminaniana Vidiny • Zonage AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Sélecteur de sous-onglets enrichi
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = AgroGreenPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Conseil & Chat", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Grille Achat (8 Q)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Simulateurs Cas", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = { selectedSubTab = 3 },
                text = { Text("Prédiction Prix", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedSubTab == 4,
                onClick = { selectedSubTab = 4 },
                text = { Text("Stockage IA (400 m²)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (selectedSubTab) {
            0 -> AdvisorChatSection(viewModel)
            1 -> PurchaseDecisionGridSection(viewModel)
            2 -> ScenarioSimulatorsSection(viewModel)
            3 -> PredictivePriceSectionView(onOpenFullDialog = { showPriceAnalysisDialog = true })
            4 -> SmartStorageSectionView(viewModel = viewModel, onOpenFullDialog = { showSmartStorageDialog = true })
        }
    }

    if (showPriceAnalysisDialog) {
        com.example.ui.components.PredictivePriceAnalysisDialog(
            onDismiss = { showPriceAnalysisDialog = false }
        )
    }

    if (showSmartStorageDialog) {
        com.example.ui.components.SmartStorageRecommendationDialog(
            initialLot = viewModel.lots.value.firstOrNull(),
            onDismiss = { showSmartStorageDialog = false }
        )
    }
}

@Composable
fun AdvisorChatSection(viewModel: MainViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAdvisorThinking.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()
    val isSearchEnabled by viewModel.isGoogleSearchEnabled.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val suggestedQuestions = listOf(
        "Météo & pluie RN2 : risque sur stocks de riz ?",
        "Prévision prix riz Alaotra : quand acheter et revendre ?",
        "Où stocker 2 tonnes de girofle dans l'entrepôt ?",
        "Riz : rendement décorticage et rentabilité ?",
        "Haricot sec : vendre à 2 mois ou garder 5 mois ?",
        "Vanille : comment appliquer la règle des 10% ?",
        "Girofle : que faire si le lot contient 11% de tiges ?",
        "Client en retard : comment gérer l'échéancier ?"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Google Search Grounding Météo & Web
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isSearchEnabled) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(if (isSearchEnabled) Color(0xFF2563EB) else Color(0xFFCBD5E1))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = if (isSearchEnabled) Color(0xFF2563EB) else Color.Gray,
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.TravelExplore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isSearchEnabled) "Recherche Météo & Web Google (Activée)" else "Recherche Web Google (Désactivée)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSearchEnabled) Color(0xFF1E40AF) else Color.DarkGray
                        )
                        Text(
                            text = "Croise le guide 2026 avec les alertes météo et cours réels",
                            fontSize = 9.5.sp,
                            color = Color.Gray
                        )
                    }
                }
                Switch(
                    checked = isSearchEnabled,
                    onCheckedChange = { viewModel.toggleGoogleSearch() },
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        // Suggestions rapides
        Text(
            text = if (lang == "FR") "Questions fréquentes du guide :" else "Fanontaniana mahazatra :",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(suggestedQuestions) { q ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable {
                        viewModel.askAdvisor(q)
                    }
                ) {
                    Text(
                        text = q,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(msg)
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AgroGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == "FR") "Le Conseiller GrosGrains analyse les règles du livre..." else "Mandinika ny fitsipika ny Mpanolotsaina...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Barre de saisie
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (lang == "FR") "Posez votre question de gestion..." else "Mametraha fanontaniana...",
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 2,
                shape = RoundedCornerShape(24.dp)
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val q = inputText
                        inputText = ""
                        viewModel.askAdvisor(q)
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AgroGreenPrimary)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Envoyer",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage) {
    val isUser = msg.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                color = SpiceOrangeSecondary,
                shape = CircleShape,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            color = if (isUser) AgroGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = msg.text,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                if (!isUser && msg.isGroundedWithGoogleSearch) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vérifié Google Search • Météo & Cours", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                        }
                    }
                }

                if (!isUser && msg.searchSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Sources web consultées :", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        msg.searchSources.take(3).forEach { src ->
                            Text("• ${src.title}", fontSize = 8.5.sp, color = Color(0xFF2563EB))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = msg.timestamp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun PurchaseDecisionGridSection(viewModel: MainViewModel) {
    val criteria by viewModel.purchaseCriteria.collectAsState()
    val evaluation by viewModel.purchaseEvaluation.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (evaluation.canProceed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (evaluation.canProceed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (evaluation.canProceed) StatusGreenOk else StatusRedDanger,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = evaluation.verdict,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (evaluation.canProceed) StatusGreenOk else StatusRedDanger
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = evaluation.bookReference,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    if (evaluation.requiredConditions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        evaluation.requiredConditions.forEach { cond ->
                            Text(
                                text = "• $cond",
                                fontSize = 11.sp,
                                color = if (evaluation.canProceed) AgroGreenPrimary else StatusRedDanger
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionTitle(
                title = if (lang == "FR") "Les 4 Critères Éliminatoires (p. 96)" else "Fepetra 4 tsy maintsy fenoina",
                subtitle = if (lang == "FR") "Si l'un de ces 4 est NON, l'achat doit être catégoriquement refusé !" else "Raha misy 'TSIA' na iray aza dia aza mividy mihitsy !"
            )
        }

        item {
            DecisionCheckItem(
                title = "1. Prix de revient complet calculé ?",
                subtitle = "Prix d'achat + fret + manutention + pertes transport + financement",
                checked = criteria.hasFullCostPriceCalculated,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q1 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "2. Marge cible atteignable au prix du marché ?",
                subtitle = "Prix de vente réaliste >= Coût variable / (1 - marge visée)",
                checked = criteria.isTargetMarginAchievable,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q2 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "3. Acheteur identifié ou contrat signé ?",
                subtitle = "Un besoin confirmé par des commandes réelles, pas de spéculation aveugle",
                checked = criteria.hasIdentifiedBuyerOrContract,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q3 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "4. Qualité contrôlée avant paiement ?",
                subtitle = "Mesures d'humidité à l'humidimètre et impuretés faites contradictoirement",
                checked = criteria.isQualityControlledPrePayment,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q4 = it) }
            )
        }

        item {
            SectionTitle(
                title = if (lang == "FR") "Les 4 Critères de Prudence Financière" else "Fepetra 4 fitandremana vola",
                subtitle = if (lang == "FR") "Si l'un est NON, réduire le volume du lot ou différer l'opération" else "Raha misy TSIA dia ahenao ny fatra na andraso kely"
            )
        }

        item {
            DecisionCheckItem(
                title = "5. Durée de stockage compatible ?",
                subtitle = "Pas de stockage prolongé sur légumineuses sensibles aux bruches",
                checked = criteria.isStorageDurationSafe,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q5 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "6. Règle des 10% / 30% respectée ?",
                subtitle = "Lot < 10% des fonds propres si produit volatil (vanille), < 30% trésorerie sinon",
                checked = criteria.isExposureWithinLimits,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q6 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "7. Trésorerie reste >= 2 mois de charges ?",
                subtitle = "Sambatra Négoce : au moins 10 000 000 Ar disponibles après décaissement",
                checked = criteria.doesTreasuryRemainAboveTwoMonths,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q7 = it) }
            )
        }
        item {
            DecisionCheckItem(
                title = "8. Deuxième fournisseur disponible ?",
                subtitle = "Avoir au moins deux sources pour éviter d'être pris en otage",
                checked = criteria.hasSecondSupplierAvailable,
                onCheckedChange = { viewModel.updatePurchaseCriteria(q8 = it) }
            )
        }
    }
}

@Composable
fun DecisionCheckItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = AgroGreenPrimary, checkedTrackColor = AgroGreenContainer)
            )
        }
    }
}

@Composable
fun ScenarioSimulatorsSection(viewModel: MainViewModel) {
    var activeSimulator by remember { mutableStateOf(0) } // 0: Décorticage Riz, 1: Remise & Volume, 2: Coût du Crédit

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeSimulator == 0,
                    onClick = { activeSimulator = 0 },
                    label = { Text("Décorticage Riz (p. 88)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeSimulator == 1,
                    onClick = { activeSimulator = 1 },
                    label = { Text("Remise & Volume (p. 37)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeSimulator == 2,
                    onClick = { activeSimulator = 2 },
                    label = { Text("Crédit & Impayé (p. 40)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = activeSimulator == 3,
                    onClick = { activeSimulator = 3 },
                    label = { Text("Achat & Stockage (p. 30, 36)", fontSize = 11.sp) }
                )
            }
        }

        when (activeSimulator) {
            0 -> {
                item {
                    MillingSimulatorCard(viewModel)
                }
            }
            1 -> {
                item {
                    DiscountSimulatorCard(viewModel)
                }
            }
            2 -> {
                item {
                    CreditSimulatorCard(viewModel)
                }
            }
            3 -> {
                item {
                    PurchaseAndStorageSimulatorCard(viewModel)
                }
            }
        }
    }
}


@Composable
fun MillingSimulatorCard(viewModel: MainViewModel) {
    var paddyPrice by remember { mutableStateOf(1300.0) }
    var whiteRicePrice by remember { mutableStateOf(2300.0) }
    var yield by remember { mutableStateOf(65.0) }

    val sim = viewModel.simulatePaddyMilling(paddyPrice, whiteRicePrice, yield)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulateur Décorticage Paddy -> Riz Blanc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AgroGreenPrimary
            )
            Text(
                text = "Inspiré de l'Étude de Cas 1 (p. 88) : la rentabilité dépend du rendement réel de la machine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sélecteur de rendement
            Text(text = "Rendement de décorticage : ${yield.toInt()}%", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                listOf(60.0, 63.0, 65.0, 68.0).forEach { y ->
                    Button(
                        onClick = { yield = y },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (yield == y) AgroGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (yield == y) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${y.toInt()}%", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Résultats du calcul
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paddy requis par kg de riz blanc :", fontSize = 12.sp)
                        Text("${String.format(Locale.US, "%.2f", sim.paddyRequiredKg)} kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prix de revient complet par kg :", fontSize = 12.sp)
                        Text("${sim.totalCostPerKgWhiteRiceAr.toInt()} Ar/kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Marge brute unitaire :", fontSize = 12.sp)
                        Text("${sim.grossMarginAr.toInt()} Ar/kg", fontWeight = FontWeight.Bold, color = AgroGreenPrimary, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Taux de marge brute :", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${String.format(Locale.US, "%.1f", sim.grossMarginPercent)}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sim.grossMarginPercent >= 14.0) StatusGreenOk else if (sim.grossMarginPercent >= 10.0) StatusAmberWarning else StatusRedDanger
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Verdict du guide : ${sim.recommendation}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DiscountSimulatorCard(viewModel: MainViewModel) {
    var discount by remember { mutableStateOf(3.0) }
    val sim = viewModel.simulateDiscount(3151.0, 3664.0, discount)

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulateur : Coût Caché d'une Remise",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SpiceOrangeSecondary
            )
            Text(
                text = "Exemple Haricot sec (Revient 3 151 Ar • Vente 3 664 Ar • Marge 14%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Remise accordée : ${discount.toInt()}%", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                listOf(0.0, 2.0, 3.0, 5.0).forEach { d ->
                    Button(
                        onClick = { discount = d },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (discount == d) SpiceOrangeSecondary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (discount == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${d.toInt()}%", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFFFFFBEB),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Nouveau prix : ${sim.discountedPriceAr.toInt()} Ar/kg (Marge ${String.format(Locale.US, "%.1f", sim.newMarginPercent)}%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Marge unitaire perdue : ${sim.lostUnitMarginAr.toInt()} Ar/kg (-${String.format(Locale.US, "%.1f", sim.percentUnitMarginLost)}% de marge)",
                        fontSize = 12.sp,
                        color = StatusRedDanger,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Volume supplémentaire obligatoire à vendre : +${String.format(Locale.US, "%.1f", sim.requiredVolumeIncreasePercent)}% pour conserver le même gain !",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusAmberWarning
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "« Une remise permanente détruit votre marge et conditionne le client à demander toujours plus. Réservez les remises à des contreparties strictes (volume engagé, paiement immédiat). » (Guide p. 45)",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreditSimulatorCard(viewModel: MainViewModel) {
    var creditDays by remember { mutableStateOf(45) }
    var unpaidAmount by remember { mutableStateOf(4_200_000.0) }

    val sim = viewModel.simulateCredit(
        salePriceAr = 3664.0,
        creditDays = creditDays,
        monthlyRatePercent = 1.5,
        targetMarginPercent = 14.0,
        potentialUnpaidAmountAr = unpaidAmount
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulateur : Coût Caché du Crédit & Impayé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StatusRedDanger
            )
            Text(
                text = "Inspiré des chapitres 7.3 et 8.4 du guide (taux financement 1.5% par mois)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Durée du crédit client accordé : $creditDays jours", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                listOf(15, 30, 45, 60).forEach { d ->
                    Button(
                        onClick = { creditDays = d },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (creditDays == d) StatusRedDanger else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (creditDays == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("$d jours", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFFFEE2E2),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Coût financier du crédit : ${String.format(Locale.US, "%.2f", sim.creditCostPercentOfPrice)}% du prix de vente",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusRedDanger
                    )
                    Text(
                        text = "Érosion de marge : supprime ${String.format(Locale.US, "%.1f", sim.marginErosionPercent)}% de votre marge de 14% !",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusRedDanger
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ventes nécessaires pour compenser un impayé de ${(unpaidAmount / 1_000_000.0).toString().take(3)}M Ar :",
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", sim.salesNeededToCompensateUnpaidAr / 1_000_000.0)} Millions d'Ariary de CA supplémentaire !",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "« Ventes nécessaires pour compenser un impayé = montant impayé ÷ taux de marge. Un impayé de 6M Ar à 14% de marge détruit le gain de 42,9 millions Ar de ventes. » (Guide p. 40)",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PurchaseAndStorageSimulatorCard(viewModel: MainViewModel) {
    val currentTreasury by viewModel.currentTreasury.collectAsState()
    var purchaseAmountText by remember { mutableStateOf("12000000") }
    var storageMonths by remember { mutableStateOf(2.0) }
    var resalePriceText by remember { mutableStateOf("14500000") }

    val purchaseAmount = purchaseAmountText.toDoubleOrNull() ?: 12_000_000.0
    val resalePrice = resalePriceText.toDoubleOrNull() ?: 14_500_000.0

    val sim = viewModel.simulateImpactFinancierAchatOuStockage(
        montantAchatAr = purchaseAmount,
        dureeStockageMois = storageMonths,
        tauxPortageFinancierMensuel = 1.5,
        tauxFreinteMensuel = 1.0,
        prixVentePrevisionnelAr = resalePrice
    )

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
                Column {
                    Text(
                        text = "Simulateur Achat Important & Stockage Prolongé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = "Règles d'or du Guide 2026 : Portage 1.5%/mois • Seuil 2 mois de trésorerie (10M Ar)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = when (sim.niveauRisque) {
                        com.example.data.model.NiveauRisqueAchat.FAIBLE -> StatusGreenOk
                        com.example.data.model.NiveauRisqueAchat.MODERE -> StatusAmberWarning
                        com.example.data.model.NiveauRisqueAchat.ELEVE -> SpiceOrangeSecondary
                        com.example.data.model.NiveauRisqueAchat.CRITIQUE -> StatusRedDanger
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = sim.niveauRisque.label.take(15),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Saisie du montant de l'achat
            OutlinedTextField(
                value = purchaseAmountText,
                onValueChange = { purchaseAmountText = it },
                label = { Text("Montant d'achat envisagé (Ariary)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Sélecteurs de montants rapides
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5_000_000.0, 10_000_000.0, 15_000_000.0, 20_000_000.0).forEach { amt ->
                    FilterChip(
                        selected = purchaseAmount == amt,
                        onClick = { purchaseAmountText = amt.toInt().toString() },
                        label = { Text("${(amt / 1_000_000.0).toInt()}M Ar", fontSize = 10.sp) }
                    )
                }
            }

            // Durée de stockage prévue
            Text("Durée de stockage prévue : ${storageMonths.toInt()} mois", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1.0, 2.0, 3.0, 4.0, 6.0).forEach { m ->
                    FilterChip(
                        selected = storageMonths == m,
                        onClick = { storageMonths = m },
                        label = { Text("${m.toInt()} mois", fontSize = 10.sp) }
                    )
                }
            }

            OutlinedTextField(
                value = resalePriceText,
                onValueChange = { resalePriceText = it },
                label = { Text("Prix de revente prévisionnel total (Ariary)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Résultats chiffrés détaillés
            Surface(
                color = when (sim.niveauRisque) {
                    com.example.data.model.NiveauRisqueAchat.CRITIQUE -> Color(0xFFFEF2F2)
                    com.example.data.model.NiveauRisqueAchat.ELEVE -> Color(0xFFFFFBEB)
                    com.example.data.model.NiveauRisqueAchat.MODERE -> Color(0xFFFFFBEB)
                    com.example.data.model.NiveauRisqueAchat.FAIBLE -> Color(0xFFF0FDF4)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trésorerie disponible actuelle :", fontSize = 11.sp)
                        Text("${(sim.tresorerieInitialeAr / 1_000_000.0).toString().take(4)}M Ar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trésorerie résiduelle après achat :", fontSize = 11.sp)
                        Text(
                            text = "${(sim.tresorerieResiduelleAr / 1_000_000.0).toString().take(4)}M Ar (${String.format(Locale.US, "%.1f", sim.moisChargesResiduels)} mois charges)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (sim.moisChargesResiduels < 2.0) StatusRedDanger else AgroGreenPrimary
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Coût portage financier (1.5%/mois) :", fontSize = 11.sp)
                        Text("${sim.coutPortageFinancierAr.toInt()} Ar", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = StatusRedDanger)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Freinte & pertes stock (1.0%/mois) :", fontSize = 11.sp)
                        Text("${sim.coutFreinteEtPertesAr.toInt()} Ar", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SpiceOrangeSecondary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Coût de revient complet réel :", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${sim.coutRevientCompletAchatAr.toInt()} Ar (+${String.format(Locale.US, "%.1f", sim.surcoutPourcentage)}%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    if (resalePrice > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Marge brute finale nette de portage :", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${sim.margeBruteFinaleAr.toInt()} Ar (${String.format(Locale.US, "%.1f", sim.margeBruteFinalePercent)}%)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (sim.margeBruteFinalePercent >= 14.0) StatusGreenOk else StatusRedDanger
                            )
                        }
                    }
                }
            }

            // Synthèse et règles du guide
            Text(
                text = sim.syntheseRecommandation,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = when (sim.niveauRisque) {
                    com.example.data.model.NiveauRisqueAchat.CRITIQUE -> StatusRedDanger
                    com.example.data.model.NiveauRisqueAchat.ELEVE -> Color(0xFFC2410C)
                    com.example.data.model.NiveauRisqueAchat.MODERE -> Color(0xFFB45309)
                    com.example.data.model.NiveauRisqueAchat.FAIBLE -> AgroGreenPrimary
                }
            )

            sim.conseilsRisqueGuide.forEach { conseil ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Text("•", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = conseil, fontSize = 11.sp, lineHeight = 15.sp, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bouton pour interroger Gemini
            Button(
                onClick = {
                    val query = "Simule l'impact d'un achat de ${(purchaseAmount / 1_000_000.0).toString().take(4)}M Ar stocké pendant ${storageMonths.toInt()} mois avec notre trésorerie actuelle de ${(currentTreasury / 1_000_000.0).toString().take(4)}M Ar selon le Guide 2026."
                    viewModel.askAdvisor(query)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Demander l'analyse approfondie à l'IA Gemini", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun PredictivePriceSectionView(onOpenFullDialog: () -> Unit) {
    val commodities = com.example.data.ai.PredictivePriceEngine.COMMODITIES_DATA
    val numberFormat = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale.FRANCE) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Moteur Prédictif des Prix & Arbitrage (Guide 2026)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Anticipation des cours récolte vs soudure sur 12 mois avec ROI net déduit des frais de stockage.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = onOpenFullDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulateur ROI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(commodities) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Bassin : ${item.productionBasin}", fontSize = 10.5.sp, color = Color.Gray)
                        }
                        Surface(
                            color = Color(item.riskLevel.colorHex).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.riskLevel.labelFr,
                                color = Color(item.riskLevel.colorHex),
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Achat Récolte :", fontSize = 9.5.sp, color = Color(0xFF15803D))
                                Text("${numberFormat.format(item.harvestFloorPriceAr.toInt())} Ar/kg", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                                Text(item.harvestMonthName, fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            color = Color(0xFFFFF7ED),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Revente Soudure :", fontSize = 9.5.sp, color = Color(0xFFC2410C))
                                Text("${numberFormat.format(item.leanPeakPriceAr.toInt())} Ar/kg", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC2410C))
                                Text(item.leanPeakMonthName, fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Surface(
                            color = Color(0xFFEFF6FF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Gain Net Estimé :", fontSize = 9.5.sp, color = Color(0xFF1D4ED8))
                                Text("+${numberFormat.format(item.netExpectedGainPerKgAr.toInt())} Ar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E40AF))
                                Text("ROI : +${item.netReturnOnInvestmentPercent.toInt()}%", fontSize = 9.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Conseil : ${item.strategicActionAdvice}",
                        fontSize = 10.sp,
                        color = Color.DarkGray,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmartStorageSectionView(
    viewModel: MainViewModel,
    onOpenFullDialog: () -> Unit
) {
    val lots by viewModel.lots.collectAsState()
    val zones = com.example.data.ai.SmartStorageRecommender.ZONE_CAPACITIES.values.toList()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Conseiller de Stockage Intelligent (400 m²)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "L'IA assigne l'allée et la palette optimale en évitant les contaminations d'odeurs (épices vs céréales).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }

                        Button(
                            onClick = onOpenFullDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tester un Lot", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Vue 2D de l'entrepôt
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Occupation Actuelle des 4 Zones d'Entrepôt", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    zones.forEach { zone ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(zone.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${zone.currentOccupiedBags} / ${zone.totalCapacityBags} sacs (${zone.occupancyRatePercent.toInt()}%)", fontSize = 10.5.sp, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (zone.occupancyRatePercent / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = if (zone.occupancyRatePercent > 85.0) StatusRedDanger else AgroGreenPrimary,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // Lots en attente d'optimisation
        item {
            Text("Lots en stock à auditer par l'IA :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        items(lots.take(4)) { lot ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lot.lotNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                        Text("${lot.productName} • ${lot.quantityKg.toInt()} kg (${lot.bagsCount} sacs)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Zone actuelle : ${lot.warehouseZone} • Humidité : ${lot.moisturePercent}%", fontSize = 10.sp, color = Color.Gray)
                    }

                    OutlinedButton(
                        onClick = onOpenFullDialog,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Audit IA", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


