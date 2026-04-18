package com.sakura_ai_reviewer.feature.auth.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.ui.theme.Primary

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    when (loginState) {
        is LoginState.GotAuthUrl -> {
            val authUrl = (loginState as LoginState.GotAuthUrl).authorizationUrl
            OAuthWebView(
                authUrl = authUrl,
                onCodeReceived = { code, state ->
                    viewModel.completeOAuthCallback(code, state)
                },
                onError = {
                    viewModel.resetLoginState()
                }
            )
        }
        else -> {
            LoginContent(
                loginState = loginState,
                onLoginClick = { viewModel.initiateGitHubLogin() },
                onRetryClick = { viewModel.resetLoginState() }
            )
        }
    }
}

@Composable
private fun LoginContent(
    loginState: LoginState,
    onLoginClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Sakura AI Reviewer",
            modifier = Modifier.size(120.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sakura AI Reviewer",
            style = MaterialTheme.typography.headlineLarge,
            color = Primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AI-powered code review assistant",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (loginState is LoginState.Loading) {
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier.size(48.dp)
            )
        } else {
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Sign in with GitHub",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (loginState is LoginState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (loginState as LoginState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OAuthWebView(
    authUrl: String,
    onCodeReceived: (code: String, state: String) -> Unit,
    onError: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    CookieManager.getInstance().removeAllCookies(null)

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false

                            // Intercept the callback URL from the server
                            if (url.contains("/auth/callback")) {
                                val uri = request.url
                                val error = uri.getQueryParameter("error")

                                if (error != null) {
                                    // User denied authorization or other OAuth error
                                    onError()
                                    return true
                                }

                                val code = uri.getQueryParameter("code")
                                val state = uri.getQueryParameter("state")

                                if (code != null && state != null) {
                                    onCodeReceived(code, state)
                                    return true
                                }
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }

                    loadUrl(authUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = Primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
    }
}
