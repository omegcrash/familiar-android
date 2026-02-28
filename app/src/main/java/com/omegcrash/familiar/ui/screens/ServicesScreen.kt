package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.omegcrash.familiar.data.ConnectStatus
import com.omegcrash.familiar.data.FamiliarClient
import com.omegcrash.familiar.data.ServiceInfo
import com.omegcrash.familiar.service.FamiliarService
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.components.ServiceCard
import kotlinx.coroutines.launch

private val categoryLabels = mapOf(
    "llm" to "LLM Providers",
    "integrations" to "Integrations",
    "selfhosted" to "Self-Hosted",
    "security" to "Security",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    serviceState: ServiceState,
    onNavigateToChat: (prefill: String) -> Unit,
) {
    val client = remember {
        FamiliarClient(apiKey = FamiliarService.dashboardApiKey)
    }
    var connectStatus by remember { mutableStateOf<ConnectStatus?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            client.getConnectStatus().onSuccess { connectStatus = it }
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(serviceState) {
        if (serviceState is ServiceState.Running) {
            isLoading = true
            loadData()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Services") })
        },
    ) { padding ->
        when {
            serviceState !is ServiceState.Running -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Service not running",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            isLoading && connectStatus == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                val status = connectStatus ?: return@Scaffold
                val grouped = status.services.groupBy { it.category }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        loadData()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Active provider header
                        item {
                            Text(
                                text = "Active provider: ${status.active_provider}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }

                        for (category in status.categories) {
                            val services = grouped[category] ?: continue

                            item {
                                Text(
                                    text = categoryLabels[category] ?: category,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }

                            items(services, key = { it.name }) { service ->
                                ServiceCard(
                                    service = service,
                                    onTap = {
                                        if (!service.connected) {
                                            onNavigateToChat(service.connect_command)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
