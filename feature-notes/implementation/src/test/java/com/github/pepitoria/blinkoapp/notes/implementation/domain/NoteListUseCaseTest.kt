package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteListUseCaseTest {

  private lateinit var noteListUseCase: NoteListUseCase

  private val noteRepository: NoteRepository = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    noteListUseCase = NoteListUseCase(
      noteRepository = noteRepository,
      authenticationRepository = authenticationRepository,
    )
  }

  @Test
  fun `listNotes returns success when repository returns success`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val notes = listOf(
      BlinkoNote(id = 1, content = "Note 1", type = BlinkoNoteType.BLINKO, isArchived = false),
      BlinkoNote(id = 2, content = "Note 2", type = BlinkoNoteType.NOTE, isArchived = false),
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = "https://test.com",
        token = "token123",
        type = 0,
        archived = false,
      )
    } returns BlinkoResult.Success(notes)

    val result = noteListUseCase.listNotes(type = 0)

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(2, result.value.size)
    assertEquals("Note 1", result.value[0].content)
  }

  @Test
  fun `listNotes returns error when repository returns error`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = "https://test.com",
        token = "token123",
        type = 0,
        archived = false,
      )
    } returns BlinkoResult.Error(500, "Server error")

    val result = noteListUseCase.listNotes(type = 0)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(500, result.code)
    assertEquals("Server error", result.message)
  }

  @Test
  fun `listNotes passes archived parameter to repository`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = any(),
        token = any(),
        type = any(),
        archived = true,
      )
    } returns BlinkoResult.Success(emptyList())

    noteListUseCase.listNotes(type = 1, archived = true)

    coVerify(exactly = 1) {
      noteRepository.list(
        url = "https://test.com",
        token = "token123",
        type = 1,
        archived = true,
      )
    }
  }

  @Test
  fun `listNotes uses session url and token from authenticationRepository`() = runTest {
    val session = BlinkoSession(
      url = "https://myserver.com",
      token = "myToken",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.list(
        url = "https://myserver.com",
        token = "myToken",
        type = 2,
        archived = false,
      )
    } returns BlinkoResult.Success(emptyList())

    noteListUseCase.listNotes(type = 2)

    coVerify(exactly = 1) {
      noteRepository.list(
        url = "https://myserver.com",
        token = "myToken",
        type = 2,
        archived = false,
      )
    }
  }

  @Test
  fun `listNotes uses empty strings when session is null`() = runTest {
    every { authenticationRepository.getSession() } returns null
    coEvery {
      noteRepository.list(
        url = "",
        token = "",
        type = 0,
        archived = false,
      )
    } returns BlinkoResult.Error(401, "Unauthorized")

    noteListUseCase.listNotes(type = 0)

    coVerify(exactly = 1) {
      noteRepository.list(
        url = "",
        token = "",
        type = 0,
        archived = false,
      )
    }
  }
}
