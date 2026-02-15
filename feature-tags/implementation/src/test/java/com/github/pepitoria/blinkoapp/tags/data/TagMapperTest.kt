package com.github.pepitoria.blinkoapp.tags.data

import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TagMapperTest {

  private lateinit var tagMapper: TagMapper

  @BeforeEach
  fun setUp() {
    tagMapper = TagMapper()
  }

  @Test
  fun `toBlinkoTag maps name correctly`() {
    val responseTag = ResponseTag(
      id = 1,
      name = "work",
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("work", result.name)
  }

  @Test
  fun `toBlinkoTag handles null name`() {
    val responseTag = ResponseTag(
      id = 2,
      name = null,
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("", result.name)
  }

  @Test
  fun `toBlinkoTag ignores extra fields`() {
    val responseTag = ResponseTag(
      id = 3,
      name = "personal",
      icon = "icon.png",
      parent = 1,
      sortOrder = 5,
      createdAt = "2024-01-01",
      updatedAt = "2024-01-02",
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("personal", result.name)
  }

  @Test
  fun `toBlinkoTag maps empty name`() {
    val responseTag = ResponseTag(
      id = 4,
      name = "",
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("", result.name)
  }

  @Test
  fun `toBlinkoTag maps tag with special characters`() {
    val responseTag = ResponseTag(
      id = 5,
      name = "work/project-2024",
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("work/project-2024", result.name)
  }

  @Test
  fun `toBlinkoTag maps tag with unicode characters`() {
    val responseTag = ResponseTag(
      id = 6,
      name = "trabajo",
    )

    val result = tagMapper.toBlinkoTag(responseTag)

    assertEquals("trabajo", result.name)
  }
}
