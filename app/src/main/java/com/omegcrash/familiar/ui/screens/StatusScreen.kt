package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.gson.JsonObject
import com.omegcrash.familiar.data.AgentConfig
import com.omegcrash.familiar.data.FamiliarClient
import com.omegcrash.familiar.data.SkillDetail
import com.omegcrash.familiar.data.ToolInfo
import com.omegcrash.familiar.service.FamiliarService
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.components.StatusBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(serviceState: ServiceState) {
    val client = remember {
        FamiliarClient(apiKey = FamiliarService.dashboardApiKey)
    }
    var agentStatus by remember { mutableStateOf<JsonObject?>(null) }
    var config by remember { mutableStateOf<AgentConfig?>(null) }
    var skills by remember { mutableStateOf<List<SkillDetail>>(emptyList()) }
    var tools by remember { mutableStateOf<List<ToolInfo>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(serviceState) {
        if (serviceState is ServiceState.Running) {
            client.getStatus().onSuccess { agentStatus = it }
            client.getConfig().onSuccess { config = it }
            client.getSkills().onSuccess { skills = it.skills }
            client.getTools().onSuccess { tools = it.tools }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Agent Status") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Service health
            StatusBar(serviceState = serviceState)

            // Service info card
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

            // Agent info card
            config?.let { cfg ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Agent", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow("Name", cfg.agent_name.ifBlank { "Familiar" })
                        InfoRow("Provider", cfg.provider)
                        agentStatus?.let { status ->
                            if (status.has("uptime")) {
                                InfoRow("Uptime", status.get("uptime").asString)
                            }
                        }
                        InfoRow("Memory", if (cfg.memory_enabled) "Enabled" else "Disabled")
                        InfoRow("Skills", if (cfg.skills_enabled) "Enabled" else "Disabled")
                        InfoRow("Scheduler", if (cfg.scheduler_enabled) "Enabled" else "Disabled")
                    }
                }
            }

            // Skills card with toggles
            if (skills.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Skills", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        skills.forEach { skill ->
                            var enabled by remember(skill.name) { mutableStateOf(skill.enabled) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = skill.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                )
                                Switch(
                                    checked = enabled,
                                    onCheckedChange = { newValue ->
                                        enabled = newValue
                                        scope.launch {
                                            client.toggleSkill(skill.name).onSuccess {
                                                enabled = it.enabled
                                            }.onFailure {
                                                enabled = !newValue // revert
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Tools summary card
            if (tools.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tools", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        val byCategory = tools.groupBy { it.category.ifBlank { "general" } }
                        byCategory.forEach { (category, categoryTools) ->
                            Text(
                                text = "${category.replaceFirstChar { it.uppercase() }}: ${categoryTools.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${tools.size} tools total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
