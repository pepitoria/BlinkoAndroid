package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoSession
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import com.github.pepitoria.blinkoapp.tags.data.net.TagsApiClient
import com.github.pepitoria.blinkoapp.tags.domain.BlinkoTag
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TagsRepositoryImplTest {

  private lateinit var tagsRepository: TagsRepositoryImpl

  private val api: TagsApiClient = mockk()
  private val tagMapper: TagMapper = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()

  @BeforeEach
  fun setUp() {
    tagsRepository = TagsRepositoryImpl(
      api = api,
      tagMapper = tagMapper,
      authenticationRepository = authenticationRepository,
    )
  }

  @Test
  fun `getTags returns mapped tags when api succeeds`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val responseTags = listOf(
      ResponseTag(id = 1, name = "Tag1"),
      ResponseTag(id = 2, name = "Tag2"),
    )
    val blinkoTags = listOf(
      BlinkoTag(name = "Tag1"),
      BlinkoTag(name = "Tag2"),
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.getTags(url = "https://test.com", token = "token123") } returns ApiResult.ApiSuccess(responseTags)
    every { tagMapper.toBlinkoTag(responseTags[0]) } returns blinkoTags[0]
    every { tagMapper.toBlinkoTag(responseTags[1]) } returns blinkoTags[1]

    val result = tagsRepository.getTags()

    assertEquals(2, result.size)
    assertEquals("Tag1", result[0].name)
    assertEquals("Tag2", result[1].name)
  }

  @Test
  fun `getTags returns empty list when api fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.getTags(url = "https://test.com", token = "token123") } returns ApiResult.ApiErrorResponse(
      code = 500,
      message = "Server error",
    )

    val result = tagsRepository.getTags()

    assertTrue(result.isEmpty())
  }

  @Test
  fun `getTags returns empty list when no session exists`() = runTest {
    every { authenticationRepository.getSession() } returns null

    val result = tagsRepository.getTags()

    assertTrue(result.isEmpty())
    coVerify(exactly = 0) { api.getTags(any(), any()) }
  }

  @Test
  fun `getTags uses session url and token`() = runTest {
    val session = BlinkoSession(
      url = "https://myserver.com",
      token = "myToken",
      userName = "user",
      password = "pass",
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.getTags(url = "https://myserver.com", token = "myToken") } returns ApiResult.ApiSuccess(emptyList())

    tagsRepository.getTags()

    coVerify(exactly = 1) { api.getTags(url = "https://myserver.com", token = "myToken") }
  }
}
