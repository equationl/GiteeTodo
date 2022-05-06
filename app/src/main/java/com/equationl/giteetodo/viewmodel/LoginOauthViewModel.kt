package com.equationl.giteetodo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.constants.ClientInfo
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginOauthViewModel : ViewModel() {
    private val oAuthApi by lazy { RetrofitManger.getOAuthApi() }

    private val _viewEvents = Channel<LoginOauthViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: LoginOauthViewAction) {
        when (action) {
            is LoginOauthViewAction.RequestToken -> requestToken(action.code)
            is LoginOauthViewAction.WebViewLoadError -> webViewLoadError(action.message)
        }
    }

    private fun requestToken(code: String) {
        val exception = CoroutineExceptionHandler { _, throwable ->
            webViewLoadError("错误："+throwable.message)
        }

        viewModelScope.launch(exception) {
            val response = oAuthApi.getTokenByCode(
                code = code,
                clientId = ClientInfo.ClientId,
                clientSecret = ClientInfo.ClientSecret,
                redirectUri = ClientInfo.AuthUri
            )

            println("response = $response")

            if (response.isSuccessful) {
                val tokenBody = response.body()
                if (tokenBody == null) {
                    webViewLoadError("获取 token 失败：返回数据为空！")
                }
                else {
                    val currentTime = (System.currentTimeMillis() / 1000).toInt()
                    DataStoreUtils.saveSyncStringData(DataKey.LoginAccessToken, tokenBody.accessToken)
                    DataStoreUtils.saveSyncStringData(DataKey.LoginMethod, LoginMethod.OAuth2.name)
                    DataStoreUtils.saveSyncStringData(DataKey.LoginRefreshToken, tokenBody.refreshToken)
                    DataStoreUtils.saveSyncIntData(DataKey.LoginTokenExpireTime, tokenBody.expiresIn+currentTime)
                    DataStoreUtils.saveSyncIntData(DataKey.LoginTokenRefreshTime, currentTime)

                    _viewEvents.send(LoginOauthViewEvent.Goto(Route.REPO_LIST))
                }
            }
            else {
                val result = kotlin.runCatching {
                    webViewLoadError("获取 token 失败："+response.errorBody()?.string())
                }
                if (result.isFailure) {
                    webViewLoadError("获取 token 失败：，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}")
                }
            }
        }
    }

    private fun webViewLoadError(message: String) {
        viewModelScope.launch {
            _viewEvents.send(LoginOauthViewEvent.ShowMessage(message))
            delay(3000)
            _viewEvents.send(LoginOauthViewEvent.Goto(Route.LOGIN))
        }
    }
}

sealed class LoginOauthViewEvent {
    data class Goto(val route: String):LoginOauthViewEvent()
    data class ShowMessage(val message: String) :LoginOauthViewEvent()
}

sealed class LoginOauthViewAction {
    data class RequestToken(val code: String): LoginOauthViewAction()
    data class WebViewLoadError(val message: String): LoginOauthViewAction()
}