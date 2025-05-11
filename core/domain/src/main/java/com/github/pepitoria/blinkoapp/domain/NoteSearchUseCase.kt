package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import javax.inject.Inject

class NoteSearchUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun searchNotes(
    searchTerm: String,
  ): BlinkoResult<List<BlinkoNote>> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.search(
      url = session?.url ?: "",
      token = session?.token ?: "",
      searchTerm = searchTerm,
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