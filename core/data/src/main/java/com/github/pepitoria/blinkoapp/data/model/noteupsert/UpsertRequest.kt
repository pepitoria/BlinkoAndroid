package com.github.pepitoria.blinkoapp.data.model.noteupsert

import com.google.gson.annotations.SerializedName

data class UpsertRequest(
  @SerializedName("id") var id: Int? = null,
  @SerializedName("content") var content: String,
  @SerializedName("type") var type: Int,
//  @SerializedName("attachments") var attachments: ArrayList<String> = arrayListOf(),
  @SerializedName("isArchived") var isArchived: Boolean? = false,
//  @SerializedName("isTop") var isTop: Boolean? = true,
//  @SerializedName("isShare") var isShare: String? = null,
//  @SerializedName("isRecycle") var isRecycle: String? = null,
//  @SerializedName("references") var references: ArrayList<Int> = arrayListOf()

)