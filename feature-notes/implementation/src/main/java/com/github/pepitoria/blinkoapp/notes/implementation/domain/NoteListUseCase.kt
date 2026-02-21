package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class NoteListUseCase @Inject constructor(
  private val noteRepository: NoteRepository,
  private val authenticationRepository: AuthenticationRepository,
) {

  suspend fun listNotes(
    type: Int,
    archived: Boolean = false,
  ): BlinkoResult<List<BlinkoNote>> {
    val session = authenticationRepository.getSession()

    val response = noteRepository.list(
      url = session?.url ?: "",
      token = session?.token ?: "",
      type = type,
      archived = archived,
    )

    return when (response) {
      is BlinkoResult.Success -> {
        BlinkoResult.Success(response.value)
      }

      is BlinkoResult.Error -> {
        response
      }
    }
  }

  fun listNotesAsFlow(
    type: Int,
    archived: Boolean = false,
  ): Flow<List<BlinkoNote>> {
    return noteRepository.listAsFlow(type = type, archived = archived)
  }

  val pendingSyncCount: Flow<Int> = noteRepository.pendingSyncCount

  val conflicts: Flow<List<BlinkoNote>> = noteRepository.conflicts

  suspend fun refreshFromServer(
    type: Int,
    archived: Boolean = false,
  ): BlinkoResult<Unit> {
    val session = authenticationRepository.getSession() ?: return BlinkoResult.Error.MISSING_USER_DATA
    return noteRepository.refreshFromServer(
      url = session.url,
      token = session.token,
      type = type,
      archived = archived,
    )
  }

  suspend fun resolveConflict(
    note: BlinkoNote,
    keepLocal: Boolean,
  ): BlinkoResult<BlinkoNote> {
    return noteRepository.resolveConflict(note, keepLocal)
  }
}
