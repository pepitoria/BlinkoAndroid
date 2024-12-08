package com.github.pepitoria.blinkoapp.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlinkoApiClient @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val api: BlinkoApi,
) {

  suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteListResponse>> {
    if (!isConnected()) {
      //TODO handle no internet connection
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    var noteListUrl = url
    if (url.endsWith("/")) {
      noteListUrl = "${url}api/v1/note/list"
    } else {
      noteListUrl = "${url}/api/v1/note/list"
    }

    withContext(Dispatchers.IO) {
      val apiResponse = api.noteList(
        noteListRequest = noteListRequest,
        url = noteListUrl,
        authorization = "Bearer $token",
      )

      if (apiResponse.isSuccessful) {
        apiResponse.body()?.let { response ->
          return@withContext ApiResult.ApiSuccess(response)
        }
      } else {
        return@withContext ApiResult.ApiErrorResponse(
          code = apiResponse.code(),
          message = apiResponse.message()
        )
      }
    }
    return ApiResult.ApiErrorResponse(
      code = -1,
      message = "Unknown error"
    )
  }

  fun isConnected(): Boolean {
    val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }

}