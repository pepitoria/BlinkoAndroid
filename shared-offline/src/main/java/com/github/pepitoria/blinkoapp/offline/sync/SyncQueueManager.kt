package com.github.pepitoria.blinkoapp.offline.sync

import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.dao.SyncQueueDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncOperation
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncQueueEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Singleton
class SyncQueueManager @Inject constructor(
  private val syncQueueDao: SyncQueueDao,
  private val noteDao: NoteDao,
) {

  val pendingCount: Flow<Int> = syncQueueDao.getCountAsFlow()

  suspend fun enqueueCreate(note: NoteEntity) {
    Timber.d("Enqueueing CREATE for note ${note.localId}")

    val payload = SyncPayload(
      serverId = null,
      content = note.content,
      type = note.type,
      isArchived = note.isArchived,
    )

    syncQueueDao.insert(
      SyncQueueEntity(
        noteLocalId = note.localId,
        noteServerId = null,
        operation = SyncOperation.CREATE.value,
        payload = payload.toJson(),
      ),
    )

    noteDao.updateSyncStatus(note.localId, SyncStatus.PENDING_CREATE.value)
  }

  suspend fun enqueueUpdate(note: NoteEntity) {
    Timber.d("Enqueueing UPDATE for note ${note.localId}")

    val existingEntry = syncQueueDao.getLatestForNote(note.localId)

    when {
      existingEntry == null -> {
        // No existing entry, add new UPDATE
        addUpdateEntry(note)
      }
      existingEntry.operation == SyncOperation.CREATE.value -> {
        // CREATE then UPDATE = merge into single CREATE with updated payload
        Timber.d("Merging UPDATE into existing CREATE for note ${note.localId}")
        val payload = SyncPayload(
          serverId = null,
          content = note.content,
          type = note.type,
          isArchived = note.isArchived,
        )
        syncQueueDao.update(existingEntry.copy(payload = payload.toJson()))
      }
      existingEntry.operation == SyncOperation.UPDATE.value -> {
        // UPDATE then UPDATE = replace payload
        Timber.d("Replacing existing UPDATE for note ${note.localId}")
        val payload = SyncPayload(
          serverId = note.serverId,
          content = note.content,
          type = note.type,
          isArchived = note.isArchived,
        )
        syncQueueDao.update(existingEntry.copy(payload = payload.toJson()))
      }
      else -> {
        // DELETE is pending, ignore update
        Timber.d("Ignoring UPDATE for note ${note.localId} - DELETE is pending")
      }
    }
  }

  private suspend fun addUpdateEntry(note: NoteEntity) {
    val payload = SyncPayload(
      serverId = note.serverId,
      content = note.content,
      type = note.type,
      isArchived = note.isArchived,
    )

    syncQueueDao.insert(
      SyncQueueEntity(
        noteLocalId = note.localId,
        noteServerId = note.serverId,
        operation = SyncOperation.UPDATE.value,
        payload = payload.toJson(),
      ),
    )

    noteDao.updateSyncStatus(note.localId, SyncStatus.PENDING_UPDATE.value)
  }

  suspend fun enqueueDelete(note: NoteEntity) {
    Timber.d("Enqueueing DELETE for note ${note.localId}")

    val existingEntry = syncQueueDao.getLatestForNote(note.localId)

    when {
      existingEntry == null -> {
        // No existing entry, add DELETE if note has server ID
        if (note.serverId != null) {
          addDeleteEntry(note)
        } else {
          // Local-only note, just delete
          Timber.d("Deleting local-only note ${note.localId}")
          noteDao.deleteByLocalId(note.localId)
        }
      }
      existingEntry.operation == SyncOperation.CREATE.value -> {
        // CREATE then DELETE = remove from queue and delete locally
        Timber.d("Removing CREATE from queue and deleting local note ${note.localId}")
        syncQueueDao.deleteByNoteLocalId(note.localId)
        noteDao.deleteByLocalId(note.localId)
      }
      else -> {
        // UPDATE or other -> clear queue and add DELETE
        Timber.d("Clearing queue and adding DELETE for note ${note.localId}")
        syncQueueDao.deleteByNoteLocalId(note.localId)
        if (note.serverId != null) {
          addDeleteEntry(note)
        } else {
          noteDao.deleteByLocalId(note.localId)
        }
      }
    }
  }

  private suspend fun addDeleteEntry(note: NoteEntity) {
    val payload = SyncPayload(
      serverId = note.serverId,
      content = note.content,
      type = note.type,
      isArchived = note.isArchived,
    )

    syncQueueDao.insert(
      SyncQueueEntity(
        noteLocalId = note.localId,
        noteServerId = note.serverId,
        operation = SyncOperation.DELETE.value,
        payload = payload.toJson(),
      ),
    )

    noteDao.updateSyncStatus(note.localId, SyncStatus.PENDING_DELETE.value)
  }

  suspend fun getNextPendingOperation(): SyncQueueEntity? = syncQueueDao.getNext()

  suspend fun getAllPendingOperations(): List<SyncQueueEntity> = syncQueueDao.getAll()

  suspend fun markOperationComplete(queueId: Long) {
    Timber.d("Marking operation $queueId as complete")
    syncQueueDao.deleteById(queueId)
  }

  suspend fun markOperationFailed(
    queueId: Long,
    error: String?,
  ) {
    Timber.d("Marking operation $queueId as failed: $error")
    syncQueueDao.incrementRetryCount(queueId, error)
  }

  suspend fun clearQueue() {
    Timber.d("Clearing sync queue")
    syncQueueDao.deleteAll()
  }

  suspend fun hasPendingOperations(): Boolean = syncQueueDao.getCount() > 0
}
