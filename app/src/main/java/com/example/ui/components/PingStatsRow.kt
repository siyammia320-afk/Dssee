package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.PingResultState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.RedHighPing
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric

@Composable
fun PingStatsRow(
    pingState: PingResultState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ping_stats_container")
    ) {
        // Row 1: Min / Avg / Max
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "MIN PING",
                value = if (pingState.packetsSent == 0) "--" else "${pingState.minPingMs} ms",
                valueColor = EmeraldPing,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "AVG PING",
                value = if (pingState.packetsSent == 0) "--" else "${pingState.avgPingMs} ms",
                valueColor = CyanNeon,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "MAX PING",
                value = if (pingState.packetsSent == 0) "--" else "${pingState.maxPingMs} ms",
                valueColor = AmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Jitter / Loss / Stability
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatBox(
                label = "JITTER",
                value = if (pingState.packetsSent == 0) "--" else "${pingState.jitterMs} ms",
                valueColor = VioletElectric,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "PACKET LOSS",
                value = if (pingState.packetsSent == 0) "--" else String.format("%.1f%%", pingState.packetLossPercent),
                valueColor = if (pingState.packetLossPercent > 5f) RedHighPing else TextSecondary,
                modifier = Modifier.weight(1f)
            )
            StatBox(
                label = "STABILITY",
                value = if (pingState.packetsSent == 0) "--" else "${pingState.stabilityScore}%",
                valueColor = if (pingState.stabilityScore > 85) EmeraldPing else AmberWarning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
