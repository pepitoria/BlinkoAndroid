package com.github.pepitoria.blinkoapp.auth.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.auth.data.model.LoginRequest
import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
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

class AuthApiClientNetImpl @Inject constructor(
  @ApplicationContext private val appContext: Context,
  private val api: AuthApi,
) : AuthApiClient {

  override suspend fun login(
    url: String,
    userName: String,
    password: String,
  ): ApiResult<LoginResponse> {
    if (!isConnected()) {
      return ApiResult.ApiErrorResponse.SERVER_UNREACHABLE
    }

    val loginUrl = if (url.endsWith("/")) {
      "${url}api/v1/user/login"
    } else {
      "$url/api/v1/user/login"
    }

    return withContext(Dispatchers.IO) {
      try {
        val apiResponse = api.login(
          url = loginUrl,
          loginRequest = LoginRequest(
            name = userName,
            password = password,
          ),
        )

        var apiResult: ApiResult<LoginResponse> = ApiResult.ApiErrorResponse.UNKNOWN

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
