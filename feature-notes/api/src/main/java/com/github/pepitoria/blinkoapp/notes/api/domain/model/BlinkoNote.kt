package com.github.pepitoria.blinkoapp.notes.api.domain.model

data class BlinkoNote(
  val id: Int? = null,
  val content: String,
  val type: BlinkoNoteType,
  val isArchived: Boolean,
) {
  companion object {
    val EMPTY = BlinkoNote(
      content = "",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
  }
}
