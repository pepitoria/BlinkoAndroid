package com.github.pepitoria.blinkoapp.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.theme.Black
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme

@Composable
fun TokenLoginWidget(
  viewModel: TokenLoginScreenViewModel = hiltViewModel()
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val isSessionActive = viewModel.isSessionActive.collectAsState()
  val isLoading = viewModel.isLoading.collectAsState()

  BlinkoAppTheme {
    if (isLoading.value) {
      Loading()
    } else if (isSessionActive.value) {
      SessionActive(
        logout = {
          viewModel.logout()
        }
      )
    } else {
      TokenLoginScreenViewState(
        url = viewModel.getStoredUrl() ?: "",
        token = viewModel.getStoredToken() ?: "",
        onLoginClicked = { url, token ->
          viewModel.checkSession(
            url = url,
            token = token
          )
        }
      )
    }
  }
}

@Composable
@Preview
private fun Loading() {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    CircularProgressIndicator(
      modifier = Modifier.width(64.dp),
      color = MaterialTheme.colorScheme.secondary,
      trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
  }
}

@Composable
@Preview
private fun SessionActive(
  logout: () -> Unit = {},
) {

  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Session active",
      modifier = Modifier,
      color = Black
    )
    Button(
      onClick = logout,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = "Logout",
        fontSize = 16.sp
      )
    }
  }
}

@Preview
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TokenLoginScreenViewState(
  onLoginClicked: (String, String) -> Unit = { _, _ -> },
  url: String = "",
  token: String = "",
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
        mutableStateOf(url)
      }

      var token by rememberSaveable {
        mutableStateOf(token)
      }

      val (first, second) = remember { FocusRequester.createRefs() }
      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = "Login with token",
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

      TokenField(
        username = token,
        label = "Token",
        onUsernameChange = { token = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(second)
      )
      Spacer(modifier = Modifier.height(12.dp))

      TokenLoginButton(
        onClick = {
          onLoginClicked(url, token)
        },
      )
      Spacer(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
fun TokenLoginButton(
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
fun TokenField(
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
