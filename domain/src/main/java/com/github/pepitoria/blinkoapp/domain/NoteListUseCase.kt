package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import timber.log.Timber
import javax.inject.Inject

class NoteListUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  //TODO WIP
  suspend fun listNotes(
    url: String,
    token: String,
  ): Boolean {
    val session = authenticationRepository.getSession()

    val response = noteRepository.list(
      url = session?.url ?: "",
      token = session?.token ?: "",
      noteListRequest = NoteListRequest()
    )

    return true
  }
}