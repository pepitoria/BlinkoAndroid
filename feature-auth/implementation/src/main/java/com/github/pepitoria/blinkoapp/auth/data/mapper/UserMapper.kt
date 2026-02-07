package com.github.pepitoria.blinkoapp.auth.data.mapper

import com.github.pepitoria.blinkoapp.auth.data.model.LoginResponse
import com.github.pepitoria.blinkoapp.shared.domain.model.BlinkoUser
import javax.inject.Inject

class UserMapper @Inject constructor() {

  fun toBlinkoUser(userResponse: LoginResponse): BlinkoUser {
    return BlinkoUser(
      id = userResponse.id ?: 0,
      name = userResponse.name ?: "",
      nickname = userResponse.nickname ?: "",
      token = userResponse.token ?: "",
    )
  }
}
