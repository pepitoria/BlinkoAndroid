package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
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

class NoteDeleteUseCaseTest {

  private lateinit var noteDeleteUseCase: NoteDeleteUseCase

  private val noteRepository: NoteRepository = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    noteDeleteUseCase = NoteDeleteUseCase(
      noteRepository = noteRepository,
      authenticationRepository = authenticationRepository,
    )
  }

  @Test
  fun `deleteNote returns success when repository returns success`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.delete(
        url = "https://test.com",
        token = "token123",
        id = 1,
      )
    } returns BlinkoResult.Success(true)

    val result = noteDeleteUseCase.deleteNote(id = 1)

    assertIs<BlinkoResult.Success<Boolean>>(result)
    assertTrue(result.value)
  }

  @Test
  fun `deleteNote returns error when repository returns error`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.delete(
        url = "https://test.com",
        token = "token123",
        id = 999,
      )
    } returns BlinkoResult.Error(404, "Note not found")

    val result = noteDeleteUseCase.deleteNote(id = 999)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(404, result.code)
    assertEquals("Note not found", result.message)
  }

  @Test
  fun `deleteNote uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://myserver.com",
      token = "myToken",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.delete(
        url = "https://myserver.com",
        token = "myToken",
        id = 42,
      )
    } returns BlinkoResult.Success(true)

    noteDeleteUseCase.deleteNote(id = 42)

    coVerify(exactly = 1) {
      noteRepository.delete(
        url = "https://myserver.com",
        token = "myToken",
        id = 42,
      )
    }
  }

  @Test
  fun `deleteNote uses empty strings when session is null`() = runTest {
    every { authenticationRepository.getSession() } returns null
    coEvery {
      noteRepository.delete(
        url = "",
        token = "",
        id = 1,
      )
    } returns BlinkoResult.Error(401, "Unauthorized")

    noteDeleteUseCase.deleteNote(id = 1)

    coVerify(exactly = 1) {
      noteRepository.delete(
        url = "",
        token = "",
        id = 1,
      )
    }
  }
}
