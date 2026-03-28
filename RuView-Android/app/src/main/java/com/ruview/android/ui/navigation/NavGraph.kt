package com.ruview.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Live     : Screen("live",     "Live",     Icons.Default.Wifi)
    object Vitals   : Screen("vitals",   "Vitals",   Icons.Default.FitnessCenter)
    object Zones    : Screen("zones",    "Zones",    Icons.Default.Map)
    object Mat      : Screen("mat",      "MAT",      Icons.Default.MedicalServices)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Live,
    Screen.Vitals,
    Screen.Zones,
    Screen.Mat,
    Screen.Settings
)
