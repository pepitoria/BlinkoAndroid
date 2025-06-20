package com.github.pepitoria.blinkoapp.data.model.notedelete

import com.google.gson.annotations.SerializedName

data class DeleteNoteResponse(
  @SerializedName("ok") var ok: Boolean?,
)