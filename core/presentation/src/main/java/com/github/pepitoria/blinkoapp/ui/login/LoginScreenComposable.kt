package com.github.pepitoria.blinkoapp.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.presentation.R
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.loading.Loading
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoButton
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoPasswordField
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoTextField
import com.github.pepitoria.blinkoapp.ui.theme.getBackgroundBrush
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LoginWidget(
  viewModel: LoginScreenViewModel = hiltViewModel(),
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
      )
    } else {
      LoginScreenViewState(
        urlParam = viewModel.getStoredUrl() ?: "",
        userNameParam = viewModel.getStoredUserName() ?: "",
        passwordParam = viewModel.getStoredToken() ?: "",
        onLoginClicked = { url, userName, password, insecureConnectionCheck ->
          viewModel.doLogin(
            url = url,
            userName = userName,
            password = password,
            insecureConnectionCheck = insecureConnectionCheck
          )
        }
      )
    }
  }
}

@Composable
private fun ListenForEvents(
  events: SharedFlow<LoginScreenViewModel.Events>,
  goToHome: () -> Unit,
) {

  val context = LocalContext.current

  LaunchedEffect(Unit) {
    events.collect { event ->
      when (event) {
        is LoginScreenViewModel.Events.SessionOk -> {
          goToHome()
        }
        is LoginScreenViewModel.Events.InsecureConnection -> {
          Toast.makeText(context, R.string.login_token_insecure_toast, Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}

@Composable
@Preview
private fun SessionActive(
  logout: () -> Unit = {},
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

    BlinkoButton(
      onClick = logout,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      text = stringResource(id = R.string.login_token_logout),
    )
  }
}



@Preview
@Composable
fun LoginScreenViewState(
  onLoginClicked: (String, String, String, Boolean) -> Unit = { _, _, _, _ -> },
  urlParam: String = "",
  userNameParam: String = "",
  passwordParam: String = "",
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
        mutableStateOf(urlParam)
      }

      var username by rememberSaveable {
        mutableStateOf(userNameParam)
      }

      var password by rememberSaveable {
        mutableStateOf(passwordParam)
      }
      var insecureUrlCheckedState by remember { mutableStateOf(false) }

      val (first, second, third) = remember { FocusRequester.createRefs() }
      Spacer(modifier = Modifier.weight(1f))

      Text(
        text = stringResource(id = R.string.login_token_login_title_password),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(12.dp))

      BlinkoTextField(
        url = url,
        label = stringResource(id = R.string.login_blinko_url),
        onUrlChange = { url = it },
        keyboardType = KeyboardType.Uri,
        imeAction = ImeAction.Next,
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(first)
      )
      Spacer(modifier = Modifier.height(12.dp))

      BlinkoTextField(
        url = username,
        label = stringResource(id = R.string.login_username),
        onUrlChange = { username = it },
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(second)
      )
      Spacer(modifier = Modifier.height(12.dp))
      
      BlinkoPasswordField(
        username = password,
        label = stringResource(id = R.string.login_password),
        onUsernameChange = { password = it },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(third)
      )

      if (url.startsWith("http://")) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Switch(
            checked = insecureUrlCheckedState,
            onCheckedChange = { insecureUrlCheckedState = it }
          )
          Text(
            text = stringResource(id = R.string.login_token_insecure_url),
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 8.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LoginButton(
        onClick = {
          onLoginClicked(url, username, password, insecureUrlCheckedState)
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
      text = stringResource(id = R.string.login_token_login_button),
      fontSize = 16.sp
    )
  }
}




