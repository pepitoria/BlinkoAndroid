package com.github.pepitoria.blinkoapp.ui.debug

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.pepitoria.blinkoapp.BuildConfig
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme

@Composable
fun DebugScreenComposable(
  goToEditWithBlinko: () -> Unit = {},
) {
  BlinkoAppTheme {
    GoToEditWithBlinko(goToEditWithBlinko = goToEditWithBlinko)
  }
}

@Composable
private fun GoToEditWithBlinko(
  goToEditWithBlinko: () -> Unit = {}
) {
  if (!BuildConfig.DEBUG) {
    return
  }

  Button(
    onClick = goToEditWithBlinko,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Text(
      text = "Goto Edit with Blinko",
      fontSize = 16.sp
    )
  }
}
