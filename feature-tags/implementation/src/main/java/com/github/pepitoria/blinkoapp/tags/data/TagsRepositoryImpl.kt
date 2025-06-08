package com.github.pepitoria.blinkoapp.tags.data

import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.domain.data.AuthenticationRepository
import com.github.pepitoria.blinkoapp.tags.data.net.TagsApiClient
import com.github.pepitoria.blinkoapp.tags.domain.BlinkoTag
import javax.inject.Inject

class TagsRepositoryImpl @Inject constructor(
    private val api: TagsApiClient,
    private val tagMapper: TagMapper,
    private val authenticationRepository: AuthenticationRepository,
) : TagsRepository {

    override suspend fun getTags(): List<BlinkoTag> {

        authenticationRepository.getSession()?.let { session ->

            val token = session.token
            val url = session.url

            return when (val response = api.getTags(url = url, token = token)) {
                is ApiResult.ApiSuccess -> {
                    response.value.map { tagMapper.toBlinkoTag(it) }
                }

                is ApiResult.ApiErrorResponse -> {
                    // For now, we just return an empty list
                    emptyList()
                }
            }
        }
        return emptyList()
    }
}

