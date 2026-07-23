package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PingViewModel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.RedHighPing
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric
import kotlin.math.roundToInt

@Composable
fun FloatingHudCard(
    isHudActive: Boolean,
    hudScale: PingViewModel.HudScale,
    isVpnActive: Boolean,
    vpnCountdown: Int,
    isTeleActive: Boolean,
    isFreezeActive: Boolean,
    onToggleHud: () -> Unit,
    onSetHudScale: (PingViewModel.HudScale) -> Unit,
    onToggleVpn: () -> Unit,
    onToggleTele: () -> Unit,
    onToggleFreeze: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_hud_controller_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // Header Row with START HUD Launcher Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FLOATING OVERLAY HUD",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isHudActive) "Floating Overlay Active" else "Tap START to launch overlay",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onToggleHud,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHudActive) RedHighPing else EmeraldPing
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("start_hud_button")
                ) {
                    Icon(
                        imageVector = if (isHudActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHudActive) "STOP HUD" else "START HUD",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            AnimatedVisibility(visible = isHudActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Size Selector Bar ("ছোট বড় করা যাবে")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = null,
                                tint = VioletElectric,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HUD Size:",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PingViewModel.HudScale.values().forEach { scale ->
                                val isSelected = hudScale == scale
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) CyanNeon else MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                        .testTag("scale_btn_${scale.name.lowercase()}")
                                ) {
                                    Text(
                                        text = scale.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Interactive Floating HUD Panel Preview
                    Text(
                        text = "FLOATING CONTROL PANEL (DRAGGABLE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Draggable Overlay Window Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Game / App Screen Preview Area",
                            fontSize = 10.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.align(Alignment.BottomStart)
                        )

                        // Floating Widget inside
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                                .scale(hudScale.scaleFactor)
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        offsetX = (offsetX + dragAmount.x).coerceIn(-120f, 120f)
                                        offsetY = (offsetY + dragAmount.y).coerceIn(-40f, 40f)
                                    }
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                                .border(1.5.dp, CyanNeon, RoundedCornerShape(18.dp))
                                .padding(12.dp)
                                .align(Alignment.Center)
                                .testTag("floating_hud_window")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Status Banner
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = EmeraldPing,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when {
                                            isFreezeActive -> "FREEZE: FULL PACKET DROP"
                                            isTeleActive -> "TELE: UPLOAD BLOCKED"
                                            isVpnActive && vpnCountdown > 0 -> "VPN 10s PAUSE ($vpnCountdown s)"
                                            isVpnActive -> "VPN CONNECTED"
                                            else -> "PROX-HUD READY"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isFreezeActive -> RedHighPing
                                            isTeleActive -> AmberWarning
                                            isVpnActive -> EmeraldPing
                                            else -> CyanNeon
                                        }
                                    )
                                }

                                // 3 FLOATING ACTION BUTTONS: VPN, FREEZE, TELE
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 1. VPN BUTTON
                                    Button(
                                        onClick = onToggleVpn,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isVpnActive) EmeraldPing else SurfaceDark
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("hud_btn_vpn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "VPN",
                                            tint = if (isVpnActive) Color.Black else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isVpnActive && vpnCountdown > 0) "VPN (${vpnCountdown}s)" else "VPN",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isVpnActive) Color.Black else Color.White
                                        )
                                    }

                                    // 2. TELE BUTTON (Upload Blocked)
                                    Button(
                                        onClick = onToggleTele,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTeleActive) AmberWarning else SurfaceDark
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("hud_btn_tele")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = "Tele",
                                            tint = if (isTeleActive) Color.Black else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "TELE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isTeleActive) Color.Black else Color.White
                                        )
                                    }

                                    // 3. FREEZE BUTTON (Full Block)
                                    Button(
                                        onClick = onToggleFreeze,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFreezeActive) RedHighPing else SurfaceDark
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("hud_btn_frez")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AcUnit,
                                            contentDescription = "Freeze",
                                            tint = if (isFreezeActive) Color.White else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "FREZ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Details Explanation Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "MODE EXPLANATION & LOGIC:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• VPN: On activation, connects VPN & pauses internet traffic for 10s, then normalizes connection.\n" +
                                        "• TELE: Blocks outbound upload packet stream while keeping download active.\n" +
                                        "• FREZ: Freezes all inbound and outbound network data streams (full pause).",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
