package com.github.pepitoria.blinkoapp.domain.mapper

import com.github.pepitoria.blinkoapp.data.model.noteupsert.Note
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote

fun Note.toBlinkoNote(): BlinkoNote {
  return BlinkoNote(
    content = this.content?: ""
  )
}