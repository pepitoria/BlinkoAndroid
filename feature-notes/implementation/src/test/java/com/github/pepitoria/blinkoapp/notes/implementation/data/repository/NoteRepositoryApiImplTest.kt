package com.github.pepitoria.blinkoapp.notes.implementation.data.repository

import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.net.NotesApiClient
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
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

  @Test
  fun testListReturnsBlinkoSuccessWhenApiSuccess() = runTest {
    val apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiSuccess(
      value = emptyList(),
    )

    coEvery { api.noteList(any(), any(), any()) } returns apiResult

    val result = noteRepository.list(
      url = "url",
      token = "token",
      type = 1,
    )

    assert(result is BlinkoResult.Success)
  }

  @Test
  fun testListReturnsBlinkoErrorWhenApiError() = runTest {
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

    assertTrue(result is BlinkoResult.Error)
    assertEquals(result, BlinkoResult.Error(3, "error"))
  }

//  @Test
//  fun search() {
//  }
//
//  @Test
//  fun listByIds() {
//  }
//
//  @Test
//  fun testListByIds() {
//  }
//
//  @Test
//  fun upsertNote() {
//  }
}
