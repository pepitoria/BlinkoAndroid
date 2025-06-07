package com.github.pepitoria.blinkoapp.data.model.login

import com.google.gson.annotations.SerializedName

data class LoginRequest(
  @SerializedName("name") val name: String,
  @SerializedName("password") val password: String,
)