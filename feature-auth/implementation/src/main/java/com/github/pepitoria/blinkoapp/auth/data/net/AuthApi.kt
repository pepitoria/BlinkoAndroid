package com.github.pepitoria.blinkoapp.auth.data.net

import com.github.pepitoria.blinkoapp.auth.data.model.LoginRequest
import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthApi {

  @POST
  suspend fun login(
    @Body loginRequest: LoginRequest,
    @Url url: String,
  ): Response<LoginResponse>
}
