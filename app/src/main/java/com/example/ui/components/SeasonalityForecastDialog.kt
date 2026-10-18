package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.data.forecasting.HarvestSeasonalityEngine
import com.example.data.forecasting.ReplenishmentAlertPriority
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SeasonalityForecastDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var selectedCropCode by remember { mutableStateOf("RIZ") }
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()

    val cropCalendar = remember(selectedCropCode) {
        HarvestSeasonalityEngine.HARVEST_CALENDARS.find { it.productCode == selectedCropCode }
            ?: HarvestSeasonalityEngine.HARVEST_CALENDARS[0]
    }

    val priceForecast = remember(selectedCropCode) {
        val basePrice = when (selectedCropCode) {
            "RIZ" -> 2300.0
            "HAR" -> 3660.0
            "MAI" -> 1550.0
            "PDC" -> 4050.0
            "GIR" -> 35900.0
            "POI" -> 23000.0
            else -> 2500.0
        }
        HarvestSeasonalityEngine.generate12MonthPriceForecast(selectedCropCode, basePrice)
    }

    // Extraction des entités pour calcul des alertes
    val alerts = remember(products, lots) {
        // Conversion des modèles de domaine pour évaluation des alertes de réapprovisionnement
        val demoProduits = products.map { p ->
            com.example.data.local.room.ProduitEntity(
                id = p.id,
                code = p.code,
                nom = p.name,
                nomMalagasy = p.nameMg,
                type = p.type.name,
                classeAbc = p.abcClass.name,
                variete = "",
                cmupCostAr = p.cmupCostAr,
                prixVenteGrosAr = p.wholesalePriceAr,
                consoJournaliereMoyenneKg = p.dailyConsumptionKg,
                stockSecuriteKg = p.dailyConsumptionKg * 14,
                pointDeCommandeKg = p.dailyConsumptionKg * 24,
                seuilHumiditeMaxPercent = p.maxMoisturePercent,
                formatConditionnement = "Sacs",
                zoneEntrepot = "ZONE_A",
                risquesSpecifiques = ""
            )
        }
        val demoLots = lots.map { l ->
            com.example.data.local.room.StockLotEntity(
                numeroLot = l.lotNumber,
                produitId = l.productId,
                fournisseurId = "f",
                nomFournisseur = l.supplierName,
                regionOrigine = l.originRegion,
                dateReception = l.receptionDate,
                quantiteInitialeKg = l.quantityKg,
                quantiteDisponibleKg = l.netReceivedKg,
                nombreSacs = l.bagsCount,
                nombreSacsPeses = l.weighedBagsCount,
                humiditeMesureePercent = l.moisturePercent,
                impuretesPercent = l.impuritiesPercent,
                decisionControle = l.decision.name,
                identifiantContreEchantillonScelle = l.sealedCounterSampleId,
                zoneStockageEntrepot = l.warehouseZone,
                coutUnitaireAchatAr = l.unitCostAr,
                dateLimiteConservation = "",
                observationsControle = ""
            )
        }
        HarvestSeasonalityEngine.evaluateReplenishmentAlerts(demoProduits, demoLots, currentMonth = 10)
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
                            color = SpiceOrangeSecondary.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Saisonnalité & Réapprovisionnement Automatique", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("Cycles de récolte, cours de soudure & seuils Guide 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    // Sélecteur Produit
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("RIZ" to "Riz (Alaotra)", "HAR" to "Haricot (Tsipoy)", "MAI" to "Maïs", "PDC" to "Pois du Cap", "GIR" to "Girofle (CG1)").forEach { (code, label) ->
                                FilterChip(
                                    selected = selectedCropCode == code,
                                    onClick = { selectedCropCode = code },
                                    label = { Text(label, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AgroGreenPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Carte Calendrier de la Récolte
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(cropCalendar.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgroGreenPrimary)
                                Text("Bassin de production : ${cropCalendar.mainProductionBasin}", fontSize = 11.sp, color = Color(0xFF475569))

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "Récolte: Mois ${cropCalendar.harvestStartMonth} à ${cropCalendar.harvestEndMonth} (${cropCalendar.seasonalPriceDipPercent}%)",
                                            color = Color(0xFF166534),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "Soudure: Mois ${cropCalendar.leanSeasonStartMonth} à ${cropCalendar.leanSeasonEndMonth} (+${cropCalendar.seasonalPriceSurgePercent}%)",
                                            color = Color(0xFF991B1B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "⚠️ Risque hygrométrique à l'achat : ${cropCalendar.postHarvestMoistureRisk}",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    // Prévision des Prix sur 12 Mois
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Trajectoire Prévisionnelle des Prix Saisonniers (Guide 2026) :", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    priceForecast.forEach { month ->
                                        val isCurrent = month.monthNumber == 10 // Octobre
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = month.monthLabel,
                                                color = if (isCurrent) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            month.isHarvestPeriod -> Color(0xFF4ADE80)
                                                            month.isLeanSeason -> Color(0xFFF87171)
                                                            else -> Color(0xFF94A3B8)
                                                        }
                                                    )
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${month.forecastedPriceAr.toInt()}",
                                                color = Color.White,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Alertes de réapprovisionnement automatique
                    item {
                        Text("Alertes Automatisées de Réapprovisionnement :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    items(alerts) { alert ->
                        val (bg, border, textCol) = when (alert.priority) {
                            ReplenishmentAlertPriority.CRITIQUE_IMMEDIAT -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFF991B1B))
                            ReplenishmentAlertPriority.OPPORTUNITE_RECOLTE -> Triple(Color(0xFFF0FDF4), Color(0xFF16A34A), Color(0xFF166534))
                            ReplenishmentAlertPriority.ANTICIPATION_SOUDURE -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFF92400E))
                            ReplenishmentAlertPriority.OPTIMAL -> Triple(Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF334155))
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = bg),
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(border)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(alert.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textCol)
                                    Surface(color = border, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = alert.priority.labelFr,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Stock dispo : ${alert.currentStockKg.toInt()} kg (${alert.remainingCoverageDays}j)", fontSize = 10.5.sp, color = Color(0xFF1E293B))
                                    Text("Point de commande : ${alert.reorderPointKg.toInt()} kg", fontSize = 10.5.sp, color = Color(0xFF64748B))
                                }

                                if (alert.recommendedOrderQuantityKg > 0) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "📦 Commande préconisée : ${alert.recommendedOrderQuantityKg.toInt()} kg (~${String.format("%,d", alert.estimatedOrderCostAr.toLong())} Ar)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = border
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))
                                Text("🌱 ${alert.seasonalContext}", fontSize = 10.sp, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("💡 ${alert.strategicAction}", fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = textCol)
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
                    Text("Fermer l'Outil de Prévision", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
