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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.finance.DebtRecoveryManager
import com.example.data.finance.DebtSettlementPlan
import com.example.data.model.Client
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtRecoveryPlanDialog(
    client: Client,
    onDismiss: () -> Unit,
    onPlanConfirmed: (DebtSettlementPlan) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var upfrontPercentSlider by remember { mutableStateOf(30f) }
    var installmentsCountSlider by remember { mutableStateOf(3f) }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Plan d'apurement, 1: Courrier Mise en Demeure

    val settlementPlan = remember(client, upfrontPercentSlider, installmentsCountSlider) {
        DebtRecoveryManager.generateSettlementPlan(
            client = client,
            upfrontDepositPercent = upfrontPercentSlider.toDouble(),
            installmentsCount = installmentsCountSlider.toInt()
        )
    }

    val formalLetter = remember(client) {
        DebtRecoveryManager.buildFormalDunningLetter(client)
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
                        .background(StatusRedDanger)
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
                                        Icons.Default.Gavel,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Protocole de Recouvrement (Guide p. 44)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${client.name} • Créance : ${String.format(Locale.US, "%,.0f", client.currentDebtAr).replace(',', ' ')} Ar",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }
                }

                // Onglets
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = StatusRedDanger
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text("Échéancier Apurement", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text("Mise en Demeure Officielle", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (activeSubTab == 0) {
                        // Alerte statut
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StatusRedDanger)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Clause d'application stricte du Guide (p. 4, 37 & 44) :",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = StatusRedDanger
                                    )
                                    Text(
                                        text = "« La marge se perd au crédit. Tout client ayant une facture échue au-delà de 30 jours voit ses livraisons immédiatement suspendues. La reprise d'approvisionnement exige un accord transactionnel écrit avec versement comptant immédiat. »",
                                        fontSize = 10.5.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                            }
                        }

                        // Réglage du plan
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Paramètres du Protocole Transactionnel", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = AgroGreenPrimary)

                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Acompte immédiat exigible :", fontSize = 11.sp)
                                            Text(
                                                "${upfrontPercentSlider.toInt()}% (${String.format(Locale.US, "%,.0f", settlementPlan.immediateUpfrontDepositAr).replace(',', ' ')} Ar)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                                color = StatusRedDanger
                                            )
                                        }
                                        Slider(
                                            value = upfrontPercentSlider,
                                            onValueChange = { upfrontPercentSlider = it },
                                            valueRange = 20f..60f,
                                            steps = 7
                                        )
                                    }

                                    Column {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Nombre d'échéances hebdomadaires du solde :", fontSize = 11.sp)
                                            Text("${installmentsCountSlider.toInt()} versements", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = AgroGreenPrimary)
                                        }
                                        Slider(
                                            value = installmentsCountSlider,
                                            onValueChange = { installmentsCountSlider = it },
                                            valueRange = 1f..4f,
                                            steps = 2
                                        )
                                    }
                                }
                            }
                        }

                        // Échéances générées
                        item {
                            Text("Calendrier des Échéances de Règlement", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        // Acompte
                        item {
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("ACOMPTE IMMÉDIAT (Jour J)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF92400E))
                                        Text("Condition préalable obligatoire à la signature", fontSize = 9.sp, color = Color(0xFFB45309))
                                    }
                                    Text(
                                        "${String.format(Locale.US, "%,.0f", settlementPlan.immediateUpfrontDepositAr).replace(',', ' ')} Ar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        items(settlementPlan.installments) { inst ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Échéance N°${inst.installmentNumber} • Date : ${inst.dueDateFormatted}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Text("Statut : ${inst.status}", fontSize = 9.sp, color = Color.Gray)
                                    }
                                    Text(
                                        "${String.format(Locale.US, "%,.0f", inst.amountAr).replace(',', ' ')} Ar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AgroGreenPrimary
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = settlementPlan.formalAgreementLegalNotice,
                                fontSize = 9.5.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Courrier formel
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Lettre Recommandée de Mise en Demeure", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusRedDanger)
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(formalLetter))
                                                Toast.makeText(context, "Texte du courrier copié dans le presse-papier", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = AgroGreenPrimary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text(
                                        text = formalLetter,
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer actions
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
                                onPlanConfirmed(settlementPlan)
                                Toast.makeText(context, "Protocole validé pour ${client.name}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusRedDanger),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Activer le Protocole", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}
