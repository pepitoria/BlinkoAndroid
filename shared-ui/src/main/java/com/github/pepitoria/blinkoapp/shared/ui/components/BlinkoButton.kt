package com.github.pepitoria.blinkoapp.shared.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.pepitoria.blinkoapp.shared.theme.White

@Composable
@Preview(
  showBackground = true,
  name = "Dark Theme",
  uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
  showBackground = true,
  name = "Light Theme",
  uiMode = Configuration.UI_MODE_NIGHT_NO,
)
private fun BlinkoButtonPreview() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(color = White)
      .padding(8.dp),

  ) {
    BlinkoButton(
      text = "Hola",
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
    border = BorderStroke(
      width = 0.5.dp,
      color = MaterialTheme.colorScheme.secondary,
    ),
    colors = ButtonDefaults.buttonColors(
      containerColor = MaterialTheme.colorScheme.background,
    ),
    modifier = Modifier
      .then(modifier),

  ) {
    Text(
      text = text,
      fontSize = 16.sp,
    )
  }
}
