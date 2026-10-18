package com.example.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity): Long

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingSyncItemsFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE syncStatus = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingSyncItems(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE syncStatus = 'PENDING'")
    suspend fun getPendingCount(): Int

    @Update
    suspend fun updateSyncItem(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET syncStatus = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE sync_queue SET syncStatus = 'FAILED', errorMessage = :error, retryCount = retryCount + 1 WHERE id = :id")
    suspend fun markAsFailed(id: Long, error: String)

    @Query("SELECT * FROM sync_queue ORDER BY timestamp DESC LIMIT 30")
    fun getRecentSyncLogsFlow(): Flow<List<SyncQueueEntity>>

    @Query("DELETE FROM sync_queue WHERE syncStatus = 'SYNCED'")
    suspend fun clearSynced()
}
