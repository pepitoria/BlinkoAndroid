package com.github.pepitoria.blinkoapp.shared.navigation

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import androidx.navigation.NavHostController

fun NavHostController.goBack(): () -> Unit = {
  this.popBackStack()
}

fun NavHostController.exit(): () -> Unit = {
  (this.context as? Activity)?.finishAffinity()
}

fun NavHostController.goToEditWithBlinko(noteType: Int): () -> Unit = {
  val intent = Intent(Intent.ACTION_SEND).apply {
    component = ComponentName(
      context,
      "com.github.pepitoria.blinkoapp.notes.implementation.presentation.ShareAndEditWithBlinkoActivity",
    )
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, "")
    putExtra("ShareAndEditWithBlinkoActivity.NOTE_TYPE", noteType)
  }
  context.startActivity(intent)
}

fun NavHostController.goToHome(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToNoteList(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.NoteList.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToBlinkoList(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.BlinkoList.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToTodoList(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.TodoList.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToSearch(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.Search.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToSettings(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.Settings.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToNoteEdit(): (id: Int) -> Unit = { id ->
  this.navigate(route = BlinkoNavigationRouter.NavHome.NoteEdit.createRoute(noteId = id))
}
