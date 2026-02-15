package com.github.pepitoria.blinkoapp.notes.implementation.presentation

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.domain.NoteUpsertUseCase
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareAndEditWithBlinkoViewModelTest {

  private lateinit var viewModel: ShareAndEditWithBlinkoViewModel

  private val noteUpsertUseCase: NoteUpsertUseCase = mockk(relaxed = true)

  private val testDispatcher = StandardTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @BeforeEach
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    viewModel = ShareAndEditWithBlinkoViewModel(
      noteUpsertUseCase = noteUpsertUseCase,
    )
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // updateLocalNote tests (synchronous)

  @Test
  fun `updateLocalNote updates content in noteUiModel`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Test content")

    assertEquals("Test content", viewModel.noteUiModel.value.content)
  }

  @Test
  fun `updateLocalNote updates content and type`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Test content", noteType = BlinkoNoteType.NOTE.value)

    assertEquals("Test content", viewModel.noteUiModel.value.content)
    assertEquals(BlinkoNoteType.NOTE, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `updateLocalNote with TODO type`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Todo item", noteType = BlinkoNoteType.TODO.value)

    assertEquals("Todo item", viewModel.noteUiModel.value.content)
    assertEquals(BlinkoNoteType.TODO, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `updateLocalNote defaults to BLINKO type`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Quick note")

    assertEquals(BlinkoNoteType.BLINKO, viewModel.noteUiModel.value.type)
  }

  // setNoteType tests (synchronous)

  @Test
  fun `setNoteType changes note type to NOTE`() = testScope.runTest {
    viewModel.setNoteType(BlinkoNoteType.NOTE.value)

    assertEquals(BlinkoNoteType.NOTE, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `setNoteType changes note type to TODO`() = testScope.runTest {
    viewModel.setNoteType(BlinkoNoteType.TODO.value)

    assertEquals(BlinkoNoteType.TODO, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `setNoteType changes note type to BLINKO`() = testScope.runTest {
    viewModel.setNoteType(BlinkoNoteType.NOTE.value)
    viewModel.setNoteType(BlinkoNoteType.BLINKO.value)

    assertEquals(BlinkoNoteType.BLINKO, viewModel.noteUiModel.value.type)
  }

  @Test
  fun `setNoteType preserves content`() = testScope.runTest {
    viewModel.updateLocalNote(content = "My content")

    viewModel.setNoteType(BlinkoNoteType.NOTE.value)

    assertEquals("My content", viewModel.noteUiModel.value.content)
  }

  // createNote tests

  @Test
  fun `createNote calls upsertUseCase with current note`() = testScope.runTest {
    viewModel.updateLocalNote(content = "Note to save")
    val expectedNote = viewModel.noteUiModel.value
    coEvery { noteUpsertUseCase.upsertNote(expectedNote) } returns BlinkoResult.Success(expectedNote)

    viewModel.createNote()
    advanceUntilIdle()

    coVerify(exactly = 1) { noteUpsertUseCase.upsertNote(expectedNote) }
  }

  @Test
  fun `createNote calls upsertUseCase with empty note`() = testScope.runTest {
    coEvery { noteUpsertUseCase.upsertNote(BlinkoNote.EMPTY) } returns BlinkoResult.Success(BlinkoNote.EMPTY)

    viewModel.createNote()
    advanceUntilIdle()

    coVerify(exactly = 1) { noteUpsertUseCase.upsertNote(BlinkoNote.EMPTY) }
  }

  @Test
  fun `createNote calls upsertUseCase with updated note type`() = testScope.runTest {
    viewModel.setNoteType(BlinkoNoteType.NOTE.value)
    val expectedNote = viewModel.noteUiModel.value
    coEvery { noteUpsertUseCase.upsertNote(expectedNote) } returns BlinkoResult.Success(expectedNote)

    viewModel.createNote()
    advanceUntilIdle()

    coVerify(exactly = 1) { noteUpsertUseCase.upsertNote(expectedNote) }
  }

  @Test
  fun `createNote initially has null noteCreated state`() = testScope.runTest {
    assertNull(viewModel.noteCreated.value)
  }

  @Test
  fun `createNote initially has null error state`() = testScope.runTest {
    assertNull(viewModel.error.value)
  }
}
