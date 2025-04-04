package com.github.pepitoria.blinkoapp.ui.login

import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun TokenLoginWidget(
  viewModel: TokenLoginScreenViewModel = hiltViewModel(),
  goToDebug: () -> Unit,
  goToHome: () -> Unit,
) {
  val events = viewModel.events
  ListenForEvents(events, goToHome)

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
        },
        goToDebug = goToDebug,
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
private fun ListenForEvents(
  events: SharedFlow<NavigationEvents>,
  goToHome: () -> Unit,
) {
  LaunchedEffect(Unit) {
    events.collect { event ->
      when (event) {
        is NavigationEvents.GoToNoteList -> {
          goToHome()
        }
      }
    }
  }
}

@Composable
@Preview
private fun SessionActive(
  logout: () -> Unit = {},
  goToDebug: () -> Unit = {}
) {

  Column(
    modifier = Modifier.fillMaxSize().background(getBackgroundBrush()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = stringResource(id = R.string.login_token_session_active),
      modifier = Modifier,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
    )
    Button(
      onClick = logout,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Text(
        text = stringResource(id = R.string.login_token_logout),
        fontSize = 16.sp
      )
    }

//    GoToDebugButton(goToDebug = goToDebug)
  }
}

@Composable
private fun GoToDebugButton(
  goToDebug: () -> Unit = {}
) {
  Button(
    onClick = goToDebug,
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp)
  ) {
    Text(
      text = "Goto Debug",
      fontSize = 16.sp
    )
  }
}

@Preview
@Composable
fun TokenLoginScreenViewState(
  onLoginClicked: (String, String) -> Unit = { _, _ -> },
  url: String = "",
  token: String = "",
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(getBackgroundBrush())
      .padding(32.dp),
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
        text = stringResource(id = R.string.login_token_login_title),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(12.dp))

      BlinkoUrlField(
        url = url,
        label = stringResource(id = R.string.login_blinko_url),
        onUrlChange = { url = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(first)
      )
      Spacer(modifier = Modifier.height(12.dp))

      TokenField(
        username = token,
        label = stringResource(id = R.string.login_token_label),
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
      text = stringResource(id = R.string.login_token_login_button),
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
