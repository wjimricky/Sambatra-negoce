package com.example.ui.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.finance.WorkingCapitalEngine
import com.example.data.finance.WorkingCapitalStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingCapitalDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val currentTreasury by viewModel.currentTreasury.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Diagnostic BFR, 1: Simulateur Stress Achats

    // Paramètres simulateur stress achat
    var selectedCrop by remember { mutableStateOf("Riz Paddy (Makalioka)") }
    var purchaseTonsText by remember { mutableStateOf("15.0") }
    var unitPriceText by remember { mutableStateOf("1350") }
    var upfrontPercentSlider by remember { mutableStateOf(50f) }

    val diagnosis = remember(products, clients, suppliers, currentTreasury) {
        WorkingCapitalEngine.calculateWorkingCapital(
            products = products,
            clients = clients,
            suppliers = suppliers,
            currentTreasuryAr = currentTreasury
        )
    }

    val stressResult = remember(selectedCrop, purchaseTonsText, unitPriceText, upfrontPercentSlider, currentTreasury) {
        val tons = purchaseTonsText.toDoubleOrNull() ?: 15.0
        val price = unitPriceText.toDoubleOrNull() ?: 1350.0
        WorkingCapitalEngine.simulatePurchaseCashStress(
            scenarioName = "Achat Récolte $selectedCrop",
            commodityName = selectedCrop,
            purchaseVolumeTons = tons,
            unitPriceAr = price,
            upfrontCashPercentage = upfrontPercentSlider.toDouble(),
            currentTreasuryAr = currentTreasury
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgroGreenPrimary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Contrôle Budgétaire & BFR (Guide 2026)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Règle des 2 mois de charges fixes & Stress-Test Trésorerie",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }

                // Onglets
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = AgroGreenPrimary
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Diagnostic BFR & Cash", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Simulateur Stress Achat", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CrisisAlert, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (activeTab == 0) {
                        // Diagnostic BFR
                        item {
                            val cardBg = when (diagnosis.status) {
                                WorkingCapitalStatus.HEALTHY -> Color(0xFFF0FDF4)
                                WorkingCapitalStatus.VIGILANCE -> Color(0xFFFFFBEB)
                                WorkingCapitalStatus.CRITICAL -> Color(0xFFFEF2F2)
                            }
                            val borderColor = when (diagnosis.status) {
                                WorkingCapitalStatus.HEALTHY -> AgroGreenPrimary
                                WorkingCapitalStatus.VIGILANCE -> SpiceOrangeSecondary
                                WorkingCapitalStatus.CRITICAL -> StatusRedDanger
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderColor)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (diagnosis.status == WorkingCapitalStatus.CRITICAL) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = borderColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = diagnosis.statusMessage,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = borderColor
                                        )
                                    }
                                }
                            }
                        }

                        // Jauge Réserve des 2 Mois de Charges Fixes
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Règle d'or : Réserve de Sécurité Trésorerie (p. 81)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = AgroGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Minimum 2 mois de charges fixes (5M Ar/mois x 2 = 10 000 000 Ar) sanctuarisés en banque pour parer aux aléas logistiques et retards.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Trésorerie Actuelle : ${String.format(Locale.US, "%,.0f", diagnosis.treasuryAvailableAr).replace(',', ' ')} Ar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "${String.format(Locale.US, "%.1f", diagnosis.fixedChargesCoverageMonths)} / 2.0 mois",
                                            fontWeight = FontWeight.Bold,
                                            color = if (diagnosis.fixedChargesCoverageMonths < 2.0) StatusRedDanger else AgroGreenPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { ((diagnosis.fixedChargesCoverageMonths / 2.0).toFloat()).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                                        color = if (diagnosis.fixedChargesCoverageMonths < 2.0) StatusRedDanger else AgroGreenPrimary,
                                        trackColor = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }

                        // Décomposition du BFR (Actif - Passif)
                        item {
                            Text("Décomposition Détaillée du BFR d'Exploitation", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BfrValueCard(
                                    title = "Stocks (CMUP)",
                                    amountAr = diagnosis.stockValueAr,
                                    subtitle = "+ Actif circulant",
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.weight(1f)
                                )
                                BfrValueCard(
                                    title = "Créances Clients",
                                    amountAr = diagnosis.accountsReceivableAr,
                                    subtitle = "+ Encours à percevoir",
                                    color = SpiceOrangeSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                BfrValueCard(
                                    title = "Dettes Fournisseurs",
                                    amountAr = diagnosis.supplierPayablesAr,
                                    subtitle = "- Avances / Crédit",
                                    color = Color(0xFF7C3AED),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // BFR Synthèse
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("BFR Net en Ariary", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Text(
                                            "${String.format(Locale.US, "%,.0f", diagnosis.workingCapitalAr).replace(',', ' ')} Ar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("BFR en Jours de CA", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        Text(
                                            "${diagnosis.workingCapitalDaysOfSales.toInt()} Jours",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (diagnosis.workingCapitalDaysOfSales > 50) StatusRedDanger else AgroGreenPrimary
                                        )
                                        Text("Cible: < 45-50 jours", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // Recommandations
                        item {
                            Text("Recommandations Stratégiques (Guide 2026 p. 81-83)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                diagnosis.keyRecommendations.forEach { rec ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowRight, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(rec, fontSize = 11.5.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Onglet 2 : Simulateur de Stress Achat
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Paramètres de l'Opportunité de Campagne", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgroGreenPrimary)

                                    // Choix culture
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = selectedCrop.contains("Riz"),
                                            onClick = { selectedCrop = "Riz Paddy (Makalioka)" },
                                            label = { Text("Riz Paddy", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = selectedCrop.contains("Maïs"),
                                            onClick = { selectedCrop = "Maïs grain sec" },
                                            label = { Text("Maïs", fontSize = 11.sp) }
                                        )
                                        FilterChip(
                                            selected = selectedCrop.contains("Girofle"),
                                            onClick = { selectedCrop = "Girofle Clous CG3" },
                                            label = { Text("Girofle", fontSize = 11.sp) }
                                        )
                                    }

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = purchaseTonsText,
                                            onValueChange = { purchaseTonsText = it },
                                            label = { Text("Volume (Tonnes)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = unitPriceText,
                                            onValueChange = { unitPriceText = it },
                                            label = { Text("Prix Achat (Ar/kg)") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Part payée comptant à l'enlèvement :", fontSize = 11.sp)
                                            Text("${upfrontPercentSlider.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                        }
                                        Slider(
                                            value = upfrontPercentSlider,
                                            onValueChange = { upfrontPercentSlider = it },
                                            valueRange = 10f..100f,
                                            steps = 8
                                        )
                                    }
                                }
                            }
                        }

                        // Résultat du Stress-Test
                        item {
                            val alertBg = if (stressResult.isIlliquidityRisk) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                            val alertColor = if (stressResult.isIlliquidityRisk) StatusRedDanger else AgroGreenPrimary

                            Card(
                                colors = CardDefaults.cardColors(containerColor = alertBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(alertColor)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (stressResult.isIlliquidityRisk) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = alertColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (stressResult.isIlliquidityRisk) "ALERTE LIQUIDITÉ CRITIQUE" else "OPÉRATION FINANÇABLE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = alertColor
                                        )
                                    }

                                    Text(
                                        text = stressResult.alertMessage,
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = if (stressResult.isIlliquidityRisk) Color(0xFF7F1D1D) else Color(0xFF14532D)
                                    )

                                    HorizontalDivider(color = alertColor.copy(alpha = 0.3f))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Coût total d'achat :", fontSize = 11.sp)
                                        Text("${String.format(Locale.US, "%,.0f", stressResult.purchaseCostAr).replace(',', ' ')} Ar", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Décaissement cash immédiat :", fontSize = 11.sp)
                                        Text("${String.format(Locale.US, "%,.0f", stressResult.cashOutflowImmediateAr).replace(',', ' ')} Ar", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = StatusRedDanger)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Trésorerie restante :", fontSize = 11.sp)
                                        Text("${String.format(Locale.US, "%,.0f", stressResult.resultingTreasuryAr).replace(',', ' ')} Ar (${String.format(Locale.US, "%.1f", stressResult.resultingCoverageMonths)} mois)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = alertColor)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Stratégie recommandée : ${stressResult.mitigationStrategy}",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = alertColor
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

@Composable
private fun BfrValueCard(
    title: String,
    amountAr: Double,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.5f))),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "${(amountAr / 1_000_000.0).toString().take(4)}M Ar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(subtitle, fontSize = 8.5.sp, color = Color.Gray)
        }
    }
}
