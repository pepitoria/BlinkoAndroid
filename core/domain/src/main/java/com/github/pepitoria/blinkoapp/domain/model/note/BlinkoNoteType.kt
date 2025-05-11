package com.github.pepitoria.blinkoapp.domain.model.note

enum class BlinkoNoteType(val value: Int) {
  BLINKO(0),
  NOTE(1);

  companion object {
    fun fromResponseType(type: Int?): BlinkoNoteType {
      return when (type) {
        0 -> BLINKO
        1 -> NOTE
        else -> BLINKO
      }
    }
  }
}