package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.CommodityPriceForecast
import com.example.data.ai.PredictivePriceEngine
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PredictivePriceAnalysisDialog(
    initialProductCode: String = "RIZ_MAK",
    onDismiss: () -> Unit
) {
    val commodities = PredictivePriceEngine.COMMODITIES_DATA
    var selectedProductCode by remember { mutableStateOf(initialProductCode) }
    var volumeTonsText by remember { mutableStateOf("10.0") }
    var holdingMonthsText by remember { mutableStateOf("6") }

    val currentForecast = remember(selectedProductCode) {
        PredictivePriceEngine.getForecast(selectedProductCode)
    }

    val volumeTons = volumeTonsText.toDoubleOrNull() ?: 10.0
    val holdingMonths = holdingMonthsText.toIntOrNull() ?: currentForecast.recommendedStorageMonths

    val projection = remember(currentForecast, volumeTons, holdingMonths) {
        PredictivePriceEngine.calculateProjectedArbitrage(
            currentForecast,
            volumeTons,
            holdingMonths
        )
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.FRANCE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SpiceOrangeSecondary,
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Analyse Prédictive des Prix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Fluctuations saisonnières, arbitrage & opportunités (Guide 2026)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sélecteur de Produits
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(commodities) { item ->
                        val isSelected = item.productCode == selectedProductCode
                        Surface(
                            color = if (isSelected) AgroGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { selectedProductCode = item.productCode }
                        ) {
                            Text(
                                text = item.productName,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Carte d'identité du produit et fenêtres optimales
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
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
                                    Column {
                                        Text(
                                            text = currentForecast.productName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Bassin : ${currentForecast.productionBasin}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = Color(currentForecast.riskLevel.colorHex).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = currentForecast.riskLevel.labelFr,
                                            color = Color(currentForecast.riskLevel.colorHex),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Creux Récolte
                                    Surface(
                                        color = Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.South, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Creux Récolte", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                            }
                                            Text(
                                                text = "${numberFormat.format(currentForecast.harvestFloorPriceAr.toInt())} Ar/kg",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF15803D)
                                            )
                                            Text(currentForecast.harvestMonthName, fontSize = 9.5.sp, color = Color(0xFF166534))
                                        }
                                    }

                                    // Pic Soudure
                                    Surface(
                                        color = Color(0xFFFFF7ED),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.North, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Pic Soudure", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                            }
                                            Text(
                                                text = "${numberFormat.format(currentForecast.leanPeakPriceAr.toInt())} Ar/kg",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFC2410C)
                                            )
                                            Text(currentForecast.leanPeakMonthName, fontSize = 9.5.sp, color = Color(0xFF9A3412))
                                        }
                                    }

                                    // Écart Brut
                                    val grossSpread = currentForecast.leanPeakPriceAr - currentForecast.harvestFloorPriceAr
                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Écart Brut", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                            Text(
                                                text = "+${numberFormat.format(grossSpread.toInt())} Ar",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFF1E40AF)
                                            )
                                            val spreadPct = (grossSpread / currentForecast.harvestFloorPriceAr) * 100.0
                                            Text("+${spreadPct.toInt()}% brut", fontSize = 9.5.sp, color = Color(0xFF1E3A8A))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Graphique de projection mensuelle (Courbe de prix 12 mois)
                    item {
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
                                        text = "Courbe Prévisionnelle sur 12 Mois (Ar/kg)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), CircleShape))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Récolte", fontSize = 9.sp, color = Color.Gray)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).background(SpiceOrangeSecondary, CircleShape))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Soudure", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                PriceProjectionCanvas(projections = currentForecast.monthlyPriceProjections)

                                Spacer(modifier = Modifier.height(8.dp))

                                // Recommandations Fenêtres d'Achat & Revente
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
                                            Text("🛒 Fenêtre d'Achat Optimale", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF15803D))
                                            Text(currentForecast.optimalPurchaseWindow, fontSize = 9.5.sp, color = Color(0xFF166534))
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFFFFBEB),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("💰 Fenêtre de Revente Cible", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFB45309))
                                            Text(currentForecast.optimalSellingWindow, fontSize = 9.5.sp, color = Color(0xFF92400E))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Simulateur d'arbitrage financier et calcul du Gain Net Réel
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Simulateur d'Arbitrage Financier (Gain Net Réel)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = AgroGreenPrimary
                                )
                                Text(
                                    text = "Déduit les frais de stockage, le coût de trésorerie (1.5%/mois) et la freinte",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = volumeTonsText,
                                        onValueChange = { volumeTonsText = it },
                                        label = { Text("Volume (Tonnes)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = holdingMonthsText,
                                        onValueChange = { holdingMonthsText = it },
                                        label = { Text("Stockage (Mois)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Bilan Financier
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FinancialRow("Coût d'achat récolte :", "${numberFormat.format(projection.totalBuyCostAr.toLong())} Ar")
                                    FinancialRow("Chiffre d'affaires revente en soudure :", "${numberFormat.format(projection.grossSaleRevenueAr.toLong())} Ar")
                                    FinancialRow("Frais stockage & immobilisation :", "- ${numberFormat.format(projection.totalHoldingCostAr.toLong())} Ar", isNegative = true)
                                    FinancialRow("Freinte & pertes estimées :", "- ${numberFormat.format(projection.totalLossCostAr.toLong())} Ar", isNegative = true)
                                    
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("BÉNÉFICE NET ESTIMÉ :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                        Text(
                                            text = "${numberFormat.format(projection.netGainAr.toLong())} Ar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (projection.netGainAr > 0) Color(0xFF15803D) else Color(0xFFDC2626)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Retour sur investissement net (ROI) :", fontSize = 11.sp, color = Color.Gray)
                                        Text(
                                            text = "${projection.roiPercent.toInt()}%",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (projection.roiPercent >= 20.0) Color(0xFF15803D) else SpiceOrangeSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Conseil Stratégique IA
                                Surface(
                                    color = if (projection.isViable) Color(0xFFECFDF5) else Color(0xFFFFF1F2),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (projection.isViable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (projection.isViable) Color(0xFF15803D) else Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = currentForecast.strategicActionAdvice,
                                            fontSize = 10.5.sp,
                                            color = if (projection.isViable) Color(0xFF166534) else Color(0xFF991B1B),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer l'Analyse Prédictive", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PriceProjectionCanvas(projections: List<com.example.data.ai.MonthlyPriceDataPoint>) {
    if (projections.isEmpty()) return

    val minPrice = (projections.minOfOrNull { it.estimatedPriceAr } ?: 1000.0) * 0.90
    val maxPrice = (projections.maxOfOrNull { it.estimatedPriceAr } ?: 5000.0) * 1.10
    val priceRange = (maxPrice - minPrice).coerceAtLeast(100.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = width / (projections.size - 1)

            val points = projections.mapIndexed { index, dp ->
                val x = index * stepX
                val normalizedY = 1.0 - ((dp.estimatedPriceAr - minPrice) / priceRange)
                val y = (normalizedY * height).toFloat()
                Offset(x, y)
            }

            // Draw connecting smooth path
            val path = Path()
            points.forEachIndexed { i, p ->
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, color = Color(0xFF2563EB), style = Stroke(width = 3.dp.toPx()))

            // Draw points with specific harvest / lean color
            points.forEachIndexed { i, p ->
                val dp = projections[i]
                val pointColor = when {
                    dp.isHarvestPeriod -> Color(0xFF16A34A)
                    dp.isLeanSeason -> Color(0xFFEA580C)
                    else -> Color(0xFF2563EB)
                }
                drawCircle(color = Color.White, radius = 5.dp.toPx(), center = p)
                drawCircle(color = pointColor, radius = 3.5.dp.toPx(), center = p)
            }
        }
    }
}

@Composable
private fun FinancialRow(label: String, value: String, isNegative: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isNegative) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
        )
    }
}
