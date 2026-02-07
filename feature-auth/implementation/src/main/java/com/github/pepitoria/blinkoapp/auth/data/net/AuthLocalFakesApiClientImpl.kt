package com.github.pepitoria.blinkoapp.auth.data.net

import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import javax.inject.Inject

class AuthLocalFakesApiClientImpl @Inject constructor() : AuthApiClient {

  override suspend fun login(
    url: String,
    userName: String,
    password: String,
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
      ),
    )
  }

  override fun isConnected(): Boolean {
    return true
  }
}
