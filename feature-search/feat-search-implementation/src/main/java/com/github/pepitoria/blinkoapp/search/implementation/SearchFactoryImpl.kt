package com.github.pepitoria.blinkoapp.search.implementation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import javax.inject.Inject

class SearchFactoryImpl @Inject constructor(
) : SearchFactory {

  @Composable
  override fun SearchComposable() {
    Text(
      text = "Search widget",
      fontSize = 40.sp,
      color = Color.Red
    )
  }
}