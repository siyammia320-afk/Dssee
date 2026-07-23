package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ping_history")
data class PingHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetHost: String,
    val targetName: String,
    val avgPingMs: Int,
    val minPingMs: Int,
    val maxPingMs: Int,
    val packetLossPercent: Float,
    val jitterMs: Int,
    val stabilityScore: Int,
    val networkType: String,
    val timestamp: Long = System.currentTimeMillis()
)
