package com.github.pepitoria.blinkoapp.data.mapper

import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType

fun List<NoteResponse>.toBlinkoNotes(): List<BlinkoNote> {
  return this.map { it.toBlinkoNote() }
}

fun NoteResponse.toBlinkoNote(): BlinkoNote {
  return BlinkoNote(
    id = this.id,
    content = this.content?:"",
    type = BlinkoNoteType.fromResponseType(this.type),
    isArchived = this.isArchived ?: false,
  )
}

fun BlinkoNote.toUpsertRequest(): UpsertRequest {
  return UpsertRequest(
    id = this.id,
    content = this.content,
    type = this.type.value,
    isArchived = this.isArchived,
  )
}