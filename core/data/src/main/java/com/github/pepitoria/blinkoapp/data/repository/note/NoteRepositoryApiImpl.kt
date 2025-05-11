package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.data.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.data.net.BlinkoApiClient
import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import javax.inject.Inject

class NoteRepositoryApiImpl @Inject constructor(
  private val api: BlinkoApiClient,
  private val authenticationRepository: AuthenticationRepository,
) : NoteRepository {
  override suspend fun list(url: String, token: String, type: Int): BlinkoResult<List<BlinkoNote>> {

    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = NoteListRequest(type = type)
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

  override suspend fun search(url: String, token: String, searchTerm: String): BlinkoResult<List<BlinkoNote>> {

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

  override suspend fun listByIds(url: String, token: String, id: Int): BlinkoResult<List<BlinkoNote>> {

    val response = api.noteListByIds(
      url = url,
      token = token,
      noteListByIdsRequest = getNoteListByIdsRequest(id)
    )

    return mapNoteListByIdsResponseToBlinkoNoteList(response)
  }

  override suspend fun listByIds(url: String, token: String, ids: List<Int>): BlinkoResult<List<BlinkoNote>> {

    val response = api.noteListByIds(
      url = url,
      token = token,
      noteListByIdsRequest = getNoteListByIdsRequest(ids)
    )

    return mapNoteListByIdsResponseToBlinkoNoteList(response)
  }

  private fun mapNoteListByIdsResponseToBlinkoNoteList(
    response: ApiResult<List<NoteResponse>>
  ): BlinkoResult<List<BlinkoNote>>  {
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
      ids = ids
    )
  }

  private fun getNoteListByIdsRequest(id: Int): NoteListByIdsRequest {
    return NoteListByIdsRequest(
      ids = listOf(id)
    )
  }

  override suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote> {

    authenticationRepository.getSession()?.let { sessionDto ->
      val response = api.upsertNote(
        url = sessionDto.url,
        token = sessionDto.token,
        upsertNoteRequest = UpsertRequest(
          id = blinkoNote.id,
          content = blinkoNote.content,
          type = blinkoNote.type.value,
        )
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
}