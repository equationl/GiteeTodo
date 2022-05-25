package com.equationl.giteetodo.ui.page

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.giteetodo.constants.ClientInfo
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.CustomWebView
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.LoginOauthViewAction
import com.equationl.giteetodo.viewmodel.LoginOauthViewEvent
import com.equationl.giteetodo.viewmodel.LoginOauthViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "el, OAuthLogin"

@Composable
fun OAuthLoginScreen(
    navHostController: NavHostController,
    viewModel: LoginOauthViewModel = hiltViewModel()
) {
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is LoginOauthViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is LoginOauthViewEvent.Goto) {
                println("Goto route=${it.route}")
                navHostController.navigate(it.route)
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("授权登录") {
                    navHostController.navigate(Route.LOGIN)
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }
            }
        )
        {
            OAuthWebView(viewModel)
        }
    }
}

@Composable
fun OAuthWebView(viewModel: LoginOauthViewModel) {
    var rememberWebProgress: Int by remember { mutableStateOf(-1)}

    Box(Modifier.fillMaxSize()) {
        val scope = URLEncoder.encode(ClientInfo.PermissionScope, StandardCharsets.UTF_8.toString())
        CustomWebView(
            url = "https://gitee.com/oauth/authorize?client_id=${ClientInfo.ClientId}&redirect_uri=${ClientInfo.AuthUri}&response_type=code&scope=$scope",
            onBack = {
            it?.goBack()
        },
            onShouldOverrideUrlLoading = { _: WebView?, request: WebResourceRequest? ->
                if (request != null && request.url != null &&
                    request.url.toString().startsWith(ClientInfo.AuthUri)) {
                    val code = request.url.getQueryParameter("code")
                    if (code != null) {
                        Log.i(TAG, "OAuthLoginScreen: url=${request.url}")
                        viewModel.dispatch(LoginOauthViewAction.RequestToken(code))
                        true
                    }
                    else {
                        Log.i(TAG, "OAuthWebView: code 为空！")
                        viewModel.dispatch(LoginOauthViewAction.WebViewLoadError("参数读取错误！"))
                        false
                    }
                }
                else {
                    Log.i(TAG, "OAuthWebView: 地址不符合")
                    false
                }
            },
            onProgressChange = {progress ->
                rememberWebProgress = progress
            },
            onReceivedError = {
                Log.e(TAG, "OAuthWebView: 加载失败：code=${it?.errorCode}, des=${it?.description}")
                viewModel.dispatch(LoginOauthViewAction.WebViewLoadError(it?.description.toString()))
            },
            modifier = Modifier.fillMaxSize())

        LinearProgressIndicator(
            progress = rememberWebProgress * 1.0F / 100F,
            color = Color.Red,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (rememberWebProgress == 100) 0.dp else 5.dp))
    }
}