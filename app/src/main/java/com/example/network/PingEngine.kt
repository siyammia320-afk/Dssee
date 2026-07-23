package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

data class PingPacket(
    val sequenceNumber: Int,
    val rttMs: Int,
    val isSuccess: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class PingResultState(
    val target: ServerTarget,
    val currentRttMs: Int = 0,
    val packetsSent: Int = 0,
    val packetsReceived: Int = 0,
    val minPingMs: Int = Int.MAX_VALUE,
    val maxPingMs: Int = 0,
    val avgPingMs: Int = 0,
    val jitterMs: Int = 0,
    val packetLossPercent: Float = 0f,
    val stabilityScore: Int = 100,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val packetHistory: List<PingPacket> = emptyList()
)

object PingEngine {

    /**
     * Performs a single socket ping measurement to a host and port.
     * Fallbacks to InetAddress.isReachable if socket connection is refused.
     */
    fun pingSingle(host: String, port: Int = 80, timeoutMs: Int = 2000): Pair<Boolean, Int> {
        val startTime = System.nanoTime()
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, if (port <= 0) 80 else port), timeoutMs)
            val elapsedMs = ((System.nanoTime() - startTime) / 1_000_000).toInt()
            socket.close()
            Pair(true, elapsedMs.coerceAtLeast(1))
        } catch (e: Exception) {
            // Fallback to InetAddress.isReachable or HTTP ping
            try {
                val startNs = System.nanoTime()
                val reachable = InetAddress.getByName(host).isReachable(timeoutMs)
                val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000).toInt()
                if (reachable) {
                    Pair(true, elapsedMs.coerceAtLeast(1))
                } else {
                    Pair(false, -1)
                }
            } catch (e2: Exception) {
                Pair(false, -1)
            }
        }
    }

    /**
     * Emits a flow of live PingResultState as packet samples arrive.
     */
    fun pingStream(
        target: ServerTarget,
        count: Int = 20,
        intervalMs: Long = 500L,
        timeoutMs: Int = 2000
    ): Flow<PingResultState> = flow {
        var state = PingResultState(target = target, isRunning = true)
        emit(state)

        val history = mutableListOf<PingPacket>()
        var minRtt = Int.MAX_VALUE
        var maxRtt = 0
        var totalRttSum = 0L
        var successCount = 0

        for (seq in 1..count) {
            val (success, rtt) = pingSingle(target.host, target.port, timeoutMs)

            val packet = PingPacket(sequenceNumber = seq, rttMs = rtt, isSuccess = success)
            history.add(packet)

            var jitter = 0
            if (success) {
                successCount++
                totalRttSum += rtt
                if (rtt < minRtt) minRtt = rtt
                if (rtt > maxRtt) maxRtt = rtt
            }

            // Calculate Jitter (average difference between consecutive successful pings)
            val successfulPackets = history.filter { it.isSuccess }
            if (successfulPackets.size > 1) {
                var diffSum = 0
                for (i in 1 until successfulPackets.size) {
                    diffSum += abs(successfulPackets[i].rttMs - successfulPackets[i - 1].rttMs)
                }
                jitter = diffSum / (successfulPackets.size - 1)
            }

            val avgPing = if (successCount > 0) (totalRttSum / successCount).toInt() else 0
            val lossPercent = ((seq - successCount).toFloat() / seq.toFloat()) * 100f

            // Calculate stability score (100 - (packet loss * 2) - (jitter * 0.5))
            val rawStability = 100 - (lossPercent * 2.5f).toInt() - (jitter * 0.8f).toInt()
            val stability = rawStability.coerceIn(0, 100)

            state = state.copy(
                currentRttMs = if (success) rtt else state.currentRttMs,
                packetsSent = seq,
                packetsReceived = successCount,
                minPingMs = if (minRtt == Int.MAX_VALUE) 0 else minRtt,
                maxPingMs = maxRtt,
                avgPingMs = avgPing,
                jitterMs = jitter,
                packetLossPercent = lossPercent,
                stabilityScore = stability,
                packetHistory = history.toList()
            )

            emit(state)

            if (seq < count) {
                delay(intervalMs)
            }
        }

        state = state.copy(isRunning = false, isFinished = true)
        emit(state)
    }.flowOn(Dispatchers.IO)
}
