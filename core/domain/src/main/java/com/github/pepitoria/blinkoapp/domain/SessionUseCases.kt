package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.data.model.ApiResult
import com.github.pepitoria.blinkoapp.domain.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.domain.data.model.session.SessionDto
import javax.inject.Inject

class SessionUseCases @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  fun logout() {
    authenticationRepository.logout()
  }

  suspend fun isSessionActive(): Boolean {
    val session = authenticationRepository.getSession()

    session?.let {
      val response = noteRepository.list(
        url = session.url,
        token = session.token,
        noteListRequest = NoteListRequest()
      )

      return response is ApiResult.ApiSuccess
    }

    return false
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