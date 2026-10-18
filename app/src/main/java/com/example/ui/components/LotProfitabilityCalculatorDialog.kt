package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.theme.SpiceOrangeSecondary
import com.example.ui.theme.StatusGreenOk
import com.example.ui.theme.StatusRedDanger
import java.util.Locale

@Composable
fun LotProfitabilityCalculatorDialog(
    initialProduct: Product,
    onDismiss: () -> Unit
) {
    var quantityKgText by remember { mutableStateOf("10000") }
    var purchasePriceText by remember { mutableStateOf(initialProduct.cmupCostAr.toInt().toString()) }
    var storageLossPercentText by remember { mutableStateOf("1.5") }
    var handlingCostText by remember { mutableStateOf("35") }
    var storageDaysText by remember { mutableStateOf("45") }
    var deliveryCostText by remember { mutableStateOf("60") }
    var salesCostText by remember { mutableStateOf("30") }
    var targetMarginPercentText by remember { mutableStateOf(initialProduct.marginPercent.toInt().toString()) }

    val qty = quantityKgText.toDoubleOrNull() ?: 10000.0
    val purchasePrice = purchasePriceText.toDoubleOrNull() ?: 2916.0
    val lossPct = storageLossPercentText.toDoubleOrNull() ?: 1.5
    val handling = handlingCostText.toDoubleOrNull() ?: 35.0
    val days = storageDaysText.toIntOrNull() ?: 45
    val delivery = deliveryCostText.toDoubleOrNull() ?: 60.0
    val sales = salesCostText.toDoubleOrNull() ?: 30.0
    val targetMargin = targetMarginPercentText.toDoubleOrNull() ?: 14.0

    // Formules exactes du Chapitre 7.1 (p. 36)
    val storageLossAr = purchasePrice * (lossPct / 100.0)
    val financingCostAr = purchasePrice * ((1.5 / 100.0) * (days / 30.0)) // 1.5% par mois
    val totalVariableCostPerKg = purchasePrice + storageLossAr + handling + financingCostAr + delivery + sales

    // Prix de vente pour marge visée m : Coût / (1 - m)
    val marginFraction = (targetMargin / 100.0).coerceIn(0.01, 0.90)
    val recommendedSalePricePerKg = totalVariableCostPerKg / (1.0 - marginFraction)
    val unitMarginAr = recommendedSalePricePerKg - totalVariableCostPerKg
    val totalLotMarginAr = unitMarginAr * qty
    val equivalentMarkup = (unitMarginAr / totalVariableCostPerKg) * 100.0 // Taux de majoration

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = SpiceOrangeSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Calcul de Rentabilité par Lot",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Text(
                        text = "Méthode du prix de revient complet (Guide Ch. 7.1, p. 36)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Produit sélectionné : ${initialProduct.name}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgroGreenPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityKgText,
                        onValueChange = { quantityKgText = it },
                        label = { Text("Volume (kg)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it },
                        label = { Text("Prix achat (Ar/kg)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = storageDaysText,
                        onValueChange = { storageDaysText = it },
                        label = { Text("Stockage (jours)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetMarginPercentText,
                        onValueChange = { targetMarginPercentText = it },
                        label = { Text("Marge visée (%)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = handlingCostText,
                        onValueChange = { handlingCostText = it },
                        label = { Text("Manutention (Ar/kg)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deliveryCostText,
                        onValueChange = { deliveryCostText = it },
                        label = { Text("Livraison (Ar/kg)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Synthèse détaillée des coûts
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Décomposition du prix de revient :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Prix achat rendu entrepôt : ${purchasePrice.toInt()} Ar/kg", fontSize = 11.sp)
                        Text("• Pertes stockage ($lossPct%) : ${storageLossAr.toInt()} Ar/kg", fontSize = 11.sp)
                        Text("• Manutention directe : ${handling.toInt()} Ar/kg", fontSize = 11.sp)
                        Text("• Financement ($days j à 1,5%/m) : ${financingCostAr.toInt()} Ar/kg", fontSize = 11.sp)
                        Text("• Livraison & frais commerciaux : ${(delivery + sales).toInt()} Ar/kg", fontSize = 11.sp)

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Prix de revient variable complet :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("${totalVariableCostPerKg.toInt()} Ar/kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Carte Résultats & Prix de vente recommandé
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prix de vente recommandé :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "${recommendedSalePricePerKg.toInt()} Ar/kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF166534)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Marge unitaire nette :", fontSize = 12.sp)
                            Text("${unitMarginAr.toInt()} Ar/kg ($targetMargin%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gain total sur le lot (${(qty / 1000).toInt()} t) :", fontSize = 12.sp)
                            Text(
                                "${String.format(Locale.US, "%.2f", totalLotMarginAr / 1_000_000.0)} Millions Ar",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Avertissement Marge vs Majoration (p. 36-37)
                Surface(
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Règle du guide : Taux de marge $targetMargin% = Majoration de +${String.format(Locale.US, "%.1f", equivalentMarkup)}% sur le coût ! Ne jamais faire l'erreur d'ajouter seulement $targetMargin% au coût.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary)
            ) {
                Text("Fermer")
            }
        }
    )
}
