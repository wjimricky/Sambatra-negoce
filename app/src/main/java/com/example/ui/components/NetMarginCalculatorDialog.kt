package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.finance.NetMarginCalculator
import com.example.data.finance.NetMarginInputs
import com.example.data.finance.ProfitabilityRating
import com.example.data.model.Product
import com.example.ui.theme.*

@Composable
fun NetMarginCalculatorDialog(
    initialProduct: Product? = null,
    onDismiss: () -> Unit
) {
    var quantityKgText by remember { mutableStateOf(if (initialProduct != null) "2000" else "5000") }
    var purchasePriceText by remember { mutableStateOf(initialProduct?.cmupCostAr?.toInt()?.toString() ?: "2080") }
    var sellingPriceText by remember { mutableStateOf(initialProduct?.wholesalePriceAr?.toInt()?.toString() ?: "2300") }
    var transportDistanceText by remember { mutableStateOf("120") }
    var storageDaysText by remember { mutableStateOf("25") }
    var fuelRateText by remember { mutableStateOf("4500") }
    var observedLossPercentText by remember { mutableStateOf("0.50") }

    val qty = quantityKgText.toDoubleOrNull() ?: 5000.0
    val buyPrice = purchasePriceText.toDoubleOrNull() ?: 2080.0
    val sellPrice = sellingPriceText.toDoubleOrNull() ?: 2300.0
    val distKm = transportDistanceText.toDoubleOrNull() ?: 120.0
    val storageDays = storageDaysText.toIntOrNull() ?: 25
    val fuelRate = fuelRateText.toDoubleOrNull() ?: 4500.0
    val lossPct = observedLossPercentText.toDoubleOrNull() ?: 0.50

    val breakdown = remember(qty, buyPrice, sellPrice, distKm, storageDays, fuelRate, lossPct) {
        val inputs = NetMarginInputs(
            productCode = initialProduct?.code ?: "RIZ",
            productName = initialProduct?.name ?: "Riz blanc usiné (Makalioka)",
            lotNumber = "SIM-LOT-2026",
            quantityKg = qty,
            purchasePricePerKgAr = buyPrice,
            sellingPricePerKgAr = sellPrice,
            transportDistanceKm = distKm,
            fuelRatePerKmAr = fuelRate,
            holdingDurationDays = storageDays,
            observedLossPercent = lossPct
        )
        NetMarginCalculator.calculateNetMargin(inputs)
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
                            color = AgroGreenPrimary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Calculateur de Marge Nette en Temps Réel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Prix de revient complet & frais logistiques fluctuants (Guide 2026)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    // Carte Paramètres d'entrée
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Paramètres Fluctuants de la Transaction :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = quantityKgText,
                                        onValueChange = { quantityKgText = it },
                                        label = { Text("Quantité (kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = storageDaysText,
                                        onValueChange = { storageDaysText = it },
                                        label = { Text("Stockage (jours)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = purchasePriceText,
                                        onValueChange = { purchasePriceText = it },
                                        label = { Text("Prix Achat (Ar/kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = sellingPriceText,
                                        onValueChange = { sellingPriceText = it },
                                        label = { Text("Prix Vente (Ar/kg)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = transportDistanceText,
                                        onValueChange = { transportDistanceText = it },
                                        label = { Text("Distance Fret (km)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = observedLossPercentText,
                                        onValueChange = { observedLossPercentText = it },
                                        label = { Text("Freinte (%)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Carte Résultat Marge Nette
                    item {
                        val ratingColor = Color(breakdown.profitabilityRating.colorHex)
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
                                        Text("Marge Bénéficiaire Nette", color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                                        Text(
                                            text = "${String.format("%,d", breakdown.netMarginAr.toLong())} Ar",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Surface(
                                        color = ratingColor,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${breakdown.netMarginPercent}% (${breakdown.profitabilityRating.labelFr})",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Prix de revient complet (PRC) :", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text("${breakdown.costPricePerKgAr} Ar/kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Marge nette unitaire :", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                                    Text("${breakdown.netMarginPerKgAr} Ar/kg (Cible: ${breakdown.targetMarginPercent}%)", color = ratingColor, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }

                    // Décomposition des Coûts selon Guide 2026
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Décomposition Détaillée des Frais Fluctuants :", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                CostRowItem("Coût d'achat brut fournisseur", breakdown.purchaseCostAr, Color(0xFF334155))
                                CostRowItem("Fret routier & carburant (${distKm.toInt()} km)", breakdown.transportCostAr, SpiceOrangeSecondary)
                                CostRowItem("Magasinage & entreposage (${storageDays}j)", breakdown.warehouseHoldingCostAr, Color(0xFF0284C7))
                                CostRowItem("Sacs & emballage polypropylène", breakdown.packagingCostAr, Color(0xFF475569))
                                CostRowItem("Perte de matière par freinte (${lossPct}%)", breakdown.lossAndShrinkageCostAr, Color(0xFFDC2626))
                                CostRowItem("Coût financier immobilisation capital", breakdown.financialHoldingCostAr, Color(0xFF7C3AED))
                                CostRowItem("Charges fixes de structure réparties", breakdown.fixedOverheadCostAr, Color(0xFF64748B))

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Chiffre d'Affaires Net Facturable", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                    Text("${String.format("%,d", breakdown.totalRevenueAr.toLong())} Ar", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                }
                            }
                        }
                    }

                    // Recommandation stratégique Guide 2026
                    item {
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = breakdown.strategicAdvice,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 16.sp
                                )
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
                    Text("Fermer le Calculateur", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CostRowItem(label: String, amountAr: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color(0xFF475569))
        Text("${String.format("%,d", amountAr.toLong())} Ar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
