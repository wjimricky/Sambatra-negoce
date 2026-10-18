package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Modèle pour le suivi trimestriel de la consommation et des niveaux de stocks d'épices (Guide 2026 Ch. 6 & 8).
 * Couvre les 3 derniers mois (Août, Septembre, Octobre 2026).
 */
data class WeeklySpiceDataPoint(
    val weekIndex: Int, // 0 à 11 (12 semaines sur 3 mois)
    val weekLabel: String,
    val monthName: String,
    val totalStockKg: Float,
    val weeklyConsumptionKg: Float,
    val girofleStockKg: Float,
    val poivreStockKg: Float,
    val vanilleStockKg: Float
)

object QuarterlySpicesProvider {
    val threeMonthsData = listOf(
        // Août 2026
        WeeklySpiceDataPoint(0, "S1 Août", "Août 2026", 8600f, 160f, 5100f, 2600f, 900f),
        WeeklySpiceDataPoint(1, "S2 Août", "Août 2026", 8440f, 175f, 4980f, 2560f, 900f),
        WeeklySpiceDataPoint(2, "S3 Août", "Août 2026", 8265f, 190f, 4850f, 2515f, 900f),
        WeeklySpiceDataPoint(3, "S4 Août", "Août 2026", 8075f, 210f, 4710f, 2465f, 900f),
        // Septembre 2026
        WeeklySpiceDataPoint(4, "S1 Sept", "Septembre 2026", 7865f, 180f, 4560f, 2405f, 900f),
        WeeklySpiceDataPoint(5, "S2 Sept", "Septembre 2026", 7685f, 195f, 4420f, 2365f, 900f),
        WeeklySpiceDataPoint(6, "S3 Sept", "Septembre 2026", 7490f, 170f, 4290f, 2300f, 900f),
        WeeklySpiceDataPoint(7, "S4 Sept", "Septembre 2026", 7320f, 185f, 4160f, 2260f, 900f),
        // Octobre 2026 (Actuel)
        WeeklySpiceDataPoint(8, "S1 Oct", "Octobre 2026", 7135f, 165f, 4030f, 2205f, 900f),
        WeeklySpiceDataPoint(9, "S2 Oct", "Octobre 2026", 6970f, 190f, 3910f, 2160f, 900f),
        WeeklySpiceDataPoint(10, "S3 Oct", "Octobre 2026", 6780f, 215f, 3780f, 2100f, 900f),
        WeeklySpiceDataPoint(11, "S4 Oct", "Octobre 2026", 6565f, 220f, 3640f, 2025f, 900f)
    )

    const val SAFETY_STOCK_KG = 3000f // Seuil critique Guide 2026
    const val REORDER_POINT_KG = 5000f // Point de commande anticipée
}

@Composable
fun QuarterlySpicesChart(
    modifier: Modifier = Modifier
) {
    val data = QuarterlySpicesProvider.threeMonthsData
    var selectedWeekIndex by remember { mutableStateOf<Int?>(11) } // Par défaut, dernière semaine (Octobre)
    var selectedSpiceFilter by remember { mutableStateOf("Tous") } // "Tous", "Girofle", "Poivre", "Vanille"

    val activePoint = selectedWeekIndex?.let { data.getOrNull(it) } ?: data.last()
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.FRANCE) }

    // Ratios du trimestre
    val totalConsumptionThreeMonths = data.sumOf { it.weeklyConsumptionKg.toDouble() }
    val avgWeeklyConsumption = totalConsumptionThreeMonths / data.size
    val currentStock = activePoint.totalStockKg
    val coverageWeeks = if (avgWeeklyConsumption > 0) (currentStock / avgWeeklyConsumption).toInt() else 0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Recharts Trimestriel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Graphique Trimestriel Épices (Recharts)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9)
                        )
                        Text(
                            text = "Consommation & Niveau de Stock sur 3 mois (Août - Oct. 2026)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF3E8FF),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Zone B • Noble",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6D28D9),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtres rapides Épices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Tous", "Girofle", "Poivre", "Vanille").forEach { spice ->
                    val isSelected = selectedSpiceFilter == spice
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSpiceFilter = spice },
                        label = { Text(spice, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C3AED),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Recharts : Surface & Courbes Stock + Barres Consommation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFAFAFA))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / data.size
                            val index = (offset.x / stepX).toInt().coerceIn(0, data.size - 1)
                            selectedWeekIndex = index
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val width = size.width
                            val stepX = width / data.size
                            val index = (change.position.x / stepX).toInt().coerceIn(0, data.size - 1)
                            selectedWeekIndex = index
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp)) {
                    val width = size.width
                    val height = size.height
                    val maxStock = 10000f
                    val maxConsumption = 300f

                    // Grille horizontale
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = height * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Seuil Point de commande (5000 kg) en pointillés orange
                    val yReorder = height * (1f - (QuarterlySpicesProvider.REORDER_POINT_KG / maxStock))
                    drawLine(
                        color = Color(0xFFD97706),
                        start = Offset(0f, yReorder),
                        end = Offset(width, yReorder),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Seuil Sécurité (3000 kg) en pointillés rouge
                    val ySafety = height * (1f - (QuarterlySpicesProvider.SAFETY_STOCK_KG / maxStock))
                    drawLine(
                        color = Color(0xFFDC2626),
                        start = Offset(0f, ySafety),
                        end = Offset(width, ySafety),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )

                    val stepX = width / (data.size - 1)

                    // 1. Barres de consommation hebdomadaire
                    val barWidth = stepX * 0.4f
                    data.forEachIndexed { i, dp ->
                        val x = i * stepX
                        val barHeight = (dp.weeklyConsumptionKg / maxConsumption) * (height * 0.5f)
                        val barY = height - barHeight
                        drawRoundRect(
                            color = Color(0xFF06B6D4).copy(alpha = 0.5f),
                            topLeft = Offset(x - barWidth / 2f, barY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }

                    // 2. Courbe & Surface de stock total (ou filtré)
                    val stockPoints = data.mapIndexed { i, dp ->
                        val x = i * stepX
                        val stockValue = when (selectedSpiceFilter) {
                            "Girofle" -> dp.girofleStockKg
                            "Poivre" -> dp.poivreStockKg
                            "Vanille" -> dp.vanilleStockKg
                            else -> dp.totalStockKg
                        }
                        val y = height * (1f - (stockValue / maxStock))
                        Offset(x, y)
                    }

                    // Gradient Fill (Recharts Area style)
                    val areaPath = Path().apply {
                        stockPoints.forEachIndexed { i, p ->
                            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                        }
                        lineTo(stockPoints.last().x, height)
                        lineTo(stockPoints.first().x, height)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6).copy(alpha = 0.35f),
                                Color(0xFF8B5CF6).copy(alpha = 0.05f)
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Ligne de stock
                    val linePath = Path().apply {
                        stockPoints.forEachIndexed { i, p ->
                            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = Color(0xFF7C3AED),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Points sur la courbe
                    stockPoints.forEachIndexed { i, p ->
                        val isSelected = selectedWeekIndex == i
                        drawCircle(color = Color.White, radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(), center = p)
                        drawCircle(
                            color = if (isSelected) SpiceOrangeSecondary else Color(0xFF7C3AED),
                            radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                            center = p
                        )
                    }

                    // Ligne de sélection verticale
                    selectedWeekIndex?.let { selIdx ->
                        val selX = selIdx * stepX
                        drawLine(
                            color = Color(0xFF7C3AED).copy(alpha = 0.5f),
                            start = Offset(selX, 0f),
                            end = Offset(selX, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Légende Recharts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF7C3AED), CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stock Total (kg)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF06B6D4), RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ventes / Semaine", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color(0xFFD97706)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Pt Cde (5T)", fontSize = 9.sp, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(10.dp).height(2.dp).background(Color(0xFFDC2626)))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Sécu (3T)", fontSize = 9.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tooltip interactif Recharts
            Surface(
                color = Color(0xFFF5F3FF),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(Color(0xFF8B5CF6))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePoint.weekLabel} • ${activePoint.monthName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF6D28D9)
                        )

                        val statusColor = if (activePoint.totalStockKg > QuarterlySpicesProvider.REORDER_POINT_KG) {
                            Color(0xFF15803D)
                        } else Color(0xFFD97706)

                        val statusText = if (activePoint.totalStockKg > QuarterlySpicesProvider.REORDER_POINT_KG) {
                            "STOCK CONFORTABLE"
                        } else "POINT DE COMMANDE ATTEINT"

                        Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = statusText,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Stock Épices :", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "${numberFormat.format(activePoint.totalStockKg.toInt())} kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF6D28D9)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ventes semaine :", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "${numberFormat.format(activePoint.weeklyConsumptionKg.toInt())} kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0E7490)
                            )
                        }

                        Column(modifier = Modifier.weight(1.2f)) {
                            Text("Couverture :", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                text = "$coverageWeeks semaines (${(coverageWeeks * 7)} j)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Détail par épice pour la semaine sélectionnée
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Girofle : ${activePoint.girofleStockKg.toInt()} kg", fontSize = 10.sp, color = Color.DarkGray)
                        Text("• Poivre : ${activePoint.poivreStockKg.toInt()} kg", fontSize = 10.sp, color = Color.DarkGray)
                        Text("• Vanille : ${activePoint.vanilleStockKg.toInt()} kg", fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bilan Trimestriel & Décision d'Approvisionnement
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📋 Bilan des 3 Derniers Mois & Décision Guide 2026 :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Ventes cumulées : ${numberFormat.format(totalConsumptionThreeMonths.toInt())} kg écoulés (~61,2M Ar de CA réalisé).\n" +
                               "• Vitesse moyenne de déstockage : ${avgWeeklyConsumption.toInt()} kg/semaine.\n" +
                               "• Recommandation : Les stocks actuels couvrent encore $coverageWeeks semaines. Attendre l'ouverture de la campagne Analanjirofo en novembre pour réapprovisionner au prix plancher (creux de récolte).",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
