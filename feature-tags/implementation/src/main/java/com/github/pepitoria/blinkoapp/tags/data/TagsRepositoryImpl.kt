package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.offline.connectivity.ServerReachabilityMonitor
import com.github.pepitoria.blinkoapp.offline.data.db.dao.TagDao
import com.github.pepitoria.blinkoapp.offline.data.db.entity.TagEntity
import com.github.pepitoria.blinkoapp.shared.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import com.github.pepitoria.blinkoapp.tags.data.net.TagsApiClient
import com.github.pepitoria.blinkoapp.tags.domain.BlinkoTag
import javax.inject.Inject

class TagsRepositoryImpl @Inject constructor(
  private val api: TagsApiClient,
  private val tagMapper: TagMapper,
  private val authenticationRepository: AuthenticationRepository,
  private val tagDao: TagDao,
  private val serverReachabilityMonitor: ServerReachabilityMonitor,
) : TagsRepository {

  override suspend fun getTags(): List<BlinkoTag> {
    // If offline or server unreachable, return cached tags
    if (!serverReachabilityMonitor.shouldAttemptServerCall()) {
      return getCachedTags()
    }

    // If online, try to fetch from server
    authenticationRepository.getSession()?.let { session ->
      val token = session.token
      val url = session.url

      return when (val response = api.getTags(url = url, token = token)) {
        is ApiResult.ApiSuccess -> {
          serverReachabilityMonitor.reportSuccess()
          val responseTags = response.value
          val tags = responseTags.map { tagMapper.toBlinkoTag(it) }
          // Cache the tags
          cacheTags(tags, responseTags)
          tags
        }

        is ApiResult.ApiErrorResponse -> {
          if (response.isServerUnreachable) {
            serverReachabilityMonitor.reportUnreachable()
          }
          // On error, return cached tags
          getCachedTags()
        }
      }
    }
    return getCachedTags()
  }

  private suspend fun getCachedTags(): List<BlinkoTag> {
    return tagDao.getAll().map { entity ->
      BlinkoTag(
        name = entity.name,
      )
    }
  }

  private suspend fun cacheTags(
    tags: List<BlinkoTag>,
    responseTags: List<ResponseTag>,
  ) {
    val entities = responseTags.mapNotNull { responseTag ->
      responseTag.id?.let { id ->
        TagEntity(
          id = id,
          name = responseTag.name ?: "",
          icon = responseTag.icon,
          parent = responseTag.parent,
        )
      }
    }
    tagDao.deleteAll()
    tagDao.insertAll(entities)
  }
}
