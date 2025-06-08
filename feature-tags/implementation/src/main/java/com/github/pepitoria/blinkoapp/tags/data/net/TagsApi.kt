package com.github.pepitoria.blinkoapp.tags.data.net

import com.github.pepitoria.blinkoapp.tags.data.ResponseTag
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface TagsApi {

  @GET()
  suspend fun getTags(
    @Url url: String,
    @Header("Authorization") authorization: String
  ): Response<List<ResponseTag>>

}