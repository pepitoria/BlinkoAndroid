package com.github.pepitoria.blinkoapp.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents

@Composable
internal fun TagListComposableInternal(
  viewModel: TagsListViewModel = hiltViewModel(),
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val tags = viewModel.tags.collectAsState()

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 96.dp),
  ) {
    items(tags.value) { tag ->
      Tag(tag = tag)
    }
  }
}

@Composable
private fun Tag(
  tag: String,
) {
  Text(
    modifier = Modifier
      .padding(all = 4.dp)
      .background(
        color = Color.Gray,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(all = 4.dp),
    maxLines = 1,
    text = tag,
    textAlign = TextAlign.Center,
    color = Color.White,
  )
}