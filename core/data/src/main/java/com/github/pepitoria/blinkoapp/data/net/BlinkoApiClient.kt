package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest

interface BlinkoApiClient {

  suspend fun login(url: String, userName: String, password: String): ApiResult<LoginResponse>
  suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>>
  suspend fun noteListByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>>
  suspend fun upsertNote(url: String, token: String, upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse>
  fun isConnected(): Boolean
}