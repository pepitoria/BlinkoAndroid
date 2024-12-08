package com.github.pepitoria.blinkoapp.data.repository.auth

import com.github.pepitoria.blinkoapp.data.localstorage.LocalStorage
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.session.SessionDto
import javax.inject.Inject

private const val URL_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.URL_KEY"
private const val TOKEN_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.TOKEN_KEY"

class AuthenticationRepositoryApiImpl @Inject constructor(
  private val localStorage: LocalStorage
) : AuthenticationRepository {
  override suspend fun login(loginRequest: LoginRequest): LoginResponse {
    //TODO: no api call yet
    return LoginResponse()
  }

  override fun saveSession(sessionDto: SessionDto) {
    localStorage.saveString(URL_KEY, sessionDto.url)
    localStorage.saveString(TOKEN_KEY, sessionDto.token)
  }

  override fun getSession(): SessionDto? {
    val url = localStorage.getString(URL_KEY)
    val token = localStorage.getString(TOKEN_KEY)
    return if (url != null && token != null) {
      SessionDto(url, token)
    } else {
      null
    }
  }
}