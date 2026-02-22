package com.github.pepitoria.blinkoapp.shared.networking.model

sealed class ApiResult<out T> {
  data class ApiSuccess<out R>(val value: R) : ApiResult<R>()

  data class ApiErrorResponse(
    var code: Int? = null,
    var message: String? = null,
    val isServerUnreachable: Boolean = false,
  ) : ApiResult<Nothing>() {
    companion object {
      val UNKNOWN = ApiErrorResponse(
        code = -1,
        message = "unknown error",
      )

      val SERVER_UNREACHABLE = ApiErrorResponse(
        code = -2,
        message = "Server unreachable",
        isServerUnreachable = true,
      )
    }
  }
}
