package com.example.ecobite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize // ✅ added
import androidx.compose.material.icons.filled.BarChart
import com.example.ecobite.ui.dashboard.DashboardScreen
import com.example.ecobite.ui.pantry.AddItemScreen
import com.example.ecobite.ui.pantry.BarcodeScannerScreen
import com.example.ecobite.ui.pantry.PantryScreen
import com.example.ecobite.ui.pantry.PantryViewModel
import com.example.ecobite.ui.waste.WasteLogScreen
import com.example.ecobite.ui.dashboard.AnalyticsScreen
import com.example.ecobite.ui.dashboard.AnalyticsViewModel

// ── Route constants ───────────────────────────────────────────────────────────
object Routes {
    const val DASHBOARD      = "dashboard"
    const val PANTRY         = "pantry"
    const val ADD_ITEM       = "add_item"
    const val WASTE_LOG      = "waste_log"
    const val BARCODE_SCANNER = "barcode_scanner"
    const val ANALYTICS = "analytics"
}

// ── Bottom nav items ──────────────────────────────────────────────────────────
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Filled.Dashboard, Routes.DASHBOARD),
    BottomNavItem("Pantry",    Icons.Filled.Kitchen,   Routes.PANTRY),
    BottomNavItem("Waste Log", Icons.Filled.Delete,    Routes.WASTE_LOG),
    BottomNavItem("Analytics", Icons.Filled.BarChart,  Routes.ANALYTICS),
)

// ── Main navigation ───────────────────────────────────────────────────────────
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: PantryViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Routes.DASHBOARD,
        Routes.PANTRY,
        Routes.WASTE_LOG,
        Routes.ANALYTICS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(
                                        navController.graph
                                            .findStartDestination().id
                                    ) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController    = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier
                .fillMaxSize()        // 👈 THIS FIXES ALL SCREENS
                .padding(paddingValues)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    viewModel    = viewModel,
                    onGoToPantry = { navController.navigate(Routes.PANTRY) }
                )
            }
            composable(Routes.PANTRY) {
                PantryScreen(
                    viewModel = viewModel,
                    onAddItem = { navController.navigate(Routes.ADD_ITEM) }
                )
            }
            composable(Routes.ADD_ITEM) {
                AddItemScreen(
                    viewModel      = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenScanner  = {
                        navController.navigate(Routes.BARCODE_SCANNER)
                    }
                )
            }
            composable(Routes.WASTE_LOG) {
                WasteLogScreen(
                    viewModel      = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BARCODE_SCANNER) {
                BarcodeScannerScreen(
                    onBarcodeDetected = { barcode ->
                        viewModel.lookupBarcode(barcode)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ANALYTICS) {
                val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
                AnalyticsScreen(viewModel = analyticsViewModel)
            }
        }
    }
}