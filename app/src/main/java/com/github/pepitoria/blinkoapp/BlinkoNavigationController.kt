package com.github.pepitoria.blinkoapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.ui.debug.DebugScreenComposable
import com.github.pepitoria.blinkoapp.ui.login.TokenLoginWidget
import com.github.pepitoria.blinkoapp.ui.note.edit.NoteEditScreenComposable
import com.github.pepitoria.blinkoapp.ui.note.list.NoteListScreenComposable

@Composable
fun BlinkoNavigationController(
  navController: NavHostController,
) {
  NavHost(
    navController = navController,
    startDestination = BlinkoNavigationRouter.NavAuth.route,
    modifier = Modifier
      .fillMaxSize(),
  ) {
    ///// Debug
    navigation(
      startDestination = BlinkoNavigationRouter.NavDebug.Debug.route,
      route = BlinkoNavigationRouter.NavDebug.route,
    ) {
      composable(BlinkoNavigationRouter.NavDebug.Debug.route) {
        DebugNavigator(navController = navController)
      }
    }

    ///// AUTHENTICATION
    navigation(
      startDestination = BlinkoNavigationRouter.NavAuth.Login.route,
      route = BlinkoNavigationRouter.NavAuth.route,
    ) {
      composable(BlinkoNavigationRouter.NavAuth.Login.route) {
        LoginNavigator(navController = navController)
      }
    }

    ///// Home
    navigation(
      startDestination = BlinkoNavigationRouter.NavHome.NoteList.route,
      route = BlinkoNavigationRouter.NavHome.route,
    ) {
      composable(BlinkoNavigationRouter.NavHome.NoteList.route) {
        HomeNoteListNavigatorNotes(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.NoteList.route,
        )
      }
      composable(BlinkoNavigationRouter.NavHome.BlinkoList.route) {
        HomeNoteListNavigatorBlinkos(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.BlinkoList.route,
        )
      }
      composable(
        route = BlinkoNavigationRouter.NavHome.NoteEdit.route,
        arguments = BlinkoNavigationRouter.NavHome.NoteEdit.arguments,
      ) {
        HomeNoteEditNavigator(
          navController = navController,
          id = it.arguments?.getInt(BlinkoNavigationRouter.NavHome.ARG_NOTE_ID) ?: 0)
      }
    }
  }
}


@Composable
fun LoginNavigator(
  navController: NavHostController,
) {
  TokenLoginWidget(
    goToDebug = navController.goToDebug(),
    goToNoteList = navController.goToNoteList(),
  )
}

@Composable
fun DebugNavigator(
  navController: NavHostController,
) {
  DebugScreenComposable(
    goToEditWithBlinko = navController.goToEditWithBlinko(),
  )
}

@Composable
fun HomeNoteListNavigatorBlinkos(
  navController: NavHostController,
  route: String,
) {
  NoteListScreenComposable(
    noteOnClick = navController.goToNoteEdit(),
    noteType = BlinkoNoteType.BLINKO,
    currentRoute = route,
    goToNotes = navController.goToNoteList(),
    goToBlinkos = navController.goToBlinkoList(),
  )
}

@Composable
fun HomeNoteListNavigatorNotes(
  navController: NavHostController,
  route: String,
) {
  NoteListScreenComposable(
    noteOnClick = navController.goToNoteEdit(),
    noteType = BlinkoNoteType.NOTE,
    currentRoute = route,
    goToNotes = navController.goToNoteList(),
    goToBlinkos = navController.goToBlinkoList(),
  )
}

@Composable
fun HomeNoteEditNavigator(
  navController: NavHostController,
  id: Int,
) {
  NoteEditScreenComposable(
    noteId = id,
    goBack = navController.goBack(),
  )
}