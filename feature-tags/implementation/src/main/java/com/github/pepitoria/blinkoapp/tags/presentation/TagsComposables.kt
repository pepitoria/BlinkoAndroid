package com.github.pepitoria.blinkoapp.tags.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading

@Composable
internal fun TagListComposableInternal(
  viewModel: TagsListViewModel = hiltViewModel(),
  onTagClick: (String) -> Unit,
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isLoading = viewModel.isLoading.collectAsState()
  val tags = viewModel.tags.collectAsState()

  if (isLoading.value) {
    Loading()
  } else {
    FlowRow(
      modifier = Modifier.padding(8.dp),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      tags.value.forEach { tag ->
        Tag(
          tag = tag,
          onTagClick = onTagClick,
        )
      }
    }
  }
}

@Composable
private fun Tag(
  tag: String,
  onTagClick: (String) -> Unit,
) {
  Text(
    modifier = Modifier
      .padding(all = 4.dp)
      .background(
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
      )
      .padding(horizontal = 16.dp, vertical = 4.dp)
      .clickable { onTagClick(tag) },
    maxLines = 1,
    text = tag,
    textAlign = TextAlign.Center,
    color = Color.Black,
  )
}

@Preview
@Composable
private fun TagListPreview() {
  Column {
    Tag("Pending") { }
    Tag("Pending") { }
    Tag("Pending") { }
  }
}