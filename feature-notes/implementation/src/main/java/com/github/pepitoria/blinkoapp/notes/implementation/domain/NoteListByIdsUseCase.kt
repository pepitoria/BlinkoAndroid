package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import javax.inject.Inject

class NoteListByIdsUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun getNoteById(
    id: Int,
  ): BlinkoResult<BlinkoNote> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.listByIds(
      url = session?.url ?: "",
      token = session?.token ?: "",
      id = id,
    )

    return when (response) {
      is BlinkoResult.Success -> {
        response.value.firstOrNull()?.let {
          BlinkoResult.Success(it)
        } ?: BlinkoResult.Error.NOTFOUND
      }

      is BlinkoResult.Error -> {
        response
      }
    }
  }

  suspend fun listNotesByIds(
    ids: List<Int>,
  ): BlinkoResult<List<BlinkoNote>> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.listByIds(
      url = session?.url ?: "",
      token = session?.token ?: "",
      ids = ids,
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
