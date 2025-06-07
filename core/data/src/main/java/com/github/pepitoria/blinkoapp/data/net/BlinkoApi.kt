package com.github.pepitoria.blinkoapp.data.net

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

interface BlinkoApi {

  @POST()
  suspend fun login(
    @Body loginRequest: LoginRequest,
    @Url url: String,
  ): Response<LoginResponse>

  @POST()
  suspend fun noteList(
    @Body noteListRequest: NoteListRequest,
    @Url url: String,
    @Header("Authorization") authorization: String
  ): Response<List<NoteResponse>>

  @POST()
  suspend fun noteListByIds(
    @Body noteListByIdsRequest: NoteListByIdsRequest,
    @Url url: String,
    @Header("Authorization") authorization: String
  ): Response<List<NoteResponse>>

  @POST()
  suspend fun noteUpsert(
    @Body noteCreateRequest: UpsertRequest,
    @Url url: String,
    @Header("Authorization") authorization: String
  ): Response<NoteResponse>


}
