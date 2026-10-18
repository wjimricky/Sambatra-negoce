package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.theme.SpiceOrangeSecondary
import com.example.ui.theme.StatusAmberWarning
import com.example.ui.viewmodel.ConseillerGrosGrainsViewModel
import com.example.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val titleFr: String, val titleMg: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Tableau de bord", "Biraon'ny orinasa", Icons.Default.Dashboard)
    object Stocks : Screen("stocks", "Stocks & Lots", "Tahiry & Loko", Icons.Default.Inventory2)
    object Ventes : Screen("ventes", "Ventes & Crédits", "Varotra & Trosa", Icons.Default.People)
    object Conseiller : Screen("conseiller", "Conseiller IA", "Mpanolotsaina AI", Icons.Default.Psychology)
    object Outils : Screen("outils", "Checklists & Cas", "Fitaovana & Ohatra", Icons.AutoMirrored.Filled.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = viewModel(),
    conseillerViewModel: ConseillerGrosGrainsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
    val lang by mainViewModel.selectedLanguage.collectAsState()

    val screens = listOf(
        Screen.Dashboard,
        Screen.Stocks,
        Screen.Ventes,
        Screen.Conseiller,
        Screen.Outils
    )

    val selectedTab by mainViewModel.selectedTab.collectAsState()
    LaunchedEffect(selectedTab) {
        val targetRoute = when (selectedTab) {
            com.example.data.model.AppTab.DASHBOARD -> Screen.Dashboard.route
            com.example.data.model.AppTab.STOCKS -> Screen.Stocks.route
            com.example.data.model.AppTab.CLIENTS -> Screen.Ventes.route
            com.example.data.model.AppTab.ADVISOR -> Screen.Conseiller.route
            com.example.data.model.AppTab.TOOLS -> Screen.Outils.route
        }
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(Screen.Dashboard.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    var showSyncDialog by remember { mutableStateOf(false) }
    val syncState by mainViewModel.syncState.collectAsState()

    LaunchedEffect(Unit) {
        mainViewModel.initRoom(context)
    }

    if (showSyncDialog) {
        com.example.ui.components.SyncStatusDialog(
            viewModel = mainViewModel,
            onDismiss = { showSyncDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SpiceOrangeSecondary,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GrosGrains Pro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sambatra Négoce • Guide 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Indicateur Statut Synchronisation Room
                    Surface(
                        color = if (syncState.isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showSyncDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (syncState.isOnline) AgroGreenPrimary else StatusAmberWarning)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (syncState.isOnline) {
                                    if (syncState.isSyncing) "Sync..." else "En ligne"
                                } else {
                                    "Hors-ligne (${syncState.pendingCount})"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (syncState.isOnline) AgroGreenPrimary else Color(0xFF92400E)
                            )
                        }
                    }

                    // Sélecteur de langue Français / Malagasy
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        TextButton(
                            onClick = { mainViewModel.toggleLanguage() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (lang == "FR") "🇲🇬 MG" else "🇫🇷 FR",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AgroGreenPrimary
                            )
                        }
                    }

                    // Accès rapide au Conseiller IA
                    IconButton(
                        onClick = {
                            if (currentRoute != Screen.Conseiller.route) {
                                navController.navigate(Screen.Conseiller.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Conseiller IA",
                            tint = SpiceOrangeSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    val label = if (lang == "FR") screen.titleFr else screen.titleMg

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgroGreenPrimary,
                            selectedTextColor = AgroGreenPrimary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Écran d'accueil : Tableau de bord
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = mainViewModel)
            }

            // Écran secondaire : Gestion des Stocks & Traçabilité Lots
            composable(Screen.Stocks.route) {
                BackHandler {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
                StockScreen(viewModel = mainViewModel)
            }

            // Écran secondaire : Ventes & Crédits clients
            composable(Screen.Ventes.route) {
                BackHandler {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
                ClientsScreen(viewModel = mainViewModel)
            }

            // Écran secondaire : Chat & Recommandations avec le Conseiller IA
            composable(Screen.Conseiller.route) {
                BackHandler {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
                AdvisorScreen(viewModel = mainViewModel)
            }

            // Écran secondaire : Checklists, Outils & Études de Cas
            composable(Screen.Outils.route) {
                BackHandler {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                    }
                }
                ToolsScreen(viewModel = mainViewModel)
            }
        }
    }
}
