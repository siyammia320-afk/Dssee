package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric

@Composable
fun FloatingHudSimulatorCard(
    modifier: Modifier = Modifier
) {
    var isOverlayActive by remember { mutableStateOf(true) }
    var opacity by remember { mutableStateOf(0.85f) }
    var selectedPosition by remember { mutableStateOf("Top-Right") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hud_simulator_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = VioletElectric,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FLOATING PING HUD SIMULATOR",
                    style = MaterialTheme.typography.labelMedium,
                    color = VioletElectric,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isOverlayActive,
                    onCheckedChange = { isOverlayActive = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyanNeon
                    ),
                    modifier = Modifier.testTag("toggle_hud_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Screen Canvas Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF020617))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "[ In-Game Screen Preview ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF475569),
                    modifier = Modifier.align(Alignment.Center)
                )

                if (isOverlayActive) {
                    val alignModifier = when (selectedPosition) {
                        "Top-Left" -> Alignment.TopStart
                        "Top-Right" -> Alignment.TopEnd
                        else -> Alignment.BottomCenter
                    }

                    Box(
                        modifier = Modifier
                            .align(alignModifier)
                            .alpha(opacity)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(1.dp, CyanNeon.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = EmeraldPing,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PING: 24ms",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPing
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Position & Opacity Sliders
            Text(text = "HUD Opacity: ${(opacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Slider(
                value = opacity,
                onValueChange = { opacity = it },
                valueRange = 0.3f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Position:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                listOf("Top-Left", "Top-Right", "Bottom-Center").forEach { pos ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedPosition == pos) CyanNeon else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("pos_button_$pos")
                    ) {
                        Text(
                            text = pos,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedPosition == pos) Color.Black else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
