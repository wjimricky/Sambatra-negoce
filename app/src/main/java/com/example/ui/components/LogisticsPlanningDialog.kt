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
import com.example.data.logistics.*
import com.example.ui.theme.*

@Composable
fun LogisticsPlanningDialog(
    onDismiss: () -> Unit
) {
    var selectedVehicleId by remember { mutableStateOf(LogisticsOptimizer.VEHICLES[0].id) }
    var selectedCorridorId by remember { mutableStateOf(LogisticsOptimizer.CORRIDORS[0].id) }
    var selectedOrderIds by remember { mutableStateOf(setOf("CMD-001", "CMD-002")) }
    var checkedChecklistIds by remember { mutableStateOf(setOf("chk-1", "chk-2")) }
    var showSuccessConfirmation by remember { mutableStateOf(false) }

    val tripPlan = remember(selectedVehicleId, selectedCorridorId, selectedOrderIds) {
        LogisticsOptimizer.buildOptimizedTrip(
            vehicleId = selectedVehicleId,
            corridorId = selectedCorridorId,
            selectedOrderIds = selectedOrderIds.toList()
        )
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
                            color = SpiceOrangeSecondary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = SpiceOrangeSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Planification Logistique & Trajets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Optimisation charge, freinte & compatibilité (Guide 2026)",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Étape 1 : Choix du Véhicule
                    item {
                        Text(
                            text = "1. Sélection du Véhicule de Transport :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LogisticsOptimizer.VEHICLES.forEach { veh ->
                                val isSelected = veh.id == selectedVehicleId
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) AgroGreenPrimary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) AgroGreenPrimary else Color(0xFFCBD5E1))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedVehicleId = veh.id }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedVehicleId = veh.id },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = veh.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    color = if (isSelected) AgroGreenPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "Charge max: ${veh.maxPayloadKg.toInt()} kg • Volume: ${veh.maxVolumeM3} m³ • Chauffeur: ${veh.driverName}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                                            )
                                        }

                                        if (veh.hasInsulatedCompartment) {
                                            Surface(
                                                color = Color(0xFFE0E7FF),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "Isotherme",
                                                    color = Color(0xFF3730A3),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Étape 2 : Choix du Corridor Routier
                    item {
                        Text(
                            text = "2. Axe Routier & Destination :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LogisticsOptimizer.CORRIDORS.forEach { corridor ->
                                val isSelected = corridor.id == selectedCorridorId
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) SpiceOrangeSecondary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) SpiceOrangeSecondary else Color(0xFFCBD5E1))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedCorridorId = corridor.id }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedCorridorId = corridor.id },
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = corridor.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) SpiceOrangeSecondary else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Text(
                                                text = "${corridor.distanceKm.toInt()} km • ~${corridor.typicalDurationHours}h",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                        Text(
                                            text = "⚠️ Risque climatique : ${corridor.climaticRisk} • Freinte moyenne: ${corridor.baselineRoadShrinkagePct}%",
                                            fontSize = 10.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Étape 3 : Sélection des Commandes / Lots
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. Lots à Charger dans la Benne :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${selectedOrderIds.size}/${LogisticsOptimizer.INITIAL_PENDING_ORDERS.size} sélectionnés",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            LogisticsOptimizer.INITIAL_PENDING_ORDERS.forEach { order ->
                                val isChecked = selectedOrderIds.contains(order.id)
                                Surface(
                                    color = if (isChecked) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(if (isChecked) AgroGreenPrimary else Color(0xFFE2E8F0))
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedOrderIds = if (isChecked) selectedOrderIds - order.id else selectedOrderIds + order.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                selectedOrderIds = if (it) selectedOrderIds + order.id else selectedOrderIds - order.id
                                            },
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = order.productName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF0F172A)
                                                )
                                                if (order.isSpice) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Color(0xFFF3E8FF),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Épice",
                                                            color = Color(0xFF7E22CE),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                                if (order.isPriorityFefo) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = Color(0xFFFEF3C7),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "FEFO",
                                                            color = Color(0xFFB45309),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${order.clientName} (${order.destinationCity}) • ${order.weightKg.toInt()} kg (${order.volumeM3} m³) • Hum: ${order.moistureAtDeparturePct}%",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Étape 4 : Diagnostic & Résultat de l'Optimisation
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Bilan de Chargement & Freinte Routière (Guide 2026)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Jauge de charge utile
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Charge Utile : ${tripPlan.totalWeightKg.toInt()} / ${tripPlan.vehicle.maxPayloadKg.toInt()} kg",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = "${tripPlan.weightUtilizationPct}%",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tripPlan.weightUtilizationPct > 100) Color(0xFFEF4444) else Color(0xFF34D399)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (tripPlan.weightUtilizationPct / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = if (tripPlan.weightUtilizationPct > 100) Color(0xFFEF4444) else Color(0xFF34D399),
                                    trackColor = Color(0xFF334155)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Calcul de la Freinte de transport (Guide 2026 p. 50)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Perte estimée au transport (Freinte) :",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "${tripPlan.estimatedRoadShrinkageKg} kg (~${String.format("%,d", tripPlan.estimatedRoadShrinkageCostAr.toLong())} Ar)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Coût carburant trajet :",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "${String.format("%,d", tripPlan.transportCostAr.toLong())} Ar",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Alertes de compatibilité Guide 2026
                    if (tripPlan.compatibilityWarnings.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                tripPlan.compatibilityWarnings.forEach { warn ->
                                    val (bg, border, textCol) = when (warn.level) {
                                        WarningSeverity.CRITICAL_BLOCKING -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFF991B1B))
                                        WarningSeverity.WARNING -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFF92400E))
                                        WarningSeverity.INFO -> Triple(Color(0xFFF0FDF4), Color(0xFF16A34A), Color(0xFF166534))
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = bg),
                                        shape = RoundedCornerShape(10.dp),
                                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(border)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    if (warn.level == WarningSeverity.CRITICAL_BLOCKING) Icons.Default.Dangerous else Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = border,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = warn.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = textCol
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = warn.detail,
                                                fontSize = 11.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "💡 Remède Guide 2026 : ${warn.recommendedRemedy}",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textCol
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Checklist de conformité départ (Guide 2026)
                    item {
                        Text(
                            text = "Protocole Anti-Pertes au Départ (Guide 2026) :",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            tripPlan.lossMitigationChecklist.forEach { chk ->
                                val isChecked = checkedChecklistIds.contains(chk.id)
                                Surface(
                                    color = if (isChecked) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            checkedChecklistIds = if (isChecked) checkedChecklistIds - chk.id else checkedChecklistIds + chk.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                checkedChecklistIds = if (it) checkedChecklistIds + chk.id else checkedChecklistIds - chk.id
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = chk.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = chk.guideReference,
                                                fontSize = 9.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showSuccessConfirmation) {
                        item {
                            Surface(
                                color = Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF166534))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Feuille de route logistique validée ! Départ calé à ${tripPlan.departureTime} (Heure fraîche).",
                                        color = Color(0xFF166534),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
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
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showSuccessConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SpiceOrangeSecondary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = tripPlan.weightUtilizationPct <= 100.0 && tripPlan.orders.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Valider la Feuille de Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
