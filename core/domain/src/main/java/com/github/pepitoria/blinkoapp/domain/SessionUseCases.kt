package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.domain.data.NoteRepository
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
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