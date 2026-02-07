package com.github.pepitoria.blinkoapp.auth.data.net

import android.content.Context
import android.net.ConnectivityManager
import com.github.pepitoria.blinkoapp.auth.data.model.LoginRequest
import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
      return ApiResult.ApiErrorResponse(message = "No internet connection")
    }

    val loginUrl = if (url.endsWith("/")) {
      "${url}api/v1/user/login"
    } else {
      "$url/api/v1/user/login"
    }

    return withContext(Dispatchers.IO) {
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
