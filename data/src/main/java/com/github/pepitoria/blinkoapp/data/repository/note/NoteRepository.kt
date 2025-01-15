package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest

interface NoteRepository {
    suspend fun list(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>>
    suspend fun listByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>>
    suspend fun upsertNote(upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse>
}