package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.offline.connectivity.ServerReachabilityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.TagDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.TagEntity
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TagsRepositoryImplTest {

  private lateinit var tagsRepository: TagsRepositoryImpl

  private val api: TagsApiClient = mockk()
  private val tagMapper: TagMapper = mockk()
  private val authenticationRepository: AuthenticationRepository = mockk()
  private val tagDao: TagDao = mockk(relaxed = true)
  private val serverReachabilityMonitor: ServerReachabilityMonitor = mockk()
  private val shouldAttemptFlow = MutableStateFlow(true)

  @BeforeEach
  fun setUp() {
    every { serverReachabilityMonitor.shouldAttemptServerCall() } returns true
    every { serverReachabilityMonitor.reportSuccess() } returns Unit
    every { serverReachabilityMonitor.reportUnreachable() } returns Unit

    tagsRepository = TagsRepositoryImpl(
      api = api,
      tagMapper = tagMapper,
      authenticationRepository = authenticationRepository,
      tagDao = tagDao,
      serverReachabilityMonitor = serverReachabilityMonitor,
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
  fun `getTags caches tags when api succeeds`() = runTest {
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

    tagsRepository.getTags()

    coVerify { tagDao.deleteAll() }
    coVerify { tagDao.insertAll(any()) }
  }

  @Test
  fun `getTags returns cached tags when api fails`() = runTest {
    val session = BlinkoSession(
      url = "https://test.com",
      token = "token123",
      userName = "user",
      password = "pass",
    )
    val cachedTags = listOf(
      TagEntity(id = 1, name = "CachedTag1"),
      TagEntity(id = 2, name = "CachedTag2"),
    )

    every { authenticationRepository.getSession() } returns session
    coEvery { api.getTags(url = "https://test.com", token = "token123") } returns ApiResult.ApiErrorResponse(
      code = 500,
      message = "Server error",
    )
    coEvery { tagDao.getAll() } returns cachedTags

    val result = tagsRepository.getTags()

    assertEquals(2, result.size)
    assertEquals("CachedTag1", result[0].name)
    assertEquals("CachedTag2", result[1].name)
  }

  @Test
  fun `getTags returns cached tags when offline`() = runTest {
    every { serverReachabilityMonitor.shouldAttemptServerCall() } returns false
    val cachedTags = listOf(
      TagEntity(id = 1, name = "OfflineTag1"),
      TagEntity(id = 2, name = "OfflineTag2"),
    )

    coEvery { tagDao.getAll() } returns cachedTags

    val result = tagsRepository.getTags()

    assertEquals(2, result.size)
    assertEquals("OfflineTag1", result[0].name)
    assertEquals("OfflineTag2", result[1].name)
    coVerify(exactly = 0) { api.getTags(any(), any()) }
  }

  @Test
  fun `getTags returns cached tags when no session exists`() = runTest {
    val cachedTags = listOf(
      TagEntity(id = 1, name = "CachedTag"),
    )

    every { authenticationRepository.getSession() } returns null
    coEvery { tagDao.getAll() } returns cachedTags

    val result = tagsRepository.getTags()

    assertEquals(1, result.size)
    assertEquals("CachedTag", result[0].name)
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
