package com.github.pepitoria.blinkoapp.ui.tabbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.github.pepitoria.blinkoapp.BlinkoNavigationRouter
import com.github.pepitoria.blinkoapp.presentation.R

@Composable
fun TabBar(
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  goToTodoList: () -> Unit,
  goToSearch: () -> Unit,
  goToSettings: () -> Unit,
  floatingActionButton: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    floatingActionButton = floatingActionButton,
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          icon = { Icon(ImageVector.vectorResource(id = R.drawable.blinko), contentDescription = stringResource(R.string.tab_bar_blinkos)) },
          label = { Text(text = stringResource(R.string.tab_bar_blinkos)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.BlinkoList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.BlinkoList.route) {
              goToBlinkos()
            }
          }
        )

        NavigationBarItem(
          icon = { Icon(ImageVector.vectorResource(id = R.drawable.note), contentDescription = stringResource(R.string.tab_bar_notes)) },
          label = { Text(text = stringResource(R.string.tab_bar_notes)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.NoteList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.NoteList.route) {
              goToNotes()
            }
          }
        )

        NavigationBarItem(
          icon = { Icon(ImageVector.vectorResource(id = R.drawable.todo), contentDescription = stringResource(R.string.tab_bar_todos)) },
          label = { Text(text = stringResource(R.string.tab_bar_todos)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.TodoList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.TodoList.route) {
              goToTodoList()
            }
          }
        )

        NavigationBarItem(
          icon = { Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.tab_bar_search)) },
          label = { Text(text = stringResource(R.string.tab_bar_search)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.Search.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.Search.route) {
              goToSearch()
            }
          }
        )

        NavigationBarItem(
          icon = { Icon(ImageVector.vectorResource(id = R.drawable.settings), contentDescription = stringResource(R.string.tab_bar_settings)) },
          label = { Text(text = stringResource(R.string.tab_bar_settings)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.Settings.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.Settings.route) {
              goToSettings()
            }
          }
        )
      }
    }
  ) { innerPadding ->
    content(innerPadding)
  }
}