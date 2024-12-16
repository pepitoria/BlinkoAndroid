package com.github.pepitoria.blinkoapp.domain.model.note

data class BlinkoNote(
  val content: String,
) {
  companion object {
    val EMPTY = BlinkoNote(
      content = ""
    )
  }
}
