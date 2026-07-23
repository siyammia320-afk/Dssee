package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.PingViewModel
import com.example.ui.components.FloatingHudCard
import com.example.ui.components.FloatingHudSimulatorCard
import com.example.ui.components.LatencyWaveformChart
import com.example.ui.components.PingGaugeCard
import com.example.ui.components.PingStatsRow
import com.example.ui.components.TargetSelectorCard

@Composable
fun DashboardScreen(
    viewModel: PingViewModel,
    modifier: Modifier = Modifier
) {
    val pingState by viewModel.pingState.collectAsState()
    val selectedTarget by viewModel.selectedTarget.collectAsState()
    val customHost by viewModel.customHostInput.collectAsState()
    val customPort by viewModel.customPortInput.collectAsState()

    val isHudActive by viewModel.isHudActive.collectAsState()
    val hudScale by viewModel.hudScale.collectAsState()
    val isVpnActive by viewModel.isVpnActive.collectAsState()
    val vpnCountdown by viewModel.vpnCountdownSeconds.collectAsState()
    val isTeleActive by viewModel.isTeleActive.collectAsState()
    val isFreezeActive by viewModel.isFreezeActive.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Floating HUD Controller Card with START button & VPN/TELE/FREZ buttons
        FloatingHudCard(
            isHudActive = isHudActive,
            hudScale = hudScale,
            isVpnActive = isVpnActive,
            vpnCountdown = vpnCountdown,
            isTeleActive = isTeleActive,
            isFreezeActive = isFreezeActive,
            onToggleHud = { viewModel.toggleHud() },
            onSetHudScale = { viewModel.setHudScale(it) },
            onToggleVpn = { viewModel.toggleVpn(context) },
            onToggleTele = { viewModel.toggleTele() },
            onToggleFreeze = { viewModel.toggleFreeze() }
        )

        // Main Circular Ping Gauge Card
        PingGaugeCard(
            pingState = pingState,
            onStartPing = { viewModel.startPing() },
            onStopPing = { viewModel.stopPing() },
            onSaveTest = { viewModel.saveCurrentTestToHistory() }
        )

        // Realtime Latency Waveform Chart
        LatencyWaveformChart(
            packets = pingState.packetHistory,
            avgPingMs = pingState.avgPingMs
        )

        // Telemetry Stats (Min, Max, Avg, Jitter, Loss, Stability)
        PingStatsRow(pingState = pingState)

        // Target Selector Card
        TargetSelectorCard(
            selectedTarget = selectedTarget,
            onSelectTarget = { viewModel.selectTarget(it) },
            customHost = customHost,
            customPort = customPort,
            onUpdateCustomHost = { viewModel.updateCustomHost(it) },
            onUpdateCustomPort = { viewModel.updateCustomPort(it) },
            onSetCustomTarget = { viewModel.setCustomTarget() }
        )

        // Floating HUD Simulator Card
        FloatingHudSimulatorCard()

        Spacer(modifier = Modifier.height(24.dp))
    }
}
