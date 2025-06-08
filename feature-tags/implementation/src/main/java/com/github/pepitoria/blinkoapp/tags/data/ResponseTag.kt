package com.github.pepitoria.blinkoapp.tags.data

import com.google.gson.annotations.SerializedName

data class ResponseTag (
  @SerializedName("id") val id: Int? = null,
  @SerializedName("name") val name: String? = null,
  @SerializedName("icon") val icon: String? = null,
  @SerializedName("parent") val parent: Int? = null,
  @SerializedName("sortOrder") val sortOrder: Int? = null,
  @SerializedName("createdAt") val createdAt: String? = null,
  @SerializedName("updatedAt") val updatedAt: String? = null
)
