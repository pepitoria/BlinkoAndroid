package com.github.pepitoria.blinkoapp.shared.domain.model

sealed class BlinkoResult<out T> {
  data class Success<out R>(val value: R) : BlinkoResult<R>()
  data class Error(
    val code: Int,
    val message: String,
  ) : BlinkoResult<Nothing>() {

    companion object {
      val UNKNOWN = Error(
        code = -1,
        message = "Unknown error",
      )
      val NOTFOUND = Error(
        code = -2,
        message = "Note not found",
      )
      val MISSING_USER_DATA = Error(
        code = -3,
        message = "Missing user data, cannot login without user data",
      )
    }
  }
}
