package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import javax.inject.Inject

class BlinkoLocalFakesApiClientImpl @Inject constructor(): BlinkoApiClient {

  override suspend fun login(
    url: String,
    userName: String,
    password: String
  ): ApiResult<LoginResponse> {
    return ApiResult.ApiSuccess(
      LoginResponse(
        id = 0,
        name = userName,
        nickname = userName,
        role = "",
        token = "fake_token",
        image = "",
        loginType = "local",
      )
    )
  }

  private fun getNote(number: Int = 0): NoteResponse {
    return NoteResponse(
      id = number,
      content = "this is a note: $number",
      type = number.mod(2)
    )
  }

  override suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>> {

    val list = mutableListOf<NoteResponse>().apply {
      for (i in 0..10) {
        add(getNote(i))
      }
    }

    return ApiResult.ApiSuccess(list)
  }

  override suspend fun noteListByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>> {
    val list = mutableListOf<NoteResponse>().apply {
        add(getNote(0))
    }

    return ApiResult.ApiSuccess(list)
  }

  override suspend fun upsertNote(url: String, token: String, upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse> {
    return ApiResult.ApiSuccess(getNote(0))
  }

  override suspend fun deleteNote(
    url: String,
    token: String,
    deleteNoteRequest: DeleteNoteRequest
  ): ApiResult<DeleteNoteResponse> {
    return ApiResult.ApiErrorResponse.UNKNOWN
  }

  override fun isConnected(): Boolean {
    return true
  }

}