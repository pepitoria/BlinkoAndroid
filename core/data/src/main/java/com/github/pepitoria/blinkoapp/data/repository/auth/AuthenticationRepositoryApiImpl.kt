package com.github.pepitoria.blinkoapp.data.repository.auth

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.LocalStorage
import com.github.pepitoria.blinkoapp.domain.model.BlinkoSession
import javax.inject.Inject

private const val URL_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.URL_KEY"
private const val TOKEN_KEY = "com.github.pepitoria.blinkoapp.data.repository.auth.TOKEN_KEY"

class AuthenticationRepositoryApiImpl @Inject constructor(
  private val localStorage: LocalStorage
) : AuthenticationRepository {

  override fun saveSession(url: String, token: String) {
    localStorage.saveString(URL_KEY, url)
    localStorage.saveString(TOKEN_KEY, token)
  }

  override fun getSession(): BlinkoSession? {
    val url = localStorage.getString(URL_KEY)
    val token = localStorage.getString(TOKEN_KEY)
    return if (url != null && token != null) {
      BlinkoSession(url, token)
    } else {
      null
    }
  }

  override fun logout() {
    localStorage.removeValue(URL_KEY)
    localStorage.removeValue(TOKEN_KEY)
  }
}