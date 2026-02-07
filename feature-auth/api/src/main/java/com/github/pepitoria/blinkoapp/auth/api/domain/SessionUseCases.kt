package com.github.pepitoria.blinkoapp.auth.api.domain

interface SessionUseCases {
  fun logout()
  suspend fun isSessionActive(): Boolean
  suspend fun login(): SessionResult
  suspend fun login(
    url: String,
    userName: String,
    password: String,
  ): SessionResult
}
