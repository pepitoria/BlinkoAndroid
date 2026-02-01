package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import javax.inject.Inject

class NoteDeleteUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun deleteNote(
    id: Int
  ): BlinkoResult<Boolean> {
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
}
