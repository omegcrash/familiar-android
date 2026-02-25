package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.omegcrash.familiar.data.FamiliarClient
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.components.StatusBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    serviceState: ServiceState,
    onBack: () -> Unit,
) {
    val client = remember { FamiliarClient() }
    var agentStatus by remember { mutableStateOf<JsonObject?>(null) }
    var skills by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(serviceState) {
        if (serviceState is ServiceState.Running) {
            client.getStatus().onSuccess { agentStatus = it }
            client.getSkills().onSuccess { skills = it }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Status") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusBar(serviceState = serviceState)

            // Service info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Service", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = when (serviceState) {
                            is ServiceState.Idle -> "Idle"
                            is ServiceState.Starting -> "Starting Python runtime..."
                            is ServiceState.Running -> "Running on port ${serviceState.port}"
                            is ServiceState.Error -> "Error: ${serviceState.message}"
                            is ServiceState.Stopped -> "Stopped"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Agent info from API
            agentStatus?.let { status ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Agent", style = MaterialTheme.typography.titleMedium)
                        status.entrySet().forEach { (key, value) ->
                            Text(
                                text = "$key: $value",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            // Skills
            if (skills.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Skills", style = MaterialTheme.typography.titleMedium)
                        skills.forEach { skill ->
                            Text(
                                text = skill,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
