package com.github.pepitoria.blinkoapp.offline.sync

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SyncPayload(
  @SerializedName("id") val serverId: Int? = null,
  @SerializedName("content") val content: String,
  @SerializedName("type") val type: Int,
  @SerializedName("isArchived") val isArchived: Boolean,
) {
  fun toJson(): String = Gson().toJson(this)

  companion object {
    fun fromJson(json: String): SyncPayload = Gson().fromJson(json, SyncPayload::class.java)
  }
}
