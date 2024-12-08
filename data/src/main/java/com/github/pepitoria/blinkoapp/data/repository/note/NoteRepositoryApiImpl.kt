package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListResponse
import com.github.pepitoria.blinkoapp.data.net.BlinkoApi
import com.github.pepitoria.blinkoapp.data.net.BlinkoApiClient
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import retrofit2.Response
import javax.inject.Inject

class NoteRepositoryApiImpl @Inject constructor(
  private val api: BlinkoApiClient
) : NoteRepository {
  override suspend fun list(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteListResponse>> {

    val response = api.noteList(
      url = url,
      token = token,
      noteListRequest = noteListRequest
    )

    return response
  }
}