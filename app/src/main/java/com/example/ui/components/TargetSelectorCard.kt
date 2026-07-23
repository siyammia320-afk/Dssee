package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.network.DefaultServerTargets
import com.example.network.ServerTarget
import com.example.network.TargetCategory
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric

@Composable
fun TargetSelectorCard(
    selectedTarget: ServerTarget,
    onSelectTarget: (ServerTarget) -> Unit,
    customHost: String,
    customPort: String,
    onUpdateCustomHost: (String) -> Unit,
    onUpdateCustomPort: (String) -> Unit,
    onSetCustomTarget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(TargetCategory.GAME_SERVERS) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("target_selector_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "SELECT PING TARGET",
                style = MaterialTheme.typography.labelMedium,
                color = CyanNeon,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = TargetCategory.values().indexOf(selectedCategory),
                containerColor = Color.Transparent,
                contentColor = CyanNeon,
                edgePadding = 0.dp
            ) {
                TargetCategory.values().forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                text = category.title,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == category) CyanNeon else TextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedCategory == TargetCategory.CUSTOM) {
                // Custom Host Input Form
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customHost,
                            onValueChange = onUpdateCustomHost,
                            label = { Text("IP / Hostname (e.g. 8.8.8.8)") },
                            modifier = Modifier
                                .weight(2f)
                                .testTag("custom_host_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = TextSecondary,
                                focusedLabelColor = CyanNeon,
                                unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = customPort,
                            onValueChange = onUpdateCustomPort,
                            label = { Text("Port") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_port_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = TextSecondary,
                                focusedLabelColor = CyanNeon,
                                unfocusedLabelColor = TextSecondary
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onSetCustomTarget,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_custom_host_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VioletElectric),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SET CUSTOM TARGET", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // Server Cards Horizontal List
                val categoryTargets = DefaultServerTargets.servers.filter { it.category == selectedCategory }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categoryTargets) { target ->
                        val isSelected = selectedTarget.id == target.id
                        val itemBg = if (isSelected) CyanNeon.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(itemBg)
                                .clickable { onSelectTarget(target) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (target.iconName) {
                                    "gamepad" -> Icons.Default.Gamepad
                                    "dns" -> Icons.Default.Dns
                                    else -> Icons.Default.Public
                                },
                                contentDescription = null,
                                tint = if (isSelected) CyanNeon else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = target.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = target.region,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = EmeraldPing,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
