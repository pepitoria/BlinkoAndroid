package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import javax.inject.Inject

@Deprecated("Use TokenLoginWidget instead")
class LoginUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend fun login(
      url: String,
      email: String,
      password: String
    ): Boolean {
      val loginRequest = LoginRequest()
      val response = authenticationRepository.login(loginRequest)
      return true
    }
}