package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest

interface BlinkoApiClient {

  suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>>
  suspend fun noteListByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>>
  suspend fun upsertNote(url: String, token: String, upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse>
  suspend fun deleteNote(url: String, token: String, deleteNoteRequest: DeleteNoteRequest): ApiResult<DeleteNoteResponse>
  fun isConnected(): Boolean
}