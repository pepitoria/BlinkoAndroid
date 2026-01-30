package com.github.pepitoria.blinkoapp.auth.api.domain

sealed class SessionResult {
  data class Success(
    val userName: String,
    val token: String,
  ) : SessionResult()

  data class Error(
    val code: Int,
    val message: String,
  ) : SessionResult()
}
