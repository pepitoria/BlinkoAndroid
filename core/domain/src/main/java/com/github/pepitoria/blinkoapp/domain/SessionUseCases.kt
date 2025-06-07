package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoUser
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
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
        type = BlinkoNoteType.BLINKO.value
        )

      return response is BlinkoResult.Success
    }

    return false
  }

  suspend fun login(): BlinkoResult<BlinkoUser> {
    authenticationRepository.getSession()?.let { session ->
      return authenticationRepository.login(
        url = session.url,
        userName = session.userName,
        password = session.password
      )
    }

    return BlinkoResult.Error.MISSING_USER_DATA
  }


  suspend fun login(url: String, userName: String, password: String): BlinkoResult<BlinkoUser> {

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

        response
      }

      is BlinkoResult.Error -> {
        BlinkoResult.Error(
          message = response.message,
          code = response.code
        )
      }
    }
  }

  suspend fun checkSession(url: String, token: String): Boolean {

    val response = noteRepository.list(
      url = url,
      token = token,
      type = BlinkoNoteType.BLINKO.value,
    )

    return when (response) {
      is BlinkoResult.Success-> {
        authenticationRepository.saveSession(
          url = url,
          token = token,
        )
        true
      }

      is BlinkoResult.Error -> {
        false
      }
    }
  }
}