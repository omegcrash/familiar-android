package com.omegcrash.familiar.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.omegcrash.familiar.data.PreferencesStore
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.screens.ChatScreen
import com.omegcrash.familiar.ui.screens.ServicesScreen
import com.omegcrash.familiar.ui.screens.SettingsScreen
import com.omegcrash.familiar.ui.screens.SetupScreen
import com.omegcrash.familiar.ui.screens.StatusScreen

object Routes {
    const val SETUP = "setup"
    const val CHAT = "chat"
    const val SERVICES = "services"
    const val STATUS = "status"
    const val SETTINGS = "settings"
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    TabItem(Routes.CHAT, "Chat", Icons.AutoMirrored.Filled.Chat),
    TabItem(Routes.SERVICES, "Services", Icons.Default.Hub),
    TabItem(Routes.STATUS, "Status", Icons.Default.Info),
    TabItem(Routes.SETTINGS, "Settings", Icons.Default.Settings),
)

@Composable
fun NavGraph(serviceState: ServiceState) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = PreferencesStore(context)
    val isSetup by prefs.isSetupComplete.collectAsState(initial = false)

    val startDest = if (isSetup) Routes.CHAT else Routes.SETUP

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route?.substringBefore("?")
    val showBottomBar = currentRoute in tabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDest,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.SETUP) {
                SetupScreen(
                    onSetupComplete = {
                        navController.navigate(
                            "${Routes.CHAT}?prefill=Hello! I just finished setting up."
                        ) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = "${Routes.CHAT}?prefill={prefill}",
                arguments = listOf(
                    navArgument("prefill") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { backStackEntry ->
                val prefill = backStackEntry.arguments?.getString("prefill") ?: ""
                ChatScreen(
                    serviceState = serviceState,
                    initialMessage = prefill,
                )
            }

            composable(Routes.SERVICES) {
                ServicesScreen(
                    serviceState = serviceState,
                    onNavigateToChat = { prefill ->
                        navController.navigate("${Routes.CHAT}?prefill=$prefill") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                )
            }

            composable(Routes.STATUS) {
                StatusScreen(serviceState = serviceState)
            }

            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
        }
    }
}
