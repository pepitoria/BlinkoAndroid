package com.github.pepitoria.blinkoapp.data.mapper

import com.github.pepitoria.blinkoapp.data.model.login.LoginResponse
import com.github.pepitoria.blinkoapp.domain.model.BlinkoUser
import javax.inject.Inject

class UserMapper @Inject constructor() {

  fun toBlinkoUser(userResponse: LoginResponse): BlinkoUser {
    return BlinkoUser(
      id = userResponse.id ?: 0,
      name = userResponse.name ?: "",
      nickname = userResponse.nickname ?: "",
      token = userResponse.token ?: ""
    )
  }
}