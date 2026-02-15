package com.github.pepitoria.blinkoapp.tags.domain

import com.github.pepitoria.blinkoapp.tags.data.TagsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetTagsUseCaseTest {

  private lateinit var getTagsUseCase: GetTagsUseCase

  private val tagsRepository: TagsRepository = mockk()

  @BeforeEach
  fun setUp() {
    getTagsUseCase = GetTagsUseCase(
      tagsRepository = tagsRepository,
    )
  }

  @Test
  fun `invoke returns tags from repository`() = runTest {
    val tags = listOf(
      BlinkoTag(name = "Tag1"),
      BlinkoTag(name = "Tag2"),
    )

    coEvery { tagsRepository.getTags() } returns tags

    val result = getTagsUseCase()

    assertEquals(2, result.size)
    assertEquals("Tag1", result[0].name)
    assertEquals("Tag2", result[1].name)
  }

  @Test
  fun `invoke returns empty list when repository returns empty`() = runTest {
    coEvery { tagsRepository.getTags() } returns emptyList()

    val result = getTagsUseCase()

    assertTrue(result.isEmpty())
  }

  @Test
  fun `invoke delegates to repository`() = runTest {
    coEvery { tagsRepository.getTags() } returns emptyList()

    getTagsUseCase()

    coVerify(exactly = 1) { tagsRepository.getTags() }
  }
}
