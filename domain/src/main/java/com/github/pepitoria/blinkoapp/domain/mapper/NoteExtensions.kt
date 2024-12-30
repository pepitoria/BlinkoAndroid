package com.github.pepitoria.blinkoapp.domain.mapper

import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListResponse
import com.github.pepitoria.blinkoapp.data.model.noteupsert.Note
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote

fun Note.toBlinkoNote(): BlinkoNote {
  return BlinkoNote(
    content = this.content?: ""
  )
}

fun List<NoteListResponse>.toBlinkoNotes(): List<BlinkoNote> {
  return this.map { it.toBlinkoNote() }
}

fun NoteListResponse.toBlinkoNote(): BlinkoNote {
  return BlinkoNote(
    id = this.id,
    content = this.content?:"",
//    createdAt = this.createdAt,
//    updatedAt = this.updatedAt
  )
}