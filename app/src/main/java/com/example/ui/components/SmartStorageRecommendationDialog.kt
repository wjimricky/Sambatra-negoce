package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.ai.LotStorageRequirement
import com.example.data.ai.SmartStorageRecommender
import com.example.data.model.BatchLot
import com.example.data.model.WarehouseZoneId
import com.example.ui.theme.*

@Composable
fun SmartStorageRecommendationDialog(
    initialLot: BatchLot? = null,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(if (initialLot?.productName?.contains("Girofle") == true || initialLot?.productName?.contains("Poivre") == true) "Épices" else "Céréales") }
    var productName by remember { mutableStateOf(initialLot?.productName ?: "Riz blanc usiné Makalioka") }
    var quantityKgText by remember { mutableStateOf(initialLot?.quantityKg?.toInt()?.toString() ?: "5000") }
    var bagsCountText by remember { mutableStateOf(initialLot?.bagsCount?.toString() ?: "100") }
    var moistureText by remember { mutableStateOf(initialLot?.moisturePercent?.toString() ?: "12.8") }
    var isCompliant by remember { mutableStateOf(initialLot?.decision?.name != "REFUSE" && initialLot?.decision?.name != "QUARANTAINE") }
    var abcClass by remember { mutableStateOf("A") }

    val qty = quantityKgText.toDoubleOrNull() ?: 5000.0
    val bags = bagsCountText.toIntOrNull() ?: 100
    val moisture = moistureText.toDoubleOrNull() ?: 12.8

    val requirement = remember(selectedCategory, productName, qty, bags, moisture, isCompliant, abcClass) {
        val isSpice = selectedCategory == "Épices"
        val maxMoisture = if (isSpice) 12.0 else 13.5
        LotStorageRequirement(
            lotNumber = initialLot?.lotNumber ?: "LOT-SIM-2026",
            productCode = if (isSpice) "GIR" else "RIZ",
            productName = productName,
            category = selectedCategory,
            quantityKg = qty,
            bagsCount = bags,
            moisturePercent = moisture,
            maxMoistureTolerancePercent = maxMoisture,
            abcClass = abcClass,
            hasStrongAromaticOdor = isSpice,
            isQualityCompliant = isCompliant,
            clientLeadDays = 15
        )
    }

    val recommendation = remember(requirement) {
        SmartStorageRecommender.recommendStorageSlot(requirement)
    }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = AgroGreenPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Recommandation Stockage IA",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                            Text(
                                text = "Emplacement intelligent & zonage 400 m² (Guide 2026)",
                                fontSize = 11.sp,
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
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Carte Paramètres du Lot
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Caractéristiques du Lot à Entreposer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Catégorie
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Céréales", "Épices", "Légumineuses").forEach { cat ->
                                        val isSelected = selectedCategory == cat
                                        Surface(
                                            color = if (isSelected) AgroGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedCategory = cat
                                                    productName = when (cat) {
                                                        "Céréales" -> "Riz blanc usiné (Makalioka)"
                                                        "Épices" -> "Girofle Clous CG3 Trié"
                                                        else -> "Haricot sec rouge"
                                                    }
                                                }
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = cat,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = quantityKgText,
                                        onValueChange = { quantityKgText = it },
                                        label = { Text("Poids (kg)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = bagsCountText,
                                        onValueChange = { bagsCountText = it },
                                        label = { Text("Nb Sacs", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = moistureText,
                                        onValueChange = { moistureText = it },
                                        label = { Text("Humidité (%)", fontSize = 10.sp) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Qualité conforme :", fontSize = 11.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Switch(
                                            checked = isCompliant,
                                            onCheckedChange = { isCompliant = it },
                                            modifier = Modifier.height(24.dp)
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Rotation ABC :", fontSize = 11.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        listOf("A", "B", "C").forEach { cls ->
                                            Surface(
                                                color = if (abcClass == cls) AgroGreenPrimary else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp)
                                                    .clickable { abcClass = cls }
                                            ) {
                                                Text(
                                                    text = cls,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (abcClass == cls) Color.White else Color.DarkGray,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Plan interactif 2D du Zonage Entrepôt 400 m²
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Plan d'Entrepôt 400 m² & Emplacement Suggéré",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AgroGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Warehouse2DPlanView(recommendedZone = recommendation.recommendedZone)
                            }
                        }
                    }

                    // Recommandation IA Détaillée
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(14.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
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
                                            text = recommendation.zoneLabel,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = AgroGreenPrimary
                                        )
                                        Text(
                                            text = "Emplacement précis : ${recommendation.specificAisleAndPalletId}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Surface(
                                        color = AgroGreenPrimary,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${recommendation.suitabilityScorePercent}% Match",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Avertissement Sanitaire ou Isolation
                                if (recommendation.sanitaryRiskWarning != null) {
                                    Surface(
                                        color = Color(0xFFFEF2F2),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = recommendation.sanitaryRiskWarning,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // 4 Règles de Gerbage Guide 2026
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("📦 Directives d'Empilage & Conservation (Guide p. 43) :", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E293B))
                                        RuleRow("Hauteur max de la pile :", "${recommendation.stackingHeightMaxLayers} sacs de haut maximum")
                                        RuleRow("Élévation au sol :", "Palettes de ${recommendation.palletClearanceCm} cm (aucun sac au sol)")
                                        RuleRow("Distance des murs :", "Espace d'inspection de ${recommendation.wallSpacingCm} cm")
                                        RuleRow("Dégagement sous toiture :", "${recommendation.roofClearanceCm} cm sous les tôles (anti-chaleur)")
                                        RuleRow("Gestion des flux :", recommendation.rotationStrategy)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Explication IA
                                Text(
                                    text = recommendation.aiReasoningExplanation,
                                    fontSize = 11.sp,
                                    color = Color(0xFF166534),
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = recommendation.guide2026Reference,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFF15803D),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
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
                    Text("Valider l'Assignation d'Emplacement", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun Warehouse2DPlanView(recommendedZone: WarehouseZoneId) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Rangée Haut : Zone A (200 m²)
        val isA = recommendedZone == WarehouseZoneId.ZONE_A
        ZoneBox(
            title = "ZONE A : Céréales & Grands Volumes (200 m² - Riz & Maïs)",
            capacity = "Capacité : 4 000 sacs • Travées 20m avec allées transpalette",
            isSelected = isA,
            borderColor = if (isA) AgroGreenPrimary else Color(0xFFCBD5E1),
            bgColor = if (isA) Color(0xFFDCFCE7) else Color.White
        )

        // Rangée Bas : Zone B, Zone C, Zone Q
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isB = recommendedZone == WarehouseZoneId.ZONE_B
            ZoneBox(
                title = "ZONE B (60 m²)\nÉpices & Noble",
                capacity = "1200 sacs • Fermée sous clé",
                isSelected = isB,
                borderColor = if (isB) SpiceOrangeSecondary else Color(0xFFCBD5E1),
                bgColor = if (isB) Color(0xFFFEF3C7) else Color.White,
                modifier = Modifier.weight(1.2f)
            )

            val isC = recommendedZone == WarehouseZoneId.ZONE_C
            ZoneBox(
                title = "ZONE C (50 m²)\nLégumineuses",
                capacity = "1000 sacs • FEFO",
                isSelected = isC,
                borderColor = if (isC) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                bgColor = if (isC) Color(0xFFE0F2FE) else Color.White,
                modifier = Modifier.weight(1.0f)
            )

            val isQ = recommendedZone == WarehouseZoneId.ZONE_Q
            ZoneBox(
                title = "ZONE Q (30 m²)\nQuarantaine",
                capacity = "600 sacs • Litiges",
                isSelected = isQ,
                borderColor = if (isQ) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                bgColor = if (isQ) Color(0xFFFEE2E2) else Color.White,
                modifier = Modifier.weight(0.9f)
            )
        }
    }
}

@Composable
private fun ZoneBox(
    title: String,
    capacity: String,
    isSelected: Boolean,
    borderColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFF475569)
                )
                if (isSelected) {
                    Surface(
                        color = borderColor,
                        shape = CircleShape,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = capacity,
                fontSize = 8.5.sp,
                color = Color.Gray,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun RuleRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 10.5.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
    }
}
