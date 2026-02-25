package com.omegcrash.familiar.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.omegcrash.familiar.R
import com.omegcrash.familiar.data.ChatResponse
import com.omegcrash.familiar.data.FamiliarClient
import com.omegcrash.familiar.service.ServiceState
import com.omegcrash.familiar.ui.components.MessageBubble
import com.omegcrash.familiar.ui.components.StatusBar
import com.omegcrash.familiar.ui.components.ToolCallCard
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val toolCalls: List<ChatResponse>? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    serviceState: ServiceState,
    onNavigateToStatus: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val client = remember { FamiliarClient() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToStatus) {
                        Icon(Icons.Default.Info, contentDescription = "Status")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            StatusBar(serviceState = serviceState)

            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                items(messages) { msg ->
                    MessageBubble(
                        text = msg.text,
                        isUser = msg.isUser,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    // Show tool calls inline
                    msg.toolCalls?.forEach { response ->
                        response.tool_calls?.forEach { call ->
                            ToolCallCard(
                                skillName = call.skill,
                                args = call.args,
                                result = call.result,
                            )
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            // Input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message your Familiar...") },
                    enabled = serviceState is ServiceState.Running && !isLoading,
                    maxLines = 4,
                )
                IconButton(
                    onClick = {
                        val text = input.trim()
                        if (text.isNotEmpty()) {
                            input = ""
                            messages.add(ChatMessage(text = text, isUser = true))
                            isLoading = true
                            scope.launch {
                                val result = client.chat(text)
                                isLoading = false
                                result.onSuccess { response ->
                                    messages.add(
                                        ChatMessage(
                                            text = response.response,
                                            isUser = false,
                                            toolCalls = if (response.tool_calls != null) {
                                                listOf(response)
                                            } else {
                                                null
                                            },
                                        ),
                                    )
                                }.onFailure { err ->
                                    messages.add(
                                        ChatMessage(
                                            text = "Error: ${err.message}",
                                            isUser = false,
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    enabled = serviceState is ServiceState.Running
                        && !isLoading
                        && input.isNotBlank(),
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}
