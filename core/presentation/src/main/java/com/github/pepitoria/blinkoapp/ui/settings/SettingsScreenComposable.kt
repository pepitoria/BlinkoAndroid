package com.github.pepitoria.blinkoapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.tabbar.TabBar
import com.github.pepitoria.blinkoapp.ui.theme.Black
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun SettingsScreenComposable(
  viewModel: SettingsScreenViewModel = hiltViewModel(),
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  goToTodoList: () -> Unit,
  goToSearch: () -> Unit,
  goToSettings: () -> Unit,
  exit: () -> Unit,
  ) {
  ComposableLifecycleEvents(viewModel = viewModel)
  val events = viewModel.events
  ListenForEvents(events, exit)

  BlinkoAppTheme {
    TabBar(
      currentRoute = currentRoute,
      goToNotes = goToNotes,
      goToBlinkos = goToBlinkos,
      goToTodoList = goToTodoList,
      goToSearch = goToSearch,
      goToSettings = goToSettings,
    ) { paddingValues ->
      SessionActive(
        logout = {
          viewModel.logout()
        },
        listOfTabs = viewModel.getTabsOrder()?.toList() ?: listOf("Blinkos", "Notes", "Todos", "Search"),
        saveTabsOrder = { tabs ->
          viewModel.saveTabsOrder(tabs)
        }
      )
    }
  }
}

@Composable
private fun ListenForEvents(
  events: SharedFlow<SettingsScreenViewModel.Events>,
  exit: () -> Unit,
) {
  LaunchedEffect(Unit) {
    events.collect { event ->
      when (event) {
        is SettingsScreenViewModel.Events.Exit -> {
          exit()
        }
      }
    }
  }
}

@Composable
fun SessionActive(
  logout: () -> Unit = {},
  listOfTabs: List<String>,
  saveTabsOrder: (List<String>) -> Unit,
) {

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(getBackgroundBrush()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(id = R.string.login_token_session_active),
      modifier = Modifier,
      color = Black
    )
    Button(
      onClick = logout,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = stringResource(id = R.string.login_token_logout),
        fontSize = 16.sp
      )
    }

    ReorderableSettingsScreen(
      listOfTabs = listOfTabs,
      saveTabsOrder = saveTabsOrder
    )
  }
}

@Composable
fun ReorderableSettingsScreen(
  listOfTabs: List<String>,
  saveTabsOrder: (List<String>) -> Unit,
) {

  var list1 by remember { mutableStateOf(listOfTabs) }
  val draggableItems by remember {
    derivedStateOf { list1.size }
  }
  val stateList = rememberLazyListState()

  val dragDropState =
    rememberDragDropState(
      lazyListState = stateList,
      draggableItemsNum = draggableItems,
      onMove = { fromIndex, toIndex ->
        list1 = list1.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
        saveTabsOrder(list1)
      })

  LazyColumn(
    modifier = Modifier.dragContainer(dragDropState),
    state = stateList,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    draggableItems(items = list1, dragDropState = dragDropState) { modifier, item ->
      Item(
        modifier = modifier,
        text = item,
      )
    }
  }
}


@Composable
private fun Item(modifier: Modifier = Modifier, text: String) {
  Card(
    modifier = modifier
  ) {
    Text(
      text = text,
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    )
  }
}