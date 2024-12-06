package com.github.pepitoria.blinkoapp.ui.login

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginWidget(
  viewModel: LoginScreenViewModel = hiltViewModel()
) {
  Greeting(name = "homie")
}

@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier
  )
}