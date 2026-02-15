package com.github.pepitoria.blinkoapp.notes.implementation.domain

import com.github.pepitoria.blinkoapp.notes.api.domain.NoteRepository
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteUpsertUseCaseTest {

  private lateinit var noteUpsertUseCase: NoteUpsertUseCase

  private val noteRepository: NoteRepository = mockk()

  @BeforeEach
  fun setUp() {
    noteUpsertUseCase = NoteUpsertUseCase(
      noteRepository = noteRepository,
    )
  }

  @Test
  fun `upsertNote returns success when repository returns success`() = runTest {
    val inputNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )
    val savedNote = BlinkoNote(
      id = 1,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    coEvery { noteRepository.upsertNote(inputNote) } returns BlinkoResult.Success(savedNote)

    val result = noteUpsertUseCase.upsertNote(inputNote)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    assertEquals(1, result.value.id)
    assertEquals("New note", result.value.content)
  }

  @Test
  fun `upsertNote returns error when repository returns error`() = runTest {
    val inputNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    coEvery { noteRepository.upsertNote(inputNote) } returns BlinkoResult.Error(400, "Bad request")

    val result = noteUpsertUseCase.upsertNote(inputNote)

    assertIs<BlinkoResult.Error>(result)
    assertEquals(400, result.code)
    assertEquals("Bad request", result.message)
  }

  @Test
  fun `upsertNote updates existing note`() = runTest {
    val inputNote = BlinkoNote(
      id = 5,
      content = "Updated content",
      type = BlinkoNoteType.NOTE,
      isArchived = false,
    )
    val updatedNote = BlinkoNote(
      id = 5,
      content = "Updated content",
      type = BlinkoNoteType.NOTE,
      isArchived = false,
    )

    coEvery { noteRepository.upsertNote(inputNote) } returns BlinkoResult.Success(updatedNote)

    val result = noteUpsertUseCase.upsertNote(inputNote)

    assertIs<BlinkoResult.Success<BlinkoNote>>(result)
    assertEquals(5, result.value.id)
    assertEquals("Updated content", result.value.content)
  }

  @Test
  fun `upsertNote delegates to repository`() = runTest {
    val inputNote = BlinkoNote(
      id = 1,
      content = "Test",
      type = BlinkoNoteType.TODO,
      isArchived = true,
    )

    coEvery { noteRepository.upsertNote(inputNote) } returns BlinkoResult.Success(inputNote)

    noteUpsertUseCase.upsertNote(inputNote)

    coVerify(exactly = 1) { noteRepository.upsertNote(inputNote) }
  }
}
