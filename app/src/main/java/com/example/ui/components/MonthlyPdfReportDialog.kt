package com.example.ui.components

import android.widget.Toast
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
import com.example.data.report.MonthlyReportData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.util.Locale

@Composable
fun MonthlyPdfReportDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf("Octobre 2026") }
    val months = listOf("Octobre 2026", "Septembre 2026", "Août 2026")

    val reportData = remember(selectedMonth) {
        viewModel.getMonthlyReportData(selectedMonth)
    }

    var generatedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AgroGreenPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rapport Mensuel KPI & Pertes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Génération PDF selon le Guide de Référence 2026",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sélecteur de mois
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    months.forEach { m ->
                        FilterChip(
                            selected = selectedMonth == m,
                            onClick = {
                                selectedMonth = m
                                generatedFile = null
                            },
                            label = { Text(m, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgroGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenu scrollable : Prévisualisation complète du rapport
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Carte Synthèse Financière
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Synthèse Exécutive • ${reportData.period}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AgroGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Chiffre d'Affaires", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%,.0f", reportData.totalTurnoverAr)} Ar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Column {
                                        Text("Marge Brute", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%,.0f", reportData.totalGrossMarginAr)} Ar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = AgroGreenPrimary
                                        )
                                    }
                                    Column {
                                        Text("Taux de Marge", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            "${String.format(Locale.US, "%.1f", reportData.overallMarginPercent)}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (reportData.overallMarginPercent >= 14.0) AgroGreenPrimary else SpiceOrangeSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. Grille des Indicateurs Clés
                    item {
                        Text(
                            text = "1. Tableau de bord des 5 KPI Majeurs",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reportData.executiveKpis.forEach { kpi ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(kpi.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                            Text(kpi.comment, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(kpi.value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (kpi.status == "EXCELLENT" || kpi.status == "CONFORME") AgroGreenPrimary else StatusRedDanger)
                                            Text("Cible: ${kpi.target}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Analyse Détaillée des Marges par Produit / Lot
                    item {
                        Text(
                            text = "2. Analyse des Marges par Lot & Produit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                reportData.marginBreakdown.forEachIndexed { idx, row ->
                                    if (idx > 0) Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(row.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                if (row.isLocomotive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = AgroGreenPrimary.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text("Locomotive", fontSize = 9.sp, color = AgroGreenPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text("Lot: ${row.lotNumber} • ${String.format(Locale.US, "%,.0f", row.quantitySoldKg)} kg vendus", fontSize = 11.sp, color = Color.Gray)
                                            Text("Coût revient: ${row.fullCostPriceAr.toInt()} Ar/kg → Vente: ${row.salePriceAr.toInt()} Ar/kg", fontSize = 11.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                "+${String.format(Locale.US, "%,.0f", row.totalMarginAr)} Ar",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = AgroGreenPrimary
                                            )
                                            Text(
                                                "Marge: ${String.format(Locale.US, "%.1f", row.marginPercent)}%",
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

                    // 3. Suivi Rigoureux des Pertes & Freintes
                    item {
                        Text(
                            text = "3. Suivi des Pertes & Freintes de Stockage",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Perte Totale Constatée :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                    Text(
                                        "${String.format(Locale.US, "%.1f", reportData.totalLossKg)} kg (${String.format(Locale.US, "%,.0f", reportData.totalLossValueAr)} Ar)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                                Text(
                                    "Taux de freinte moyen : ${String.format(Locale.US, "%.2f", reportData.averageLossRatePercent)}% (Seuil guide < 1.50%)",
                                    fontSize = 11.sp,
                                    color = AgroGreenPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                reportData.lossTracking.forEachIndexed { i, loss ->
                                    if (i > 0) Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFFDE68A))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(loss.category, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF78350F))
                                            Text("${loss.product} • ${loss.explanation}", fontSize = 10.sp, color = Color(0xFF92400E))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("${String.format(Locale.US, "%.1f", loss.quantityLostKg)} kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Taux: ${String.format(Locale.US, "%.2f", loss.realLossRatePercent)}% (Max ${loss.maxAllowedRatePercent}%)", fontSize = 10.sp, color = if (loss.isCompliant) AgroGreenPrimary else StatusRedDanger)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Recommandations Guide 2026
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "4. Prescriptions Stratégiques du Guide",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AgroGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                reportData.recommendations.forEach { reco ->
                                    Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = AgroGreenPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(reco, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions PDF
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bouton Générer PDF
                    Button(
                        onClick = {
                            isGenerating = true
                            try {
                                val file = viewModel.generateMonthlyPdfReport(context, selectedMonth)
                                generatedFile = file
                                Toast.makeText(context, "PDF généré : ${file.name}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erreur génération PDF : ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isGenerating = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isGenerating) "Génération..." else "Générer PDF", fontSize = 12.sp)
                    }

                    // Bouton Partager PDF
                    FilledTonalButton(
                        onClick = {
                            viewModel.shareMonthlyPdfReport(context, selectedMonth)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Partager", fontSize = 12.sp)
                    }

                    // Bouton Ouvrir PDF
                    OutlinedButton(
                        onClick = {
                            viewModel.openMonthlyPdfReport(context, selectedMonth)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ouvrir", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
