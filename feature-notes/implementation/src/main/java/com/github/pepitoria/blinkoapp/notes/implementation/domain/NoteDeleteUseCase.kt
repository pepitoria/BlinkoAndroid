package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import javax.inject.Inject

class NoteDeleteUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun deleteNote(id: Int): BlinkoResult<Boolean> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.delete(
      url = session?.url ?: "",
      token = session?.token ?: "",
      id = id,
    )

    return when (response) {
      is BlinkoResult.Success -> {
        BlinkoResult.Success(response.value)
      }

      is BlinkoResult.Error -> {
        response
      }
    }
  }

  suspend fun deleteNote(note: BlinkoNote): BlinkoResult<Boolean> {
    return noteRepository.deleteNote(note)
  }
}
