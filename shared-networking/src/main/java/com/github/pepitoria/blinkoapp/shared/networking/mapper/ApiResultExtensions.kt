package com.github.pepitoria.blinkoapp.shared.networking.mapper

import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult.Success
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult

inline fun <reified T> ApiResult<T>.toBlinkoResult(): BlinkoResult<T> {
    return when (this) {
        is ApiResult.ApiSuccess -> Success(
          value
        )
        is ApiResult.ApiErrorResponse -> this.toBlinkoResult()
    }
}

fun ApiResult.ApiErrorResponse.toBlinkoResult(): BlinkoResult.Error {
    return when (this) {
        ApiResult.ApiErrorResponse.Companion.UNKNOWN -> BlinkoResult.Error.Companion.UNKNOWN
        else -> {

          BlinkoResult.Error(
            code = this.code ?: -1,
            message = this.message ?: ""
          )
        }

    }
}
