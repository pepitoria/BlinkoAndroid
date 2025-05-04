package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoNotes
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import javax.inject.Inject

class NoteListUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun listNotes(
    type: Int
  ): BlinkoResult<List<BlinkoNote>> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.list(
      url = session?.url ?: "",
      token = session?.token ?: "",
      noteListRequest = NoteListRequest(type = type)
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        BlinkoResult.Success(response.value.toBlinkoNotes())
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }
}