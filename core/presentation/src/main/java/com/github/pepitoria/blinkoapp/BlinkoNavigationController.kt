package com.github.pepitoria.blinkoapp

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.github.pepitoria.blinkoapp.auth.api.AuthFactory
import com.github.pepitoria.blinkoapp.domain.model.note.BlinkoNoteType
import com.github.pepitoria.blinkoapp.search.api.SearchFactory
import com.github.pepitoria.blinkoapp.settings.api.domain.SettingsEntryPoint
import com.github.pepitoria.blinkoapp.settings.api.domain.Tab
import com.github.pepitoria.blinkoapp.ui.note.edit.NoteEditScreenComposable
import com.github.pepitoria.blinkoapp.ui.note.list.NoteListScreenComposable
import com.github.pepitoria.blinkoapp.ui.settings.SettingsScreenComposable
import dagger.hilt.android.EntryPointAccessors

@Composable
fun BlinkoNavigationController(
  navController: NavHostController,
  searchFactory: SearchFactory,
  authFactory: AuthFactory,
) {

  val defaultTabRoute = getDefaultTabRoute()

  NavHost(
    navController = navController,
    startDestination = BlinkoNavigationRouter.NavAuth.route,
    modifier = Modifier
      .fillMaxSize(),
  ) {
    ///// AUTHENTICATION
    navigation(
      startDestination = BlinkoNavigationRouter.NavAuth.Login.route,
      route = BlinkoNavigationRouter.NavAuth.route,
    ) {
      composable(BlinkoNavigationRouter.NavAuth.Login.route) {
        LoginNavigator(navController = navController, authFactory = authFactory)
      }
    }

    ///// Home
    navigation(
      startDestination = defaultTabRoute,
      route = BlinkoNavigationRouter.NavHome.route,
    ) {
      composable(route = BlinkoNavigationRouter.NavHome.NoteList.route) {
        HomeNoteListNavigatorNotes(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.NoteList.route,
        )
      }
      composable(route = BlinkoNavigationRouter.NavHome.BlinkoList.route) {
        HomeNoteListNavigatorBlinkos(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.BlinkoList.route,
        )
      }
      composable(route = BlinkoNavigationRouter.NavHome.TodoList.route) {
        HomeNoteListNavigatorTodos(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.TodoList.route,
        )
      }
      composable(route = BlinkoNavigationRouter.NavHome.Search.route) {
        HomeSearchNavigator(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.Search.route,
          searchFactory = searchFactory,
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
      composable(route = BlinkoNavigationRouter.NavHome.Settings.route) {
        SettingsNavigatorNotes(
          navController = navController,
          route = BlinkoNavigationRouter.NavHome.Settings.route,
        )
      }
    }
  }
}

@Composable
private fun getDefaultTabRoute(): String {
  val activity = LocalActivity.current as Activity

  val getDefaultTabUseCase = EntryPointAccessors.fromActivity(
    activity = activity,
    entryPoint = SettingsEntryPoint::class.java
  ).getDefaultTabUseCase()

  val defaultTab = getDefaultTabUseCase.getDefaultTab()

  val homeDefaultRoute = when (defaultTab) {
    Tab.BLINKOS -> BlinkoNavigationRouter.NavHome.BlinkoList.route
    Tab.NOTES -> BlinkoNavigationRouter.NavHome.NoteList.route
    Tab.TODOS -> BlinkoNavigationRouter.NavHome.TodoList.route
    Tab.SEARCH -> BlinkoNavigationRouter.NavHome.Search.route
    else -> BlinkoNavigationRouter.NavHome.NoteList.route
  }

  return homeDefaultRoute
}

@Composable
fun LoginNavigator(
  navController: NavHostController,
  authFactory: AuthFactory,
) {
  authFactory.LoginScreenComposable(
    goToHome = navController.goToHome(),
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
    goToTodoList = navController.goToTodoList(),
    goToSettings = navController.goToSettings(),
    goToNewNote = navController.goToEditWithBlinko(BlinkoNoteType.BLINKO.value),
    goToSearch = navController.goToSearch(),
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
    goToTodoList = navController.goToTodoList(),
    goToSettings = navController.goToSettings(),
    goToNewNote = navController.goToEditWithBlinko(BlinkoNoteType.NOTE.value),
    goToSearch = navController.goToSearch(),
  )
}

@Composable
fun HomeNoteListNavigatorTodos(
  navController: NavHostController,
  route: String,
) {
  NoteListScreenComposable(
    noteOnClick = navController.goToNoteEdit(),
    noteType = BlinkoNoteType.TODO,
    currentRoute = route,
    goToNotes = navController.goToNoteList(),
    goToBlinkos = navController.goToBlinkoList(),
    goToTodoList = navController.goToTodoList(),
    goToSettings = navController.goToSettings(),
    goToNewNote = navController.goToEditWithBlinko(BlinkoNoteType.TODO.value),
    goToSearch = navController.goToSearch(),
  )
}

@Composable
fun HomeSearchNavigator(
  navController: NavHostController,
  route: String,
  searchFactory: SearchFactory,
) {
  searchFactory.SearchScreenComposable(
    noteOnClick = navController.goToNoteEdit(),
    currentRoute = route,
    goToNotes = navController.goToNoteList(),
    goToBlinkos = navController.goToBlinkoList(),
    goToTodoList = navController.goToTodoList(),
    goToSearch = navController.goToSearch(),
    goToSettings = navController.goToSettings(),
  )
}

@Composable
fun SettingsNavigatorNotes(
  navController: NavHostController,
  route: String,
) {
  SettingsScreenComposable(
    currentRoute = route,
    goToNotes = navController.goToNoteList(),
    goToBlinkos = navController.goToBlinkoList(),
    goToTodoList = navController.goToTodoList(),
    goToSettings = navController.goToSettings(),
    goToSearch = navController.goToSearch(),
    exit = navController.exit(),
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