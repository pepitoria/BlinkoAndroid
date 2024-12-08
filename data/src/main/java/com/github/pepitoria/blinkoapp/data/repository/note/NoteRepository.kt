package com.github.pepitoria.blinkoapp.data.repository.note

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListResponse

interface NoteRepository {
    suspend fun list(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteListResponse>>
}