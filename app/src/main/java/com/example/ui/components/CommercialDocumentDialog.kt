package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.report.CommercialDocumentGenerator
import com.example.data.report.DeliveryNoteData
import com.example.data.report.MonthlyAuditInventoryData
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommercialDocumentDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clients by viewModel.clients.collectAsState()
    val products by viewModel.products.collectAsState()
    val lots by viewModel.lots.collectAsState()

    var activeDocType by remember { mutableStateOf(0) } // 0: Bon de Livraison, 1: Audit Inventaire Physique

    var selectedClient by remember { mutableStateOf(clients.firstOrNull() ?: clients[0]) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val deliveryData = remember(selectedClient, products, lots) {
        CommercialDocumentGenerator.createSampleDeliveryNote(selectedClient, products, lots)
    }

    val inventoryData = remember(products) {
        CommercialDocumentGenerator.createSampleMonthlyAuditData(products)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgroGreenPrimary)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Documents Commerciaux & Fiches d'Audit",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mentions légales malgaches (NIF, STAT) & Traçabilité sanitaire",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }

                // Onglets types de documents
                TabRow(
                    selectedTabIndex = activeDocType,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = AgroGreenPrimary
                ) {
                    Tab(
                        selected = activeDocType == 0,
                        onClick = { activeDocType = 0 },
                        text = { Text("Bon de Livraison (BL)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = activeDocType == 1,
                        onClick = { activeDocType = 1 },
                        text = { Text("Fiche Inventaire & Freinte", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (activeDocType == 0) {
                        // Sélecteur de client
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Client destinataire de l'expédition :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        clients.take(3).forEach { cl ->
                                            FilterChip(
                                                selected = selectedClient.id == cl.id,
                                                onClick = { selectedClient = cl },
                                                label = { Text(cl.name.take(18), fontSize = 10.5.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Aperçu En-tête BL
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("SAMBATRA NÉGOCE SARL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                        Text(deliveryData.deliveryNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SpiceOrangeSecondary)
                                    }
                                    Text(CommercialDocumentGenerator.COMPANY_LEGAL, fontSize = 9.sp, color = Color.Gray)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Client : ${deliveryData.client.name}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Date : ${deliveryData.dateFormatted}", fontSize = 10.5.sp)
                                    }
                                    Text("Transport : ${deliveryData.vehiclePlate} • Chauffeur : ${deliveryData.driverName}", fontSize = 10.sp)
                                    Text("N° Scellé de benne : ${deliveryData.sealNumber}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AgroGreenPrimary)
                                }
                            }
                        }

                        // Lignes de marchandises
                        item {
                            Text("Détail des Lots Expédiés & Traçabilité Sanitaire", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        items(deliveryData.items) { item ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("Lot : ${item.lotNumber} • ${item.bagsCount} sacs (${item.packagingFormat})", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${item.netWeightKg.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                        Text("${String.format(Locale.US, "%,.0f", item.totalAmountAr).replace(',', ' ')} Ar", fontSize = 10.sp, color = AgroGreenPrimary)
                                    }
                                }
                            }
                        }

                        // Totaux & Clause contradictoire
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Poids Net Total :", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text("${deliveryData.totalNetWeightKg.toInt()} kg (${deliveryData.totalBags} sacs)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Total Facturé :", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                                        Text("${String.format(Locale.US, "%,.0f", deliveryData.totalAmountAr).replace(',', ' ')} Ar", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AgroGreenPrimary)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text(
                                        text = "Règle p. 22 : Pesée contradictoire d'au moins 10% des sacs obligatoire par le réceptionnaire avant déchargement.",
                                        fontSize = 9.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                    } else {
                        // Fiche d'Inventaire Contradictoire & Contrôle Freinte
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Fiche d'Inventaire Contradictoire Mensuel (${inventoryData.inventoryNumber})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Date de constat : ${inventoryData.auditDate} • Auditeur : ${inventoryData.auditorName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                            }
                        }

                        items(inventoryData.lines) { line ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(line.productName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Surface(
                                            color = if (line.isVarianceNormal) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = if (line.isVarianceNormal) "FREINTE ADMISSIBLE" else "ANOMALIE",
                                                color = if (line.isVarianceNormal) AgroGreenPrimary else StatusRedDanger,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("Zone : ${line.warehouseZone}", fontSize = 9.5.sp, color = Color.Gray)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Théorique : ${line.theoreticalStockKg.toInt()} kg", fontSize = 10.sp)
                                        Text("Réel pesé : ${line.actualCountedStockKg.toInt()} kg", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            "Écart : ${line.varianceKg.toInt()} kg (${String.format(Locale.US, "%.2f", line.variancePercent)}%)",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (line.isVarianceNormal) AgroGreenPrimary else StatusRedDanger
                                        )
                                    }
                                    Text(
                                        text = line.explanation,
                                        fontSize = 9.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Conclusion de l'audit
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Conclusion du Commissaire aux Comptes / Chef de Dépôt :", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AgroGreenPrimary)
                                    Text(inventoryData.auditConclusion, fontSize = 10.5.sp, lineHeight = 15.sp, color = Color(0xFF14532D))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Taux global de freinte :", fontSize = 10.5.sp)
                                        Text("${String.format(Locale.US, "%.2f", inventoryData.overallShrinkageRatePercent)}% (Plafond : 1.20%)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AgroGreenPrimary)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Valeur de la freinte constatée :", fontSize = 10.5.sp)
                                        Text("${String.format(Locale.US, "%,.0f", Math.abs(inventoryData.totalVarianceValueAr)).replace(',', ' ')} Ar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Actions
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Fermer", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                isGeneratingPdf = true
                                try {
                                    val pdfFile = CommercialDocumentGenerator.generateDeliveryNotePdf(context, deliveryData)
                                    Toast.makeText(context, "PDF généré avec succès : ${pdfFile.name}", Toast.LENGTH_SHORT).show()
                                    CommercialDocumentGenerator.shareDocument(context, pdfFile, "Partager le Bon de Livraison")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erreur génération PDF : ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isGeneratingPdf = false
                                }
                            },
                            enabled = !isGeneratingPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isGeneratingPdf) "Génération..." else "Générer PDF", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}
