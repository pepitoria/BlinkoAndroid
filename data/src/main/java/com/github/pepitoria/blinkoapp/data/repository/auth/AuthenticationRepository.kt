package com.github.pepitoria.blinkoapp.data.repository.auth

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse

interface AuthenticationRepository {
    suspend fun login(loginRequest: LoginRequest): LoginResponse
}