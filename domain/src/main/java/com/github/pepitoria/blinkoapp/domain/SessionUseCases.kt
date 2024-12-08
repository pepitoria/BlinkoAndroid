package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.session.SessionDto
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import javax.inject.Inject

class SessionUseCases @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun isSessionActive(): Boolean {
    val session = authenticationRepository.getSession()

    val response = noteRepository.list(
      url = session?.url ?: "",
      token = session?.token ?: "",
      noteListRequest = NoteListRequest()
    )

    return response is ApiResult.ApiSuccess
  }

  suspend fun checkSession(url: String, token: String): Boolean {

    val response = noteRepository.list(
      url = url,
      token = token,
      noteListRequest = NoteListRequest()
    )

    return when (response) {
      is ApiResult.ApiSuccess -> {
        authenticationRepository.saveSession(SessionDto(url = url, token = token))
        true
      }

      is ApiResult.ApiErrorResponse -> {
        false
      }
    }
  }
}