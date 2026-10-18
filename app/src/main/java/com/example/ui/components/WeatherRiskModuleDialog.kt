package com.example.ui.components

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.weather.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WeatherRiskModuleDialog(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedLocation by remember { mutableStateOf(WeatherService.LOCATIONS[0]) }
    var assessmentState by remember { mutableStateOf<WeatherRiskAssessment?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshWeather(loc: WarehouseLocation) {
        isLoading = true
        coroutineScope.launch {
            val result = WeatherService.fetchWeather(loc)
            assessmentState = result
            isLoading = false
        }
    }

    LaunchedEffect(selectedLocation) {
        refreshWeather(selectedLocation)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // En-tête Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Météo & Conservation des Grains",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Prévention dégradation & hygrométrie Guide 2026",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sélecteur de Comptoir / Région Agricole
                ScrollableTabRow(
                    selectedTabIndex = WeatherService.LOCATIONS.indexOf(selectedLocation),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = AgroGreenPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WeatherService.LOCATIONS.forEachIndexed { index, loc ->
                        Tab(
                            selected = selectedLocation == loc,
                            onClick = { selectedLocation = loc },
                            text = {
                                Text(
                                    text = loc.nameFr.substringBefore(" ("),
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedLocation == loc) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading || assessmentState == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = AgroGreenPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Connexion satellite & relevé hygrométrique en cours...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val assessment = assessmentState!!
                    val weather = assessment.weather

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Carte Météo Temps Réel
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = selectedLocation.nameFr,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = selectedLocation.region + " • " + weather.recordedAt,
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }

                                        Surface(
                                            color = if (weather.isLiveFromNetwork) Color(0xFF166534) else Color(0xFF9A3412),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (weather.isLiveFromNetwork) Color(0xFF4ADE80) else Color(0xFFFDBA74))
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (weather.isLiveFromNetwork) "Direct API" else "Guide 2026",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Température & Ciel
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                if (weather.precipitationMm > 0) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                                                contentDescription = null,
                                                tint = if (weather.precipitationMm > 0) Color(0xFF38BDF8) else Color(0xFFFBBF24),
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "${weather.temperatureC}°C",
                                                    color = Color.White,
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = weather.conditionDescription,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        // Humidité Relative & Point de rosée
                                        Column(horizontalAlignment = Alignment.End) {
                                            Surface(
                                                color = if (weather.relativeHumidityPct > 72.0) Color(0xFF7F1D1D) else Color(0xFF1E293B),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.WaterDrop,
                                                        contentDescription = null,
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${weather.relativeHumidityPct}% HR",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Point de rosée: ${weather.dewPointC}°C",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Décision Aération Psychrométrique
                        item {
                            val aeration = assessment.aerationRecommendation
                            val isAllowed = aeration.canOpenAerationNow

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAllowed) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isAllowed) Icons.Default.Air else Icons.Default.Block,
                                            contentDescription = null,
                                            tint = if (isAllowed) Color(0xFF166534) else Color(0xFF991B1B),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = aeration.statusHeadlineFr,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isAllowed) Color(0xFF166534) else Color(0xFF991B1B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = aeration.reasonFr,
                                        fontSize = 11.5.sp,
                                        color = if (isAllowed) Color(0xFF14532D) else Color(0xFF7F1D1D)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⏰ Créneau recommandé : ${aeration.safeAerationWindow}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllowed) Color(0xFF166534) else Color(0xFF991B1B)
                                    )
                                }
                            }
                        }

                        // Risque par Zone de Stockage Entrepôt
                        item {
                            Text(
                                text = "Évaluation des Risques par Zone & Produit :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        items(assessment.zoneRisks) { zoneRisk ->
                            val statusBg = when (zoneRisk.riskLevel) {
                                StorageRiskLevel.OPTIMAL -> Color(0xFFF0FDF4)
                                StorageRiskLevel.VIGILANCE -> Color(0xFFFFFBEB)
                                StorageRiskLevel.DANGER_CRITIQUE -> Color(0xFFFEF2F2)
                            }
                            val borderCol = Color(zoneRisk.riskLevel.severityColorHex)

                            Card(
                                colors = CardDefaults.cardColors(containerColor = statusBg),
                                shape = RoundedCornerShape(12.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderCol)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = zoneRisk.zoneName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = borderCol
                                        )
                                        Surface(
                                            color = borderCol,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = zoneRisk.riskLevel.labelFr,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Produits: ${zoneRisk.mainProducts}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Seuil critique toléré: < ${zoneRisk.maxAllowedAmbientHumidityPct}% HR (Actuel: ${zoneRisk.currentAmbientHumidityPct}%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (zoneRisk.currentAmbientHumidityPct > zoneRisk.maxAllowedAmbientHumidityPct) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚠️ Risques de dégradation :",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF78350F)
                                    )
                                    zoneRisk.degradationRisks.forEach { r ->
                                        Text(
                                            text = "• $r",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(start = 4.dp, top = 1.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🛡️ Actions préventives immédiates (Guide 2026) :",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AgroGreenPrimary
                                    )
                                    zoneRisk.preventiveActions.forEach { a ->
                                        Text(
                                            text = "✓ $a",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF0F172A),
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(start = 4.dp, top = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { refreshWeather(selectedLocation) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Actualiser relevés", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Appliquer Recommandations", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
