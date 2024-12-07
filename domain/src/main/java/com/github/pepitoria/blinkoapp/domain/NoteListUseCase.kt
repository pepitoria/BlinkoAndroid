package com.github.pepitoria.blinkoapp.domain

import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.repository.auth.AuthenticationRepository
import com.github.pepitoria.blinkoapp.data.repository.note.NoteRepository
import timber.log.Timber
import javax.inject.Inject

class NoteListUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun listNotes(
      url: String,
      token: String,
    ): Boolean {

      val response = noteRepository.list(
        url = url,
        token = token,
        noteListRequest = NoteListRequest()
      )
      Timber.d("response size: ${response.size}")
      return true
    }
}