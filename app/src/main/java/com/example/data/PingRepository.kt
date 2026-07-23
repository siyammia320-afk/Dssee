package com.example.data

import kotlinx.coroutines.flow.Flow

class PingRepository(private val dao: PingHistoryDao) {
    val allHistory: Flow<List<PingHistoryEntity>> = dao.getAllHistory()

    suspend fun saveTestResult(entity: PingHistoryEntity) {
        dao.insertHistory(entity)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }

    suspend fun deleteHistoryItem(id: Long) {
        dao.deleteById(id)
    }
}
