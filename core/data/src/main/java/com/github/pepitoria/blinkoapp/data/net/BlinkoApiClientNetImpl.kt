package com.github.pepitoria.blinkoapp.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.data.model.ApiResult
import com.github.pepitoria.blinkoapp.data.model.login.LoginRequest
import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteRequest
import com.github.pepitoria.blinkoapp.data.model.notedelete.DeleteNoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteListRequest
import com.github.pepitoria.blinkoapp.data.model.notelist.NoteResponse
import com.github.pepitoria.blinkoapp.data.model.notelistbyids.NoteListByIdsRequest
import com.github.pepitoria.blinkoapp.data.model.noteupsert.UpsertRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BlinkoApiClientNetImpl @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val api: BlinkoApi,
): BlinkoApiClient {

  override suspend fun login(
    url: String,
    userName: String,
    password: String
  ): ApiResult<LoginResponse> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    var loginUrl = url
    if (url.endsWith("/")) {
      loginUrl = "${url}api/v1/user/login"
    } else {
      loginUrl = "${url}/api/v1/user/login"
    }

    return withContext(Dispatchers.IO) {
      val apiResponse = api.login(
        url = loginUrl,
        loginRequest = LoginRequest(
          name = userName,
          password = password
        )
      )

      var apiResult: ApiResult<LoginResponse> = ApiResult.ApiErrorResponse.UNKNOWN

      if (apiResponse.isSuccessful) {
        apiResponse.body()?.let { resp ->
          apiResult = ApiResult.ApiSuccess(resp)
        }
      } else {
        apiResult = ApiResult.ApiErrorResponse(
          code = apiResponse.code(),
          message = apiResponse.message()
        )
      }

      apiResult
    }
  }

  override suspend fun noteList(url: String, token: String, noteListRequest: NoteListRequest): ApiResult<List<NoteResponse>> {
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
          message = apiResponse.message()
        )
      }

      apiResult
    }
  }

  override suspend fun noteListByIds(url: String, token: String, noteListByIdsRequest: NoteListByIdsRequest): ApiResult<List<NoteResponse>> {
    if (!isConnected()) {
      //TODO handle no internet connection
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    var noteListUrl = url
    if (url.endsWith("/")) {
      noteListUrl = "${url}api/v1/note/list-by-ids"
    } else {
      noteListUrl = "${url}/api/v1/note/list-by-ids"
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
          message = apiResponse.message()
        )
      }

      apiResult
    }
  }

  override suspend fun upsertNote(url: String, token: String, upsertNoteRequest: UpsertRequest): ApiResult<NoteResponse> {
    if (!isConnected()) {
      //TODO handle no internet connection
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    var upsertNoteUrl = url
    if (url.endsWith("/")) {
      upsertNoteUrl = "${url}api/v1/note/upsert"
    } else {
      upsertNoteUrl = "${url}/api/v1/note/upsert"
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
          message = apiResponse.message()
        )
      }

      apiResult
    }
  }

  override suspend fun deleteNote(url: String, token: String, deleteNoteRequest: DeleteNoteRequest): ApiResult<DeleteNoteResponse> {
    if (!isConnected()) {
      //TODO handle no internet connection
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val deleteNoteUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/batch-delete"
    } else {
      "${url}/api/v1/note/batch-delete"
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
          message = apiResponse.message()
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