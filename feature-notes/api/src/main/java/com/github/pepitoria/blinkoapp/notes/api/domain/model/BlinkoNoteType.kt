package com.github.pepitoria.blinkoapp.notes.api.domain.model

enum class BlinkoNoteType(val value: Int) {
  BLINKO(value = 0),
  NOTE(value = 1),
  TODO(value = 2);

  companion object {
    fun fromResponseType(type: Int?): BlinkoNoteType {
      return when (type) {
        0 -> BLINKO
        1 -> NOTE
        2 -> TODO
        else -> BLINKO
      }
    }
  }
}
