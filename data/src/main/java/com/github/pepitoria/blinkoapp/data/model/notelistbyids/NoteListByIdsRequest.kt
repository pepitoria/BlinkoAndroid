package com.github.pepitoria.blinkoapp.data.model.notelistbyids

import com.google.gson.annotations.SerializedName

data class NoteListByIdsRequest(
  @SerializedName("ids") val ids: List<Int> = emptyList(),
)