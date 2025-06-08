package com.github.pepitoria.blinkoapp.data.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
  @SerializedName("id") var id: Int? = null,
  @SerializedName("name") var name: String? = null,
  @SerializedName("nickname") var nickname: String? = null,
  @SerializedName("role") var role: String? = null,
  @SerializedName("token") var token: String? = null,
  @SerializedName("image") var image: String? = null,
  @SerializedName("loginType") var loginType: String? = null
)
