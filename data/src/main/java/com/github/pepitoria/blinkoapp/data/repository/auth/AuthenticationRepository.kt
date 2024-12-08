package com.github.pepitoria.blinkoapp.data.repository.auth

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.session.SessionDto

interface AuthenticationRepository {
    suspend fun login(loginRequest: LoginRequest): LoginResponse
    fun saveSession(sessionDto: SessionDto)
    fun getSession(): SessionDto?
}