package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.PingPacket
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.RedHighPing
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric

@Composable
fun LatencyWaveformChart(
    packets: List<PingPacket>,
    avgPingMs: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("latency_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REALTIME LATENCY WAVEFORM",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanNeon,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (packets.isNotEmpty()) {
                    Text(
                        text = "${packets.size} Pkts",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (packets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start Ping test to stream latency graph",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Calculate Max scale for graph
                        val sampleRtts = packets.map { if (it.isSuccess) it.rttMs else 200 }
                        val maxRttScale = (sampleRtts.maxOrNull() ?: 100).coerceAtLeast(80).toFloat() * 1.2f

                        // Draw Grid Lines
                        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = height * (i.toFloat() / gridLines.toFloat())
                            drawLine(
                                color = Color(0xFF334155),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f,
                                pathEffect = dashedEffect
                            )
                        }

                        // Build Waveform Path
                        val path = Path()
                        val fillPath = Path()

                        val stepX = width / (packets.size.coerceAtLeast(2) - 1).toFloat()

                        packets.forEachIndexed { index, packet ->
                            val rtt = if (packet.isSuccess) packet.rttMs.toFloat() else maxRttScale
                            val x = index * stepX
                            val y = height - ((rtt / maxRttScale) * height).coerceIn(0f, height)

                            if (index == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (index == packets.size - 1) {
                                fillPath.lineTo(x, height)
                                fillPath.close()
                            }
                        }

                        // Draw Area Fill Gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    CyanNeon.copy(alpha = 0.35f),
                                    VioletElectric.copy(alpha = 0.05f)
                                )
                            )
                        )

                        // Draw Stroke Line
                        drawPath(
                            path = path,
                            color = CyanNeon,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw Target Avg Line
                        if (avgPingMs > 0) {
                            val avgY = height - ((avgPingMs.toFloat() / maxRttScale) * height).coerceIn(0f, height)
                            drawLine(
                                color = EmeraldPing,
                                start = Offset(0f, avgY),
                                end = Offset(width, avgY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }

                        // Draw Pulse points on last sample
                        val lastPacket = packets.last()
                        val lastRtt = if (lastPacket.isSuccess) lastPacket.rttMs.toFloat() else maxRttScale
                        val lastX = (packets.size - 1) * stepX
                        val lastY = height - ((lastRtt / maxRttScale) * height).coerceIn(0f, height)

                        drawCircle(
                            color = if (lastPacket.isSuccess) EmeraldPing else RedHighPing,
                            radius = 6.dp.toPx(),
                            center = Offset(lastX, lastY)
                        )
                    }
                }
            }
        }
    }
}
