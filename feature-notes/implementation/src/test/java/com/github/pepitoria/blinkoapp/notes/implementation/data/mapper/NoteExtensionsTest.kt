package com.github.pepitoria.blinkoapp.notes.implementation.data.mapper

import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNote
import com.github.pepitoria.blinkoapp.notes.api.domain.model.BlinkoNoteType
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

class NoteExtensionsTest {

  // toBlinkoNote tests

  @Test
  fun `toBlinkoNote maps all fields correctly`() {
    val noteResponse = NoteResponse(
      id = 1,
      content = "Test content",
      type = 0,
      isArchived = false,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals(1, result.id)
    assertEquals("Test content", result.content)
    assertEquals(BlinkoNoteType.BLINKO, result.type)
    assertFalse(result.isArchived)
  }

  @Test
  fun `toBlinkoNote maps NOTE type correctly`() {
    val noteResponse = NoteResponse(
      id = 2,
      content = "Note content",
      type = 1,
      isArchived = false,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals(BlinkoNoteType.NOTE, result.type)
  }

  @Test
  fun `toBlinkoNote maps TODO type correctly`() {
    val noteResponse = NoteResponse(
      id = 3,
      content = "Todo content",
      type = 2,
      isArchived = true,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals(BlinkoNoteType.TODO, result.type)
    assertTrue(result.isArchived)
  }

  @Test
  fun `toBlinkoNote handles null content`() {
    val noteResponse = NoteResponse(
      id = 4,
      content = null,
      type = 0,
      isArchived = false,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals("", result.content)
  }

  @Test
  fun `toBlinkoNote handles null isArchived`() {
    val noteResponse = NoteResponse(
      id = 5,
      content = "Content",
      type = 0,
      isArchived = null,
    )

    val result = noteResponse.toBlinkoNote()

    assertFalse(result.isArchived)
  }

  @Test
  fun `toBlinkoNote handles null type`() {
    val noteResponse = NoteResponse(
      id = 6,
      content = "Content",
      type = null,
      isArchived = false,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals(BlinkoNoteType.BLINKO, result.type)
  }

  @Test
  fun `toBlinkoNote handles unknown type`() {
    val noteResponse = NoteResponse(
      id = 7,
      content = "Content",
      type = 99,
      isArchived = false,
    )

    val result = noteResponse.toBlinkoNote()

    assertEquals(BlinkoNoteType.BLINKO, result.type)
  }

  // toBlinkoNotes tests

  @Test
  fun `toBlinkoNotes maps empty list`() {
    val noteResponses = emptyList<NoteResponse>()

    val result = noteResponses.toBlinkoNotes()

    assertTrue(result.isEmpty())
  }

  @Test
  fun `toBlinkoNotes maps single note`() {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Note 1", type = 0, isArchived = false),
    )

    val result = noteResponses.toBlinkoNotes()

    assertEquals(1, result.size)
    assertEquals(1, result[0].id)
    assertEquals("Note 1", result[0].content)
  }

  @Test
  fun `toBlinkoNotes maps multiple notes`() {
    val noteResponses = listOf(
      NoteResponse(id = 1, content = "Note 1", type = 0, isArchived = false),
      NoteResponse(id = 2, content = "Note 2", type = 1, isArchived = true),
      NoteResponse(id = 3, content = "Note 3", type = 2, isArchived = false),
    )

    val result = noteResponses.toBlinkoNotes()

    assertEquals(3, result.size)
    assertEquals(1, result[0].id)
    assertEquals(2, result[1].id)
    assertEquals(3, result[2].id)
    assertEquals(BlinkoNoteType.BLINKO, result[0].type)
    assertEquals(BlinkoNoteType.NOTE, result[1].type)
    assertEquals(BlinkoNoteType.TODO, result[2].type)
  }

  // toUpsertRequest tests

  @Test
  fun `toUpsertRequest maps all fields correctly`() {
    val blinkoNote = BlinkoNote(
      id = 1,
      content = "Test content",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    val result = blinkoNote.toUpsertRequest()

    assertEquals(1, result.id)
    assertEquals("Test content", result.content)
    assertEquals(0, result.type)
    assertEquals(false, result.isArchived)
  }

  @Test
  fun `toUpsertRequest maps NOTE type correctly`() {
    val blinkoNote = BlinkoNote(
      id = 2,
      content = "Note content",
      type = BlinkoNoteType.NOTE,
      isArchived = false,
    )

    val result = blinkoNote.toUpsertRequest()

    assertEquals(1, result.type)
  }

  @Test
  fun `toUpsertRequest maps TODO type correctly`() {
    val blinkoNote = BlinkoNote(
      id = 3,
      content = "Todo content",
      type = BlinkoNoteType.TODO,
      isArchived = true,
    )

    val result = blinkoNote.toUpsertRequest()

    assertEquals(2, result.type)
    assertEquals(true, result.isArchived)
  }

  @Test
  fun `toUpsertRequest handles null id`() {
    val blinkoNote = BlinkoNote(
      id = null,
      content = "New note",
      type = BlinkoNoteType.BLINKO,
      isArchived = false,
    )

    val result = blinkoNote.toUpsertRequest()

    assertEquals(null, result.id)
    assertEquals("New note", result.content)
  }

  @Test
  fun `toUpsertRequest preserves archived state`() {
    val archivedNote = BlinkoNote(
      id = 5,
      content = "Archived note",
      type = BlinkoNoteType.TODO,
      isArchived = true,
    )

    val result = archivedNote.toUpsertRequest()

    assertEquals(true, result.isArchived)
  }
}
