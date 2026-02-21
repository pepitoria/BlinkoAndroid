package com.github.pepitoria.blinkoapp.offline.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

  @Query(
    "SELECT * " +
      "FROM notes " +
      "WHERE type = :type " +
      "AND isArchived = :archived " +
      "AND syncStatus != :deletedStatus " +
      "ORDER BY localUpdatedAt DESC",
  )
  fun listAsFlow(
    type: Int,
    archived: Boolean,
    deletedStatus: Int = SyncStatus.PENDING_DELETE.value,
  ): Flow<List<NoteEntity>>

  @Query(
    "SELECT * " +
      "FROM notes " +
      "WHERE type = :type " +
      "AND isArchived = :archived " +
      "AND syncStatus != :deletedStatus " +
      "ORDER BY localUpdatedAt DESC",
  )
  suspend fun list(
    type: Int,
    archived: Boolean,
    deletedStatus: Int = SyncStatus.PENDING_DELETE.value,
  ): List<NoteEntity>

  @Query("SELECT * FROM notes WHERE localId = :localId")
  suspend fun getByLocalId(localId: String): NoteEntity?

  @Query("SELECT * FROM notes WHERE serverId = :serverId")
  suspend fun getByServerId(serverId: Int): NoteEntity?

  @Query("SELECT * FROM notes WHERE serverId IN (:serverIds)")
  suspend fun getByServerIds(serverIds: List<Int>): List<NoteEntity>

  @Query(
    "SELECT * " +
      "FROM notes " +
      "WHERE content LIKE '%' || :searchTerm || '%' " +
      "AND syncStatus != :deletedStatus " +
      "ORDER BY localUpdatedAt DESC",
  )
  suspend fun search(
    searchTerm: String,
    deletedStatus: Int = SyncStatus.PENDING_DELETE.value,
  ): List<NoteEntity>

  @Query("SELECT * FROM notes WHERE syncStatus = :conflictStatus")
  fun getConflictsAsFlow(conflictStatus: Int = SyncStatus.CONFLICT.value): Flow<List<NoteEntity>>

  @Query("SELECT COUNT(*) FROM notes WHERE syncStatus IN (:pendingStatuses)")
  fun getPendingSyncCountAsFlow(
    pendingStatuses: List<Int> = listOf(
      SyncStatus.PENDING_CREATE.value,
      SyncStatus.PENDING_UPDATE.value,
      SyncStatus.PENDING_DELETE.value,
    ),
  ): Flow<Int>

  @Query("SELECT * FROM notes WHERE syncStatus IN (:pendingStatuses)")
  suspend fun getPendingNotes(
    pendingStatuses: List<Int> = listOf(
      SyncStatus.PENDING_CREATE.value,
      SyncStatus.PENDING_UPDATE.value,
      SyncStatus.PENDING_DELETE.value,
    ),
  ): List<NoteEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(note: NoteEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(notes: List<NoteEntity>)

  @Update
  suspend fun update(note: NoteEntity)

  @Query("DELETE FROM notes WHERE localId = :localId")
  suspend fun deleteByLocalId(localId: String)

  @Query("DELETE FROM notes WHERE serverId = :serverId")
  suspend fun deleteByServerId(serverId: Int)

  @Query("UPDATE notes SET syncStatus = :status WHERE localId = :localId")
  suspend fun updateSyncStatus(
    localId: String,
    status: Int,
  )

  @Query(
    "UPDATE notes " +
      "SET serverId = :serverId, " +
      "syncStatus = :status, " +
      "serverUpdatedAt = :serverUpdatedAt " +
      "WHERE localId = :localId",
  )
  suspend fun updateAfterSync(
    localId: String,
    serverId: Int,
    status: Int = SyncStatus.SYNCED.value,
    serverUpdatedAt: String?,
  )

  @Query("DELETE FROM notes")
  suspend fun deleteAll()
}
