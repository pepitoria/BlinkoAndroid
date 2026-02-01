package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
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
