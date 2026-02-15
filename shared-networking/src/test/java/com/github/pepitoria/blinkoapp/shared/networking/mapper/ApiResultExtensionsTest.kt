package com.github.pepitoria.blinkoapp.shared.networking.mapper

import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoResult
import com.github.pepitoria.blinkoapp.shared.networking.model.ApiResult
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.jupiter.api.Test

class ApiResultExtensionsTest {

  // toBlinkoResult for ApiSuccess tests

  @Test
  fun `toBlinkoResult maps ApiSuccess to BlinkoResult Success`() {
    val apiResult: ApiResult<String> = ApiResult.ApiSuccess("test data")

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Success<String>>(result)
    assertEquals("test data", result.value)
  }

  @Test
  fun `toBlinkoResult maps ApiSuccess with Int value`() {
    val apiResult: ApiResult<Int> = ApiResult.ApiSuccess(42)

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Success<Int>>(result)
    assertEquals(42, result.value)
  }

  @Test
  fun `toBlinkoResult maps ApiSuccess with complex object`() {
    data class TestData(val id: Int, val name: String)
    val testData = TestData(1, "test")
    val apiResult: ApiResult<TestData> = ApiResult.ApiSuccess(testData)

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Success<TestData>>(result)
    assertEquals(1, result.value.id)
    assertEquals("test", result.value.name)
  }

  @Test
  fun `toBlinkoResult maps ApiSuccess with list`() {
    val apiResult: ApiResult<List<String>> = ApiResult.ApiSuccess(listOf("a", "b", "c"))

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Success<List<String>>>(result)
    assertEquals(3, result.value.size)
  }

  // toBlinkoResult for ApiErrorResponse tests

  @Test
  fun `toBlinkoResult maps ApiErrorResponse to BlinkoResult Error`() {
    val apiResult: ApiResult<String> = ApiResult.ApiErrorResponse(
      code = 500,
      message = "Server error",
    )

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(500, result.code)
    assertEquals("Server error", result.message)
  }

  @Test
  fun `toBlinkoResult maps ApiErrorResponse with null code`() {
    val apiResult: ApiResult<String> = ApiResult.ApiErrorResponse(
      code = null,
      message = "Error message",
    )

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(-1, result.code)
    assertEquals("Error message", result.message)
  }

  @Test
  fun `toBlinkoResult maps ApiErrorResponse with null message`() {
    val apiResult: ApiResult<String> = ApiResult.ApiErrorResponse(
      code = 404,
      message = null,
    )

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(404, result.code)
    assertEquals("", result.message)
  }

  @Test
  fun `toBlinkoResult maps UNKNOWN ApiErrorResponse to UNKNOWN BlinkoResult Error`() {
    val apiResult: ApiResult<String> = ApiResult.ApiErrorResponse.UNKNOWN

    val result = apiResult.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(BlinkoResult.Error.UNKNOWN.code, result.code)
    assertEquals(BlinkoResult.Error.UNKNOWN.message, result.message)
  }

  // toBlinkoResult for ApiErrorResponse extension function tests

  @Test
  fun `ApiErrorResponse toBlinkoResult maps correctly`() {
    val apiError = ApiResult.ApiErrorResponse(
      code = 403,
      message = "Forbidden",
    )

    val result = apiError.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(403, result.code)
    assertEquals("Forbidden", result.message)
  }

  @Test
  fun `ApiErrorResponse UNKNOWN toBlinkoResult returns UNKNOWN`() {
    val apiError = ApiResult.ApiErrorResponse.UNKNOWN

    val result = apiError.toBlinkoResult()

    assertEquals(BlinkoResult.Error.UNKNOWN, result)
  }

  @Test
  fun `ApiErrorResponse with both null values toBlinkoResult`() {
    val apiError = ApiResult.ApiErrorResponse(
      code = null,
      message = null,
    )

    val result = apiError.toBlinkoResult()

    assertIs<BlinkoResult.Error>(result)
    assertEquals(-1, result.code)
    assertEquals("", result.message)
  }
}
