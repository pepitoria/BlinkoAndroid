package com.github.pepitoria.blinkoapp.auth.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
  @SerializedName("name") val name: String,
  @SerializedName("password") val password: String,
)
