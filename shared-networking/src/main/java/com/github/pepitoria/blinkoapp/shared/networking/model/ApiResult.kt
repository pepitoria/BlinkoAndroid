package com.github.pepitoria.blinkoapp.shared.networking.model

sealed class ApiResult<out T> {
  data class ApiSuccess<out R>(val value: R) : ApiResult<R>()

  data class ApiErrorResponse(
    var code: Int? = null,
    var message: String? = null,
  ) : ApiResult<Nothing>() {
    companion object {
      val UNKNOWN = ApiErrorResponse(
        code = -1,
        message = "unknown error",
      )
    }
  }
}
