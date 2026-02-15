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

class NoteListByIdsUseCaseTest {

  private lateinit var noteListByIdsUseCase: NoteListByIdsUseCase

  private val noteRepository: NoteRepository = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    noteListByIdsUseCase = NoteListByIdsUseCase(
      noteRepository = noteRepository,
      authenticationRepository = authenticationRepository,
    )
  }

  // getNoteById tests

  @Test
  fun `getNoteById returns success when note found`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val note = BlinkoNote(
      id = 1,
      content = "Found note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://test.com",
        token = "token123",
        id = 1,
      )
    } returns BlinkoResult.Success(listOf(note))

    val result = noteListByIdsUseCase.getNoteById(id = 1)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    assertEquals(1, result.value.id)
    assertEquals("Found note", result.value.content)
  }

  @Test
  fun `getNoteById returns NOTFOUND when empty list returned`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://test.com",
        token = "token123",
        id = 999,
      )
    } returns BlinkoResult.Success(emptyList())

    val result = noteListByIdsUseCase.getNoteById(id = 999)

    assertEquals(BlinkoResult.Error.NOTFOUND, result)
  }

  @Test
  fun `getNoteById returns error when repository returns error`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://test.com",
        token = "token123",
        id = 1,
      )
    } returns BlinkoResult.Error(500, "Server error")

    val result = noteListByIdsUseCase.getNoteById(id = 1)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(500, result.code)
  }

  @Test
  fun `getNoteById uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://myserver.com",
      token = "myToken",
      userName = "user",
      password = "pass",
    )
    val note = BlinkoNote(
      id = 42,
      content = "Note",
      type = BlinkoNoteType.NOTE,
      isArchived = false,
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://myserver.com",
        token = "myToken",
        id = 42,
      )
    } returns BlinkoResult.Success(listOf(note))

    noteListByIdsUseCase.getNoteById(id = 42)

    coVerify(exactly = 1) {
      noteRepository.listByIds(
        url = "https://myserver.com",
        token = "myToken",
        id = 42,
      )
    }
  }

  // listNotesByIds tests

  @Test
  fun `listNotesByIds returns success when notes found`() = runTest {
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
      noteRepository.listByIds(
        url = "https://test.com",
        token = "token123",
        ids = listOf(1, 2),
      )
    } returns BlinkoResult.Success(notes)

    val result = noteListByIdsUseCase.listNotesByIds(ids = listOf(1, 2))

    assertIs<BlinkoResult.Success<List<BlinkoNote>>>(result)
    assertEquals(2, result.value.size)
  }

  @Test
  fun `listNotesByIds returns error when repository returns error`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://test.com",
        token = "token123",
        ids = listOf(1, 2, 3),
      )
    } returns BlinkoResult.Error(500, "Server error")

    val result = noteListByIdsUseCase.listNotesByIds(ids = listOf(1, 2, 3))

    assertIs<BlinkoResult.Error>(result)
    assertEquals(500, result.code)
  }

  @Test
  fun `listNotesByIds uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://myserver.com",
      token = "myToken",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery {
      noteRepository.listByIds(
        url = "https://myserver.com",
        token = "myToken",
        ids = listOf(1, 2, 3),
      )
    } returns BlinkoResult.Success(emptyList())

    noteListByIdsUseCase.listNotesByIds(ids = listOf(1, 2, 3))

    coVerify(exactly = 1) {
      noteRepository.listByIds(
        url = "https://myserver.com",
        token = "myToken",
        ids = listOf(1, 2, 3),
      )
    }
  }

  @Test
  fun `listNotesByIds uses empty strings when session is null`() = runTest {
    every { authenticationRepository.getSession() } returns null
    coEvery {
      noteRepository.listByIds(
        url = "",
        token = "",
        ids = listOf(1),
      )
    } returns BlinkoResult.Error(401, "Unauthorized")

    noteListByIdsUseCase.listNotesByIds(ids = listOf(1))

    coVerify(exactly = 1) {
      noteRepository.listByIds(
        url = "",
        token = "",
        ids = listOf(1),
      )
    }
  }
}
