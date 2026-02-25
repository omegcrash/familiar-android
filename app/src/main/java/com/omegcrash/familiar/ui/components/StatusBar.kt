package com.omegcrash.familiar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omegcrash.familiar.service.ServiceState

@Composable
fun StatusBar(
    serviceState: ServiceState,
    modifier: Modifier = Modifier,
) {
    val (color, label) = when (serviceState) {
        is ServiceState.Idle -> Color.Gray to "Idle"
        is ServiceState.Starting -> Color.Yellow to "Starting..."
        is ServiceState.Running -> Color(0xFF4CAF50) to "Running on :${serviceState.port}"
        is ServiceState.Error -> Color.Red to "Error: ${serviceState.message}"
        is ServiceState.Stopped -> Color.Gray to "Stopped"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
