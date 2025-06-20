package com.github.pepitoria.blinkoapp.data.model.notedelete

import com.google.gson.annotations.SerializedName

data class DeleteNoteRequest(
  @SerializedName("ids") var ids: List<Int>,
)