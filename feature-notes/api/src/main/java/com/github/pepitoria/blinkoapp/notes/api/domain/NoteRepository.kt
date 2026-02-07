package com.github.pepitoria.blinkoapp.notes.api.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult

interface NoteRepository {
  suspend fun list(
    url: String,
    token: String,
    type: Int,
    archived: Boolean = false,
  ): BlinkoResult<List<BlinkoNote>>
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
}
