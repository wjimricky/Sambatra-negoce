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

/**
 * Modèle de données pour la visualisation Recharts Native Jetpack Compose
 */
data class StockDataPoint(
    val dateLabel: String,
    val dateFull: String,
    val cerealesTonnes: Float,
    val legumineusesTonnes: Float,
    val epicesTonnes: Float,
    val totalTonnes: Float,
    val isForecast: Boolean = false
)

data class CategoryTurnoverMetric(
    val categoryName: String,
    val actualTurnoverRate: Float, // Rotations par an (ex: 8.5x)
    val targetBenchmarkRate: Float, // Cible Guide 2026 (ex: 8.0x)
    val averageStockDaysDio: Int, // Days of Inventory Outstanding (ex: 43 jours)
    val targetDaysDio: Int, // Cible en jours (ex: 45 jours)
    val currentStockKg: Double,
    val safetyStockKg: Double,
    val reorderPointKg: Double,
    val reorderUrgency: ReorderUrgencyStatus,
    val reorderRecommendation: String,
    val primaryColor: Color
)

enum class ReorderUrgencyStatus(val label: String, val colorHex: Long) {
    IMMEDIATE_REORDER("Réapprovisionnement Urgent (<7j)", 0xFFDC2626),
    COMMAND_SOON("Point de Commande Atteint", 0xFFD97706),
    OPTIMAL("Stock & Rotation Optimaux", 0xFF16A34A),
    OVERSTOCK("Sur-stockage (Rotation Lente)", 0xFF2563EB)
}

@Composable
fun RechartsStockVisualizer(
    modifier: Modifier = Modifier,
    lang: String = "FR"
) {
    var selectedChartMode by remember { mutableStateOf(0) } // 0: Évolution Temporelle (Courbe), 1: Taux de Rotation & DIO (Barres)
    var selectedCategoryFilter by remember { mutableStateOf("Tous") } // "Tous", "Céréales", "Légumineuses", "Épices"
    var showSafetyLines by remember { mutableStateOf(true) }

    // Historique sur 6 mois + projection 30 jours (étalonné Guide 2026)
    val timeSeriesData = remember {
        listOf(
            StockDataPoint("Mai", "15 Mai 2026", 42.0f, 18.0f, 6.5f, 66.5f),
            StockDataPoint("Juin", "15 Juin 2026", 38.5f, 22.0f, 7.0f, 67.5f),
            StockDataPoint("Juil", "15 Juillet 2026", 55.0f, 25.5f, 8.2f, 88.7f), // Récolte riz
            StockDataPoint("Août", "15 Août 2026", 48.0f, 20.0f, 7.8f, 75.8f),
            StockDataPoint("Sept", "15 Septembre 2026", 36.0f, 16.5f, 7.2f, 59.7f),
            StockDataPoint("Oct (Actuel)", "16 Octobre 2026", 31.5f, 14.0f, 6.8f, 52.3f),
            StockDataPoint("Nov (Proj.)", "15 Novembre 2026", 24.0f, 11.2f, 6.0f, 41.2f, isForecast = true),
            StockDataPoint("Déc (Proj.)", "15 Décembre 2026", 45.0f, 19.0f, 7.5f, 71.5f, isForecast = true) // Réapprovisionnement
        )
    }

    // Métriques de rotation selon les ratios clés du Chapitre 6 du Guide 2026
    val turnoverMetrics = remember {
        listOf(
            CategoryTurnoverMetric(
                categoryName = "Céréales (Riz Blanc, Paddy, Maïs)",
                actualTurnoverRate = 8.6f,
                targetBenchmarkRate = 8.0f,
                averageStockDaysDio = 42,
                targetDaysDio = 45,
                currentStockKg = 31500.0,
                safetyStockKg = 15000.0,
                reorderPointKg = 28000.0,
                reorderUrgency = ReorderUrgencyStatus.COMMAND_SOON,
                reorderRecommendation = "Lancer un bon de commande de 25 Tonnes de Riz Makalioka sous 6 jours pour éviter la rupture de soudure.",
                primaryColor = AgroGreenPrimary
            ),
            CategoryTurnoverMetric(
                categoryName = "Légumineuses (Pois du Cap, Haricots)",
                actualTurnoverRate = 5.8f,
                targetBenchmarkRate = 6.0f,
                averageStockDaysDio = 62,
                targetDaysDio = 60,
                currentStockKg = 14000.0,
                safetyStockKg = 6000.0,
                reorderPointKg = 12000.0,
                reorderUrgency = ReorderUrgencyStatus.OPTIMAL,
                reorderRecommendation = "Stock conforme aux prévisions de vente. Prochain réapprovisionnement planifié début Novembre.",
                primaryColor = SpiceOrangeSecondary
            ),
            CategoryTurnoverMetric(
                categoryName = "Épices Nobles (Poivre Noir, Girofle)",
                actualTurnoverRate = 3.2f,
                targetBenchmarkRate = 3.5f,
                averageStockDaysDio = 114,
                targetDaysDio = 100,
                currentStockKg = 6800.0,
                safetyStockKg = 3000.0,
                reorderPointKg = 5000.0,
                reorderUrgency = ReorderUrgencyStatus.OVERSTOCK,
                reorderRecommendation = "Rotation lente à très forte immobilisation de trésorerie (42M Ar). Prioriser la vente des lots FEFO avant tout nouvel achat.",
                primaryColor = Color(0xFF8B5CF6)
            )
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Recharts Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = AgroGreenPrimary.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                tint = AgroGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Visualisation des Stocks & Rotation (Recharts Native)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = "Évolution temporelle • Taux de rotation • Anticipation des achats",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Guide 2026 Ch. 6",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch entre les 2 visualisations
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedChartMode == 0,
                    onClick = { selectedChartMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Évolution Stocks (Tonnes)", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
                SegmentedButton(
                    selected = selectedChartMode == 1,
                    onClick = { selectedChartMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Taux de Rotation & DIO", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedChartMode == 0) {
                // Filtre par catégorie de produit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Tous", "Céréales", "Légumineuses", "Épices").forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat, fontSize = 10.5.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgroGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showSafetyLines = !showSafetyLines }
                    ) {
                        Checkbox(
                            checked = showSafetyLines,
                            onCheckedChange = { showSafetyLines = it },
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Seuils", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Graphique Recharts Area & Multi-Line Interactif
                NativeRechartsAreaChart(
                    data = timeSeriesData,
                    categoryFilter = selectedCategoryFilter,
                    showSafetyThresholds = showSafetyLines,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            } else {
                // Vue Bar Chart Recharts du Taux de Rotation par Catégorie
                NativeRechartsBarChart(
                    metrics = turnoverMetrics,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cartes d'anticipation des réapprovisionnements
            Text(
                text = "Anticipation des Réapprovisionnements par Catégorie :",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            turnoverMetrics.forEach { metric ->
                Surface(
                    color = when (metric.reorderUrgency) {
                        ReorderUrgencyStatus.IMMEDIATE_REORDER -> Color(0xFFFEE2E2)
                        ReorderUrgencyStatus.COMMAND_SOON -> Color(0xFFFEF3C7)
                        ReorderUrgencyStatus.OPTIMAL -> Color(0xFFDCFCE7)
                        ReorderUrgencyStatus.OVERSTOCK -> Color(0xFFEFF6FF)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(metric.primaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = metric.categoryName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Surface(
                                color = Color(metric.reorderUrgency.colorHex),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = metric.reorderUrgency.label,
                                    color = Color.White,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Rotation : ${metric.actualTurnoverRate}x/an (Cible: ${metric.targetBenchmarkRate}x)",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Couverture : ${metric.averageStockDaysDio} jours",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 ${metric.reorderRecommendation}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Recharts Native Area & Line Chart avec Tooltip interactif au toucher
 */
@Composable
fun NativeRechartsAreaChart(
    data: List<StockDataPoint>,
    categoryFilter: String,
    showSafetyThresholds: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val maxVal = when (categoryFilter) {
        "Céréales" -> 60.0f
        "Légumineuses" -> 30.0f
        "Épices" -> 12.0f
        else -> 100.0f
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures(
                        onTap = { offset ->
                            val stepX = size.width / (data.size - 1)
                            val idx = ((offset.x / stepX) + 0.5f).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = idx
                        }
                    )
                }
                .pointerInput(data) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            val stepX = size.width / (data.size - 1)
                            val idx = ((change.position.x / stepX) + 0.5f).toInt().coerceIn(0, data.size - 1)
                            selectedIndex = idx
                        },
                        onDragEnd = { selectedIndex = null },
                        onDragCancel = { selectedIndex = null }
                    )
                }
        ) {
            val w = size.width
            val h = size.height - 30.dp.toPx() // marge pour labels X
            val stepX = w / (data.size - 1)

            // 1. Grille Recharts horizontale subtile
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = h * (i.toFloat() / gridLines)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            // 2. Lignes de référence Guide 2026 : Stock de sécurité & Point de commande
            if (showSafetyThresholds) {
                val safetyVal = when (categoryFilter) {
                    "Céréales" -> 20.0f
                    "Légumineuses" -> 10.0f
                    "Épices" -> 4.0f
                    else -> 35.0f
                }
                val safetyY = h - (safetyVal / maxVal) * h
                drawLine(
                    color = Color(0xFFEF4444), // Rouge seuil sécurité
                    start = Offset(0f, safetyY),
                    end = Offset(w, safetyY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )

                val reorderVal = when (categoryFilter) {
                    "Céréales" -> 35.0f
                    "Légumineuses" -> 16.0f
                    "Épices" -> 6.5f
                    else -> 55.0f
                }
                val reorderY = h - (reorderVal / maxVal) * h
                drawLine(
                    color = Color(0xFFF59E0B), // Ambre point de commande
                    start = Offset(0f, reorderY),
                    end = Offset(w, reorderY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
            }

            // 3. Zone de projection prévisionnelle (grisée)
            val forecastStartIndex = data.indexOfFirst { it.isForecast }
            if (forecastStartIndex > 0) {
                val forecastX = forecastStartIndex * stepX
                drawRect(
                    color = Color(0xFFF1F5F9).copy(alpha = 0.5f),
                    topLeft = Offset(forecastX, 0f),
                    size = Size(w - forecastX, h)
                )
            }

            // 4. Tracé de la courbe et de l'Area gradient Recharts
            fun getValue(point: StockDataPoint): Float = when (categoryFilter) {
                "Céréales" -> point.cerealesTonnes
                "Légumineuses" -> point.legumineusesTonnes
                "Épices" -> point.epicesTonnes
                else -> point.totalTonnes
            }

            val primaryColor = when (categoryFilter) {
                "Céréales" -> Color(0xFF15803D)
                "Légumineuses" -> Color(0xFFD97706)
                "Épices" -> Color(0xFF7C3AED)
                else -> AgroGreenPrimary
            }

            val path = Path()
            val fillPath = Path()

            data.forEachIndexed { i, pt ->
                val x = i * stepX
                val v = getValue(pt)
                val y = h - (v / maxVal) * h

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (i - 1) * stepX
                    val prevVal = getValue(data[i - 1])
                    val prevY = h - (prevVal / maxVal) * h
                    val cX1 = prevX + (x - prevX) / 2
                    val cX2 = prevX + (x - prevX) / 2
                    path.cubicTo(cX1, prevY, cX2, y, x, y)
                    fillPath.cubicTo(cX1, prevY, cX2, y, x, y)
                }
            }

            fillPath.lineTo((data.size - 1) * stepX, h)
            fillPath.close()

            // Dégradé de remplissage Area Recharts
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.28f), primaryColor.copy(alpha = 0.02f)),
                    startY = 0f,
                    endY = h
                )
            )

            // Trait de courbe
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Points Recharts
            data.forEachIndexed { i, pt ->
                val x = i * stepX
                val v = getValue(pt)
                val y = h - (v / maxVal) * h

                val isSelected = selectedIndex == i

                // Halo blanc
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 7.dp.toPx() else 4.5f.dp.toPx(),
                    center = Offset(x, y)
                )
                // Point coloré
                drawCircle(
                    color = if (pt.isForecast) primaryColor.copy(alpha = 0.7f) else primaryColor,
                    radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Ligne de guide Scrubber Recharts
            selectedIndex?.let { idx ->
                val x = idx * stepX
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.5f.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )
            }
        }

        // Labels de l'axe X (Mois)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { pt ->
                Text(
                    text = pt.dateLabel,
                    fontSize = 9.sp,
                    color = if (pt.isForecast) SpiceOrangeSecondary else Color(0xFF64748B),
                    fontWeight = if (pt.isForecast) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Tooltip Recharts flottant quand l'utilisateur touche ou survole un point
        selectedIndex?.let { idx ->
            val point = data[idx]
            val valDisplay = when (categoryFilter) {
                "Céréales" -> "${point.cerealesTonnes} T"
                "Légumineuses" -> "${point.legumineusesTonnes} T"
                "Épices" -> "${point.epicesTonnes} T"
                else -> "${point.totalTonnes} T"
            }

            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(if (idx > data.size / 2) Alignment.TopStart else Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text(
                        text = point.dateFull + if (point.isForecast) " (Prévision)" else "",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Stock $categoryFilter : $valDisplay",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (point.isForecast) "Plan d'approvisionnement projeté" else "Mesure réelle en entrepôt",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Recharts Native Grouped Bar Chart pour le Taux de Rotation & DIO
 */
@Composable
fun NativeRechartsBarChart(
    metrics: List<CategoryTurnoverMetric>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Légende Recharts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(AgroGreenPrimary, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Rotation Réelle (x/an)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF94A3B8), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Cible Guide 2026", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Barres horizontales comparatives
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            metrics.forEach { metric ->
                val maxTurnover = 10.0f
                val actualRatio = (metric.actualTurnoverRate / maxTurnover).coerceIn(0f, 1f)
                val targetRatio = (metric.targetBenchmarkRate / maxTurnover).coerceIn(0f, 1f)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = metric.categoryName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${metric.actualTurnoverRate}x vs ${metric.targetBenchmarkRate}x cible",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = metric.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Double barre Recharts arrondie
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Barre réelle
                        Box(
                            modifier = Modifier
                                .weight(actualRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp, topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(metric.primaryColor),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "${metric.actualTurnoverRate}x",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }

                        // Espace restant
                        Spacer(modifier = Modifier.weight((1f - actualRatio).coerceAtLeast(0.01f)))
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Barre cible
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(targetRatio)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFCBD5E1))
                        )
                        Spacer(modifier = Modifier.weight((1f - targetRatio).coerceAtLeast(0.01f)))
                    }
                }
            }
        }
    }
}
