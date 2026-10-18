package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.theme.SpiceOrangeSecondary
import com.example.ui.theme.StatusGreenOk
import com.example.ui.theme.StatusRedDanger
import java.util.Locale

data class PricePoint(
    val dayOffset: Int, // 0 = J-30, 29 = Aujourd'hui
    val dateLabel: String,
    val priceAr: Double
)

data class ProductPriceHistory(
    val productCode: String,
    val productName: String,
    val unit: String,
    val points: List<PricePoint>,
    val marketTrend: String, // "HAUSSE", "BAISSE", "STABLE"
    val seasonalAdvice: String
)

object PriceHistoryProvider {
    val sampleHistories = listOf(
        ProductPriceHistory(
            productCode = "RIZ",
            productName = "Riz blanc Makalioka",
            unit = "Ar/kg",
            marketTrend = "BAISSE",
            seasonalAdvice = "Période post-récolte Alaotra : cours détendus. Prix plancher SPM 1 700 Ar/kg sur paddy. Opportunité d'achat sur contrats échelonnés.",
            points = listOf(
                PricePoint(0, "15 Sep", 2250.0),
                PricePoint(5, "20 Sep", 2220.0),
                PricePoint(10, "25 Sep", 2180.0),
                PricePoint(15, "30 Sep", 2140.0),
                PricePoint(20, "05 Oct", 2100.0),
                PricePoint(25, "10 Oct", 2090.0),
                PricePoint(29, "15 Oct", 2080.0)
            )
        ),
        ProductPriceHistory(
            productCode = "HAR",
            productName = "Haricot sec Tsipoy",
            unit = "Ar/kg",
            marketTrend = "HAUSSE",
            seasonalAdvice = "Approche de la période de soudure : les cours montent. Attention aux bruches si stockage > 60j (Guide p. 90).",
            points = listOf(
                PricePoint(0, "15 Sep", 2750.0),
                PricePoint(5, "20 Sep", 2790.0),
                PricePoint(10, "25 Sep", 2840.0),
                PricePoint(15, "30 Sep", 2900.0),
                PricePoint(20, "05 Oct", 2980.0),
                PricePoint(25, "10 Oct", 3050.0),
                PricePoint(29, "15 Oct", 3150.0)
            )
        ),
        ProductPriceHistory(
            productCode = "GIR",
            productName = "Clous de Girofle (Grade 1)",
            unit = "Ar/kg",
            marketTrend = "STABLE",
            seasonalAdvice = "Ouverture campagne Analanjirofo : vérifier impuretés tiges (max 5%) sous peine de surcoût net de 10% (Guide p. 92).",
            points = listOf(
                PricePoint(0, "15 Sep", 27800.0),
                PricePoint(5, "20 Sep", 28100.0),
                PricePoint(10, "25 Sep", 27900.0),
                PricePoint(15, "30 Sep", 28000.0),
                PricePoint(20, "05 Oct", 28200.0),
                PricePoint(25, "10 Oct", 28050.0),
                PricePoint(29, "15 Oct", 28000.0)
            )
        ),
        ProductPriceHistory(
            productCode = "MAI",
            productName = "Maïs grain sec",
            unit = "Ar/kg",
            marketTrend = "BAISSE",
            seasonalAdvice = "Sur-stock sur le marché. Sambatra Négoce détient déjà 51j de couverture. Geler les achats spéculatifs (Guide p. 71).",
            points = listOf(
                PricePoint(0, "15 Sep", 1480.0),
                PricePoint(5, "20 Sep", 1450.0),
                PricePoint(10, "25 Sep", 1420.0),
                PricePoint(15, "30 Sep", 1390.0),
                PricePoint(20, "05 Oct", 1380.0),
                PricePoint(25, "10 Oct", 1360.0),
                PricePoint(29, "15 Oct", 1350.0)
            )
        )
    )
}

/**
 * Composant de visualisation interactive de l'évolution du prix d'achat moyen (30 derniers jours).
 * Implémenté en Jetpack Compose natif avec Canvas, gradient, courbe et infobulle au toucher.
 */
@Composable
fun PriceTrendChart(
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val currentHistory = PriceHistoryProvider.sampleHistories[selectedIndex]
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val points = currentHistory.points
    val minPrice = points.minOf { it.priceAr }
    val maxPrice = points.maxOf { it.priceAr }
    val avgPrice = points.map { it.priceAr }.average()
    val priceDiff = points.last().priceAr - points.first().priceAr
    val percentDiff = (priceDiff / points.first().priceAr) * 100.0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Titre & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Évolution Prix d'Achat Moyen (30 jours)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = "Relevés hebdomadaires de marché (Guide Ch. 1.4 & 12.6)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (currentHistory.marketTrend == "HAUSSE") Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (currentHistory.marketTrend == "HAUSSE") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (currentHistory.marketTrend == "HAUSSE") StatusRedDanger else StatusGreenOk,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (priceDiff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", percentDiff)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentHistory.marketTrend == "HAUSSE") StatusRedDanger else StatusGreenOk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sélecteur de produits clés
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PriceHistoryProvider.sampleHistories.forEachIndexed { idx, item ->
                    FilterChip(
                        selected = selectedIndex == idx,
                        onClick = {
                            selectedIndex = idx
                            selectedPointIndex = null
                        },
                        label = { Text(item.productCode, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgroGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prix actuel & moyenne
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val displayedPrice = if (selectedPointIndex != null) {
                        points[selectedPointIndex!!].priceAr
                    } else {
                        points.last().priceAr
                    }
                    val displayedDate = if (selectedPointIndex != null) {
                        points[selectedPointIndex!!].dateLabel
                    } else {
                        "Aujourd'hui"
                    }

                    Text(
                        text = "${displayedPrice.toInt()} Ar/kg",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = "Relevé au $displayedDate",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Moyenne 30j : ${avgPrice.toInt()} Ar/kg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SpiceOrangeSecondary
                    )
                    Text(
                        text = "Min: ${minPrice.toInt()} • Max: ${maxPrice.toInt()}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Graphique interactif
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (points.size - 1)
                                val index = (offset.x / spacing).toInt().coerceIn(0, points.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 20f
                    val chartHeight = height - paddingBottom
                    val range = maxOf(1.0, maxPrice - minPrice)

                    val spacing = width / (points.size - 1)
                    val offsets = points.mapIndexed { index, p ->
                        val x = index * spacing
                        val normalized = ((p.priceAr - minPrice) / range).toFloat()
                        val y = chartHeight - (normalized * (chartHeight - 30f)) - 10f
                        Offset(x, y)
                    }

                    // Ligne de moyenne
                    val avgNormalized = ((avgPrice - minPrice) / range).toFloat()
                    val avgY = chartHeight - (avgNormalized * (chartHeight - 30f)) - 10f
                    drawLine(
                        color = Color(0xFFD97706).copy(alpha = 0.5f),
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )

                    // Courbe continue et dégradé
                    val path = Path()
                    val fillPath = Path()

                    offsets.forEachIndexed { i, offset ->
                        if (i == 0) {
                            path.moveTo(offset.x, offset.y)
                            fillPath.moveTo(offset.x, chartHeight)
                            fillPath.lineTo(offset.x, offset.y)
                        } else {
                            val prev = offsets[i - 1]
                            val midX = (prev.x + offset.x) / 2f
                            path.cubicTo(midX, prev.y, midX, offset.y, offset.x, offset.y)
                            fillPath.cubicTo(midX, prev.y, midX, offset.y, offset.x, offset.y)
                        }
                    }

                    fillPath.lineTo(width, chartHeight)
                    fillPath.close()

                    // Remplissage dégradé
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B4D3E).copy(alpha = 0.25f),
                                Color(0xFF1B4D3E).copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Ligne principale
                    drawPath(
                        path = path,
                        color = Color(0xFF1B4D3E),
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Points sur la courbe
                    offsets.forEachIndexed { i, offset ->
                        val isSelected = selectedPointIndex == i
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 8f else 5f,
                            center = offset
                        )
                        drawCircle(
                            color = if (isSelected) Color(0xFFD97706) else Color(0xFF1B4D3E),
                            radius = if (isSelected) 6f else 3f,
                            center = offset
                        )
                    }
                }
            }

            // Labels de dates en bas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("J-30 (${points.first().dateLabel})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("J-15", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Aujourd'hui (${points.last().dateLabel})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conseil d'anticipation achat du guide
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = AgroGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentHistory.seasonalAdvice,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
