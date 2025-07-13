package com.github.pepitoria.blinkoapp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
        onTabSelected = { selectedTab ->
          viewModel.onTabToShowFirstSelected(tab = selectedTab)
        },
        getDefaultTab = {
          viewModel.getDefaultTab()
        }

      )
    }
  }
}

@Composable
@Preview
private fun SettingsScreenComposablePreview() {
  BlinkoAppTheme {
    SessionActive()
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
  onTabSelected: (String) -> Unit = { _ -> },
  getDefaultTab: () -> String = { "TODOS" }
) {

  Column(
    modifier = Modifier
      .background(getBackgroundBrush())
      .padding(16.dp)
      .fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(id = R.string.login_token_session_active),
      modifier = Modifier,
      color = Black
    )

    PreselectedTabWidget(
      onTabSelected = onTabSelected,
      getDefaultTab = getDefaultTab,
    )

    Button(
      onClick = logout,
      modifier = Modifier
        .fillMaxWidth()

    ) {
      Text(
        text = stringResource(id = R.string.login_token_logout),
        fontSize = 16.sp
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreselectedTabWidget(
  onTabSelected: (String) -> Unit,
  getDefaultTab: () -> String,
) {
  var expanded by remember { mutableStateOf(false) }
  val options = listOf(
    stringResource(R.string.tab_bar_blinkos),
    stringResource(R.string.tab_bar_notes),
    stringResource(R.string.tab_bar_todos),
    stringResource(R.string.tab_bar_search)
  )

  var selectedOption by remember { mutableStateOf(getDefaultTab()) }

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
  ) {
    TextField(
      value = selectedOption,
      onValueChange = {},
      readOnly = true,
      modifier = Modifier
        .menuAnchor()
        .fillMaxWidth(),
      label = { Text(stringResource(R.string.settings_preselected_tab)) },
      trailingIcon = {
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
      },
      colors = ExposedDropdownMenuDefaults.textFieldColors()
    )
    ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            selectedOption = option
            expanded = false
            onTabSelected(option)
          }
        )
      }
    }
  }
}