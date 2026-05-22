package com.sakura_ai_reviewer.feature.auth.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakura_ai_reviewer.core.ui.theme.Primary
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        is LoginState.MfaRequired -> {
            MfaVerifyScreen(
                loginState = loginState as LoginState.MfaRequired,
                onVerifyTotp = { code -> viewModel.verifyTotpCode(
                    (loginState as LoginState.MfaRequired).mfaToken, code
                )},
                onStartPasskey = { viewModel.startPasskeyAuth(
                    (loginState as LoginState.MfaRequired).mfaToken
                )},
                onBack = { viewModel.resetLoginState() }
            )
        }
        is LoginState.PasskeyOptionsReady -> {
            PasskeyVerifyScreen(
                loginState = loginState as LoginState.PasskeyOptionsReady,
                moshi = viewModel.moshi,
                onVerify = { credential -> viewModel.verifyPasskey(
                    (loginState as LoginState.PasskeyOptionsReady).mfaToken,
                    (loginState as LoginState.PasskeyOptionsReady).challengeId,
                    credential
                )},
                onBack = { viewModel.resetLoginState() }
            )
        }
        else -> {
            LoginContent(
                loginState = loginState,
                onLoginClick = { viewModel.initiateGitHubLogin() },
                onPasskeyLoginClick = { viewModel.initiatePasskeyLogin() },
                onRetryClick = { viewModel.resetLoginState() }
            )
        }
    }
}

@Composable
private fun MfaVerifyScreen(
    loginState: LoginState.MfaRequired,
    onVerifyTotp: (String) -> Unit,
    onStartPasskey: () -> Unit,
    onBack: () -> Unit
) {
    var totpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val hasTotp = loginState.methods.contains("totp")
    val hasPasskey = loginState.methods.contains("passkey")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Password,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Two-Step Verification",
            style = MaterialTheme.typography.headlineMedium,
            color = Primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your account requires additional verification",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
        } else {
            if (hasTotp) {
                OutlinedTextField(
                    value = totpCode,
                    onValueChange = { if (it.length <= 6) totpCode = it },
                    label = { Text("Verification Code") },
                    placeholder = { Text("6-digit code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        onVerifyTotp(totpCode)
                    },
                    enabled = totpCode.length == 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Verify", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (hasTotp && hasPasskey) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("or", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (hasPasskey) {
                Button(
                    onClick = {
                        isLoading = true
                        onStartPasskey()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Use Passkey", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun PasskeyVerifyScreen(
    loginState: LoginState.PasskeyOptionsReady,
    moshi: Moshi,
    onVerify: (Map<String, Any>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loginState.challengeId) {
        isLoading = true
        errorMessage = null
        try {
            val credentialManager = CredentialManager.create(context)
            val requestJson = loginState.publicKey.toRequestJson(moshi)
            val credentialResponse = withContext(Dispatchers.IO) {
                credentialManager.getCredential(
                    context = context,
                    request = GetCredentialRequest(
                        listOf(GetPublicKeyCredentialOption(requestJson))
                    )
                )
            }
            val credential = credentialResponse.credential
            if (credential is PublicKeyCredential) {
                onVerify(jsonObjectToMap(JSONObject(credential.authenticationResponseJson)))
            } else {
                errorMessage = "Unsupported credential type"
            }
        } catch (e: GetCredentialException) {
            Log.e("PasskeyVerifyScreen", "Credential Manager failed: ${e::class.java.name}, type=${e.type}, message=${e.message}", e)
            errorMessage = e.message ?: "Passkey authentication failed"
        } catch (e: Exception) {
            Log.e("PasskeyVerifyScreen", "Passkey authentication failed: ${e::class.java.name}, message=${e.message}", e)
            errorMessage = e.message ?: "Passkey authentication failed"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Key,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Passkey Authentication",
            style = MaterialTheme.typography.headlineMedium,
            color = Primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete authentication using your passkey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Waiting for passkey verification...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun LoginContent(
    loginState: LoginState,
    onLoginClick: () -> Unit,
    onPasskeyLoginClick: () -> Unit,
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
                onClick = onPasskeyLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = Icons.Filled.Key,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Sign in with Passkey",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                            if (url.contains("/auth/callback") || url.startsWith("sakura-ai-reviewer://oauth/callback")) {
                                val uri = request.url
                                val error = uri.getQueryParameter("error")

                                if (error != null) {
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

private fun jsonObjectToMap(jsonObject: JSONObject): Map<String, Any> = buildMap {
    val keys = jsonObject.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        put(key, jsonValueToKotlin(jsonObject.get(key)))
    }
}

private fun jsonArrayToList(jsonArray: JSONArray): List<Any> = buildList {
    for (index in 0 until jsonArray.length()) {
        add(jsonValueToKotlin(jsonArray.get(index)))
    }
}

private fun jsonValueToKotlin(value: Any): Any = when (value) {
    is JSONObject -> jsonObjectToMap(value)
    is JSONArray -> jsonArrayToList(value)
    JSONObject.NULL -> ""
    else -> value
}
