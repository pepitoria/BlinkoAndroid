package com.github.pepitoria.blinkoapp.domain.mapper

import com.github.pepitoria.blinkoapp.domain.data.model.ApiResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.domain.model.BlinkoResult.Success

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