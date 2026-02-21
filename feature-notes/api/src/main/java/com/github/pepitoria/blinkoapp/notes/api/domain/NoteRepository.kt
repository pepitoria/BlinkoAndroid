package com.github.pepitoria.blinkoapp.notes.api.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
  suspend fun list(
    url: String,
    token: String,
    type: Int,
    archived: Boolean = false,
  ): BlinkoResult<List<BlinkoNote>>

  fun listAsFlow(
    type: Int,
    archived: Boolean = false,
  ): Flow<List<BlinkoNote>>

  suspend fun search(
    url: String,
    token: String,
    searchTerm: String,
  ): BlinkoResult<List<BlinkoNote>>

  suspend fun listByIds(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<List<BlinkoNote>>

  suspend fun listByIds(
    url: String,
    token: String,
    ids: List<Int>,
  ): BlinkoResult<List<BlinkoNote>>

  suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote>

  suspend fun delete(
    url: String,
    token: String,
    id: Int,
  ): BlinkoResult<Boolean>

  suspend fun deleteNote(note: BlinkoNote): BlinkoResult<Boolean>

  val pendingSyncCount: Flow<Int>

  val conflicts: Flow<List<BlinkoNote>>

  suspend fun refreshFromServer(
    url: String,
    token: String,
    type: Int,
    archived: Boolean = false,
  ): BlinkoResult<Unit>

  /**
   * Fetches additional pages from the server in background.
   * The UI will be updated via Flow as each page is merged into the local database.
   * @param additionalPages Number of additional pages to fetch (e.g., 5 means pages 2-6)
   */
  suspend fun fetchAdditionalPages(
    url: String,
    token: String,
    type: Int,
    archived: Boolean = false,
    additionalPages: Int = 5,
  )

  suspend fun resolveConflict(
    note: BlinkoNote,
    keepLocal: Boolean,
  ): BlinkoResult<BlinkoNote>
}
