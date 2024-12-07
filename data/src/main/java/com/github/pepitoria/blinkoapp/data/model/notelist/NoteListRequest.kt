package com.github.pepitoria.blinkoapp.data.model.notelist

import com.google.gson.annotations.SerializedName

data class NoteListRequest(
  @SerializedName("tagId") val tagId: Int? = null,
  @SerializedName("page") val page: Int = 1,
  @SerializedName("size") val size: Int = 30,
  @SerializedName("orderBy") val orderBy: String = "desc",
  @SerializedName("type") val type: Int = -1,
  @SerializedName("isArchived") val isArchived: Boolean = false,
  @SerializedName("isRecycle") val isRecycle: Boolean = false,
  @SerializedName("searchText") val searchText: String = "",
  @SerializedName("withoutTag") val withoutTag: Boolean = false,
  @SerializedName("withFile") val withFile: Boolean = false,
  @SerializedName("withLink") val withLink: Boolean = false,
  @SerializedName("isUseAiQuery") val isUseAiQuery: Boolean = false
)