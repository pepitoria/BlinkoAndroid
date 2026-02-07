package com.github.pepitoria.blinkoapp.shared.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BlinkoTextField(
  text: String,
  label: String,
  onTextChanged: (String) -> Unit,
  minLines: Int = 1,
  singleLine: Boolean = true,
  modifier: Modifier = Modifier,
  keyboardType: KeyboardType = KeyboardType.Unspecified,
  imeAction: ImeAction = ImeAction.Unspecified,
) {
  TextField(
    label = {
      Text(
        text = label,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.secondary,
      )
    },
    minLines = minLines,
    value = text,
    singleLine = singleLine,
    onValueChange = onTextChanged,
    keyboardOptions = KeyboardOptions(
      keyboardType = keyboardType,
      imeAction = imeAction,
    ),
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .then(modifier),
    colors = TextFieldDefaults.colors(
      focusedLabelColor = MaterialTheme.colorScheme.secondary,
      focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
      cursorColor = MaterialTheme.colorScheme.secondary,
    ),
  )
}
