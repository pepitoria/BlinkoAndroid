package com.github.pepitoria.blinkoapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
private fun BlinkoButtonPreview() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(color = White)
      .padding(8.dp)

  ) {

    BlinkoButton(
      text = "Hola"
    )
  }

}

@Composable
fun BlinkoButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {},
  text: String,
) {
  Button(
    onClick = onClick,
    modifier = Modifier
      .shadow(
        elevation = 2.dp,
        shape = ButtonDefaults.shape
      )
      .then(modifier)

  ) {
    Text(
      text = text,
      fontSize = 16.sp
    )
  }
}