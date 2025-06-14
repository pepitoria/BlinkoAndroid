package com.github.pepitoria.blinkoapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
  }
}