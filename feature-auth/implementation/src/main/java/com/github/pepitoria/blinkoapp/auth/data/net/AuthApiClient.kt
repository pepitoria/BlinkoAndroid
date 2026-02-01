package com.github.pepitoria.blinkoapp.auth.data.net

import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult

interface AuthApiClient {
  suspend fun login(url: String, userName: String, password: String): ApiResult<LoginResponse>
  fun isConnected(): Boolean
}
