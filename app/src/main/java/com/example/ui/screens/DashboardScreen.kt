package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DemoDataProvider
import com.example.data.model.AppTab
import com.example.data.model.DashboardKpi
import com.example.data.model.KpiStatus
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val kpis by viewModel.kpis.collectAsState()
    val treasury by viewModel.currentTreasury.collectAsState()
    val mondayAlerts by viewModel.mondayAlerts.collectAsState()
    val lang by viewModel.selectedLanguage.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val lotsList by viewModel.lots.collectAsState()

    var selectedDomainFilter by remember { mutableStateOf("Tous") }
    var showMondayModal by remember { mutableStateOf(false) }
    var showPdfReportDialog by remember { mutableStateOf(false) }
    var showWeatherRiskDialog by remember { mutableStateOf(false) }
    var showLogisticsPlanningDialog by remember { mutableStateOf(false) }
    var showNetMarginDialog by remember { mutableStateOf(false) }
    var showCarbonFootprintDialog by remember { mutableStateOf(false) }
    var showSeasonalityDialog by remember { mutableStateOf(false) }
    var showWorkingCapitalDialog by remember { mutableStateOf(false) }
    var showCommercialDocDialog by remember { mutableStateOf(false) }
    var showAnnualClosingDialog by remember { mutableStateOf(false) }

    val domains = listOf("Tous", "Ventes", "Marge", "Stock", "Qualité", "Crédit", "Trésorerie", "Logistique")
    val filteredKpis = if (selectedDomainFilter == "Tous") kpis else kpis.filter { it.domain == selectedDomainFilter }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Hero Banner Entrepôt Sambatra Négoce
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_warehouse),
                        contentDescription = "Entrepôt Sambatra Négoce SARL",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC0F172A))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                    ) {
                        Surface(
                            color = AgroGreenPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SAMBATRA NÉGOCE SARL",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Entrepôt 400 m² • Anosibe Antananarivo • Normes 2026",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // En-tête Entreprise
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = DemoDataProvider.COMPANY_NAME,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "FR") "Antananarivo • Entrepôt 400 m² • 100M Ar capital" else "Antananarivo • Trano fitahirizana 400 m² • 100M Ar renivola",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Guide 2026",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Cartes Résumé Financier & Accès Rapport PDF
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Trésorerie
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (treasury < 10_000_000) Color(0xFFFEE2E2) else Color(0xFFE8F5EE)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == "FR") "Trésorerie" else "Vola eo am-pelatanana",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (treasury < 10_000_000) StatusRedDanger else AgroGreenPrimary
                                )
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (treasury < 10_000_000) StatusRedDanger else AgroGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(treasury / 1_000_000.0).toString().take(4)}M Ar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (treasury < 10_000_000) StatusRedDanger else AgroGreenPrimary
                            )
                            Text(
                                text = if (treasury < 10_000_000) "1.8 mois (< 2 mois !)" else "2.0 mois (Sécurisé)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (treasury < 10_000_000) StatusRedDanger else AgroGreenPrimary
                            )
                        }
                    }

                    // CA & Marge
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (lang == "FR") "CA Mensuel" else "Vola miditra",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = SpiceOrangeSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "41.2M Ar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lang == "FR") "Marge : 14.2% (Cible 14%)" else "Tombom-barotra : 14.2%",
                                fontSize = 11.sp,
                                color = AgroGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Bannière Génération Rapport Mensuel PDF (KPI & Pertes)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPdfReportDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = AgroGreenPrimary,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rapport Mensuel KPI & Pertes (PDF)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = AgroGreenPrimary
                                )
                                Text(
                                    text = "Analyse marges par lot, freinte & seuils Guide 2026",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { showPdfReportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AgroGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Générer PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Nouveaux Modules 2026 : Météo Temps Réel & Planification Logistique
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Météo & Dégradation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showWeatherRiskDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Météo & Stocks",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0369A1)
                                )
                                Text(
                                    text = "Hygrométrie & Aération",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }
                    }

                    // Planification Logistique
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showLogisticsPlanningDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SpiceOrangeSecondary,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Trajets & Freinte",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFC2410C)
                                )
                                Text(
                                    text = "Optimisation benne",
                                    fontSize = 10.sp,
                                    color = Color(0xFFEA580C)
                                )
                            }
                        }
                    }
                }

                // Outils Étape 2 : Marge Nette Fluctuante, Impact Carbone & Saisonnalité
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Marge Nette
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showNetMarginDialog = true }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Marge Nette", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AgroGreenPrimary)
                            Text("Frais réels", fontSize = 9.sp, color = Color(0xFF166534))
                        }
                    }

                    // Impact Carbone
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF16A34A))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCarbonFootprintDialog = true }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Éco-Carbone", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF16A34A))
                            Text("Eco-Score A-E", fontSize = 9.sp, color = Color(0xFF14532D))
                        }
                    }

                    // Saisonnalité & Réassort
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSeasonalityDialog = true }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = SpiceOrangeSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Saisonnalité", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = SpiceOrangeSecondary)
                            Text("Soudure & Réassort", fontSize = 9.sp, color = Color(0xFF92400E))
                        }
                    }
                }

                // Outils Étape 4 : Gestion BFR & Documents Commerciaux
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Contrôle BFR & Cash
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2563EB))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showWorkingCapitalDialog = true }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Contrôle BFR & Cash", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E40AF))
                                Text("2 mois charges • Stress test", fontSize = 9.sp, color = Color(0xFF3B82F6))
                            }
                        }
                    }
                    // Bons de Livraison & Audit
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7C3AED))),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCommercialDocDialog = true }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Documents & Audit", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF5B21B6))
                                Text("BL, NIF/STAT, Freinte", fontSize = 9.sp, color = Color(0xFF8B5CF6))
                            }
                        }
                    }
                }

                // Outils Étape 5 : Clôture Annuelle & Bilan d'Exploitation (Guide 2026 Chap. 14 & 16)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(AgroGreenPrimary)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAnnualClosingDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Clôture Annuelle & Bilan d'Exploitation (Étape 5)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                Text("P&L, Marges 8 denrées, Point Mort, Scorecard Audit & Export PDF certifié", fontSize = 9.5.sp, color = Color(0xFF15803D))
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AgroGreenPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Bannière Alerte Lundi Matin chez Sambatra Négoce (Cas p. 71)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SpiceOrangeSecondary)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMondayModal = !showMondayModal }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusAmberWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == "FR") "Alerte Lundi Matin (3 voyants rouges)" else "Fampitandremana Alatsinainy maraina",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Icon(
                            if (showMondayModal) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color(0xFF92400E)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (lang == "FR")
                            "Comme décrit au Chapitre 12 (p. 71) : le tableau de bord détecte 3 voyants rouges nécessitant 3 décisions fermes en 30 minutes !"
                        else
                            "Tahaka ny ao amin'ny Toko 12 : misy fampitandremana mena 3 mila fanapahan-kevitra maika ao anatin'ny 30 minitra !",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F)
                    )

                    AnimatedVisibility(visible = showMondayModal) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mondayAlerts.forEach { alert ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = alert.title,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusRedDanger,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alert.description,
                                            fontSize = 12.sp,
                                            color = TextPrimaryLight
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Actions livre : " + alert.recommendedActions.joinToString(" • "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AgroGreenPrimary
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.selectTab(AppTab.ADVISOR) },
                                colors = ButtonDefaults.buttonColors(containerColor = SpiceOrangeSecondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "FR") "Résoudre avec le Conseiller GrosGrains" else "Vahana amin'ny Mpanolotsaina AI",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Surveillance des Stocks & Niveaux Critiques (Guide 2026, Ch. 6.3)
        item {
            CriticalStockAlertCard(
                products = viewModel.products.collectAsState().value,
                lang = lang,
                onNavigateToStock = { viewModel.selectTab(AppTab.STOCKS) }
            )
        }

        // Visualisation de données Recharts : Évolution des stocks & Taux de rotation par catégorie (Guide 2026 Ch. 6)
        item {
            com.example.ui.components.RechartsStockVisualizer(lang = lang)
        }

        // Évolution du Prix d'Achat Moyen sur 30 jours (Composant Graphique Natif)
        item {
            com.example.ui.components.PriceTrendChart()
        }

        // Section 15 Indicateurs Clés
        item {
            SectionTitle(
                title = if (lang == "FR") "Les 15 Indicateurs du Guide (p. 68)" else "Ireo tondro 15 ao amin'ny Boky",
                subtitle = if (lang == "FR") "Mis à jour chaque semaine avec cibles et seuils d'alerte stricts" else "Havaozina isan-kerinandro miaraka amin'ny fetra"
            )

            // Filtres par domaine
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(domains) { dom ->
                    FilterChip(
                        selected = selectedDomainFilter == dom,
                        onClick = { selectedDomainFilter = dom },
                        label = { Text(dom, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgroGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Liste des KPIs
        items(filteredKpis) { kpi ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "[${kpi.domain}]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpiceOrangeSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = kpi.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = kpi.valueDisplay,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when (kpi.status) {
                                    KpiStatus.GREEN -> StatusGreenOk
                                    KpiStatus.AMBER -> StatusAmberWarning
                                    KpiStatus.RED -> StatusRedDanger
                                }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Cible: ${kpi.targetDisplay}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = kpi.explanation,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusBadge(status = kpi.status)
                }
            }
        }

        // Les 4 principes fondamentaux du livre
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Les 4 Principes Directeurs du Guide (p. 4)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgroGreenPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Ce qui n'est pas mesuré n'est pas géré (poids, humidité, délais, marge).\n" +
                                "2. Séparez les rôles (achat ≠ paiement ≠ magasin ≠ caisse).\n" +
                                "3. La marge se gagne à l'achat, se protège au stockage et se perd au crédit.\n" +
                                "4. Chaque lot a une histoire (traçabilité amont/aval, contre-échantillon scellé).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (showPdfReportDialog) {
        com.example.ui.components.MonthlyPdfReportDialog(
            viewModel = viewModel,
            onDismiss = { showPdfReportDialog = false }
        )
    }

    if (showWeatherRiskDialog) {
        com.example.ui.components.WeatherRiskModuleDialog(
            onDismiss = { showWeatherRiskDialog = false }
        )
    }

    if (showLogisticsPlanningDialog) {
        com.example.ui.components.LogisticsPlanningDialog(
            onDismiss = { showLogisticsPlanningDialog = false }
        )
    }

    if (showNetMarginDialog) {
        com.example.ui.components.NetMarginCalculatorDialog(
            initialProduct = productsList.firstOrNull(),
            onDismiss = { showNetMarginDialog = false }
        )
    }

    if (showCarbonFootprintDialog) {
        com.example.ui.components.CarbonFootprintCalculatorDialog(
            onDismiss = { showCarbonFootprintDialog = false }
        )
    }

    if (showSeasonalityDialog) {
        com.example.ui.components.SeasonalityForecastDialog(
            viewModel = viewModel,
            onDismiss = { showSeasonalityDialog = false }
        )
    }

    if (showWorkingCapitalDialog) {
        com.example.ui.components.WorkingCapitalDialog(
            viewModel = viewModel,
            onDismiss = { showWorkingCapitalDialog = false }
        )
    }

    if (showCommercialDocDialog) {
        com.example.ui.components.CommercialDocumentDialog(
            viewModel = viewModel,
            onDismiss = { showCommercialDocDialog = false }
        )
    }

    if (showAnnualClosingDialog) {
        com.example.ui.components.AnnualClosingDialog(
            viewModel = viewModel,
            onDismiss = { showAnnualClosingDialog = false }
        )
    }
}

@Composable
fun CriticalStockAlertCard(
    products: List<com.example.data.model.Product>,
    lang: String,
    onNavigateToStock: () -> Unit
) {
    // Calcul des alertes de seuils selon Chapitre 6.3 (p. 31)
    val criticalProducts = products.filter { p ->
        // Si le stock est inférieur ou égal au point de commande (ou proche du stock de sécurité)
        val safetyStock = p.dailyConsumptionKg * 15 // Référence sécurité
        p.currentStockKg <= (p.dailyConsumptionKg * 28) // Moins de 28 jours de couverture
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        tint = SpiceOrangeSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (lang == "FR") "Surveillance des Seuils Critiques" else "Fanaraha-maso ny Fetran'ny Tahiry",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgroGreenPrimary
                        )
                        Text(
                            text = "Formule Stock Sécurité & Point de Commande (Guide Ch. 6.3, p. 31)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${criticalProducts.size} réf. à surveiller",
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cas emblématique du Pois du Cap (Kabaro) proche du point de commande
            criticalProducts.forEach { p ->
                val isBelowSafety = p.currentStockKg <= (p.dailyConsumptionKg * 15)
                val statusColor = if (isBelowSafety) StatusRedDanger else StatusAmberWarning
                val statusText = if (isBelowSafety) "STOCK DE SÉCURITÉ ATTEINT !" else "POINT DE COMMANDE PROCHE"

                Surface(
                    color = if (isBelowSafety) Color(0xFFFEE2E2) else Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = p.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stock actuel : ${p.currentStockKg.toInt()} kg (${p.coverageDays} jours)",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Cible: ${p.targetCoverageDays} jours",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (p.coverageDays / (p.targetCoverageDays * 1.5f)).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = statusColor,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNavigateToStock,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == "FR") "Gérer les réapprovisionnements & lots" else "Hitantana ny fividianana & loko",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
