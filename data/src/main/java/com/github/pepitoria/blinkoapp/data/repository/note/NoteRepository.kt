package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListResponse
import com.github.pepitoria.blinkoapp.data.model.noteupsert.Note
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest

interface NoteRepository {
    suspend fun list(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteListResponse>>
    suspend fun createNote(createNoteRequest: UpsertRequest): ApiResult<Note>
}