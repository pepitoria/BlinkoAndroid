package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoNote
import com.github.pepitoria.blinkoapp.domain.mapper.toBlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote
import javax.inject.Inject

class NoteUpsertUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
) {

  suspend fun upsertNote(
    content: String,
  ): BlinkoResult<BlinkoNote> {
    val response = noteRepository.upsertNote(UpsertRequest(content = content))

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