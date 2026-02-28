package com.omegcrash.familiar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.omegcrash.familiar.data.PreferencesStore
import kotlinx.coroutines.launch

private val PROVIDERS = listOf("anthropic", "openai", "gemini", "ollama")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesStore(context) }
    val scope = rememberCoroutineScope()

    var connectionMode by remember { mutableStateOf("local") }
    var serverUrl by remember { mutableStateOf("") }
    var remoteApiKey by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf(PROVIDERS[0]) }
    var apiKey by remember { mutableStateOf("") }
    var ollamaUrl by remember { mutableStateOf("http://localhost:11434") }
    var agentName by remember { mutableStateOf("Familiar") }
    var modelName by remember { mutableStateOf("") }
    var providerExpanded by remember { mutableStateOf(false) }
    val isRemote = connectionMode == "remote"

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Welcome to Familiar") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Configure your AI agent",
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = "Your API keys are stored locally on this device and never leave it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Connection mode toggle
            Text(
                text = "Connection Mode",
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilterChip(
                    selected = connectionMode == "local",
                    onClick = { connectionMode = "local" },
                    label = { Text("On this device") },
                )
                FilterChip(
                    selected = connectionMode == "remote",
                    onClick = { connectionMode = "remote" },
                    label = { Text("Remote server") },
                )
            }

            if (isRemote) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Connect to a remote Familiar server",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                        Text(
                            text = "The AI provider and model are configured on the server. " +
                                "You only need the server URL and dashboard API key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("Server URL") },
                    placeholder = { Text("https://familiar.example.com:5000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (serverUrl.startsWith("http://") && !serverUrl.contains("localhost") && !serverUrl.contains("127.0.0.1") && !serverUrl.contains("192.168.")) {
                            Text(
                                text = "Warning: Using HTTP over the internet is insecure. Use HTTPS.",
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )

                OutlinedTextField(
                    value = remoteApiKey,
                    onValueChange = { remoteApiKey = it },
                    label = { Text("Dashboard API Key") },
                    placeholder = { Text("From ~/.familiar/.dashboard_key on server") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )
            }

            if (!isRemote) {
            // Provider selector
            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = it },
            ) {
                OutlinedTextField(
                    value = provider.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("LLM Provider") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false },
                ) {
                    PROVIDERS.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                provider = p
                                providerExpanded = false
                            },
                        )
                    }
                }
            }

            // API key (not for Ollama)
            if (provider != "ollama") {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                )
            }

            // Ollama setup
            if (provider == "ollama") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Run Ollama locally",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "Install Termux from F-Droid, then run:\npkg install ollama && ollama serve",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://f-droid.org/packages/com.termux/"),
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            Text("Get Termux from F-Droid")
                        }
                    }
                }

                OutlinedTextField(
                    value = ollamaUrl,
                    onValueChange = { ollamaUrl = it },
                    label = { Text("Ollama URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            // Model name
            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Model (optional)") },
                placeholder = {
                    Text(
                        when (provider) {
                            "anthropic" -> "claude-sonnet-4-20250514"
                            "openai" -> "gpt-4o"
                            "gemini" -> "gemini-2.0-flash"
                            "ollama" -> "llama3.2"
                            else -> ""
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = if (provider == "ollama") {
                    {
                        Text(
                            text = "Supported: llama3.2, deepseek-r1, qwen2.5, mistral, " +
                                "gemma3, phi3, smollm2, tinyllama",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    null
                },
            )
            } // end if (!isRemote)

            // Agent name
            OutlinedTextField(
                value = agentName,
                onValueChange = { agentName = it },
                label = { Text("Agent Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        prefs.saveSetup(
                            provider = provider,
                            apiKey = apiKey,
                            ollamaUrl = ollamaUrl,
                            agentName = agentName,
                            modelName = modelName,
                            connectionMode = connectionMode,
                            serverUrl = serverUrl,
                            remoteApiKey = remoteApiKey,
                        )
                        onSetupComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (isRemote) {
                    serverUrl.isNotBlank()
                } else {
                    provider == "ollama" || apiKey.isNotBlank()
                },
            ) {
                Text(if (isRemote) "Connect to Server" else "Start Familiar")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
