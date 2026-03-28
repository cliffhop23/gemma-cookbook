package com.ruview.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ruview.android.ui.navigation.Screen
import com.ruview.android.ui.navigation.bottomNavItems
import com.ruview.android.ui.screens.live.LiveScreen
import com.ruview.android.ui.screens.mat.MatScreen
import com.ruview.android.ui.screens.settings.SettingsScreen
import com.ruview.android.ui.screens.vitals.VitalsScreen
import com.ruview.android.ui.screens.zones.ZonesScreen
import com.ruview.android.ui.theme.Background
import com.ruview.android.ui.theme.CardBackground
import com.ruview.android.ui.theme.CardBorder
import com.ruview.android.ui.theme.Primary
import com.ruview.android.ui.theme.TextSecondary

@Composable
fun RuViewApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = androidx.compose.ui.unit.Dp.Hairline
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Primary,
                            selectedTextColor = Primary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = Color(0xFF1A2535)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Live.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Live.route)     { LiveScreen() }
            composable(Screen.Vitals.route)   { VitalsScreen() }
            composable(Screen.Zones.route)    { ZonesScreen() }
            composable(Screen.Mat.route)      { MatScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
