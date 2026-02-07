package com.github.pepitoria.blinkoapp.notes.implementation.data.net

import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.noteupsert.UpsertRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface NotesApi {

  @POST()
  suspend fun noteList(
    @Body noteListRequest: NoteListRequest,
    @Url url: String,
    @Header("Authorization") authorization: String,
  ): Response<List<NoteResponse>>

  @POST()
  suspend fun noteListByIds(
    @Body noteListByIdsRequest: NoteListByIdsRequest,
    @Url url: String,
    @Header("Authorization") authorization: String,
  ): Response<List<NoteResponse>>

  @POST()
  suspend fun noteUpsert(
    @Body noteCreateRequest: UpsertRequest,
    @Url url: String,
    @Header("Authorization") authorization: String,
  ): Response<NoteResponse>

  @POST()
  suspend fun deleteNote(
    @Body deleteNoteRequest: DeleteNoteRequest,
    @Url url: String,
    @Header("Authorization") authorization: String,
  ): Response<DeleteNoteResponse>
}
