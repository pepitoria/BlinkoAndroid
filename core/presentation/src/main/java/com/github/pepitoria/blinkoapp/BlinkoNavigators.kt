package com.github.pepitoria.blinkoapp

import android.app.Activity
import android.content.Intent
import androidx.navigation.NavHostController
import com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit.ShareAndEditWithBlinkoActivity

fun NavHostController.goBack(): () -> Unit = {
  this.popBackStack()
}

fun NavHostController.exit(): () -> Unit = {
  (this.context as? Activity)?.finishAffinity()
}

fun NavHostController.goToEditWithBlinko(): () -> Unit = {
  val intent = Intent(context, ShareAndEditWithBlinkoActivity::class.java)
  intent.action = Intent.ACTION_SEND
  intent.type = "text/plain"
  intent.putExtra(Intent.EXTRA_TEXT, "")
  context.startActivity(intent)
}

fun NavHostController.goToDebug(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavDebug.Debug.route)
}

fun NavHostController.goToHome(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.route) {
    launchSingleTop = true
    popUpTo(0)
  }
}

fun NavHostController.goToNoteList(): () -> Unit = {
  this.navigate(route = BlinkoNavigationRouter.NavHome.NoteList.route){
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