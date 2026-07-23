package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PingHistoryDao {
    @Query("SELECT * FROM ping_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: PingHistoryEntity)

    @Query("DELETE FROM ping_history")
    suspend fun clearHistory()

    @Query("DELETE FROM ping_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}
