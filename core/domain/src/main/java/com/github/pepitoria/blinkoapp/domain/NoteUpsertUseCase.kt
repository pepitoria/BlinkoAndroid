package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import javax.inject.Inject

class NoteUpsertUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
) {

  suspend fun upsertNote(
    blinkoNote: BlinkoNote,
  ): BlinkoResult<BlinkoNote> {
    val response = noteRepository.upsertNote(blinkoNote)

    return when (response) {
      is BlinkoResult.Success -> {
        BlinkoResult.Success(response.value)
      }

      is BlinkoResult.Error -> {
        response
      }
    }
  }
}