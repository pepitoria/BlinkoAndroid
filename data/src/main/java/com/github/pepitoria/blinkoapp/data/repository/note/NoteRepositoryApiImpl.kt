package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.data.net.BlinkoApiClient
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import javax.inject.Inject

class NoteRepositoryApiImpl @Inject constructor(
  private val api: BlinkoApiClient,
  private val authenticationRepository: AuthenticationRepository,
) : NoteRepository {
  override suspend fun list(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>> {

    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = noteListRequest
    )

    return response
  }

  override suspend fun listByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>> {

    val response = api.noteListByIds(
      url = url,
      token = token,
      noteListByIdsRequest = noteListByIdsRequest
    )

    return response
  }

  override suspend fun upsertNote(upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse> {

    authenticationRepository.getSession()?.let { sessionDto ->
      val response = api.upsertNote(
        url = sessionDto.url,
        token = sessionDto.token,
        upsertNoteRequest = upsertNoteRequest
      )

      return response
    }

    return ApiResult.ApiErrorResponse(message = "No session found")
  }
}