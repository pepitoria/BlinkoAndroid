package com.github.pepitoria.blinkoapp.data.repository.auth

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import javax.inject.Inject

class AuthenticationRepositoryApiImpl @Inject constructor() : AuthenticationRepository {
  override suspend fun login(loginRequest: LoginRequest): LoginResponse {

    return LoginResponse()
  }
}