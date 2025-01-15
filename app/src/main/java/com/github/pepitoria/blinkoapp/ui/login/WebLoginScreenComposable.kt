package com.github.pepitoria.blinkoapp.ui.login

import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.pepitoria.blinkoapp.ui.base.ComposableLifecycleEvents
import com.github.pepitoria.blinkoapp.ui.theme.BlinkoAppTheme
import timber.log.Timber
import java.net.CookieHandler
import java.net.CookieManager
import java.net.URI

@Deprecated("Use TokenLoginScreenComposable instead")
@Composable
fun WebLoginWidget(
  viewModel: WebLoginScreenViewModel = hiltViewModel()
) {
  ComposableLifecycleEvents(viewModel = viewModel)

  val url = viewModel.url.collectAsState()
  val urlValue = url.value
  BlinkoAppTheme {
    if (urlValue == null) {
      WebLoginForm(viewModel = viewModel)
    } else {
      WebLoginWebView(url = urlValue)
    }
  }
}

@Preview
@Composable
private fun WebLoginForm(
  viewModel: WebLoginScreenViewModel = hiltViewModel()
) {
  var url by rememberSaveable { mutableStateOf("https://blinko-demo.vercel.app") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    TextField(
      value = url,
      onValueChange = { url = it },
      label = { Text("Your Blinko instance url") },
      keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Uri,
        imeAction = ImeAction.Next
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
      onClick = {
        viewModel.setUrl(url)
      },
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .padding(8.dp)
    ) {
      Text("Go")
    }
  }

}

@Composable
private fun WebLoginWebView(url: String) {
  AndroidView(
    factory = { context ->
      WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = CustomWebViewClient(url)
      }
    }, update = {
      it.loadUrl(url)

    }, modifier = Modifier.fillMaxSize()
  )
}

@Suppress("OVERRIDE_DEPRECATION")
class CustomWebViewClient(
  private val blinkoUrl: String
) : WebViewClient() {
  override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
    Timber.d("URL: $url")
    return true
  }

  override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
    Timber.d("URL request: ${request?.url}")

    return super.shouldOverrideUrlLoading(view, request)
  }

  override fun shouldInterceptRequest(
    view: WebView?,
    request: WebResourceRequest?
  ): WebResourceResponse? {
//    Timber.d("URL intercept: ${request?.url}")

    return super.shouldInterceptRequest(view, request)
  }

  override fun onPageFinished(view: WebView?, url: String?) {
    if (blinkoUrl.equals(url)) {
      Timber.d("URL finished: $url")
      val cookieManager = CookieHandler.getDefault() as CookieManager
      val cookies = cookieManager.cookieStore.get(URI.create(url))
      Timber.d("URL finished with ${cookies.size} cookies for $url")
    }

    super.onPageFinished(view, url)
  }

}

