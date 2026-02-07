package com.github.pepitoria.blinkoapp.notes.implementation.data.net

import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult

interface NotesApiClient {

  suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>>
  suspend fun noteListByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>>
  suspend fun upsertNote(url: String, token: String, upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse>
  suspend fun deleteNote(url: String, token: String, deleteNoteRequest: DeleteNoteRequest): ApiResult<DeleteNoteResponse>
  fun isConnected(): Boolean
}
