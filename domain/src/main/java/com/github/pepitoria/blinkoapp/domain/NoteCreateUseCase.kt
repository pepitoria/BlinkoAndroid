package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import timber.log.Timber
import javax.inject.Inject

class NoteCreateUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
) {

  suspend fun createNote(
    content: String,
  ): BlinkoResult<BlinkoNote> {
    val response = noteRepository.createNote(UpsertRequest(content = content))

    return when (response) {
      is ApiResult.ApiSuccess -> {
        BlinkoResult.Success(response.value.toBlinkoNote())
      }

      is ApiResult.ApiErrorResponse -> {
        response.toBlinkoResult()
      }
    }
  }
}