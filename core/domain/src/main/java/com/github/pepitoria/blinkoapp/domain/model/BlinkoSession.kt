package com.github.pepitoria.blinkoapp.domain.model

data class BlinkoSession(
  val url: String,
  val token: String,
  val userName: String,
  val password: String,
)