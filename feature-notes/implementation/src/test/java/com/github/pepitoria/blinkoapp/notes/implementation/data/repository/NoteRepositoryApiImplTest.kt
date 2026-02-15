package com.github.pepitoria.blinkoapp.notes.implementation.data.repository

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteRepositoryApiImplTest {

  private lateinit var noteRepository: NoteRepositoryApiImpl

  private val api: NotesApiClient = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    noteRepository = NoteRepositoryApiImpl(
      api = api,
      authenticationRepository = authenticationRepository,
    )
  }

  // list tests

  @Test
  fun `list returns success when api succeeds`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(
      value = emptyList(),
    )

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.list(
      url = "url",
      token = "token",
      type = 1,
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
  }

  @Test
  fun `list returns error when api fails`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiErrorResponse(
      code = 3,
      message = "error",
    )

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.list(
      url = "url",
      token = "token",
      type = 1,
    )

    assertIs<BlinkoResult.Error>(result)
    assertEquals(BlinkoResult.Error(3, "error"), result)
  }

  @Test
  fun `list maps note responses to blinko notes`() = runTest {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Note 1", type = 0, isArchived = false),
      NoteResponse(id = 2, content = "Note 2", type = 1, isArchived = true),
    )
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(noteResponses)

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.list(
      url = "url",
      token = "token",
      type = 1,
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(2, result.value.size)
    assertEquals(1, result.value[0].id)
    assertEquals("Note 1", result.value[0].content)
    assertEquals(BlinkoNoteType.BLINKO, result.value[0].type)
    assertEquals(2, result.value[1].id)
    assertEquals("Note 2", result.value[1].content)
    assertEquals(BlinkoNoteType.NOTE, result.value[1].type)
  }

  // search tests

  @Test
  fun `search returns success when api succeeds`() = runTest {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Search term found", type = 0, isArchived = false),
    )
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(noteResponses)

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.search(
      url = "url",
      token = "token",
      searchTerm = "search",
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(1, result.value.size)
  }

  @Test
  fun `search returns error when api fails`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiErrorResponse(
      code = 500,
      message = "Server error",
    )

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.search(
      url = "url",
      token = "token",
      searchTerm = "search",
    )

    assertIs<BlinkoResult.Error>(result)
    assertEquals(500, result.code)
    assertEquals("Server error", result.message)
  }

  // listByIds (single id) tests

  @Test
  fun `listByIds with single id returns success when note found`() = runTest {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Found note", type = 0, isArchived = false),
    )
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(noteResponses)

    coEvery { api.noteListByIds(any(), any(), any()) } returns apiResult

    val result = noteRepository.listByIds(
      url = "url",
      token = "token",
      id = 1,
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(1, result.value.size)
    assertEquals(1, result.value[0].id)
  }

  @Test
  fun `listByIds with single id returns NOTFOUND when no notes returned`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(emptyList())

    coEvery { api.noteListByIds(any(), any(), any()) } returns apiResult

    val result = noteRepository.listByIds(
      url = "url",
      token = "token",
      id = 999,
    )

    assertEquals(BlinkoResult.Error.NOTFOUND, result)
  }

  @Test
  fun `listByIds with single id returns error when api fails`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiErrorResponse(
      code = 404,
      message = "Not found",
    )

    coEvery { api.noteListByIds(any(), any(), any()) } returns apiResult

    val result = noteRepository.listByIds(
      url = "url",
      token = "token",
      id = 1,
    )

    assertIs<BlinkoResult.Error>(result)
    assertEquals(404, result.code)
  }

  // listByIds (list of ids) tests

  @Test
  fun `listByIds with list of ids returns success when notes found`() = runTest {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Note 1", type = 0, isArchived = false),
    )
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(noteResponses)

    coEvery { api.noteListByIds(any(), any(), any()) } returns apiResult

    val result = noteRepository.listByIds(
      url = "url",
      token = "token",
      ids = listOf(1, 2, 3),
    )

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
  }

  @Test
  fun `listByIds with list of ids returns NOTFOUND when empty response`() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(emptyList())

    coEvery { api.noteListByIds(any(), any(), any()) } returns apiResult

    val result = noteRepository.listByIds(
      url = "url",
      token = "token",
      ids = listOf(1, 2, 3),
    )

    assertEquals(BlinkoResult.Error.NOTFOUND, result)
  }

  // upsertNote tests

  @Test
  fun `upsertNote returns success when api succeeds and session exists`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val blinkoNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
    val responseNote = NoteResponse(id = 1, content = "New note", type = 0, isArchived = false)

    every { authenticationRepository.getSession() } returns session
    coEvery { api.upsertNote(any(), any(), any()) } returns ApiResult.ApiSuccess(responseNote)

    val result = noteRepository.upsertNote(blinkoNote)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    assertEquals(1, result.value.id)
    assertEquals("New note", result.value.content)
  }

  @Test
  fun `upsertNote returns NOTFOUND when no session exists`() = runTest {
    val blinkoNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    every { authenticationRepository.getSession() } returns null

    val result = noteRepository.upsertNote(blinkoNote)

    assertEquals(BlinkoResult.Error.NOTFOUND, result)
  }

  @Test
  fun `upsertNote returns error when api fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val blinkoNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.upsertNote(any(), any(), any()) } returns ApiResult.ApiErrorResponse(
      code = 400,
      message = "Bad request",
    )

    val result = noteRepository.upsertNote(blinkoNote)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(400, result.code)
  }

  @Test
  fun `upsertNote uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val blinkoNote = BlinkoNote(
      id = 1,
      content = "Update note",
      type = BlinkoNoteType.NOTE,
      isArchived = false,
    )
    val responseNote = NoteResponse(id = 1, content = "Update note", type = 1, isArchived = false)

    every { authenticationRepository.getSession() } returns session
    coEvery { api.upsertNote(any(), any(), any()) } returns ApiResult.ApiSuccess(responseNote)

    noteRepository.upsertNote(blinkoNote)

    coVerify(exactly = 1) {
      api.upsertNote(
        url = "https://test.com",
        token = "token123",
        upsertNoteRequest = any(),
      )
    }
  }

  // delete tests

  @Test
  fun `delete returns success when api succeeds and session exists`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.deleteNote(any(), any(), any()) } returns ApiResult.ApiSuccess(
      DeleteNoteResponse(ok = true),
    )

    val result = noteRepository.delete(
      url = "url",
      token = "token",
      id = 1,
    )

    assertIs<BlinkoResult.Success<Boolean>>(result)
    assertTrue(result.value)
  }

  @Test
  fun `delete returns NOTFOUND when no session exists`() = runTest {
    every { authenticationRepository.getSession() } returns null

    val result = noteRepository.delete(
      url = "url",
      token = "token",
      id = 1,
    )

    assertEquals(BlinkoResult.Error.NOTFOUND, result)
  }

  @Test
  fun `delete returns error when api fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.deleteNote(any(), any(), any()) } returns ApiResult.ApiErrorResponse(
      code = 404,
      message = "Note not found",
    )

    val result = noteRepository.delete(
      url = "url",
      token = "token",
      id = 1,
    )

    assertIs<BlinkoResult.Error>(result)
    assertEquals(404, result.code)
  }

  @Test
  fun `delete uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.deleteNote(any(), any(), any()) } returns ApiResult.ApiSuccess(
      DeleteNoteResponse(ok = true),
    )

    noteRepository.delete(
      url = "url",
      token = "token",
      id = 42,
    )

    coVerify(exactly = 1) {
      api.deleteNote(
        url = "https://test.com",
        token = "token123",
        deleteNoteRequest = any(),
      )
    }
  }
}
