package com.github.pepitoria.blinkoapp.notes.implementation.data.repository

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.api.domain.model.SyncStatus
import com.github.pepitoria.blinkoapp.notes.implementation.data.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.notes.implementation.data.mapper.toUpsertRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.offline.connectivity.ConnectivityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.NoteEntity
import com.github.pepitoria.blinkoapp.offline.data.db.entity.SyncStatus as EntitySyncStatus
import com.github.pepitoria.blinkoapp.offline.sync.SyncQueueManager
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class OfflineFirstNoteRepository @Inject constructor(
  private val api: NotesApiClient,
  private val authenticationRepository: AuthenticationRepository,
  private val noteDao: NoteDao,
  private val syncQueueManager: SyncQueueManager,
  private val connectivityMonitor: ConnectivityMonitor,
) : NoteRepository {

  override suspend fun list(
    url: String,
    token: String,
    type: Int,
    archived: Boolean,
  ): BlinkoResult<List<BlinkoNote>> {
    // If online, fetch from server and merge with local
    if (connectivityMonitor.isConnected.value) {
      val response = api.noteList(
        url = url,
        token = token,
        noteListRequest = NoteListRequest(type = type, isArchived = archived),
      )

      when (response) {
        is ApiResult.ApiSuccess -> {
          mergeServerNotes(response.value)
        }
        is ApiResult.ApiErrorResponse -> {
          Timber.w("Failed to fetch from server, falling back to local: ${response.message}")
        }
      }
    }

    // Always return local data
    val localNotes = noteDao.list(type = type, archived = archived)
    return BlinkoResult.Success(localNotes.map { it.toBlinkoNote() })
  }

  override fun listAsFlow(
    type: Int,
    archived: Boolean,
  ): Flow<List<BlinkoNote>> {
    return noteDao.listAsFlow(type = type, archived = archived).map { entities ->
      entities.map { it.toBlinkoNote() }
    }
  }

  override suspend fun search(
    url: String,
    token: String,
    searchTerm: String,
  ): BlinkoResult<List<BlinkoNote>> {
    // Search locally first
    val localResults = noteDao.search(searchTerm)

    // If online, also search server
    if (connectivityMonitor.isConnected.value) {
      val response = api.noteList(
        url = url,
        token = token,
        noteListRequest = NoteListRequest(searchText = searchTerm, size = 100),
      )

      when (response) {
        is ApiResult.ApiSuccess -> {
          mergeServerNotes(response.value)
          // Re-fetch local results after merge
          val updatedResults = noteDao.search(searchTerm)
          return BlinkoResult.Success(updatedResults.map { it.toBlinkoNote() })
        }
        is ApiResult.ApiErrorResponse -> {
          Timber.w("Failed to search server, returning local results: ${response.message}")
        }
      }
    }

    return BlinkoResult.Success(localResults.map { it.toBlinkoNote() })
  }

  override suspend fun listByIds(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<List<BlinkoNote>> = listByIds(url, token, listOf(id))

  override suspend fun listByIds(
    url: String,
    token: String,
    ids: List<Int>,
  ): BlinkoResult<List<BlinkoNote>> {
    // Check local first
    val localNotes = noteDao.getByServerIds(ids)

    if (connectivityMonitor.isConnected.value) {
      val response = api.noteListByIds(
        url = url,
        token = token,
        noteListByIdsRequest = NoteListByIdsRequest(ids = ids),
      )

      when (response) {
        is ApiResult.ApiSuccess -> {
          mergeServerNotes(response.value)
          val updatedNotes = noteDao.getByServerIds(ids)
          return if (updatedNotes.isNotEmpty()) {
            BlinkoResult.Success(updatedNotes.map { it.toBlinkoNote() })
          } else {
            BlinkoResult.Error.NOTFOUND
          }
        }
        is ApiResult.ApiErrorResponse -> {
          Timber.w("Failed to fetch by IDs from server: ${response.message}")
        }
      }
    }

    return if (localNotes.isNotEmpty()) {
      BlinkoResult.Success(localNotes.map { it.toBlinkoNote() })
    } else {
      BlinkoResult.Error.NOTFOUND
    }
  }

  override suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote> {
    val isNewNote = blinkoNote.id == null && blinkoNote.localId == null
    val localId = blinkoNote.localId ?: UUID.randomUUID().toString()

    val noteEntity = NoteEntity(
      localId = localId,
      serverId = blinkoNote.id,
      content = blinkoNote.content,
      type = blinkoNote.type.value,
      isArchived = blinkoNote.isArchived,
      localUpdatedAt = System.currentTimeMillis(),
      syncStatus = if (isNewNote) EntitySyncStatus.PENDING_CREATE.value else EntitySyncStatus.PENDING_UPDATE.value,
    )

    // Save locally
    noteDao.insert(noteEntity)

    // Queue for sync
    if (isNewNote) {
      syncQueueManager.enqueueCreate(noteEntity)
    } else {
      syncQueueManager.enqueueUpdate(noteEntity)
    }

    // If online, try to sync immediately
    if (connectivityMonitor.isConnected.value) {
      return syncNoteToServer(noteEntity)
    }

    // Return local note
    return BlinkoResult.Success(noteEntity.toBlinkoNote())
  }

  private suspend fun syncNoteToServer(noteEntity: NoteEntity): BlinkoResult<BlinkoNote> {
    authenticationRepository.getSession()?.let { session ->
      val blinkoNote = noteEntity.toBlinkoNote()
      val response = api.upsertNote(
        url = session.url,
        token = session.token,
        upsertNoteRequest = blinkoNote.toUpsertRequest(),
      )

      return when (response) {
        is ApiResult.ApiSuccess -> {
          val serverNote = response.value
          noteDao.updateAfterSync(
            localId = noteEntity.localId,
            serverId = serverNote.id ?: noteEntity.serverId ?: 0,
            serverUpdatedAt = serverNote.updatedAt,
          )
          syncQueueManager.markOperationComplete(
            syncQueueManager.getNextPendingOperation()?.queueId ?: 0,
          )
          BlinkoResult.Success(serverNote.toBlinkoNote())
        }
        is ApiResult.ApiErrorResponse -> {
          Timber.w("Failed to sync note to server: ${response.message}")
          // Keep as pending, return local note
          BlinkoResult.Success(noteEntity.toBlinkoNote())
        }
      }
    }

    return BlinkoResult.Success(noteEntity.toBlinkoNote())
  }

  override suspend fun delete(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<Boolean> {
    val note = noteDao.getByServerId(id)
    return if (note != null) {
      deleteNote(note.toBlinkoNote())
    } else {
      BlinkoResult.Error.NOTFOUND
    }
  }

  override suspend fun deleteNote(note: BlinkoNote): BlinkoResult<Boolean> {
    val localId = note.localId
    val serverId = note.id
    val noteEntity = when {
      localId != null -> noteDao.getByLocalId(localId)
      serverId != null -> noteDao.getByServerId(serverId)
      else -> null
    }

    if (noteEntity == null) {
      return BlinkoResult.Error.NOTFOUND
    }

    // Queue for sync
    syncQueueManager.enqueueDelete(noteEntity)

    // If online and has server ID, delete immediately
    val entityServerId = noteEntity.serverId
    if (connectivityMonitor.isConnected.value && entityServerId != null) {
      authenticationRepository.getSession()?.let { session ->
        val response = api.deleteNote(
          url = session.url,
          token = session.token,
          deleteNoteRequest = DeleteNoteRequest(ids = listOf(entityServerId)),
        )

        return when (response) {
          is ApiResult.ApiSuccess -> {
            noteDao.deleteByLocalId(noteEntity.localId)
            syncQueueManager.markOperationComplete(
              syncQueueManager.getNextPendingOperation()?.queueId ?: 0,
            )
            BlinkoResult.Success(true)
          }
          is ApiResult.ApiErrorResponse -> {
            Timber.w("Failed to delete from server: ${response.message}")
            // Still marked as pending delete locally
            BlinkoResult.Success(true)
          }
        }
      }
    }

    return BlinkoResult.Success(true)
  }

  override val pendingSyncCount: Flow<Int> = syncQueueManager.pendingCount

  override val conflicts: Flow<List<BlinkoNote>> = noteDao.getConflictsAsFlow().map { entities ->
    entities.map { it.toBlinkoNote() }
  }

  override suspend fun refreshFromServer(
    url: String,
    token: String,
    type: Int,
    archived: Boolean,
  ): BlinkoResult<Unit> {
    if (!connectivityMonitor.isConnected.value) {
      return BlinkoResult.Error(-1, "No internet connection")
    }

    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = NoteListRequest(type = type, isArchived = archived),
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        mergeServerNotes(response.value)
        BlinkoResult.Success(Unit)
      }
      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }

  override suspend fun resolveConflict(
    note: BlinkoNote,
    keepLocal: Boolean,
  ): BlinkoResult<BlinkoNote> {
    val localId = note.localId ?: return BlinkoResult.Error.NOTFOUND
    val noteEntity = noteDao.getByLocalId(localId) ?: return BlinkoResult.Error.NOTFOUND

    return if (keepLocal) {
      // Re-queue update to force push local version
      noteDao.updateSyncStatus(localId, EntitySyncStatus.PENDING_UPDATE.value)
      syncQueueManager.enqueueUpdate(noteEntity)

      if (connectivityMonitor.isConnected.value) {
        syncNoteToServer(noteEntity)
      } else {
        BlinkoResult.Success(noteEntity.toBlinkoNote())
      }
    } else {
      // Fetch server version and overwrite local
      val noteServerId = note.id
      if (noteServerId != null && connectivityMonitor.isConnected.value) {
        authenticationRepository.getSession()?.let { session ->
          val response = api.noteListByIds(
            url = session.url,
            token = session.token,
            noteListByIdsRequest = NoteListByIdsRequest(ids = listOf(noteServerId)),
          )

          when (response) {
            is ApiResult.ApiSuccess -> {
              response.value.firstOrNull()?.let { serverNote ->
                val updatedEntity = noteEntity.copy(
                  content = serverNote.content ?: noteEntity.content,
                  type = serverNote.type ?: noteEntity.type,
                  isArchived = serverNote.isArchived ?: noteEntity.isArchived,
                  serverUpdatedAt = serverNote.updatedAt,
                  syncStatus = EntitySyncStatus.SYNCED.value,
                )
                noteDao.update(updatedEntity)
                syncQueueManager.markOperationComplete(
                  syncQueueManager.getNextPendingOperation()?.queueId ?: 0,
                )
                return BlinkoResult.Success(updatedEntity.toBlinkoNote())
              }
            }
            is ApiResult.ApiErrorResponse -> {
              return response.toBlinkoResult()
            }
          }
        }
      }
      BlinkoResult.Error.NOTFOUND
    }
  }

  private suspend fun mergeServerNotes(serverNotes: List<NoteResponse>) {
    for (serverNote in serverNotes) {
      val serverId = serverNote.id ?: continue
      val existingNote = noteDao.getByServerId(serverId)

      if (existingNote == null) {
        // New note from server
        noteDao.insert(
          NoteEntity(
            serverId = serverId,
            content = serverNote.content ?: "",
            type = serverNote.type ?: 0,
            isArchived = serverNote.isArchived ?: false,
            createdAt = serverNote.createdAt,
            updatedAt = serverNote.updatedAt,
            serverUpdatedAt = serverNote.updatedAt,
            syncStatus = EntitySyncStatus.SYNCED.value,
          ),
        )
      } else {
        // Check for conflicts
        val syncStatus = EntitySyncStatus.fromValue(existingNote.syncStatus)

        when (syncStatus) {
          EntitySyncStatus.SYNCED -> {
            // No local changes, update with server version
            noteDao.update(
              existingNote.copy(
                content = serverNote.content ?: existingNote.content,
                type = serverNote.type ?: existingNote.type,
                isArchived = serverNote.isArchived ?: existingNote.isArchived,
                updatedAt = serverNote.updatedAt,
                serverUpdatedAt = serverNote.updatedAt,
              ),
            )
          }
          EntitySyncStatus.PENDING_UPDATE, EntitySyncStatus.PENDING_CREATE -> {
            // Check if server has newer version
            val serverUpdatedAt = existingNote.serverUpdatedAt
            if (serverUpdatedAt != null && serverNote.updatedAt != serverUpdatedAt) {
              // Server has been updated since we last synced - conflict
              Timber.w("Conflict detected for note ${existingNote.localId}")
              noteDao.updateSyncStatus(existingNote.localId, EntitySyncStatus.CONFLICT.value)
            }
            // Otherwise keep local pending changes
          }
          EntitySyncStatus.PENDING_DELETE -> {
            // Keep as pending delete
          }
          EntitySyncStatus.CONFLICT -> {
            // Already in conflict, don't overwrite
          }
        }
      }
    }
  }

  private fun NoteEntity.toBlinkoNote(): BlinkoNote {
    return BlinkoNote(
      id = this.serverId,
      localId = this.localId,
      content = this.content,
      type = BlinkoNoteType.fromResponseType(this.type),
      isArchived = this.isArchived,
      syncStatus = when (EntitySyncStatus.fromValue(this.syncStatus)) {
        EntitySyncStatus.SYNCED -> SyncStatus.SYNCED
        EntitySyncStatus.PENDING_CREATE -> SyncStatus.PENDING_CREATE
        EntitySyncStatus.PENDING_UPDATE -> SyncStatus.PENDING_UPDATE
        EntitySyncStatus.PENDING_DELETE -> SyncStatus.PENDING_DELETE
        EntitySyncStatus.CONFLICT -> SyncStatus.CONFLICT
      },
      updatedAt = this.updatedAt,
    )
  }
}
