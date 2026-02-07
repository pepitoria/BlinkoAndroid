package com.github.pepitoria.blinkoapp.tags.data.net

import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import com.github.pepitoria.blinkoapp.tags.data.ResponseTag

interface TagsApiClient {
  suspend fun getTags(
    url: String,
    token: String,
  ): ApiResult<List<ResponseTag>>

  fun isConnected(): Boolean
}
