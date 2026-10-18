package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BatchLot
import com.example.data.model.Product
import com.example.data.model.QualityDecision
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualityReceptionWizardDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()

    var currentStep by remember { mutableStateOf(1) }

    // Étape 1 : Documentaire & Traçabilité
    var selectedProduct by remember { mutableStateOf(products.firstOrNull() ?: products[0]) }
    var selectedSupplierName by remember { mutableStateOf(suppliers.firstOrNull()?.name ?: "Union Rizière d'Alaotra") }
    var originRegion by remember { mutableStateOf("Alaotra-Mangoro") }
    var deliveryNoteNumber by remember { mutableStateOf("BL-2026-849") }
    var truckRegistration by remember { mutableStateOf("4521 TAB (Chauffeur Faly)") }
    var docCheckBlSigned by remember { mutableStateOf(true) }
    var docCheckOriginCertified by remember { mutableStateOf(true) }

    // Étape 2 : Transport & Conditionnement
    var totalBagsCountText by remember { mutableStateOf("100") }
    var bagType by remember { mutableStateOf("Sacs polypropylène tressé 50kg (Propres & Neufs)") }
    var truckCleanChecked by remember { mutableStateOf(true) }
    var tarpIntactChecked by remember { mutableStateOf(true) }
    var noExternalMoistureChecked by remember { mutableStateOf(true) }
    var noChemicalOdorsChecked by remember { mutableStateOf(true) }

    // Étape 3 : Pesée Contradictoire & Sondage (Guide p. 22 : min 10% des sacs)
    var weighedBagsCountText by remember { mutableStateOf("15") }
    var grossWeighedWeightText by remember { mutableStateOf("5015") }
    var bagTareKgText by remember { mutableStateOf("0.15") }
    var invoicedWeightText by remember { mutableStateOf("5000") }
    var probeSamplingConfirmed by remember { mutableStateOf(true) } // Canne sondeuse 3 points

    // Étape 4 : Critères Physiques (Humidité, Impuretés, Calibrage, Organoleptique)
    var measuredMoistureText by remember { mutableStateOf("13.2") }
    var measuredImpuritiesText by remember { mutableStateOf("1.4") }
    var brokenGrainsPercentText by remember { mutableStateOf("3.5") }
    var immatureGrainsPercentText by remember { mutableStateOf("1.0") }
    var noLivingInsectsChecked by remember { mutableStateOf(true) }
    var soundOdorChecked by remember { mutableStateOf(true) }
    var naturalColorChecked by remember { mutableStateOf(true) }
    var calibrationUniformChecked by remember { mutableStateOf(true) }

    // Étape 5 : Décision, N° Lot & Stock
    var unitCostText by remember { mutableStateOf(selectedProduct.cmupCostAr.toInt().toString()) }
    var observationsText by remember { mutableStateOf("Lot sain et conforme aux critères du guide 2026.") }
    var overrideDecision by remember { mutableStateOf<QualityDecision?>(null) }

    // Calculs dynamiques
    val totalBags = totalBagsCountText.toIntOrNull() ?: 100
    val weighedBags = weighedBagsCountText.toIntOrNull() ?: 15
    val minRequiredWeighedBags = maxOf(5, (totalBags * 0.10).toInt())
    val isWeighedCountCompliant = weighedBags >= minRequiredWeighedBags

    val grossWeight = grossWeighedWeightText.toDoubleOrNull() ?: 5000.0
    val bagTare = bagTareKgText.toDoubleOrNull() ?: 0.15
    val netCalculatedWeight = maxOf(0.0, grossWeight - (totalBags * bagTare))
    val invoicedWeight = invoicedWeightText.toDoubleOrNull() ?: 5000.0
    val weightDiscrepancyKg = netCalculatedWeight - invoicedWeight

    val measuredMoisture = measuredMoistureText.toDoubleOrNull() ?: 13.0
    val measuredImpurities = measuredImpuritiesText.toDoubleOrNull() ?: 1.5
    val maxMoistureAllowed = selectedProduct.maxMoisturePercent

    // Détermination de la décision suggérée par le guide 2026
    val isMoistureOk = measuredMoisture <= maxMoistureAllowed
    val isMoistureDecote = measuredMoisture > maxMoistureAllowed && measuredMoisture <= (maxMoistureAllowed + 1.2)
    val isImpuritiesOk = measuredImpurities <= 2.0
    val areSanitaryCriteriaOk = noLivingInsectsChecked && soundOdorChecked && naturalColorChecked

    val suggestedDecision = when {
        !areSanitaryCriteriaOk || measuredMoisture > (maxMoistureAllowed + 2.0) -> QualityDecision.REFUSE
        measuredMoisture > (maxMoistureAllowed + 1.2) -> QualityDecision.QUARANTAINE
        !isMoistureOk || measuredImpurities > 2.0 || !calibrationUniformChecked -> QualityDecision.DECOTE
        else -> QualityDecision.CONFORME
    }

    val finalDecision = overrideDecision ?: suggestedDecision

    // Génération N° Lot et Contre-échantillon selon la norme Sambatra Négoce (p. 28)
    val dateFormat = SimpleDateFormat("yyMMdd", Locale.US)
    val lotDateCode = dateFormat.format(Date())
    val regionTrigram = originRegion.take(3).uppercase()
    val lotNumberPreview = "${selectedProduct.code}-$regionTrigram-$lotDateCode-03"
    val counterSampleIdPreview = "ECH-$lotNumberPreview-SC"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = AgroGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Contrôle Qualité Réception (Guide 2026)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                        }
                        Text(
                            text = "Protocole en 5 étapes pour valider la conformité avant entrée en stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stepper visuel des 5 étapes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val steps = listOf(
                        1 to "Documents",
                        2 to "Transport",
                        3 to "Pesée ≥10%",
                        4 to "Physique",
                        5 to "Décision"
                    )
                    steps.forEach { (s, label) ->
                        val isDone = s < currentStep
                        val isCurrent = s == currentStep
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { currentStep = s }
                                .padding(2.dp)
                        ) {
                            Surface(
                                color = when {
                                    isCurrent -> SpiceOrangeSecondary
                                    isDone -> AgroGreenPrimary
                                    else -> Color.LightGray
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isDone) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    } else {
                                        Text("$s", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) SpiceOrangeSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenu défilable selon l'étape
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    when (currentStep) {
                        // Étape 1 : Contrôle Documentaire
                        1 -> Step1Documentary(
                            products = products,
                            selectedProduct = selectedProduct,
                            onProductSelect = {
                                selectedProduct = it
                                unitCostText = it.cmupCostAr.toInt().toString()
                            },
                            suppliers = suppliers,
                            selectedSupplierName = selectedSupplierName,
                            onSupplierChange = { selectedSupplierName = it },
                            originRegion = originRegion,
                            onOriginChange = { originRegion = it },
                            deliveryNoteNumber = deliveryNoteNumber,
                            onDeliveryNoteChange = { deliveryNoteNumber = it },
                            truckRegistration = truckRegistration,
                            onTruckChange = { truckRegistration = it },
                            docCheckBlSigned = docCheckBlSigned,
                            onToggleBlSigned = { docCheckBlSigned = !docCheckBlSigned },
                            docCheckOriginCertified = docCheckOriginCertified,
                            onToggleOriginCertified = { docCheckOriginCertified = !docCheckOriginCertified }
                        )

                        // Étape 2 : Transport & Conditionnement
                        2 -> Step2Transport(
                            totalBagsCountText = totalBagsCountText,
                            onTotalBagsChange = { totalBagsCountText = it },
                            bagType = bagType,
                            onBagTypeChange = { bagType = it },
                            truckCleanChecked = truckCleanChecked,
                            onToggleTruckClean = { truckCleanChecked = !truckCleanChecked },
                            tarpIntactChecked = tarpIntactChecked,
                            onToggleTarpIntact = { tarpIntactChecked = !tarpIntactChecked },
                            noExternalMoistureChecked = noExternalMoistureChecked,
                            onToggleNoMoisture = { noExternalMoistureChecked = !noExternalMoistureChecked },
                            noChemicalOdorsChecked = noChemicalOdorsChecked,
                            onToggleNoOdors = { noChemicalOdorsChecked = !noChemicalOdorsChecked }
                        )

                        // Étape 3 : Pesée Contradictoire & Sondage (≥ 10%)
                        3 -> Step3Weighing(
                            totalBags = totalBags,
                            weighedBagsCountText = weighedBagsCountText,
                            onWeighedBagsChange = { weighedBagsCountText = it },
                            minRequiredWeighedBags = minRequiredWeighedBags,
                            isWeighedCountCompliant = isWeighedCountCompliant,
                            grossWeighedWeightText = grossWeighedWeightText,
                            onGrossWeightChange = { grossWeighedWeightText = it },
                            bagTareKgText = bagTareKgText,
                            onBagTareChange = { bagTareKgText = it },
                            invoicedWeightText = invoicedWeightText,
                            onInvoicedWeightChange = { invoicedWeightText = it },
                            netCalculatedWeight = netCalculatedWeight,
                            weightDiscrepancyKg = weightDiscrepancyKg,
                            probeSamplingConfirmed = probeSamplingConfirmed,
                            onToggleProbeSampling = { probeSamplingConfirmed = !probeSamplingConfirmed }
                        )

                        // Étape 4 : Checklist des Critères Physiques (Humidité, Impuretés, Calibrage)
                        4 -> Step4PhysicalCriteria(
                            selectedProduct = selectedProduct,
                            measuredMoistureText = measuredMoistureText,
                            onMoistureChange = { measuredMoistureText = it },
                            maxMoistureAllowed = maxMoistureAllowed,
                            isMoistureOk = isMoistureOk,
                            isMoistureDecote = isMoistureDecote,
                            measuredImpuritiesText = measuredImpuritiesText,
                            onImpuritiesChange = { measuredImpuritiesText = it },
                            isImpuritiesOk = isImpuritiesOk,
                            brokenGrainsPercentText = brokenGrainsPercentText,
                            onBrokenGrainsChange = { brokenGrainsPercentText = it },
                            immatureGrainsPercentText = immatureGrainsPercentText,
                            onImmatureGrainsChange = { immatureGrainsPercentText = it },
                            noLivingInsectsChecked = noLivingInsectsChecked,
                            onToggleNoInsects = { noLivingInsectsChecked = !noLivingInsectsChecked },
                            soundOdorChecked = soundOdorChecked,
                            onToggleSoundOdor = { soundOdorChecked = !soundOdorChecked },
                            naturalColorChecked = naturalColorChecked,
                            onToggleNaturalColor = { naturalColorChecked = !naturalColorChecked },
                            calibrationUniformChecked = calibrationUniformChecked,
                            onToggleCalibration = { calibrationUniformChecked = !calibrationUniformChecked }
                        )

                        // Étape 5 : Décision, N° Lot, Contre-échantillon et Intégration Stock
                        5 -> Step5DecisionAndEntry(
                            selectedProduct = selectedProduct,
                            suggestedDecision = suggestedDecision,
                            finalDecision = finalDecision,
                            onSelectDecision = { overrideDecision = it },
                            lotNumberPreview = lotNumberPreview,
                            counterSampleIdPreview = counterSampleIdPreview,
                            netCalculatedWeight = netCalculatedWeight,
                            unitCostText = unitCostText,
                            onUnitCostChange = { unitCostText = it },
                            observationsText = observationsText,
                            onObservationsChange = { observationsText = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Boutons Précédent / Suivant / Valider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Précédent", fontSize = 12.sp)
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Abandonner")
                        }
                    }

                    if (currentStep < 5) {
                        Button(
                            onClick = { currentStep++ },
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Étape suivante (${currentStep + 1}/5)", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val netQty = netCalculatedWeight
                                val cost = unitCostText.toDoubleOrNull() ?: selectedProduct.cmupCostAr
                                val moisture = measuredMoistureText.toDoubleOrNull() ?: 13.0
                                val impurities = measuredImpuritiesText.toDoubleOrNull() ?: 1.5

                                viewModel.registerNewBatch(
                                    productCode = selectedProduct.code,
                                    originRegion = originRegion,
                                    supplierName = selectedSupplierName,
                                    quantityKg = netQty,
                                    moisturePercent = moisture,
                                    impuritiesPercent = impurities,
                                    bagsCount = totalBags,
                                    weighedBagsCount = weighedBags,
                                    decision = finalDecision,
                                    unitCostAr = cost,
                                    observations = observationsText + " [BL: $deliveryNoteNumber | Échantillon scellé: $counterSampleIdPreview]"
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (finalDecision) {
                                    QualityDecision.CONFORME -> AgroGreenPrimary
                                    QualityDecision.DECOTE -> SpiceOrangeSecondary
                                    QualityDecision.QUARANTAINE -> Color(0xFFD97706)
                                    QualityDecision.REFUSE -> StatusRedDanger
                                }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (finalDecision) {
                                    QualityDecision.CONFORME -> "Valider & Intégrer au Stock Room"
                                    QualityDecision.DECOTE -> "Valider avec Décote & Intégrer"
                                    QualityDecision.QUARANTAINE -> "Mettre en Quarantaine (Bloqué)"
                                    QualityDecision.REFUSE -> "Refuser & Émettre PV de Rejet"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step1Documentary(
    products: List<Product>,
    selectedProduct: Product,
    onProductSelect: (Product) -> Unit,
    suppliers: List<com.example.data.model.Supplier>,
    selectedSupplierName: String,
    onSupplierChange: (String) -> Unit,
    originRegion: String,
    onOriginChange: (String) -> Unit,
    deliveryNoteNumber: String,
    onDeliveryNoteChange: (String) -> Unit,
    truckRegistration: String,
    onTruckChange: (String) -> Unit,
    docCheckBlSigned: Boolean,
    onToggleBlSigned: () -> Unit,
    docCheckOriginCertified: Boolean,
    onToggleOriginCertified: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = AgroGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Étape 1 : Contrôle documentaire & Traçabilité (Guide p. 22)\nVérifier la concordance exacte entre la commande, le bon de livraison et la marchandise.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text("1. Produit livré :", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            products.take(4).forEach { prod ->
                val isSelected = prod.code == selectedProduct.code
                FilterChip(
                    selected = isSelected,
                    onClick = { onProductSelect(prod) },
                    label = { Text("${prod.code} - ${prod.name.take(12)}", fontSize = 10.sp) }
                )
            }
        }

        OutlinedTextField(
            value = selectedSupplierName,
            onValueChange = onSupplierChange,
            label = { Text("Fournisseur / Collecteur", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = originRegion,
                onValueChange = onOriginChange,
                label = { Text("Région d'origine", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = deliveryNoteNumber,
                onValueChange = onDeliveryNoteChange,
                label = { Text("N° Bon de Livraison (BL)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = truckRegistration,
            onValueChange = onTruckChange,
            label = { Text("N° Camion & Nom du Chauffeur", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text("Checklist de conformité documentaire :", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)

        ValidationCheckboxItem(
            text = "Bon de livraison régulier, cacheté et signé par l'expéditeur",
            isChecked = docCheckBlSigned,
            onToggle = onToggleBlSigned
        )
        ValidationCheckboxItem(
            text = "Carnet de collecte ou certificat d'origine phytosanitaire vérifié",
            isChecked = docCheckOriginCertified,
            onToggle = onToggleOriginCertified
        )
    }
}

@Composable
private fun Step2Transport(
    totalBagsCountText: String,
    onTotalBagsChange: (String) -> Unit,
    bagType: String,
    onBagTypeChange: (String) -> Unit,
    truckCleanChecked: Boolean,
    onToggleTruckClean: () -> Unit,
    tarpIntactChecked: Boolean,
    onToggleTarpIntact: () -> Unit,
    noExternalMoistureChecked: Boolean,
    onToggleNoMoisture: () -> Unit,
    noChemicalOdorsChecked: Boolean,
    onToggleNoOdors: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF1D4ED8))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Étape 2 : Inspection du transport et de l'emballage (Guide p. 22)\nUn camion souillé ou une bâche percée invalide la conformité sanitaire de tout le chargement.",
                    fontSize = 11.sp,
                    color = Color(0xFF1E3A8A)
                )
            }
        }

        OutlinedTextField(
            value = totalBagsCountText,
            onValueChange = onTotalBagsChange,
            label = { Text("Nombre total de sacs déclarés", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bagType,
            onValueChange = onBagTypeChange,
            label = { Text("Conditionnement (Type et état des sacs)", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text("Checklist d'inspection du véhicule et des emballages :", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)

        ValidationCheckboxItem(
            text = "Plancher du véhicule propre, sec, exempt de clous saillants et d'échardes",
            isChecked = truckCleanChecked,
            onToggle = onToggleTruckClean
        )
        ValidationCheckboxItem(
            text = "Bâche étanche sans déchirure ni trace d'infiltration de pluie",
            isChecked = tarpIntactChecked,
            onToggle = onToggleTarpIntact
        )
        ValidationCheckboxItem(
            text = "Sacs propres, coutures d'origine intactes, sans taches d'eau",
            isChecked = noExternalMoistureChecked,
            onToggle = onToggleNoMoisture
        )
        ValidationCheckboxItem(
            text = "Absence totale d'odeur d'essence, d'huile, d'engrais ou de produits phytosanitaires",
            isChecked = noChemicalOdorsChecked,
            onToggle = onToggleNoOdors
        )
    }
}

@Composable
private fun Step3Weighing(
    totalBags: Int,
    weighedBagsCountText: String,
    onWeighedBagsChange: (String) -> Unit,
    minRequiredWeighedBags: Int,
    isWeighedCountCompliant: Boolean,
    grossWeighedWeightText: String,
    onGrossWeightChange: (String) -> Unit,
    bagTareKgText: String,
    onBagTareChange: (String) -> Unit,
    invoicedWeightText: String,
    onInvoicedWeightChange: (String) -> Unit,
    netCalculatedWeight: Double,
    weightDiscrepancyKg: Double,
    probeSamplingConfirmed: Boolean,
    onToggleProbeSampling: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (isWeighedCountCompliant) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isWeighedCountCompliant) Icons.Default.Scale else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isWeighedCountCompliant) AgroGreenPrimary else StatusRedDanger
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Étape 3 : Pesée Contradictoire & Sondage (Règle d'or p. 22)\nObligation impérative : Peser contradictoirement au moins 10% des sacs au hasard ($minRequiredWeighedBags sacs requis pour $totalBags sacs).",
                    fontSize = 11.sp,
                    color = if (isWeighedCountCompliant) Color(0xFF166534) else Color(0xFF991B1B)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = weighedBagsCountText,
                onValueChange = onWeighedBagsChange,
                label = { Text("Sacs pesés (min $minRequiredWeighedBags)", fontSize = 11.sp) },
                isError = !isWeighedCountCompliant,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = bagTareKgText,
                onValueChange = onBagTareChange,
                label = { Text("Tare sac (kg)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = grossWeighedWeightText,
                onValueChange = onGrossWeightChange,
                label = { Text("Poids brut mesuré (kg)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = invoicedWeightText,
                onValueChange = onInvoicedWeightChange,
                label = { Text("Poids facturé BL (kg)", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        // Bilan de pesée contradictoire
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Poids net réel calculé :", fontSize = 12.sp)
                    Text("${netCalculatedWeight.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Écart vs Bordereau (BL) :", fontSize = 12.sp)
                    Text(
                        text = "${if (weightDiscrepancyKg >= 0) "+" else ""}${String.format(Locale.US, "%.1f", weightDiscrepancyKg)} kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (weightDiscrepancyKg < -20.0) StatusRedDanger else StatusGreenOk
                    )
                }
                if (weightDiscrepancyKg < -25.0) {
                    Text(
                        text = "ATTENTION : Manque de poids de ${(-weightDiscrepancyKg).toInt()} kg ! Mentionner obligatoirement sur le BL avant signature.",
                        fontSize = 10.sp,
                        color = StatusRedDanger,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        ValidationCheckboxItem(
            text = "Prélèvement par canne sondeuse en 3 points (haut, milieu, bas) de chaque sac échantillonné",
            isChecked = probeSamplingConfirmed,
            onToggle = onToggleProbeSampling
        )
    }
}

@Composable
private fun Step4PhysicalCriteria(
    selectedProduct: Product,
    measuredMoistureText: String,
    onMoistureChange: (String) -> Unit,
    maxMoistureAllowed: Double,
    isMoistureOk: Boolean,
    isMoistureDecote: Boolean,
    measuredImpuritiesText: String,
    onImpuritiesChange: (String) -> Unit,
    isImpuritiesOk: Boolean,
    brokenGrainsPercentText: String,
    onBrokenGrainsChange: (String) -> Unit,
    immatureGrainsPercentText: String,
    onImmatureGrainsChange: (String) -> Unit,
    noLivingInsectsChecked: Boolean,
    onToggleNoInsects: () -> Unit,
    soundOdorChecked: Boolean,
    onToggleSoundOdor: () -> Unit,
    naturalColorChecked: Boolean,
    onToggleNaturalColor: () -> Unit,
    calibrationUniformChecked: Boolean,
    onToggleCalibration: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Biotech, contentDescription = null, tint = SpiceOrangeSecondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Étape 4 : Checklist des Critères Physiques (Guide p. 23)\nValidation obligatoire des critères physiques : humidité, impuretés, calibrage et état sanitaire.",
                    fontSize = 11.sp,
                    color = Color(0xFF92400E)
                )
            }
        }

        // 1. Humidité
        Card(
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1. Humidité Hygromètre (Seuil max : $maxMoistureAllowed%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        color = when {
                            isMoistureOk -> StatusGreenOk
                            isMoistureDecote -> StatusAmberWarning
                            else -> StatusRedDanger
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = when {
                                isMoistureOk -> "CONFORME"
                                isMoistureDecote -> "DÉCOTE REQUISE"
                                else -> "TROP HUMIDE"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = measuredMoistureText,
                    onValueChange = onMoistureChange,
                    label = { Text("Taux d'humidité mesuré (%)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Impuretés & Corps étrangers
        Card(
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. Impuretés & Déchets (Max 2.0%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Surface(
                        color = if (isImpuritiesOk) StatusGreenOk else StatusAmberWarning,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isImpuritiesOk) "CONFORME" else "HORS NORME",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = measuredImpuritiesText,
                    onValueChange = onImpuritiesChange,
                    label = { Text("Matières étrangères, poussière, cailloux (%)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. Calibrage & Granulométrie
        Card(
            shape = RoundedCornerShape(10.dp),
            border = CardDefaults.outlinedCardBorder(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("3. Calibrage, Granulométrie & Brisures", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = brokenGrainsPercentText,
                        onValueChange = onBrokenGrainsChange,
                        label = { Text("Grains cassés / brisures (%)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = immatureGrainsPercentText,
                        onValueChange = onImmatureGrainsChange,
                        label = { Text("Grains avortés / immatures (%)", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Checklist Organoleptique & Sanitaire
        Text("Checklist Sanitaire & Organoleptique :", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)

        ValidationCheckboxItem(
            text = "ZÉRO insecte vivant (charançons, bruches) - Critère éliminatoire",
            isChecked = noLivingInsectsChecked,
            onToggle = onToggleNoInsects,
            isCritical = true
        )
        ValidationCheckboxItem(
            text = "Odeur saine et caractéristique (absence totale d'odeur de moisi ou fermentation)",
            isChecked = soundOdorChecked,
            onToggle = onToggleSoundOdor,
            isCritical = true
        )
        ValidationCheckboxItem(
            text = "Couleur franche, uniforme et éclat naturel du produit",
            isChecked = naturalColorChecked,
            onToggle = onToggleNaturalColor
        )
        ValidationCheckboxItem(
            text = "Calibrage homogène conforme au standard de vente de Sambatra Négoce",
            isChecked = calibrationUniformChecked,
            onToggle = onToggleCalibration
        )
    }
}

@Composable
private fun Step5DecisionAndEntry(
    selectedProduct: Product,
    suggestedDecision: QualityDecision,
    finalDecision: QualityDecision,
    onSelectDecision: (QualityDecision) -> Unit,
    lotNumberPreview: String,
    counterSampleIdPreview: String,
    netCalculatedWeight: Double,
    unitCostText: String,
    onUnitCostChange: (String) -> Unit,
    observationsText: String,
    onObservationsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Étape 5 : Synthèse & Décision d'Intégration au Stock",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AgroGreenPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recommandation automatique calculée selon le Guide 2026 :",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QualityDecision.values().forEach { d ->
                        val isSelected = finalDecision == d
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDecision(d) },
                            label = { Text(d.label.take(15), fontSize = 10.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Attribution N° Lot & Contre-Échantillon
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = AgroGreenPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identification & Traçabilité Sambatra Négoce", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Numéro de Lot Normalisé :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(lotNumberPreview, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AgroGreenPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Contre-Échantillon Scellé (Obligatoire p. 22) :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(counterSampleIdPreview, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SpiceOrangeSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Zone d'affectation en entrepôt :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedProduct.warehouseZone, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Poids net intégré au stock :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${netCalculatedWeight.toInt()} kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        OutlinedTextField(
            value = unitCostText,
            onValueChange = onUnitCostChange,
            label = { Text("Prix de revient unitaire achat (Ar/kg rendu)", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = observationsText,
            onValueChange = onObservationsChange,
            label = { Text("Observations qualité & Visa magasinier", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth()
        )

        Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockClock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Archivage légal : Le bocal du contre-échantillon ($counterSampleIdPreview) doit être scellé et conservé 90 jours au bureau pour protection en cas de réclamation ultérieure.",
                    fontSize = 10.sp,
                    color = Color(0xFF78350F)
                )
            }
        }
    }
}

@Composable
private fun ValidationCheckboxItem(
    text: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    isCritical: Boolean = false
) {
    Surface(
        color = if (isChecked) Color(0xFFF0FDF4) else if (isCritical) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .border(
                width = 1.dp,
                color = if (isChecked) AgroGreenPrimary.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = AgroGreenPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal,
                color = if (isChecked) Color(0xFF166534) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
