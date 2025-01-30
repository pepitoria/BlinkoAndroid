package com.github.pepitoria.blinkoapp.ui.tabbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
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
import com.github.pepitoria.blinkoapp.R

@Composable
fun TabBar(
  currentRoute: String,
  goToNotes: () -> Unit,
  goToBlinkos: () -> Unit,
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    bottomBar = {
      NavigationBar {
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
          icon = { Icon(ImageVector.vectorResource(id = R.drawable.blinko), contentDescription = stringResource(R.string.tab_bar_blinkos)) },
          label = { Text(text = stringResource(R.string.tab_bar_blinkos)) },
          selected = currentRoute == BlinkoNavigationRouter.NavHome.BlinkoList.route,
          onClick = {
            if (currentRoute != BlinkoNavigationRouter.NavHome.BlinkoList.route) {
              goToBlinkos()
            }
          }
        )
      }
    }
  ) { innerPadding ->
    content(innerPadding)
  }
}