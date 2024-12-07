package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface BlinkoApi {

  @POST()
  suspend fun noteList(
    @Body noteListRequest: NoteListRequest,
    @Url url: String,
    @Header("Authorization") authorization: String
  ): Response<List<NoteListRequest>>

}