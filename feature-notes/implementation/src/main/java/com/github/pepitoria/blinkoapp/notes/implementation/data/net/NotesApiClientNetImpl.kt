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
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

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
      return ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
    }

    val noteListUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/list"
    } else {
      "$url/api/v1/note/list"
    }

    return withContext(Dispatchers.IO) {
      try {
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
          val code = apiResponse.code()
          apiResult = if (code in 502..504) {
            ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
          } else {
            ApiResult.ApiErrorResponse(
              code = code,
              message = apiResponse.message(),
            )
          }
        }

        apiResult
      } catch (e: SocketTimeoutException) {
        Timber.w(e, "Server unreachable: socket timeout")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: ConnectException) {
        Timber.w(e, "Server unreachable: connection failed")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: UnknownHostException) {
        Timber.w(e, "Server unreachable: unknown host")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: IOException) {
        Timber.w(e, "Server unreachable: IO error")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      }
    }
  }

  override suspend fun noteListByIds(
    url: String,
    token: String,
    noteListByIdsRequest: NoteListByIdsRequest,
  ): ApiResult<List<NoteResponse>> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
    }

    val noteListUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/list-by-ids"
    } else {
      "$url/api/v1/note/list-by-ids"
    }

    return withContext(Dispatchers.IO) {
      try {
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
          val code = apiResponse.code()
          apiResult = if (code in 502..504) {
            ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
          } else {
            ApiResult.ApiErrorResponse(
              code = code,
              message = apiResponse.message(),
            )
          }
        }

        apiResult
      } catch (e: SocketTimeoutException) {
        Timber.w(e, "Server unreachable: socket timeout")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: ConnectException) {
        Timber.w(e, "Server unreachable: connection failed")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: UnknownHostException) {
        Timber.w(e, "Server unreachable: unknown host")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: IOException) {
        Timber.w(e, "Server unreachable: IO error")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      }
    }
  }

  override suspend fun upsertNote(
    url: String,
    token: String,
    upsertNoteRequest: UpsertRequest,
  ): ApiResult<NoteResponse> {
    Timber.d("NotesApiClient.upsertNote: checking connectivity...")
    if (!isConnected()) {
      Timber.d("NotesApiClient.upsertNote: no connectivity, returning SERVER_UNREACHABLE")
      return ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
    }

    val upsertNoteUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/upsert"
    } else {
      "$url/api/v1/note/upsert"
    }

    Timber.d("NotesApiClient.upsertNote: making API call to $upsertNoteUrl")
    return withContext(Dispatchers.IO) {
      try {
        val apiResponse = api.noteUpsert(
          noteCreateRequest = upsertNoteRequest,
          url = upsertNoteUrl,
          authorization = "Bearer $token",
        )

        var apiResult: ApiResult<NoteResponse> = ApiResult.ApiErrorResponse.UNKNOWN

        if (apiResponse.isSuccessful) {
          Timber.d("NotesApiClient.upsertNote: API call successful")
          apiResponse.body()?.let { resp ->
            apiResult = ApiResult.ApiSuccess(resp)
          }
        } else {
          val code = apiResponse.code()
          Timber.d("NotesApiClient.upsertNote: API call returned error - code=$code, message=${apiResponse.message()}")
          // Treat gateway errors as server unreachable
          apiResult = if (code in 502..504) {
            ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
          } else {
            ApiResult.ApiErrorResponse(
              code = code,
              message = apiResponse.message(),
            )
          }
        }

        apiResult
      } catch (e: SocketTimeoutException) {
        Timber.w(e, "NotesApiClient.upsertNote: Server unreachable - socket timeout")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: ConnectException) {
        Timber.w(e, "NotesApiClient.upsertNote: Server unreachable - connection failed")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: UnknownHostException) {
        Timber.w(e, "NotesApiClient.upsertNote: Server unreachable - unknown host")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: IOException) {
        Timber.w(e, "NotesApiClient.upsertNote: Server unreachable - IO error")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      }
    }
  }

  override suspend fun deleteNote(
    url: String,
    token: String,
    deleteNoteRequest: DeleteNoteRequest,
  ): ApiResult<DeleteNoteResponse> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
    }

    val deleteNoteUrl = if (url.endsWith("/")) {
      "${url}api/v1/note/batch-trash"
    } else {
      "$url/api/v1/note/batch-trash"
    }

    return withContext(Dispatchers.IO) {
      try {
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
          val code = apiResponse.code()
          apiResult = if (code in 502..504) {
            ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
          } else {
            ApiResult.ApiErrorResponse(
              code = code,
              message = apiResponse.message(),
            )
          }
        }

        apiResult
      } catch (e: SocketTimeoutException) {
        Timber.w(e, "Server unreachable: socket timeout")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: ConnectException) {
        Timber.w(e, "Server unreachable: connection failed")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: UnknownHostException) {
        Timber.w(e, "Server unreachable: unknown host")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      } catch (e: IOException) {
        Timber.w(e, "Server unreachable: IO error")
        ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
      }
    }
  }

  override fun isConnected(): Boolean {
    val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = connectivityManager.activeNetworkInfo
    return netInfo != null && netInfo.isConnected
  }
}
