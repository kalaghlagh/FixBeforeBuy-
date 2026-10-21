package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Diagnose : Screen("diagnose", "Diagnose", Icons.Default.Handyman)
    data object History : Screen("history", "History", Icons.Default.History)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object CostCalculator : Screen("cost_calculator", "Cost Comparison", Icons.Default.Calculate)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Home, Diagnose, History, Settings)
    }
}
