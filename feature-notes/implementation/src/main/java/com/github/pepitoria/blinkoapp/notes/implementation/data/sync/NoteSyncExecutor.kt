package com.github.pepitoria.blinkoapp.notes.implementation.data.sync

import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.offline.connectivity.ServerReachabilityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.NoteDao
import com.github.pepitoria.blinkoapp.offline.sync.SyncExecutor
import com.github.pepitoria.blinkoapp.offline.sync.SyncPayload
import com.github.pepitoria.blinkoapp.offline.sync.SyncResult
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import javax.inject.Inject
import timber.log.Timber

class NoteSyncExecutor @Inject constructor(
  private val api: NotesApiClient,
  private val authenticationRepository: AuthenticationRepository,
  private val noteDao: NoteDao,
  private val serverReachabilityMonitor: ServerReachabilityMonitor,
) : SyncExecutor {

  override suspend fun executeCreate(
    localId: String,
    payload: SyncPayload,
  ): SyncResult {
    val session = authenticationRepository.getSession()
      ?: return SyncResult.Failure("No session available")

    val request = UpsertRequest(
      id = null,
      content = payload.content,
      type = payload.type,
      isArchived = payload.isArchived,
    )

    val response = api.upsertNote(
      url = session.url,
      token = session.token,
      upsertNoteRequest = request,
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        Timber.d("Note created on server: ${response.value.id}")
        serverReachabilityMonitor.reportSuccess()
        SyncResult.Success(
          serverId = response.value.id,
          serverUpdatedAt = response.value.updatedAt,
        )
      }
      is ApiResult.ApiErrorResponse -> {
        Timber.w("Failed to create note: ${response.message}, isServerUnreachable=${response.isServerUnreachable}")
        if (response.isServerUnreachable) {
          serverReachabilityMonitor.reportUnreachable()
        }
        SyncResult.Failure(response.message ?: "Unknown error")
      }
    }
  }

  override suspend fun executeUpdate(
    localId: String,
    payload: SyncPayload,
  ): SyncResult {
    val session = authenticationRepository.getSession()
      ?: return SyncResult.Failure("No session available")

    val serverId = payload.serverId
      ?: return SyncResult.Failure("No server ID for update operation")

    // Check for conflicts
    val localNote = noteDao.getByLocalId(localId)
    if (localNote?.serverUpdatedAt != null) {
      // We could fetch the server version and compare updatedAt here
      // For now, we'll let the server handle conflicts
    }

    val request = UpsertRequest(
      id = serverId,
      content = payload.content,
      type = payload.type,
      isArchived = payload.isArchived,
    )

    val response = api.upsertNote(
      url = session.url,
      token = session.token,
      upsertNoteRequest = request,
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        Timber.d("Note updated on server: ${response.value.id}")
        serverReachabilityMonitor.reportSuccess()
        SyncResult.Success(
          serverId = response.value.id,
          serverUpdatedAt = response.value.updatedAt,
        )
      }
      is ApiResult.ApiErrorResponse -> {
        if (response.isServerUnreachable) {
          Timber.w("Failed to update note: server unreachable")
          serverReachabilityMonitor.reportUnreachable()
          SyncResult.Failure(response.message ?: "Server unreachable")
        } else if (response.code == 409) {
          // Conflict detected
          Timber.w("Conflict detected for note $serverId")
          SyncResult.Conflict(null)
        } else {
          Timber.w("Failed to update note: ${response.message}")
          SyncResult.Failure(response.message ?: "Unknown error")
        }
      }
    }
  }

  override suspend fun executeDelete(
    localId: String,
    serverId: Int?,
  ): SyncResult {
    if (serverId == null) {
      // Local-only note, just mark as deleted
      return SyncResult.Deleted
    }

    val session = authenticationRepository.getSession()
      ?: return SyncResult.Failure("No session available")

    val response = api.deleteNote(
      url = session.url,
      token = session.token,
      deleteNoteRequest = DeleteNoteRequest(ids = listOf(serverId)),
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        Timber.d("Note deleted from server: $serverId")
        serverReachabilityMonitor.reportSuccess()
        SyncResult.Deleted
      }
      is ApiResult.ApiErrorResponse -> {
        if (response.isServerUnreachable) {
          Timber.w("Failed to delete note: server unreachable")
          serverReachabilityMonitor.reportUnreachable()
          SyncResult.Failure(response.message ?: "Server unreachable")
        } else if (response.code == 404) {
          // Already deleted on server
          Timber.d("Note already deleted on server: $serverId")
          SyncResult.Deleted
        } else {
          Timber.w("Failed to delete note: ${response.message}")
          SyncResult.Failure(response.message ?: "Unknown error")
        }
      }
    }
  }
}
