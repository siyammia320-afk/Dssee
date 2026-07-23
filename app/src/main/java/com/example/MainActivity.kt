package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.PingViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DnsBenchmarkScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ServerRadarScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkSlateBg
import com.example.ui.theme.EmeraldPing
import com.example.ui.theme.FlashPingTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletElectric

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.RedHighPing
import kotlin.math.roundToInt

enum class AppTab(val title: String, val icon: ImageVector) {
    DASHBOARD("Ping Gauge", Icons.Default.FlashOn),
    RADAR("Server Radar", Icons.Default.Radar),
    DNS("DNS Speed", Icons.Default.Dns),
    HISTORY("Logs", Icons.Default.History)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashPingTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9001 && resultCode == RESULT_OK) {
            val intent = android.content.Intent(this, com.example.network.FlashVpnService::class.java).apply {
                action = com.example.network.FlashVpnService.ACTION_CONNECT
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: PingViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.DASHBOARD) }

    val isVpnActive by viewModel.isVpnActive.collectAsState()
    val vpnCountdown by viewModel.vpnCountdownSeconds.collectAsState()
    val isHudActive by viewModel.isHudActive.collectAsState()
    val hudScale by viewModel.hudScale.collectAsState()
    val isTeleActive by viewModel.isTeleActive.collectAsState()
    val isFreezeActive by viewModel.isFreezeActive.collectAsState()

    var floatX by remember { mutableStateOf(40f) }
    var floatY by remember { mutableStateOf(100f) }

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkSlateBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CyanNeon.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "FLASH PING",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // VPN Status Badge in Top Status Bar
                        if (isVpnActive) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPing.copy(alpha = 0.25f),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = "VPN Active",
                                        tint = EmeraldPing,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (vpnCountdown > 0) "VPN (${vpnCountdown}s)" else "VPN ON",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPing
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPing.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(EmeraldPing, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ONLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPing
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                AppTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyanNeon else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) CyanNeon else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanNeon.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.RADAR -> ServerRadarScreen(viewModel = viewModel)
                AppTab.DNS -> DnsBenchmarkScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
            }

            // Global Draggable Floating Overlay HUD Widget with 3 Buttons (VPN, TELE, FREZ)
            if (isHudActive) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(floatX.roundToInt(), floatY.roundToInt()) }
                        .scale(hudScale.scaleFactor)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                floatX += dragAmount.x
                                floatY += dragAmount.y
                            }
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF020617).copy(alpha = 0.94f))
                        .border(1.5.dp, CyanNeon, RoundedCornerShape(20.dp))
                        .padding(10.dp)
                        .testTag("global_floating_hud_overlay")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚡ FLOATING PROX HUD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. VPN Button
                            Button(
                                onClick = { viewModel.toggleVpn(context) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isVpnActive) EmeraldPing else SurfaceDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("global_btn_vpn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "VPN",
                                    tint = if (isVpnActive) Color.Black else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isVpnActive && vpnCountdown > 0) "VPN (${vpnCountdown}s)" else "VPN",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isVpnActive) Color.Black else Color.White
                                )
                            }

                            // 2. TELE Button
                            Button(
                                onClick = { viewModel.toggleTele() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTeleActive) AmberWarning else SurfaceDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("global_btn_tele")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = "Tele",
                                    tint = if (isTeleActive) Color.Black else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "TELE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isTeleActive) Color.Black else Color.White
                                )
                            }

                            // 3. FREZ Button
                            Button(
                                onClick = { viewModel.toggleFreeze() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFreezeActive) RedHighPing else SurfaceDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("global_btn_frez")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AcUnit,
                                    contentDescription = "Freeze",
                                    tint = if (isFreezeActive) Color.White else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "FREZ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
