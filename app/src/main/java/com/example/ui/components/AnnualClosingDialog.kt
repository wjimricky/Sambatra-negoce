package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.closing.AnnualClosingEngine
import com.example.data.closing.AuditCertificationGrade
import com.example.data.local.DemoDataProvider
import com.example.data.report.AnnualAuditPdfReportGenerator
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualClosingDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Calculs financiers réactifs
    val currentTreasury = 10_000_000.0 // 2 mois de charges fixes = 10M Ar
    val pnl = remember(products, lots, clients) {
        AnnualClosingEngine.calculateAnnualPnl(products = products, lots = lots, clients = clients)
    }
    val scorecard = remember(products, lots, clients) {
        AnnualClosingEngine.generateAnnualAuditScorecard(
            products = products,
            lots = lots,
            clients = clients,
            treasuryAr = currentTreasury
        )
    }

    // États de simulation budget N+1
    var targetGrowthPercent by remember { mutableDoubleStateOf(15.0) }
    var speculativeBudgetAr by remember { mutableDoubleStateOf(8_000_000.0) }
    val budgetForecast = remember(targetGrowthPercent, speculativeBudgetAr) {
        AnnualClosingEngine.simulateNextYearBudget(
            currentEquityAr = 120_000_000.0,
            targetGrowthPercent = targetGrowthPercent,
            spicesSpeculativeBudgetAr = speculativeBudgetAr
        )
    }

    var isClosingOfficiallyLocked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.93f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // En-tête
                Surface(
                    color = AgroGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clôture d'Exercice & Bilan Annuel 2026",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Text(
                                text = "Sambatra Négoce SARL • Chapitres 14 & 16 du Guide 2026",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }

                // Onglets
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = AgroGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("P&L & Marges", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scorecard Audit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Budget N+1", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Clôture & Export", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                // Contenu des onglets
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    when (selectedTab) {
                        0 -> PnlAndMarginsTab(pnl)
                        1 -> AuditScorecardTab(scorecard)
                        2 -> BudgetForecastTab(
                            budget = budgetForecast,
                            targetGrowth = targetGrowthPercent,
                            onGrowthChange = { targetGrowthPercent = it },
                            speculativeBudget = speculativeBudgetAr,
                            onSpeculativeChange = { speculativeBudgetAr = it }
                        )
                        3 -> ClosingAndExportTab(
                            pnl = pnl,
                            scorecard = scorecard,
                            viewModel = viewModel,
                            isLocked = isClosingOfficiallyLocked,
                            onLockToggle = { isLocked -> isClosingOfficiallyLocked = isLocked }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PnlAndMarginsTab(pnl: com.example.data.closing.AnnualProfitLossStatement) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Cartes KPI Clés
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Chiffre d'Affaires", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            "${String.format(Locale.FRENCH, "%,.0f", pnl.grossTurnoverAr)} Ar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("${String.format(Locale.FRENCH, "%,.0f", pnl.volumeTotalSoldKg)} kg vendus", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Marge Brute", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            "${String.format(Locale.FRENCH, "%,.0f", pnl.grossMarginAr)} Ar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text("Taux brut : ${pnl.grossMarginRatePercent}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Résultat Net", fontSize = 10.sp, color = Color(0xFF15803D))
                        Text(
                            "${String.format(Locale.FRENCH, "%,.0f", pnl.netOperatingProfitAr)} Ar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        Text("Net / CA : ${pnl.netProfitMarginPercent}%", fontSize = 9.sp, color = Color(0xFF166534))
                    }
                }
            }
        }

        // Seuil de rentabilité (Point Mort)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Flag, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Seuil de Rentabilité (Point Mort Annuel)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AgroGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pour absorber les charges fixes annuelles (60 000 000 Ar), Sambatra Négoce doit réaliser au minimum :\n" +
                                "• CA Seuil : ${String.format(Locale.FRENCH, "%,.0f", pnl.breakEvenTurnoverAr)} Ar\n" +
                                "• Tonnage Seuil : ${pnl.breakEvenVolumeTons} Tonnes de denrées\n" +
                                "• Date du point mort : atteint en ${pnl.breakEvenDays} jours d'exploitation.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Décomposition par produit
        item {
            Text(
                text = "Rentabilité Analytique des 8 Denrées (Guide Chap. 6 & 14)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(pnl.productBreakdowns) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(prod.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "${prod.category} • ${String.format(Locale.FRENCH, "%,.0f", prod.volumeSoldKg)} kg vendus",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Freinte réelle déduite : ${String.format(Locale.FRENCH, "%.0f", prod.shrinkageKg)} kg (${String.format(Locale.FRENCH, "%,.0f", prod.shrinkageLossAr)} Ar)",
                            fontSize = 9.sp,
                            color = SpiceOrangeSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                        Text(
                            "${String.format(Locale.FRENCH, "%,.0f", prod.grossMarginAr)} Ar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AgroGreenPrimary
                        )
                        Text("Taux : ${String.format(Locale.FRENCH, "%.1f", prod.grossMarginRatePercent)}%", fontSize = 10.sp, color = AgroGreenPrimary)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = AgroGreenPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${prod.shareOfTotalMarginPercent}% de la marge",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AgroGreenPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditScorecardTab(scorecard: com.example.data.closing.OperationalAuditScorecard) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Score Global
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (scorecard.certificationGrade) {
                        AuditCertificationGrade.EXCELLENT_CONFORME -> Color(0xFFF0FDF4)
                        AuditCertificationGrade.CONFORME_AVEC_RESERVES -> Color(0xFFFEFCE8)
                        AuditCertificationGrade.NON_CONFORME_RISQUE_ELEVE -> Color(0xFFFEF2F2)
                    }
                ),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SCORE D'AUDIT OPÉRATIONNEL 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${scorecard.overallScore} / 100",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (scorecard.certificationGrade) {
                            AuditCertificationGrade.EXCELLENT_CONFORME -> AgroGreenPrimary
                            AuditCertificationGrade.CONFORME_AVEC_RESERVES -> Color(0xFFB45309)
                            AuditCertificationGrade.NON_CONFORME_RISQUE_ELEVE -> StatusRedDanger
                        }
                    )
                    Text(
                        text = "Mention : ${scorecard.certificationGrade.name.replace("_", " ")}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scorecard.generalAuditorAppreciation,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Piliers
        item {
            Text("Les 4 Piliers Fondamentaux du Guide 2026", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        items(scorecard.pillars) { pillar ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pillar.name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(
                            "${pillar.score} / ${pillar.maxScore} pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AgroGreenPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pillar.score.toFloat() / pillar.maxScore },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = AgroGreenPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(pillar.details, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Règles d'Or vérifiées
        item {
            Text("Vérification des Règles d'Or Opérationnelles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        items(scorecard.verifiedRules) { rule ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (rule.isCompliant) Color(0xFFF8FAFC) else Color(0xFFFEF2F2)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        if (rule.isCompliant) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (rule.isCompliant) AgroGreenPrimary else StatusRedDanger,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${rule.ruleTitle} (${rule.referenceChapter})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Cible : ${rule.expectedTarget}  |  Réalisé : ${rule.achievedValue}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = rule.comment, fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetForecastTab(
    budget: com.example.data.closing.BudgetForecastScenario,
    targetGrowth: Double,
    onGrowthChange: (Double) -> Unit,
    speculativeBudget: Double,
    onSpeculativeChange: (Double) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Simulateur Stratégique & Budget Prévisionnel ${budget.targetYear}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = "Permet de projeter la grande collecte de paddy Alaotra et d'allouer les fonds propres en respectant la règle des 10% max sur spéculatif (Chap. 15 Cas 10).",
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Curseur de croissance
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Objectif de Croissance CA : +${targetGrowth.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text(
                            "CA Cible : ${String.format(Locale.FRENCH, "%,.0f", budget.projectedTurnoverAr)} Ar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AgroGreenPrimary
                        )
                    }
                    Slider(
                        value = targetGrowth.toFloat(),
                        onValueChange = { onGrowthChange(it.toDouble()) },
                        valueRange = 5f..35f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = AgroGreenPrimary, activeTrackColor = AgroGreenPrimary)
                    )
                }
            }
        }

        // Curseur engagement spéculatif épices
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (budget.isSpeculativeExceeded) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
                ),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Engagement Spéculatif Épices : ${String.format(Locale.FRENCH, "%,.0f", speculativeBudget)} Ar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (budget.isSpeculativeExceeded) StatusRedDanger else Color.Unspecified
                        )
                        Text(
                            "Plafond 10% : ${String.format(Locale.FRENCH, "%,.0f", budget.maxSpeculativeAuthorizedAr)} Ar",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Slider(
                        value = speculativeBudget.toFloat(),
                        onValueChange = { onSpeculativeChange(it.toDouble()) },
                        valueRange = 2_000_000f..20_000_000f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = if (budget.isSpeculativeExceeded) StatusRedDanger else AgroGreenPrimary,
                            activeTrackColor = if (budget.isSpeculativeExceeded) StatusRedDanger else AgroGreenPrimary
                        )
                    )

                    if (budget.isSpeculativeExceeded) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = StatusRedDanger, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ALERTE RISQUE CAS 10 : Ne jamais dépasser 10% des fonds propres sans contrat d'achat ferme signé !",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusRedDanger
                            )
                        }
                    }
                }
            }
        }

        // Résultats de la simulation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Synthèse Prévisionnelle 2027", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Marge Brute Prévisionnelle : ${String.format(Locale.FRENCH, "%,.0f", budget.expectedGrossMarginAr)} Ar", fontSize = 11.sp)
                    Text("• Charges Fixes Projetées : ${String.format(Locale.FRENCH, "%,.0f", budget.fixedExpensesProjectedAr)} Ar", fontSize = 11.sp)
                    Text(
                        "• Résultat Net Estimé : ${String.format(Locale.FRENCH, "%,.0f", budget.projectedNetIncomeAr)} Ar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text("• Besoin de Ligne de Crédit de Campagne : ${String.format(Locale.FRENCH, "%,.0f", budget.recommendedCreditLineNeedAr)} Ar", fontSize = 11.sp, color = SpiceOrangeSecondary)
                }
            }
        }
    }
}

@Composable
private fun ClosingAndExportTab(
    pnl: com.example.data.closing.AnnualProfitLossStatement,
    scorecard: com.example.data.closing.OperationalAuditScorecard,
    viewModel: MainViewModel,
    isLocked: Boolean,
    onLockToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var exportSummaryText by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Clôture Officielle d'Exercice & Sauvegarde", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "La clôture certifie l'inventaire physique des 8 denrées, verrouille les marges de l'exercice 2026 et génère le rapport annuel d'audit conforme aux exigences bancaires et fiscales malgaches.",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Action 1 : Générer le PDF Officiel
        item {
            Button(
                onClick = {
                    val pdfFile = AnnualAuditPdfReportGenerator.generateAnnualClosingPdf(
                        context = context,
                        pnl = pnl,
                        scorecard = scorecard
                    )
                    if (pdfFile != null) {
                        AnnualAuditPdfReportGenerator.openOrSharePdf(context, pdfFile)
                        Toast.makeText(context, "Bilan Annuel PDF généré avec succès !", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Erreur lors de la génération PDF", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Générer & Partager le Bilan Annuel PDF (Certifié)", fontWeight = FontWeight.Bold)
            }
        }

        // Action 2 : Sauvegarde Complète & Grand Livre
        item {
            OutlinedButton(
                onClick = {
                    val summary = AnnualClosingEngine.generateBackupArchiveSummary(
                        products = products,
                        lots = lots,
                        clients = clients,
                        suppliers = suppliers
                    )
                    exportSummaryText = "Archive générée :\n• ${summary.totalProductsExported} Produits valorisés\n• ${summary.totalLotsExported} Lots tracés\n• ${summary.totalClientsExported} Fiches clients\n• Valeur totale stock : ${String.format(Locale.FRENCH, "%,.0f", summary.totalInventoryValueAr)} Ar\n• Checksum : ${summary.backupChecksum}"
                    showExportSuccessDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sauvegarder la Base SQLite & Grand Livre (JSON/CSV)")
            }
        }

        // Action 3 : Verrouillage Officiel de Clôture
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocked) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                ),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isLocked) "EXERCICE 2026 CLÔTURÉ & VERROUILLÉ" else "STATUT D'EXERCICE : OUVERT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isLocked) AgroGreenPrimary else Color(0xFFB45309)
                            )
                            Text(
                                text = if (isLocked) "Toutes les écritures de freinte et stocks sont certifiées conformes." else "Prêt pour la clôture officielle de fin d'année.",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Switch(
                            checked = isLocked,
                            onCheckedChange = { checked ->
                                onLockToggle(checked)
                                Toast.makeText(
                                    context,
                                    if (checked) "Exercice 2026 clôturé avec succès !" else "Exercice réouvert pour ajustement.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showExportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgroGreenPrimary) },
            title = { Text("Export & Sauvegarde Terminés") },
            text = { Text(exportSummaryText, fontSize = 12.sp, lineHeight = 16.sp) },
            confirmButton = {
                Button(
                    onClick = { showExportSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
                ) {
                    Text("OK")
                }
            }
        )
    }
}
