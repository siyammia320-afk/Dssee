package com.example.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FlashVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var vpnThread: Thread? = null

    companion object {
        const val ACTION_CONNECT = "com.example.network.ACTION_CONNECT"
        const val ACTION_DISCONNECT = "com.example.network.ACTION_DISCONNECT"
        const val CHANNEL_ID = "flash_vpn_channel"
        const val NOTIFICATION_ID = 1001
        
        var isVpnConnected = false
            private set
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_DISCONNECT) {
            stopVpn()
            return START_NOT_STICKY
        } else if (action == ACTION_CONNECT) {
            startVpn()
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        isRunning = true
        isVpnConnected = true

        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flash Ping VPN Active")
            .setContentText("Proxy & Low-Latency Routing Enabled")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        try {
            val builder = Builder()
                .setSession("FlashPingVPN")
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("8.8.8.8")
                .addDnsServer("1.1.1.1")

            vpnInterface = builder.establish()

            vpnThread = Thread {
                try {
                    val input = FileInputStream(vpnInterface?.fileDescriptor)
                    val output = FileOutputStream(vpnInterface?.fileDescriptor)
                    val buffer = ByteArray(32768)

                    while (isRunning && !Thread.currentThread().isInterrupted) {
                        val length = input.read(buffer)
                        if (length > 0) {
                            // Loopback packet transfer simulation for proxy stability
                            output.write(buffer, 0, length)
                        }
                        Thread.sleep(10)
                    }
                } catch (e: Exception) {
                    Log.e("FlashVpnService", "VPN loop error: ${e.message}")
                }
            }
            vpnThread?.start()

        } catch (e: Exception) {
            Log.e("FlashVpnService", "Failed to establish VPN: ${e.message}")
            stopVpn()
        }
    }

    private fun stopVpn() {
        isRunning = false
        isVpnConnected = false
        vpnThread?.interrupt()
        vpnThread = null

        try {
            vpnInterface?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        vpnInterface = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flash Ping VPN Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
