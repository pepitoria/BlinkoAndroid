package com.github.pepitoria.blinkoapp.notes.implementation.data.repository

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.implementation.data.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.notes.implementation.data.mapper.toUpsertRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Legacy API-only implementation of NoteRepository.
 * This is kept for reference but [OfflineFirstNoteRepository] should be used instead.
 */
class NoteRepositoryApiImpl @Inject constructor(
  private val api: NotesApiClient,
  private val authenticationRepository: AuthenticationRepository,
) : NoteRepository {
  override suspend fun list(
    url: String,
    token: String,
    type: Int,
    archived: Boolean,
  ): BlinkoResult<List<BlinkoNote>> {
    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = NoteListRequest(type = type, isArchived = archived),
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        BlinkoResult.Success(response.value.map { it.toBlinkoNote() })
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }

  override suspend fun search(
    url: String,
    token: String,
    searchTerm: String,
  ): BlinkoResult<List<BlinkoNote>> {
    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = NoteListRequest(
        searchText = searchTerm,
        size = 100,
      ),
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        BlinkoResult.Success(response.value.map { it.toBlinkoNote() })
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }

  override suspend fun listByIds(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<List<BlinkoNote>> {
    val response = api.noteListByIds(
      url = url,
      token = token,
      noteListByIdsRequest = getNoteListByIdsRequest(id),
    )

    return mapNoteListByIdsResponseToBlinkoNoteList(response)
  }

  override suspend fun listByIds(
    url: String,
    token: String,
    ids: List<Int>,
  ): BlinkoResult<List<BlinkoNote>> {
    val response = api.noteListByIds(
      url = url,
      token = token,
      noteListByIdsRequest = getNoteListByIdsRequest(ids),
    )

    return mapNoteListByIdsResponseToBlinkoNoteList(response)
  }

  private fun mapNoteListByIdsResponseToBlinkoNoteList(
    response: ApiResult<List<NoteResponse>>,
  ): BlinkoResult<List<BlinkoNote>> {
    return when (response) {
      is ApiResult.ApiSuccess -> {
        response.value.firstOrNull()?.let {
          BlinkoResult.Success(listOf(it.toBlinkoNote()))
        } ?: BlinkoResult.Error.NOTFOUND
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }
  private fun getNoteListByIdsRequest(ids: List<Int>): NoteListByIdsRequest {
    return NoteListByIdsRequest(
      ids = ids,
    )
  }

  private fun getNoteListByIdsRequest(id: Int): NoteListByIdsRequest {
    return NoteListByIdsRequest(
      ids = listOf(id),
    )
  }

  override suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote> {
    authenticationRepository.getSession()?.let { sessionDto ->
      val response = api.upsertNote(
        url = sessionDto.url,
        token = sessionDto.token,
        upsertNoteRequest = blinkoNote.toUpsertRequest(),
      )

      return when (response) {
        is ApiResult.ApiSuccess -> {
          BlinkoResult.Success(response.value.toBlinkoNote())
        }
        is ApiResult.ApiErrorResponse -> {
          response.toBlinkoResult()
        }
      }
    }

    return BlinkoResult.Error.NOTFOUND
  }

  override suspend fun delete(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<Boolean> {
    authenticationRepository.getSession()?.let { sessionDto ->
      val response = api.deleteNote(
        url = sessionDto.url,
        token = sessionDto.token,
        deleteNoteRequest = DeleteNoteRequest(
          ids = listOf(id),
        ),
      )

      return when (response) {
        is ApiResult.ApiSuccess -> {
          BlinkoResult.Success(true)
        }
        is ApiResult.ApiErrorResponse -> {
          response.toBlinkoResult()
        }
      }
    }

    return BlinkoResult.Error.NOTFOUND
  }

  override fun listAsFlow(
    type: Int,
    archived: Boolean,
  ): Flow<List<BlinkoNote>> = emptyFlow()

  override suspend fun deleteNote(note: BlinkoNote): BlinkoResult<Boolean> {
    return note.id?.let { id ->
      authenticationRepository.getSession()?.let { session ->
        delete(session.url, session.token, id)
      } ?: BlinkoResult.Error.NOTFOUND
    } ?: BlinkoResult.Error.NOTFOUND
  }

  override val pendingSyncCount: Flow<Int> = flowOf(0)

  override val conflicts: Flow<List<BlinkoNote>> = flowOf(emptyList())

  override suspend fun refreshFromServer(
    url: String,
    token: String,
    type: Int,
    archived: Boolean,
  ): BlinkoResult<Unit> {
    return BlinkoResult.Success(Unit)
  }

  override suspend fun resolveConflict(
    note: BlinkoNote,
    keepLocal: Boolean,
  ): BlinkoResult<BlinkoNote> {
    return BlinkoResult.Success(note)
  }
}
