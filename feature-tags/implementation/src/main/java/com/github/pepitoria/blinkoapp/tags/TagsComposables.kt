package com.github.pepitoria.blinkoapp.tags

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

@Composable
internal fun TagListComposableInternal() {
  Text(
    modifier = Modifier
      .fillMaxWidth(),
    text = "here will be a list of #tags",
    textAlign = TextAlign.Center,
    color = Color.White,
  )
}