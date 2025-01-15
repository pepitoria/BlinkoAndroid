package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoNotes
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
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
      noteListByIdsRequest = NoteListByIdsRequest(listOf(id))
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        response.value.firstOrNull()?.let {
          BlinkoResult.Success(it.toBlinkoNote())
        } ?: BlinkoResult.Error.NOTFOUND
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
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
      noteListByIdsRequest = NoteListByIdsRequest(ids)
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