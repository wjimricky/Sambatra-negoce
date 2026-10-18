package com.example.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DemoDataProvider
import com.example.data.model.CaseStudy
import com.example.data.model.ChecklistItem
import com.example.ui.components.SectionTitle
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ToolsScreen(viewModel: MainViewModel) {
    val purchaseChecklist by viewModel.purchaseChecklist.collectAsState()
    val receptionChecklist by viewModel.receptionChecklist.collectAsState()
    val shippingChecklist by viewModel.shippingChecklist.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Checklists, 1: 10 Études de Cas, 2: Fournisseurs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeSubTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            contentColor = AgroGreenPrimary,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Checklists (p. 66)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("10 Cas Réels (p. 88)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Fournisseurs (p. 17)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 3,
                onClick = { activeSubTab = 3 },
                text = { Text("Rapports PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 4,
                onClick = { activeSubTab = 4 },
                text = { Text("Logistique (p. 48)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 5,
                onClick = { activeSubTab = 5 },
                text = { Text("Météo Grains (p. 33)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 6,
                onClick = { activeSubTab = 6 },
                text = { Text("Audit & BFR (p. 81)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        when (activeSubTab) {
            0 -> ChecklistsView(viewModel, purchaseChecklist, receptionChecklist, shippingChecklist)
            1 -> CaseStudiesView()
            2 -> SuppliersView(suppliers)
            3 -> MonthlyReportEmbeddedView(viewModel)
            4 -> LogisticsEmbeddedToolView()
            5 -> WeatherEmbeddedToolView()
            6 -> WorkingCapitalAndAuditEmbeddedView(viewModel)
        }
    }
}

@Composable
fun ChecklistsView(
    viewModel: MainViewModel,
    purchaseList: List<ChecklistItem>,
    receptionList: List<ChecklistItem>,
    shippingList: List<ChecklistItem>
) {
    var selectedCategory by remember { mutableStateOf("Achat") }

    val activeList = when (selectedCategory) {
        "Achat" -> purchaseList
        "Réception" -> receptionList
        else -> shippingList
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == "Achat",
                    onClick = { selectedCategory = "Achat" },
                    label = { Text("12.2 Achat (9 items)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategory == "Réception",
                    onClick = { selectedCategory = "Réception" },
                    label = { Text("12.3 Réception (9 items)", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedCategory == "Expédition",
                    onClick = { selectedCategory = "Expédition" },
                    label = { Text("12.4 Expédition (6 items)", fontSize = 11.sp) }
                )
            }
        }

        item {
            val checkedCount = activeList.count { it.isChecked }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progression des contrôles : $checkedCount / ${activeList.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AgroGreenPrimary
                    )
                    LinearProgressIndicator(
                        progress = { (checkedCount.toFloat() / activeList.size) },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp),
                        color = AgroGreenPrimary
                    )
                }
            }
        }

        items(activeList) { item ->
            val typeKey = when (selectedCategory) {
                "Achat" -> "purchase"
                "Réception" -> "reception"
                else -> "shipping"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (item.isChecked) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleChecklist(typeKey, item.id) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { viewModel.toggleChecklist(typeKey, item.id) },
                        colors = CheckboxDefaults.colors(checkedColor = AgroGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.text,
                        fontSize = 12.sp,
                        fontWeight = if (item.isChecked) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (item.isChecked) Color(0xFF166534) else TextPrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun CaseStudiesView() {
    val cases = DemoDataProvider.caseStudies
    var expandedCaseNumber by remember { mutableStateOf<Int?>(1) }

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
                    Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Les 10 Cas Réels de Sambatra Négoce (Chapitre 15)\nChaque cas enseigne une règle financière et opérationnelle décisive.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(cases) { cs ->
            val isExpanded = expandedCaseNumber == cs.caseNumber
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedCaseNumber = if (isExpanded) null else cs.caseNumber }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cas ${cs.caseNumber} : ${cs.product}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiceOrangeSecondary
                            )
                            Text(
                                text = cs.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AgroGreenPrimary
                            )
                        }
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = AgroGreenPrimary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Text(text = "Problème : ${cs.problem}", fontSize = 12.sp, color = TextPrimaryLight)
                            Text(text = "Chiffres réels : ${cs.numbers}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                            Text(text = "Erreur classique : ${cs.wrongAction}", fontSize = 11.sp, color = StatusRedDanger)
                            Text(text = "Bonne pratique : ${cs.whatShouldBeDone}", fontSize = 11.sp, color = StatusGreenOk)

                            Surface(
                                color = Color(0xFFE8F5EE),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cs.keyLesson,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgroGreenPrimary
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
fun SuppliersView(suppliers: List<com.example.data.model.Supplier>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = StatusAmberWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Règle absolue (p. 16) : « Visez au moins deux fournisseurs qualifiés par produit principal. Un fournisseur unique vous rend vulnérable à ses retards, hausses et litiges. »",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )
                }
            }
        }

        items(suppliers) { sup ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sup.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (sup.isPrimarySource) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = AgroGreenPrimary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Source Principale", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                text = "Région: ${sup.region} • Tél: ${sup.phone}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Note: ${sup.reliabilityScore5}/5",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Filières : ${sup.mainProducts}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Taux conformité : ${sup.complianceRatePercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (sup.complianceRatePercent >= 90) StatusGreenOk else StatusAmberWarning
                        )
                        Text(
                            text = "Avances en cours : ${(sup.currentAdvancesAr / 1_000_000.0).toString().take(3)}M Ar",
                            fontSize = 11.sp,
                            color = if (sup.currentAdvancesAr > 0) Color.DarkGray else StatusGreenOk
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyReportEmbeddedView(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedMonth by remember { mutableStateOf("Octobre 2026") }
    val months = listOf("Octobre 2026", "Septembre 2026", "Août 2026")

    val reportData = remember(selectedMonth) {
        viewModel.getMonthlyReportData(selectedMonth)
    }

    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
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
                        Column {
                            Text(
                                text = "Génération de Rapports Mensuels PDF",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Indicateurs de performance (KPI), analyse des marges & suivi des pertes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = AgroGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        months.forEach { m ->
                            FilterChip(
                                selected = selectedMonth == m,
                                onClick = { selectedMonth = m },
                                label = { Text(m, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isGenerating = true
                                try {
                                    val file = viewModel.generateMonthlyPdfReport(context, selectedMonth)
                                    android.widget.Toast.makeText(context, "Fichier PDF généré : ${file.name}", android.widget.Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Erreur : ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                } finally {
                                    isGenerating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isGenerating) "Création..." else "Générer PDF", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.shareMonthlyPdfReport(context, selectedMonth) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Partager", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.openMonthlyPdfReport(context, selectedMonth) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ouvrir", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Synthèse KPI
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Synthèse Exécutive • ${reportData.period}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Chiffre d'Affaires", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", reportData.totalTurnoverAr)} Ar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column {
                            Text("Marge Brute", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", reportData.totalGrossMarginAr)} Ar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AgroGreenPrimary
                            )
                        }
                        Column {
                            Text("Taux de Marge", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%.1f", reportData.overallMarginPercent)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AgroGreenPrimary
                            )
                        }
                        Column {
                            Text("Freinte Globale", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                "${String.format(java.util.Locale.US, "%.2f", reportData.averageLossRatePercent)}%",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AgroGreenPrimary
                            )
                        }
                    }
                }
            }
        }

        // Tableau des marges par produit
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Analyse Détaillée des Marges par Lot & Produit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    reportData.marginBreakdown.forEachIndexed { i, row ->
                        if (i > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Lot ${row.lotNumber} • ${String.format(java.util.Locale.US, "%,.0f", row.quantitySoldKg)} kg vendus", fontSize = 10.5.sp, color = Color.Gray)
                                Text("Coût complet : ${row.fullCostPriceAr.toInt()} Ar → Vente : ${row.salePriceAr.toInt()} Ar/kg", fontSize = 10.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "+${String.format(java.util.Locale.US, "%,.0f", row.totalMarginAr)} Ar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AgroGreenPrimary
                                )
                                Text(
                                    "${String.format(java.util.Locale.US, "%.1f", row.marginPercent)}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (row.marginPercent >= 14.0) AgroGreenPrimary else SpiceOrangeSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tableau Suivi des Pertes & Freintes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Suivi des Pertes & Freintes de Stockage (Guide p. 28-32)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF92400E)
                    )
                    Text(
                        text = "Perte totale : ${String.format(java.util.Locale.US, "%.1f", reportData.totalLossKg)} kg (${String.format(java.util.Locale.US, "%,.0f", reportData.totalLossValueAr)} Ar) • Taux global : ${String.format(java.util.Locale.US, "%.2f", reportData.averageLossRatePercent)}% (< 1.50% seuil)",
                        fontSize = 11.sp,
                        color = Color(0xFF78350F)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    reportData.lossTracking.forEachIndexed { i, loss ->
                        if (i > 0) HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFFDE68A))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(loss.category, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, color = Color(0xFF78350F))
                                Text(loss.explanation, fontSize = 10.sp, color = Color(0xFF92400E))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${String.format(java.util.Locale.US, "%.1f", loss.quantityLostKg)} kg", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                Text("Taux: ${String.format(java.util.Locale.US, "%.2f", loss.realLossRatePercent)}% (Max ${loss.maxAllowedRatePercent}%)", fontSize = 10.sp, color = if (loss.isCompliant) AgroGreenPrimary else StatusRedDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogisticsEmbeddedToolView() {
    var showFullOptimizerDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Planification Logistique & Flotte (p. 48)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF9A3412))
                                Text("Réduction des freintes & incompatibilités sanitaires", fontSize = 11.sp, color = Color(0xFFC2410C))
                            }
                        }

                        Button(
                            onClick = { showFullOptimizerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SpiceOrangeSecondary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simulateur", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Selon le Guide 2026 : Le transport est le 2ème poste de perte non maîtrisée après le stockage. Chaque trajet doit être planifié selon la sensibilité hygrométrique du lot et les caractéristiques de l'axe routier.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF7C2D12),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Flotte de Véhicules
        item {
            Text("Véhicules Disponibles & Capacités Utiles :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        items(com.example.data.logistics.LogisticsOptimizer.VEHICLES) { v ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(v.name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        Text("Charge max: ${v.maxPayloadKg.toInt()} kg • Volume: ${v.maxVolumeM3} m³", fontSize = 11.sp, color = Color(0xFF475569))
                        Text("Chauffeur référent: ${v.driverName} • ${v.fuelCostPerKmAr.toInt()} Ar/km", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (v.hasInsulatedCompartment) {
                        Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(4.dp)) {
                            Text("Isotherme Épices", color = Color(0xFF3730A3), fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Corridors & Freintes
        item {
            Text("Corridors de Livraison & Taux de Freinte Standard :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        items(com.example.data.logistics.LogisticsOptimizer.CORRIDORS) { c ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(c.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SpiceOrangeSecondary)
                        Text("${c.distanceKm.toInt()} km (~${c.typicalDurationHours}h)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text("Risque: ${c.climaticRisk}", fontSize = 10.5.sp, color = Color(0xFF334155))
                    Text("Freinte tolérée Guide 2026 : ${c.baselineRoadShrinkagePct}% • Départ frais recommandé : ${c.recommendedDepartureHour}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = AgroGreenPrimary)
                }
            }
        }
    }

    if (showFullOptimizerDialog) {
        com.example.ui.components.LogisticsPlanningDialog(
            onDismiss = { showFullOptimizerDialog = false }
        )
    }
}

@Composable
fun WeatherEmbeddedToolView() {
    var showFullWeatherDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Météo & Risques Grains/Épices (p. 33)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0369A1))
                                Text("Psychrométrie & Prévention Moisissures", fontSize = 11.sp, color = Color(0xFF0284C7))
                            }
                        }

                        Button(
                            onClick = { showFullWeatherDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Relevés Live", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Règle de conservation Guide 2026 : Le grain et l'épice sont des matériaux hygroscopiques vivants qui échangent continuellement de la vapeur d'eau avec l'air ambiant. Si l'air dépasse 70% HR pour les céréales ou 62% pour les épices, la dégradation biologique commence en moins de 48 heures.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF0C4A6E),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Seuils d'Alerte par Catégorie de Produit
        item {
            Text("Seuils Hygrométriques d'Alerte (Guide 2026) :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🍚 Riz Blanc & Paddy (Zone A)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("HR ambiante < 65% (Grain max 14.0%)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = AgroGreenPrimary)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌶️ Poivre Noir & Girofle (Zone B)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("HR ambiante < 60% (Grain max 12.0%)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFF7C3AED))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🫘 Haricots & Pois du Cap (Zone C)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("HR ambiante < 64% (Grain max 13.5%)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = SpiceOrangeSecondary)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌽 Maïs Grain Séché", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("HR ambiante < 68% (Aflatoxines si >72%)", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color(0xFFD97706))
                    }
                }
            }
        }
    }

    if (showFullWeatherDialog) {
        com.example.ui.components.WeatherRiskModuleDialog(
            onDismiss = { showFullWeatherDialog = false }
        )
    }
}

@Composable
fun WorkingCapitalAndAuditEmbeddedView(viewModel: MainViewModel) {
    val currentTreasury by viewModel.currentTreasury.collectAsState()
    val products by viewModel.products.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var showBfrDialog by remember { mutableStateOf(false) }
    var showCommercialDialog by remember { mutableStateOf(false) }

    val diagnosis = remember(products, clients, suppliers, currentTreasury) {
        com.example.data.finance.WorkingCapitalEngine.calculateWorkingCapital(
            products = products,
            clients = clients,
            suppliers = suppliers,
            currentTreasuryAr = currentTreasury
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // En-tête Guide p. 81
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = AgroGreenPrimary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Audit Financier & BFR (Guide 2026)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgroGreenPrimary
                                )
                                Text(
                                    text = "Chapitres 12, 13 & 15 : BFR, Trésorerie et Documents Légaux",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "« Le grossiste en grains ne meurt pas d'un manque de clients, il meurt d'asphyxie de trésorerie en période de récolte. Maintenir 2 mois de charges fixes sanctuarisés (10 000 000 Ar) et un BFR inférieur à 50 jours de CA est la règle absolue de survie. » (Guide p. 81)",
                        fontSize = 11.5.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color(0xFF166534),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Actions rapides Étape 4
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { showBfrDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Diagnostic BFR & Cash", fontSize = 11.sp)
                }
                Button(
                    onClick = { showCommercialDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SpiceOrangeSecondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bons BL & Audit", fontSize = 11.sp)
                }
            }
        }

        // Synthèse BFR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = CardDefaults.outlinedCardBorder(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Indicateurs Clés de Liquidité d'Exploitation :", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("BFR Net en Ariary :", fontSize = 11.sp)
                        Text("${String.format(java.util.Locale.US, "%,.0f", diagnosis.workingCapitalAr).replace(',', ' ')} Ar", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("BFR en jours de CA :", fontSize = 11.sp)
                        Text("${diagnosis.workingCapitalDaysOfSales.toInt()} Jours (Cible: < 45-50j)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (diagnosis.workingCapitalDaysOfSales > 50) StatusRedDanger else AgroGreenPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Réserve de sécurité (2 mois) :", fontSize = 11.sp)
                        Text("${String.format(java.util.Locale.US, "%,.0f", diagnosis.treasuryAvailableAr).replace(',', ' ')} Ar (${String.format(java.util.Locale.US, "%.1f", diagnosis.fixedChargesCoverageMonths)} mois)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = if (diagnosis.fixedChargesCoverageMonths < 2.0) StatusRedDanger else AgroGreenPrimary)
                    }
                }
            }
        }

        // Normes de freinte
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tolérances Maximales de Freinte & Pertes (Guide Chap. 6) :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌾 Riz Blanc & Paddy :", fontSize = 11.sp)
                        Text("Plafond max : 1.20%", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🫘 Haricots & Pois du Cap :", fontSize = 11.sp)
                        Text("Plafond max : 1.50%", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌽 Maïs Grain (manipulation) :", fontSize = 11.sp)
                        Text("Plafond max : 1.80%", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("🌶️ Épices Nobles (Girofle/Poivre) :", fontSize = 11.sp)
                        Text("Plafond max : 0.50%", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF7C3AED))
                    }
                }
            }
        }
    }

    if (showBfrDialog) {
        com.example.ui.components.WorkingCapitalDialog(
            viewModel = viewModel,
            onDismiss = { showBfrDialog = false }
        )
    }

    if (showCommercialDialog) {
        com.example.ui.components.CommercialDocumentDialog(
            viewModel = viewModel,
            onDismiss = { showCommercialDialog = false }
        )
    }
}
