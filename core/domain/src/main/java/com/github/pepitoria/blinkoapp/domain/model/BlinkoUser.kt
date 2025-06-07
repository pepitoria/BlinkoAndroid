package com.github.pepitoria.blinkoapp.domain.model

data class BlinkoUser(
  val id: Int,
  val name: String,
  val nickname: String,
  val token: String,
)