package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.omegcrash.familiar.data.PreferencesStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { PreferencesStore(context) }
    val scope = rememberCoroutineScope()
    val envVars by prefs.getEnvVars().collectAsState(initial = emptyMap())
    val connectionMode by prefs.connectionMode.collectAsState(initial = "local")
    val serverUrl by prefs.serverUrl.collectAsState(initial = "")
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // Current config
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Current Configuration", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    val modeLabel = if (connectionMode == "remote") "Remote" else "Local"
                    Text("Mode: $modeLabel")

                    if (connectionMode == "remote" && serverUrl.isNotBlank()) {
                        Text("Server: $serverUrl")
                    }

                    val provider = envVars["FAMILIAR_LLM_PROVIDER"] ?: "Not set"
                    Text("Provider: ${provider.replaceFirstChar { it.uppercase() }}")

                    val model = envVars["FAMILIAR_MODEL"]
                    if (!model.isNullOrBlank()) {
                        Text("Model: $model")
                    }

                    val agentName = envVars["FAMILIAR_AGENT_NAME"]
                    if (!agentName.isNullOrBlank()) {
                        Text("Agent Name: $agentName")
                    }

                    // Show masked API key
                    val hasKey = envVars.keys.any { it.endsWith("_API_KEY") }
                    if (hasKey) {
                        Text("API Key: ****configured****")
                    }
                }
            }

            // About
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Familiar for Android v1.3.0")
                    Text("Self-hosted AI companion")
                    Text(
                        text = "Your data stays on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Reset button
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Reset Configuration")
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Configuration?") },
            text = { Text("This will clear your API keys and settings. You'll need to set up Familiar again.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        prefs.clearAll()
                        showResetDialog = false
                    }
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
