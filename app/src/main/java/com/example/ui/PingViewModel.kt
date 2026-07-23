package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.PingHistoryEntity
import com.example.data.PingRepository
import com.example.network.DefaultServerTargets
import com.example.network.PingEngine
import com.example.network.PingResultState
import com.example.network.ServerTarget
import com.example.network.TargetCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ServerScanResult(
    val target: ServerTarget,
    val pingMs: Int,
    val isSuccess: Boolean,
    val isLoading: Boolean = false
)

data class DnsBenchmarkResult(
    val target: ServerTarget,
    val primaryPingMs: Int,
    val packetLossPercent: Float,
    val isRecommended: Boolean = false,
    val isLoading: Boolean = false
)

class PingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PingRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PingRepository(database.pingHistoryDao())
    }

    val historyList: StateFlow<List<PingHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Selected Target
    private val _selectedTarget = MutableStateFlow(DefaultServerTargets.servers.first())
    val selectedTarget: StateFlow<ServerTarget> = _selectedTarget.asStateFlow()

    // Live Ping State
    private val _pingState = MutableStateFlow(PingResultState(target = DefaultServerTargets.servers.first()))
    val pingState: StateFlow<PingResultState> = _pingState.asStateFlow()

    private var pingJob: Job? = null

    // Multi-Server Scan Results
    private val _serverScanResults = MutableStateFlow<List<ServerScanResult>>(emptyList())
    val serverScanResults: StateFlow<List<ServerScanResult>> = _serverScanResults.asStateFlow()

    private val _isScanningServers = MutableStateFlow(false)
    val isScanningServers: StateFlow<Boolean> = _isScanningServers.asStateFlow()

    // DNS Benchmark Results
    private val _dnsBenchmarkResults = MutableStateFlow<List<DnsBenchmarkResult>>(emptyList())
    val dnsBenchmarkResults: StateFlow<List<DnsBenchmarkResult>> = _dnsBenchmarkResults.asStateFlow()

    private val _isBenchmarkingDns = MutableStateFlow(false)
    val isBenchmarkingDns: StateFlow<Boolean> = _isBenchmarkingDns.asStateFlow()

    // Floating HUD State
    enum class HudScale(val label: String, val scaleFactor: Float) {
        SMALL("Small", 0.8f),
        MEDIUM("Medium", 1.0f),
        LARGE("Large", 1.25f)
    }

    private val _isHudActive = MutableStateFlow(false)
    val isHudActive: StateFlow<Boolean> = _isHudActive.asStateFlow()

    private val _hudScale = MutableStateFlow(HudScale.MEDIUM)
    val hudScale: StateFlow<HudScale> = _hudScale.asStateFlow()

    // 3 Special Floating Mode States (VPN, TELE, FREEZE)
    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    private val _vpnCountdownSeconds = MutableStateFlow(0)
    val vpnCountdownSeconds: StateFlow<Int> = _vpnCountdownSeconds.asStateFlow()

    private val _isTeleActive = MutableStateFlow(false)
    val isTeleActive: StateFlow<Boolean> = _isTeleActive.asStateFlow()

    private val _isFreezeActive = MutableStateFlow(false)
    val isFreezeActive: StateFlow<Boolean> = _isFreezeActive.asStateFlow()

    private var vpnTimerJob: Job? = null

    fun toggleHud() {
        _isHudActive.value = !_isHudActive.value
    }

    fun setHudScale(scale: HudScale) {
        _hudScale.value = scale
    }

    fun toggleVpn(context: android.content.Context? = null) {
        if (_isVpnActive.value) {
            // Turn off VPN
            _isVpnActive.value = false
            vpnTimerJob?.cancel()
            vpnTimerJob = null
            _vpnCountdownSeconds.value = 0

            context?.let { ctx ->
                try {
                    val intent = android.content.Intent(ctx, com.example.network.FlashVpnService::class.java).apply {
                        action = com.example.network.FlashVpnService.ACTION_DISCONNECT
                    }
                    ctx.startService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // Turn on VPN -> 10s Internet traffic pause/resync countdown
            _isVpnActive.value = true
            vpnTimerJob?.cancel()

            context?.let { ctx ->
                try {
                    val prepareIntent = android.net.VpnService.prepare(ctx)
                    if (prepareIntent == null) {
                        // Permission already granted -> start VPN service directly
                        val intent = android.content.Intent(ctx, com.example.network.FlashVpnService::class.java).apply {
                            action = com.example.network.FlashVpnService.ACTION_CONNECT
                        }
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            ctx.startForegroundService(intent)
                        } else {
                            ctx.startService(intent)
                        }
                    } else {
                        // Launch prepare intent if called from Activity
                        if (ctx is android.app.Activity) {
                            ctx.startActivityForResult(prepareIntent, 9001)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            vpnTimerJob = viewModelScope.launch {
                _vpnCountdownSeconds.value = 10
                while (_vpnCountdownSeconds.value > 0) {
                    kotlinx.coroutines.delay(1000L)
                    _vpnCountdownSeconds.value -= 1
                }
                // After 10s, traffic normalizes while VPN remains active
            }
        }
    }

    fun toggleTele() {
        _isTeleActive.value = !_isTeleActive.value
    }

    fun toggleFreeze() {
        _isFreezeActive.value = !_isFreezeActive.value
    }
    private val _customHostInput = MutableStateFlow("")
    val customHostInput: StateFlow<String> = _customHostInput.asStateFlow()

    private val _customPortInput = MutableStateFlow("80")
    val customPortInput: StateFlow<String> = _customPortInput.asStateFlow()

    fun updateCustomHost(host: String) {
        _customHostInput.value = host
    }

    fun updateCustomPort(port: String) {
        _customPortInput.value = port
    }

    fun selectTarget(target: ServerTarget) {
        stopPing()
        _selectedTarget.value = target
        _pingState.value = PingResultState(target = target)
    }

    fun setCustomTarget() {
        val host = _customHostInput.value.trim()
        if (host.isNotEmpty()) {
            val port = _customPortInput.value.toIntOrNull() ?: 80
            val custom = ServerTarget(
                id = "custom_${System.currentTimeMillis()}",
                name = "Custom ($host)",
                host = host,
                port = port,
                region = "User Defined",
                iconName = "globe",
                category = TargetCategory.CUSTOM
            )
            selectTarget(custom)
        }
    }

    fun startPing(count: Int = 30, intervalMs: Long = 400L) {
        stopPing()
        val target = _selectedTarget.value
        pingJob = viewModelScope.launch {
            PingEngine.pingStream(target, count, intervalMs).collect { newState ->
                _pingState.value = newState
            }
        }
    }

    fun stopPing() {
        pingJob?.cancel()
        pingJob = null
        _pingState.value = _pingState.value.copy(isRunning = false)
    }

    fun saveCurrentTestToHistory() {
        val state = _pingState.value
        if (state.packetsSent > 0) {
            viewModelScope.launch {
                repository.saveTestResult(
                    PingHistoryEntity(
                        targetHost = state.target.host,
                        targetName = state.target.name,
                        avgPingMs = state.avgPingMs,
                        minPingMs = state.minPingMs,
                        maxPingMs = state.maxPingMs,
                        packetLossPercent = state.packetLossPercent,
                        jitterMs = state.jitterMs,
                        stabilityScore = state.stabilityScore,
                        networkType = "Wi-Fi / Cellular"
                    )
                )
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Parallel Server Scan
    fun scanAllServers() {
        if (_isScanningServers.value) return
        _isScanningServers.value = true

        val targets = DefaultServerTargets.servers
        _serverScanResults.value = targets.map { ServerScanResult(it, 0, false, isLoading = true) }

        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                targets.map { target ->
                    val (success, rtt) = PingEngine.pingSingle(target.host, target.port, 1500)
                    ServerScanResult(target, rtt, success, isLoading = false)
                }.sortedWith(compareBy({ !it.isSuccess }, { if (it.isSuccess) it.pingMs else Int.MAX_VALUE }))
            }
            _serverScanResults.value = results
            _isScanningServers.value = false
        }
    }

    // DNS Benchmark
    fun runDnsBenchmark() {
        if (_isBenchmarkingDns.value) return
        _isBenchmarkingDns.value = true

        val dnsTargets = DefaultServerTargets.servers.filter { it.category == TargetCategory.DNS_PROVIDERS }
        _dnsBenchmarkResults.value = dnsTargets.map { DnsBenchmarkResult(it, 0, 0f, isLoading = true) }

        viewModelScope.launch {
            val results = withContext(Dispatchers.IO) {
                dnsTargets.map { target ->
                    var totalMs = 0
                    var successCount = 0
                    val samples = 5
                    for (i in 1..samples) {
                        val (success, ms) = PingEngine.pingSingle(target.host, 53, 1200)
                        if (success) {
                            successCount++
                            totalMs += ms
                        }
                    }
                    val avgMs = if (successCount > 0) totalMs / successCount else 0
                    val lossPercent = ((samples - successCount).toFloat() / samples.toFloat()) * 100f
                    DnsBenchmarkResult(target, avgMs, lossPercent, isLoading = false)
                }
            }

            // Mark lowest ping with loss < 20% as recommended
            val best = results.filter { it.primaryPingMs > 0 && it.packetLossPercent < 20f }
                .minByOrNull { it.primaryPingMs }

            val finalResults = results.map { res ->
                res.copy(isRecommended = res == best)
            }.sortedBy { if (it.primaryPingMs > 0) it.primaryPingMs else Int.MAX_VALUE }

            _dnsBenchmarkResults.value = finalResults
            _isBenchmarkingDns.value = false
        }
    }
}
