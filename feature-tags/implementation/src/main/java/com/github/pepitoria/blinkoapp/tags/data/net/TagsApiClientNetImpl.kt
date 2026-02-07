package com.github.pepitoria.blinkoapp.tags.data.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import com.github.pepitoria.blinkoapp.tags.data.ResponseTag
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TagsApiClientNetImpl @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val api: TagsApi,
) : TagsApiClient {

  @SuppressLint("MissingPermission")
  override suspend fun getTags(
    url: String,
    token: String,
  ): ApiResult<List<ResponseTag>> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    var tagsUrl = if (url.endsWith("/")) {
      "${url}api/v1/tags/list"
    } else {
      "$url/api/v1/tags/list"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.getTags(
        url = tagsUrl,
        authorization = "Bearer $token",
      )

      var apiResult: ApiResult<List<ResponseTag>> = ApiResult.ApiErrorResponse.UNKNOWN

      if (apiResponse.isSuccessful) {
        apiResponse.body()?.let { resp ->
          apiResult = ApiResult.ApiSuccess(resp)
        }
      } else {
        apiResult = ApiResult.ApiErrorResponse(
          code = apiResponse.code(),
          message = apiResponse.message(),
        )
      }

      apiResult
    }
  }

  @SuppressLint("MissingPermission")
  override fun isConnected(): Boolean {
    val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }
}
