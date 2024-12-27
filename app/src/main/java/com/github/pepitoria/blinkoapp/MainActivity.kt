package com.github.pepitoria.blinkoapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.pepitoria.blinkoapp.ui.login.LoginWidget
import com.github.pepitoria.blinkoapp.ui.login.TokenLoginWidget
import com.github.pepitoria.blinkoapp.ui.sharewithblinko.edit.ShareAndEditWithBlinkoActivity
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CookieHandler.setDefault(CookieManager())
    val webCookieManager = CookieHandler.getDefault() as CookieManager
    webCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

    setContent {
      BlinkoAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
//                    LoginWidget()
//                    WebLoginWidget()
          TokenLoginWidget(goToEditWithBlinko = {
            goToEditWithBlinko()
          })
        }
      }
    }
  }

  private fun goToEditWithBlinko() {
    val intent = Intent(this, ShareAndEditWithBlinkoActivity::class.java)
    intent.action = Intent.ACTION_SEND
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, "Hello, Blinko lalala!")
    startActivity(intent)
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  BlinkoAppTheme {
    LoginWidget()
  }
}