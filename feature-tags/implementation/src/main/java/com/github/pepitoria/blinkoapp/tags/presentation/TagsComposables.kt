package com.github.pepitoria.blinkoapp.tags.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.shared.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.shared.ui.loading.Loading
import com.github.pepitoria.blinkoapp.shared.ui.components.BlinkoButton

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
  BlinkoButton(
    text = tag,
    onClick = { onTagClick(tag) },
  )
}

@Preview(showBackground = true)
@Composable
private fun TagListPreview() {
  Column {
    Tag("Pending") { }
    Tag("Pending") { }
    Tag("Pending") { }
  }
}