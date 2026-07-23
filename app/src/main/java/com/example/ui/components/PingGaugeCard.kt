package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
fun PingGaugeCard(
    pingState: PingResultState,
    onStartPing: () -> Unit,
    onStopPing: () -> Unit,
    onSaveTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRtt = pingState.currentRttMs
    val pingColor by animateColorAsState(
        targetValue = when {
            currentRtt <= 0 -> TextSecondary
            currentRtt < 45 -> EmeraldPing
            currentRtt < 85 -> CyanNeon
            currentRtt < 130 -> AmberWarning
            else -> RedHighPing
        },
        animationSpec = tween(400)
    )

    val targetSweep = when {
        currentRtt <= 0 -> 0f
        else -> ((currentRtt.coerceAtMost(250).toFloat() / 250f) * 240f)
    }

    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "gaugeSweep"
    )

    // Pulse animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "pingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ping_gauge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Target Badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Target Host",
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pingState.target.name + " (" + pingState.target.host + ")",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Gauge Center Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .scale(if (pingState.isRunning) pulseScale else 1f)
            ) {
                Canvas(modifier = Modifier.size(190.dp)) {
                    val strokeWidth = 16.dp.toPx()

                    // Background Track
                    drawArc(
                        color = Color(0xFF334155),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Glowing Ping Arc
                    if (animatedSweep > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(
                                    EmeraldPing,
                                    CyanNeon,
                                    pingColor,
                                    pingColor
                                )
                            ),
                            startAngle = 150f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Value Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (pingState.packetsSent == 0) "--" else "$currentRtt",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = pingColor
                    )
                    Text(
                        text = "ms",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = CircleShape,
                        color = pingColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = when {
                                !pingState.isRunning && pingState.packetsSent == 0 -> "READY"
                                currentRtt <= 0 -> "OFFLINE"
                                currentRtt < 45 -> "ULTRA FAST"
                                currentRtt < 85 -> "GOOD"
                                currentRtt < 130 -> "MEDIUM"
                                else -> "HIGH PING"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = pingColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!pingState.isRunning) {
                    Button(
                        onClick = onStartPing,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("start_ping_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("START PING", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Button(
                        onClick = onStopPing,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("stop_ping_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = RedHighPing),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STOP", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (pingState.packetsSent > 0) {
                    OutlinedButton(
                        onClick = onSaveTest,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("save_ping_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = VioletElectric)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    }
                }
            }
        }
    }
}
