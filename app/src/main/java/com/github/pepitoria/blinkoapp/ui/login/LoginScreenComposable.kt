package com.github.pepitoria.blinkoapp.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.Transparent

@Composable
fun LoginWidget(
  viewModel: LoginScreenViewModel = hiltViewModel()
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  BlinkoAppTheme {
    LoginScreenViewState(
      onLoginClicked = { url, username, password ->
        viewModel.login(
          url = url,
          username = username,
          password = password,
        )
      }
    )
  }

}

@Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreenViewState(
  onLoginClicked: (String, String, String) -> Unit = { _, _, _ -> },
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      var url by rememberSaveable {
        mutableStateOf("")
      }
      var username by rememberSaveable {
        mutableStateOf("")
      }
      var password by rememberSaveable {
        mutableStateOf("")
      }

      val (first, second, third) = remember { FocusRequester.createRefs() }
      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = "Login",
      )

      Spacer(modifier = Modifier.height(12.dp))

      BlinkoUrlField(
        url = url,
        label = "Blinko url",
        onUrlChange = { url = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(first)
      )

      Spacer(modifier = Modifier.height(12.dp))

      UsernameField(
        username = username,
        label = "Username",
        onUsernameChange = { username = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(second)
      )
      Spacer(modifier = Modifier.height(12.dp))
      PasswordField(
        password = password,
        placeholder = "Password",
        onPasswordChange = { password = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(third)
      )
      Spacer(modifier = Modifier.height(12.dp))

      LoginButton(
        onClick = {
          onLoginClicked(url, username, password)
        },
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
fun LoginButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Button(
    onClick = onClick,
    modifier = modifier,
  ) {
    Text(
      text = "Login",
      fontSize = 16.sp
    )
  }
}

@Composable
fun BlinkoUrlField(
  url: String,
  label: String,
  onUrlChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  TextField(
    label = {
      Text(
        text = label,
        fontWeight = FontWeight.Normal
      )
    },
    value = url,
    singleLine = true,
    onValueChange = onUrlChange,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Uri,
      imeAction = ImeAction.Next
    ),
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .then(modifier),
  )
}

@Composable
fun UsernameField(
  username: String,
  label: String,
  onUsernameChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  TextField(
    label = {
      Text(
        text = label,
        fontWeight = FontWeight.Normal
      )
    },
    value = username,
    singleLine = true,
    onValueChange = onUsernameChange,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Next
    ),
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .then(modifier),
  )
}

@Composable
fun PasswordField(
  password: String,
  placeholder: String,
  onPasswordChange: (String) -> Unit,
  isError: Boolean = false,
  modifier: Modifier = Modifier
) {
  var passwordVisible by rememberSaveable { mutableStateOf(false) }

  TextField(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .then(modifier),
    value = password,
    onValueChange = onPasswordChange,
    label = {
      Text(
        text = placeholder,
        fontWeight = FontWeight.Normal
      )
    },
    singleLine = true,
    placeholder = {
      Text(
        placeholder,
        fontWeight = FontWeight.Normal
      )
    },
    isError = isError,
    visualTransformation = getVisualTransformation(passwordVisible),
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Password,
      imeAction = ImeAction.Next
    ),
    colors = TextFieldDefaults.colors(
      focusedIndicatorColor = Transparent,
      unfocusedIndicatorColor = Transparent,
      disabledIndicatorColor = Transparent,
    ),
  )
}

internal fun getVisualTransformation(passwordVisible: Boolean): VisualTransformation {
  return if (passwordVisible) {
    VisualTransformation.None
  } else {
    PasswordVisualTransformation()
  }
}