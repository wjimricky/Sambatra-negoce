package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.sustainability.CarbonCalculationInputs
import com.example.data.sustainability.CarbonFootprintCalculator
import com.example.ui.theme.*

@Composable
fun CarbonFootprintCalculatorDialog(
    onDismiss: () -> Unit
) {
    var selectedProductCode by remember { mutableStateOf("RIZ") }
    var selectedVehicleType by remember { mutableStateOf("CAMION_10T") }
    var quantityText by remember { mutableStateOf("4000") }
    var distanceKmText by remember { mutableStateOf("260") } // ex: Tamatave ou Alaotra
    var storageDaysText by remember { mutableStateOf("30") }
    var payloadUtilizationText by remember { mutableStateOf("85") }

    val qty = quantityText.toDoubleOrNull() ?: 4000.0
    val dist = distanceKmText.toDoubleOrNull() ?: 260.0
    val days = storageDaysText.toIntOrNull() ?: 30
    val payloadPct = payloadUtilizationText.toDoubleOrNull() ?: 85.0

    val assessment = remember(selectedProductCode, selectedVehicleType, qty, dist, days, payloadPct) {
        val inputs = CarbonCalculationInputs(
            productCode = selectedProductCode,
            productName = when (selectedProductCode) {
                "RIZ" -> "Riz blanc usiné (Makalioka)"
                "HAR" -> "Haricot sec (Tsipoy)"
                "MAI" -> "Maïs grain sec"
                "PDC" -> "Pois du Cap (Kabaro)"
                "GIR" -> "Clous de Girofle (CG1)"
                "POI" -> "Poivre noir Voatsiperifery"
                else -> "Produit Agro-Négoce"
            },
            quantityKg = qty,
            transportDistanceKm = dist,
            vehicleType = selectedVehicleType,
            vehiclePayloadUtilizationPercent = payloadPct,
            storageDurationDays = days
        )
        CarbonFootprintCalculator.calculateFootprint(inputs)
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
                // En-tête
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF16A34A).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Calculateur d'Impact Carbone & Durabilité", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("Objectifs éco-responsables du Guide 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Carte Paramètres Véhicule & Produit
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Filière & Véhicule de Transport :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)

                                // Sélecteur produit
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("RIZ" to "Riz", "HAR" to "Haricot", "MAI" to "Maïs", "GIR" to "Girofle", "POI" to "Poivre").forEach { (code, label) ->
                                        FilterChip(
                                            selected = selectedProductCode == code,
                                            onClick = { selectedProductCode = code },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AgroGreenPrimary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                // Sélecteur véhicule
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("CAMION_10T" to "Camion 10T", "CAMIONNETTE_3T5" to "Fourgon 3.5T", "PICKUP_1T2" to "Pick-up 1.2T").forEach { (vType, label) ->
                                        FilterChip(
                                            selected = selectedVehicleType == vType,
                                            onClick = { selectedVehicleType = vType },
                                            label = { Text(label, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = SpiceOrangeSecondary,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = quantityText,
                                        onValueChange = { quantityText = it },
                                        label = { Text("Poids du lot (kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = distanceKmText,
                                        onValueChange = { distanceKmText = it },
                                        label = { Text("Distance (km)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = payloadUtilizationText,
                                        onValueChange = { payloadUtilizationText = it },
                                        label = { Text("Remplissage benne (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = storageDaysText,
                                        onValueChange = { storageDaysText = it },
                                        label = { Text("Durée stockage (jours)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Carte Score Carbone & Éco-Score
                    item {
                        val scoreColor = Color(assessment.ecoScore.colorHex)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Émissions Totales du Trajet & Stockage", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                                        Text(
                                            text = "${assessment.totalEmissionKgCo2e} kg CO₂e",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Surface(
                                        color = scoreColor,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "Eco-Score ${assessment.ecoScore.letter}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Intensité unitaire :", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text(
                                        text = "${assessment.emissionPerKgDeliveredGramsCo2e} g CO₂e / kg (Cible Guide: < ${assessment.carbonBenchmarkGuide2026.toInt()} g)",
                                        color = if (assessment.isCompliantWith2026Target) Color(0xFF4ADE80) else Color(0xFFF87171),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Décomposition des postes d'émission
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Répartition de l'Empreinte Carbone :", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                EmissionPostRow("Transport routier & carburant", assessment.transportEmissionKgCo2e, assessment.totalEmissionKgCo2e, SpiceOrangeSecondary)
                                EmissionPostRow("Séchage & aération entrepôt", assessment.storageAndDryingEmissionKgCo2e, assessment.totalEmissionKgCo2e, AgroGreenPrimary)
                                EmissionPostRow("Emballages et conditionnement", assessment.packagingEmissionKgCo2e, assessment.totalEmissionKgCo2e, Color(0xFF64748B))
                            }
                        }
                    }

                    // Pistes d'optimisation bas-carbone (Guide 2026)
                    item {
                        Text("Leviers d'Optimisation Bas-Carbone (Guide 2026) :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    items(assessment.carbonReductionPotentials) { rec ->
                        Surface(
                            color = Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rec.title, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF166534))
                                    Surface(color = AgroGreenPrimary, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = "-${rec.potentialSavingsKgCo2e} kg CO₂e",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(rec.actionGuide2026, fontSize = 11.sp, color = Color(0xFF14532D))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fermer l'Outil Carbone", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun EmissionPostRow(label: String, kgCo2e: Double, totalKg: Double, color: Color) {
    val pct = if (totalKg > 0) ((kgCo2e / totalKg) * 100.0).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF475569))
        Text("${kgCo2e} kg CO₂e ($pct%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
