package com.github.pepitoria.blinkoapp.data.model.noteupsert

import com.google.gson.annotations.SerializedName

data class Note(

  @SerializedName("id") var id: Int? = null,
  @SerializedName("type") var type: Int? = null,
  @SerializedName("content") var content: String? = null,
  @SerializedName("isArchived") var isArchived: Boolean? = null,
  @SerializedName("isRecycle") var isRecycle: Boolean? = null,
  @SerializedName("isShare") var isShare: Boolean? = null,
  @SerializedName("isTop") var isTop: Boolean? = null,
  @SerializedName("isReviewed") var isReviewed: Boolean? = null,
  @SerializedName("sharePassword") var sharePassword: String? = null,
  @SerializedName("metadata") var metadata: String? = null,
  @SerializedName("accountId") var accountId: Int? = null,
  @SerializedName("createdAt") var createdAt: String? = null,
  @SerializedName("updatedAt") var updatedAt: String? = null

)