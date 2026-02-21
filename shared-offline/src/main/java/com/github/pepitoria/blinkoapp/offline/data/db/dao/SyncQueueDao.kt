package com.github.pepitoria.blinkoapp.offline.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

  @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
  suspend fun getAll(): List<SyncQueueEntity>

  @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC LIMIT 1")
  suspend fun getNext(): SyncQueueEntity?

  @Query("SELECT * FROM sync_queue WHERE noteLocalId = :noteLocalId ORDER BY createdAt DESC LIMIT 1")
  suspend fun getLatestForNote(noteLocalId: String): SyncQueueEntity?

  @Query("SELECT COUNT(*) FROM sync_queue")
  fun getCountAsFlow(): Flow<Int>

  @Query("SELECT COUNT(*) FROM sync_queue")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(syncQueue: SyncQueueEntity): Long

  @Update
  suspend fun update(syncQueue: SyncQueueEntity)

  @Query("DELETE FROM sync_queue WHERE queueId = :queueId")
  suspend fun deleteById(queueId: Long)

  @Query("DELETE FROM sync_queue WHERE noteLocalId = :noteLocalId")
  suspend fun deleteByNoteLocalId(noteLocalId: String)

  @Query("DELETE FROM sync_queue")
  suspend fun deleteAll()

  @Query("UPDATE sync_queue SET retryCount = retryCount + 1, lastError = :error WHERE queueId = :queueId")
  suspend fun incrementRetryCount(
    queueId: Long,
    error: String?,
  )
}
