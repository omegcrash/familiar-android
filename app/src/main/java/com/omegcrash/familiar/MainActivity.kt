package com.omegcrash.familiar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.omegcrash.familiar.service.FamiliarService
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.navigation.NavGraph
import com.omegcrash.familiar.ui.theme.FamiliarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start the foreground service
        val serviceIntent = Intent(this, FamiliarService::class.java)
        startForegroundService(serviceIntent)

        setContent {
            FamiliarTheme {
                val serviceState by FamiliarService.state.collectAsState()
                NavGraph(serviceState = serviceState)
            }
        }
    }
}
