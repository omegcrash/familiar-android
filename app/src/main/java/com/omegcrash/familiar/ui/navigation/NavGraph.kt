package com.omegcrash.familiar.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.omegcrash.familiar.data.PreferencesStore
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.screens.ChatScreen
import com.omegcrash.familiar.ui.screens.SettingsScreen
import com.omegcrash.familiar.ui.screens.SetupScreen
import com.omegcrash.familiar.ui.screens.StatusScreen

object Routes {
    const val SETUP = "setup"
    const val CHAT = "chat"
    const val STATUS = "status"
    const val SETTINGS = "settings"
}

@Composable
fun NavGraph(serviceState: ServiceState) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = PreferencesStore(context)
    val isSetup by prefs.isSetupComplete.collectAsState(initial = false)

    val startDest = if (isSetup) Routes.CHAT else Routes.SETUP

    NavHost(navController = navController, startDestination = startDest) {
        composable(Routes.SETUP) {
            SetupScreen(
                onSetupComplete = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.SETUP) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(
                serviceState = serviceState,
                onNavigateToStatus = { navController.navigate(Routes.STATUS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.STATUS) {
            StatusScreen(
                serviceState = serviceState,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
