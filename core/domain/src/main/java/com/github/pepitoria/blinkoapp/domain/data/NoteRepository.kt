package com.github.pepitoria.blinkoapp.domain.data

import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNote

interface NoteRepository {
    suspend fun list(url: String, token: String, type: Int): BlinkoResult<List<BlinkoNote>>
    suspend fun search(url: String, token: String, searchTerm: String): BlinkoResult<List<BlinkoNote>>
    suspend fun listByIds(url: String, token: String, id: Int): BlinkoResult<List<BlinkoNote>>
    suspend fun listByIds(url: String, token: String, ids: List<Int>): BlinkoResult<List<BlinkoNote>>
    suspend fun upsertNote(blinkoNote: BlinkoNote): BlinkoResult<BlinkoNote>
}