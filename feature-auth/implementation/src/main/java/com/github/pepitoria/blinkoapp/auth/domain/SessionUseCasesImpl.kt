package com.github.pepitoria.blinkoapp.auth.domain

import com.github.pepitoria.blinkoapp.auth.api.domain.SessionResult
import com.github.pepitoria.blinkoapp.auth.api.domain.SessionUseCases
import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import javax.inject.Inject

class SessionUseCasesImpl @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) : SessionUseCases {

  override fun logout() {
    authenticationRepository.logout()
  }

  override suspend fun isSessionActive(): Boolean {
    val session = authenticationRepository.getSession()

    session?.let {
      val response = noteRepository.list(
        url = session.url,
        token = session.token,
        type = BlinkoNoteType.BLINKO.value
      )

      return response is BlinkoResult.Success
    }

    return false
  }

  override suspend fun login(): SessionResult {
    authenticationRepository.getSession()?.let { session ->
      val result = authenticationRepository.login(
        url = session.url,
        userName = session.userName,
        password = session.password
      )

      return when (result) {
        is BlinkoResult.Success -> {
          SessionResult.Success(
            userName = result.value.name,
            token = result.value.token,
          )
        }
        is BlinkoResult.Error -> {
          SessionResult.Error(
            code = result.code,
            message = result.message,
          )
        }
      }
    }

    return SessionResult.Error(
      code = -3,
      message = "Missing user data, cannot login without user data",
    )
  }

  override suspend fun login(url: String, userName: String, password: String): SessionResult {
    val response = authenticationRepository.login(
      url = url,
      userName = userName,
      password = password
    )

    return when (response) {
      is BlinkoResult.Success -> {
        authenticationRepository.saveSession(
          url = url,
          userName = userName,
          password = password,
          token = response.value.token
        )

        SessionResult.Success(
          userName = response.value.name,
          token = response.value.token,
        )
      }

      is BlinkoResult.Error -> {
        SessionResult.Error(
          message = response.message,
          code = response.code
        )
      }
    }
  }
}
