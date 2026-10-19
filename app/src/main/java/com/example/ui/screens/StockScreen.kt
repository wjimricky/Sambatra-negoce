package com.example.ui.screens

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
import com.example.data.model.*
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StockScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()
    val showReceptionDialog by viewModel.showReceptionDialog.collectAsState()

    var activeView by remember { mutableStateOf(0) } // 0: Produits (ABC), 1: Lots & Traçabilité, 2: Zonage Entrepôt
    var selectedProductForProfitability by remember { mutableStateOf<Product?>(null) }
    var selectedLotForAiStorage by remember { mutableStateOf<BatchLot?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setShowReceptionDialog(true) },
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text(if (lang == "FR") "Réception Qualité (5 étapes)" else "Fandraisana entana (Dingana 5)") },
                containerColor = SpiceOrangeSecondary,
                contentColor = Color.White
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp)
        ) {
            // Sélecteur de vue
            ScrollableTabRow(
                selectedTabIndex = activeView,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = AgroGreenPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Tab(
                    selected = activeView == 0,
                    onClick = { activeView = 0 },
                    text = { Text(if (lang == "FR") "Catalogue ABC" else "Katalaogy ABC", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeView == 1,
                    onClick = { activeView = 1 },
                    text = { Text(if (lang == "FR") "Lots & Traçabilité" else "Loko & Fiaviana", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeView == 2,
                    onClick = { activeView = 2 },
                    text = { Text(if (lang == "FR") "Zonage (400 m²)" else "Drafitra trano", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeView == 3,
                    onClick = { activeView = 3 },
                    text = { Text(if (lang == "FR") "Évolution & Rotation" else "Fihetsehan'ny Tahiry", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            when (activeView) {
                0 -> ProductCatalogView(
                    products = products,
                    lang = lang,
                    onCalculateProfitability = { selectedProductForProfitability = it }
                )
                1 -> BatchLotsView(lots, lang, onRecommendSlot = { selectedLotForAiStorage = it })
                2 -> WarehouseZoningView(viewModel, lang)
                3 -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        com.example.ui.components.RechartsStockVisualizer(lang = lang)
                    }
                }
            }
        }
    }

    if (selectedProductForProfitability != null) {
        com.example.ui.components.LotProfitabilityCalculatorDialog(
            initialProduct = selectedProductForProfitability!!,
            onDismiss = { selectedProductForProfitability = null }
        )
    }

    if (selectedLotForAiStorage != null) {
        com.example.ui.components.SmartStorageRecommendationDialog(
            initialLot = selectedLotForAiStorage,
            onDismiss = { selectedLotForAiStorage = null }
        )
    }

    if (showReceptionDialog) {
        QualityReceptionDialog(viewModel)
    }
}

@Composable
fun ProductCatalogView(
    products: List<Product>,
    lang: String,
    onCalculateProfitability: (Product) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Rule, contentDescription = null, tint = AgroGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Méthode ABC (Guide p. 31) : Classe A (70-80% du CA, suivi serré) • Classe B (15-20%) • Classe C (5-10%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(products) { prod ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (prod.abcClass == AbcClass.A) AgroGreenPrimary else if (prod.abcClass == AbcClass.B) SpiceOrangeSecondary else Color.Gray,
                                shape = CircleShape,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = prod.abcClass.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (lang == "FR") prod.name else prod.nameMg,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${prod.type.labelFr} • Format: ${prod.packagingFormat}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Badge Couverture
                        Surface(
                            color = if (prod.coverageDays > 45) Color(0xFFFEE2E2) else Color(0xFFE8F5EE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${prod.coverageDays} j de stock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prod.coverageDays > 45) StatusRedDanger else StatusGreenOk,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Stock actuel :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${prod.currentStockKg.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text("CMUP (Revient) :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${prod.cmupCostAr.toInt()} Ar/kg", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column {
                            Text("Prix de gros :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${prod.wholesalePriceAr.toInt()} Ar/kg", fontWeight = FontWeight.Bold, color = AgroGreenPrimary, fontSize = 13.sp)
                        }
                        Column {
                            Text("Marge brute :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${prod.marginPercent.toInt()}%", fontWeight = FontWeight.Bold, color = SpiceOrangeSecondary, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Vigilance guide : ${prod.specificRisks} • Humidité max : ${prod.maxMoisturePercent}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onCalculateProfitability(prod) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lang == "FR") "Calculer rentabilité du lot (Marge, transport, stockage)" else "Kajio ny tombom-barotra (Fitaterana, fitahirizana)",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatchLotsView(
    lots: List<BatchLot>,
    lang: String,
    onRecommendSlot: (BatchLot) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Code Lot Normalisé (p. 28) : [Produit]-[Origine]-[AAMMJJ]-[N°]. Exemple : GIR-ANA-260915-03 avec contre-échantillon scellé obligatoire.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(lots) { lot ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = lot.lotNumber,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )

                        Surface(
                            color = when (lot.decision) {
                                QualityDecision.CONFORME -> Color(0xFFDCFCE7)
                                QualityDecision.DECOTE -> Color(0xFFFEF3C7)
                                QualityDecision.QUARANTAINE -> Color(0xFFFFEDD5)
                                QualityDecision.REFUSE -> Color(0xFFFEE2E2)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = lot.decision.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (lot.decision) {
                                    QualityDecision.CONFORME -> StatusGreenOk
                                    QualityDecision.DECOTE -> StatusAmberWarning
                                    QualityDecision.QUARANTAINE -> Color(0xFFC2410C)
                                    QualityDecision.REFUSE -> StatusRedDanger
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${lot.productName} • Origine: ${lot.originRegion}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Fournisseur: ${lot.supplierName} • Reçu le: ${lot.receptionDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Quantité nette :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lot.netReceivedKg.toInt()} kg (${lot.bagsCount} sacs)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Humidité mesurée :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lot.moisturePercent}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Impuretés :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${lot.impuritiesPercent}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Scellé: ${lot.sealedCounterSampleId}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SpiceOrangeSecondary
                        )
                        Text(
                            text = "Zone: ${lot.warehouseZone}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (lot.observations.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Observations: ${lot.observations}",
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { onRecommendSlot(lot) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recommander Emplacement IA (Zonage)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun WarehouseZoningView(viewModel: MainViewModel, lang: String) {
    val audit by viewModel.zoningAudit.collectAsState()
    val lots by viewModel.lots.collectAsState()

    var showAnomaliesOnly by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // En-tête KPI de Zonage & Audit Algorithmique
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Optimisation du Zonage Entrepôt (400 m²)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Algorithme FIFO / FEFO selon les règles de conservation du Guide 2026",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = if (audit.anomalies.isEmpty()) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${audit.complianceScorePercent.toInt()}% Conforme",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (audit.anomalies.isEmpty()) AgroGreenPrimary else Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Occupation Entrepôt", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", audit.occupancyPercent)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text("Stock Total Géré", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", audit.totalStockKg)} kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text("Lots en Stock", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${audit.totalLotsAnalyzed} lots",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text("Anomalies Détectées", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${audit.anomalies.size}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (audit.anomalies.isEmpty()) AgroGreenPrimary else StatusRedDanger
                            )
                        }
                    }
                }
            }
        }

        // Section Anomalies de Stockage & Actions de Déplacement Recommandées
        if (audit.anomalies.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StatusRedDanger)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRedDanger, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alerte Zonage : ${audit.anomalies.size} anomalie(s) à corriger",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = StatusRedDanger
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        audit.anomalies.forEach { anomaly ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Lot ${anomaly.lotNumber} (${anomaly.productName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            color = Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = anomaly.severity,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusRedDanger,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = anomaly.message,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = anomaly.guideReference,
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = AgroGreenPrimary
                                    )

                                    // Action de déplacement direct
                                    val move = audit.recommendedMoves.find { it.lotNumber == anomaly.lotNumber }
                                    if (move != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                viewModel.updateLotWarehouseZone(move.lotNumber, move.toZone)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Déplacer vers ${move.toZone}", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Carte des 4 Zones de l'Entrepôt
        item {
            Text(
                text = "Plan et État des 4 Zones Physiques",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AgroGreenPrimary
            )
        }

        items(audit.zones) { zone ->
            val zoneLots = lots.filter { lot ->
                val zName = zone.id.name
                when (zone.id) {
                    com.example.data.model.WarehouseZoneId.ZONE_A -> lot.warehouseZone.contains("Allée A") || lot.warehouseZone.contains("Grains")
                    com.example.data.model.WarehouseZoneId.ZONE_B -> lot.warehouseZone.contains("Épice") || lot.warehouseZone.contains("Epice")
                    com.example.data.model.WarehouseZoneId.ZONE_C -> lot.warehouseZone.contains("Allée B") || lot.warehouseZone.contains("Allée C") || lot.warehouseZone.contains("Allée D") || lot.warehouseZone.contains("Légumineuse")
                    com.example.data.model.WarehouseZoneId.ZONE_Q -> lot.warehouseZone.contains("Quarantaine") || lot.decision == com.example.data.model.QualityDecision.QUARANTAINE
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = zone.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Règle de rotation : ${zone.primaryRotationRule}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SpiceOrangeSecondary
                            )
                        }

                        // Capteurs
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                                    Text("${zone.currentTemperatureC}°C", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            Surface(
                                color = if (zone.currentHumidityPercent <= zone.targetHumidityMaxPercent) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(12.dp), tint = if (zone.currentHumidityPercent <= zone.targetHumidityMaxPercent) AgroGreenPrimary else StatusRedDanger)
                                    Text("${zone.currentHumidityPercent}% (Max ${zone.targetHumidityMaxPercent}%)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (zone.currentHumidityPercent <= zone.targetHumidityMaxPercent) AgroGreenPrimary else StatusRedDanger)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = zone.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Barre de capacité
                    val capPct = if (zone.maxBagsCapacity > 0) (zone.currentBagsCount.toFloat() / zone.maxBagsCapacity.toFloat()).coerceIn(0f, 1f) else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Capacité : ${zone.currentBagsCount} / ${zone.maxBagsCapacity} sacs", fontSize = 10.sp, color = Color.Gray)
                        Text("${(capPct * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { capPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (capPct > 0.85f) StatusRedDanger else AgroGreenPrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    // Lots stockés dans la zone
                    if (zoneLots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Lots présents dans cette zone :", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            zoneLots.take(4).forEach { l ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${l.lotNumber} (${l.netReceivedKg.toInt()}kg)",
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Ordre de Déstockage Prioritaire FIFO / FEFO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "File de Déstockage Optimisée (FIFO / FEFO)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Ordre impératif de sortie des lots pour préparer les commandes",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = AgroGreenPrimary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    audit.destockingQueue.forEach { item ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Badge Rang
                                Surface(
                                    color = when (item.priorityLevel) {
                                        "URGENT" -> StatusRedDanger
                                        "ÉLEVÉ" -> SpiceOrangeSecondary
                                        else -> AgroGreenPrimary
                                    },
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${item.rank}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.lotNumber} • ${item.productName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        )
                                        Surface(
                                            color = when (item.priorityLevel) {
                                                "URGENT" -> Color(0xFFFEE2E2)
                                                "ÉLEVÉ" -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFDCFCE7)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.priorityLevel,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (item.priorityLevel) {
                                                    "URGENT" -> StatusRedDanger
                                                    "ÉLEVÉ" -> Color(0xFF92400E)
                                                    else -> AgroGreenPrimary
                                                },
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${item.quantityKg.toInt()} kg • Zone: ${item.zone} • ${item.storageDays}j en stock (Max ${item.maxRecommendedStorageDays}j)",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Règle : ${item.ruleApplied} • ${item.reason}",
                                        fontSize = 10.sp,
                                        color = if (item.priorityLevel == "URGENT") StatusRedDanger else AgroGreenPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Règles d'Or du Guide 2026
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Les 10 Règles d'Or du Stockage (Guide p. 27)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Palettes de 10 à 15 cm au-dessus du sol (jamais au sol direct).\n" +
                                "2. Espace d'au moins 50 cm entre les piles et les murs.\n" +
                                "3. Allées de circulation dégagées pour les inspections.\n" +
                                "4. Hauteur de pile limitée pour éviter l'écrasement des sacs du bas.\n" +
                                "5. Un lot = un emplacement identifié avec étiquette complète.\n" +
                                "6. FIFO (premier entré, premier sorti) ou FEFO (date limite / humidité).\n" +
                                "7. Zéro produit chimique/carburant dans les zones de denrées.\n" +
                                "8. Épices fortes en contenants hermétiques séparés.\n" +
                                "9. Portes fermées, déchets évacués chaque jour.\n" +
                                "10. Inspection visuelle et relevé hygromètre chaque semaine.",
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF166534)
                    )
                }
            }
        }
    }
}

@Composable
fun QualityReceptionDialog(viewModel: MainViewModel) {
    com.example.ui.components.QualityReceptionWizardDialog(
        viewModel = viewModel,
        onDismiss = { viewModel.setShowReceptionDialog(false) }
    )
}

