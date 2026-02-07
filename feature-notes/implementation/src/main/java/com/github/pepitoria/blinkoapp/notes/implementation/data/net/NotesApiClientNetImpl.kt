package com.github.pepitoria.blinkoapp.notes.implementation.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.notes.implementation.data.model.noteupsert.UpsertRequest
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesApiClientNetImpl @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val api: NotesApi,
) : NotesApiClient {

  override suspend fun noteList(
    url: String,
    token: String,
    noteListRequest: NoteListRequest,
  ): ApiResult<List<NoteResponse>> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val noteListUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/list"
    } else {
      "$url/api/v1/note/list"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.noteList(
        noteListRequest = noteListRequest,
        url = noteListUrl,
        authorization = "Bearer $token",
      )

      var apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiErrorResponse.UNKNOWN

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

  override suspend fun noteListByIds(
    url: String,
    token: String,
    noteListByIdsRequest: NoteListByIdsRequest,
  ): ApiResult<List<NoteResponse>> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val noteListUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/list-by-ids"
    } else {
      "$url/api/v1/note/list-by-ids"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.noteListByIds(
        noteListByIdsRequest = noteListByIdsRequest,
        url = noteListUrl,
        authorization = "Bearer $token",
      )

      var apiResult: ApiResult<List<NoteResponse>> = ApiResult.ApiErrorResponse.UNKNOWN

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

  override suspend fun upsertNote(
    url: String,
    token: String,
    upsertNoteRequest: UpsertRequest,
  ): ApiResult<NoteResponse> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val upsertNoteUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/upsert"
    } else {
      "$url/api/v1/note/upsert"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.noteUpsert(
        noteCreateRequest = upsertNoteRequest,
        url = upsertNoteUrl,
        authorization = "Bearer $token",
      )

      var apiResult: ApiResult<NoteResponse> = ApiResult.ApiErrorResponse.UNKNOWN

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

  override suspend fun deleteNote(
    url: String,
    token: String,
    deleteNoteRequest: DeleteNoteRequest,
  ): ApiResult<DeleteNoteResponse> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val deleteNoteUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/batch-trash"
    } else {
      "$url/api/v1/note/batch-trash"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.deleteNote(
        deleteNoteRequest = deleteNoteRequest,
        url = deleteNoteUrl,
        authorization = "Bearer $token",
      )

      var apiResult: ApiResult<DeleteNoteResponse> = ApiResult.ApiErrorResponse.UNKNOWN

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

  override fun isConnected(): Boolean {
    val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }
}
