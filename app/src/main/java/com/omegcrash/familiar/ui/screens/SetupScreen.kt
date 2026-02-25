package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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

private val PROVIDERS = listOf("anthropic", "openai", "ollama")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onSetupComplete: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferencesStore(context) }
    val scope = rememberCoroutineScope()

    var provider by remember { mutableStateOf(PROVIDERS[0]) }
    var apiKey by remember { mutableStateOf("") }
    var ollamaUrl by remember { mutableStateOf("http://localhost:11434") }
    var agentName by remember { mutableStateOf("Familiar") }
    var modelName by remember { mutableStateOf("") }
    var providerExpanded by remember { mutableStateOf(false) }

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

            // Ollama URL
            if (provider == "ollama") {
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
                            "ollama" -> "llama3.1"
                            else -> ""
                        },
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

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
                        )
                        onSetupComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = provider == "ollama" || apiKey.isNotBlank(),
            ) {
                Text("Start Familiar")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
