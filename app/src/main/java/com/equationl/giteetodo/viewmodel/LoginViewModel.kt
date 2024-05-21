package com.equationl.giteetodo.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.constants.ClientInfo
import com.equationl.giteetodo.data.auth.OAuthApi
import com.equationl.giteetodo.data.auth.model.response.Token
import com.equationl.giteetodo.data.user.UserApi
import com.equationl.giteetodo.data.user.model.response.User
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.Utils.isEmail
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "LoginViewModel"

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val oAuthApi: OAuthApi,
    private val userApi: UserApi
) : ViewModel() {
    private var loginMethod: LoginMethod = LoginMethod.Email

    var viewStates by mutableStateOf(LoginViewState())
        private set

    private val _viewEvents = Channel<LoginViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: LoginViewAction) {
        when (action) {
            is LoginViewAction.Login -> login()
            is LoginViewAction.ClearEmail -> clearEmail()
            is LoginViewAction.ClearPassword -> clearPassword()
            is LoginViewAction.TogglePasswordVisibility -> togglePasswordVisibility()
            is LoginViewAction.SwitchToOAuth2 -> switchToOAuth2()
            is LoginViewAction.SwitchToAccessToken -> switchToAccessToken()
            is LoginViewAction.ShowLoginHelp -> showLoginHelp()
            is LoginViewAction.CheckLoginState -> checkLoginState()
            is LoginViewAction.Register -> register(action.context)
            is LoginViewAction.UpdateEmail -> updateEmail(action.email)
            is LoginViewAction.UpdatePassword -> updatePassword(action.password)
            is LoginViewAction.ToggleLoginHelpDialogShow -> toggleLoginHelpDialogShow(action.isShow)
        }
    }

    private fun login() {
        when (loginMethod) {
            LoginMethod.Email -> loginByEmail()
            LoginMethod.OAuth2 -> throw IllegalArgumentException("OAuth need open in webView!")
            LoginMethod.AccessToken -> loginByAccess()
        }
    }

    private fun loginByEmail() {
        if (viewStates.email.isBlank()) {
            viewStates = viewStates.copy(isEmailError = true, emailLabel = "请输入邮箱")
            return
        }
        if (viewStates.password.isBlank()) {
            viewStates = viewStates.copy(isPassWordError = true, passwordLabel = "请输入密码")
            return
        }
        if (!viewStates.email.isEmail()) {
            viewStates = viewStates.copy(isEmailError = true, emailLabel = "请输入正确的邮箱")
            return
        }

        viewStates = viewStates.copy(isLogging = true)

        val exception = CoroutineExceptionHandler { _, throwable ->
            viewStates = viewStates.copy(isLogging = false)
            viewModelScope.launch {
                _viewEvents.send(LoginViewEvent.ShowMessage("请求失败：${throwable.message}"))
            }
        }

        viewModelScope.launch(exception) {
            val response = oAuthApi.getTokenByPsw(
                viewStates.email,
                viewStates.password,
                ClientInfo.CLIENT_ID,
                ClientInfo.CLIENT_SECRET)

            resolveTokenResponse(response)
        }
    }

    private fun loginByAccess(isClr: Boolean = false, fromLoginMethod: LoginMethod = LoginMethod.AccessToken) {
        if (viewStates.password.isBlank()) {
            viewStates = viewStates.copy(isPassWordError = true, passwordLabel = "请输入令牌")
            return
        }

        viewStates = viewStates.copy(isLogging = true)

        viewModelScope.launch {
            val response: Response<User>
            try {
                response = userApi.getUser(viewStates.password)
            } catch (tr: Throwable) {
                viewStates = viewStates.copy(isLogging = false, password = if (isClr) "" else viewStates.password)
                _viewEvents.send(LoginViewEvent.ShowMessage("登录失败：${tr.message}"))
                return@launch
            }

            if (response.isSuccessful) {
                DataStoreUtils.putSyncData(DataKey.USER_INFO, response.body()?.toJson() ?: "")
                DataStoreUtils.saveSyncStringData(DataKey.LOGIN_ACCESS_TOKEN, viewStates.password)
                DataStoreUtils.saveSyncStringData(DataKey.LOGIN_METHOD, fromLoginMethod.name)

                navToHome()
            }
            else {
                viewStates = viewStates.copy(isLogging = false, password = if (isClr) "" else viewStates.password)
                val result = kotlin.runCatching {
                    _viewEvents.send(LoginViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
                }
                if (result.isFailure) {
                    _viewEvents.send(LoginViewEvent.ShowMessage("登录失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private suspend fun navToHome() {
        val usingRepo = DataStoreUtils.getSyncData(DataKey.USING_REPO, "")
        val repoListRoute = Route.REPO_LIST
        if (usingRepo.isBlank()) {
            _viewEvents.send(LoginViewEvent.NavTo(repoListRoute))
        }
        else {
            kotlin.runCatching {
                "${Route.HOME}/${URLEncoder.encode(usingRepo, StandardCharsets.UTF_8.toString())}"
            }.fold(
                {
                    _viewEvents.send(LoginViewEvent.NavTo(it))
                },
                {
                    _viewEvents.send(LoginViewEvent.ShowMessage("获取仓库失败，请重新选择：${it.message}"))
                    _viewEvents.send(LoginViewEvent.NavTo(repoListRoute))
                })
        }
    }

    private fun register(context: Context) {
        val uri = Uri.parse("https://gitee.com/signup")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    private fun switchToOAuth2() {
        viewModelScope.launch {
            _viewEvents.send(LoginViewEvent.NavTo(Route.OAUTH_LOGIN))
        }
    }

    private fun switchToAccessToken() {
        if (loginMethod != LoginMethod.AccessToken) {
            viewStates = viewStates.copy(
                password = "",
                passwordLabel = "令牌",
                isShowEmailEdit = false,
                accessLoginTitle = "账号密码登录")
            loginMethod = LoginMethod.AccessToken
        } else {
            viewStates = viewStates.copy(
                password = "",
                passwordLabel = "密码",
                isShowEmailEdit = true,
                accessLoginTitle = "私人令牌登录")
            loginMethod = LoginMethod.Email
        }
    }

    private fun showLoginHelp() {
        viewStates = viewStates.copy(isShowLoginHelpDialog = true)
    }

    private fun clearEmail() {
        viewStates = viewStates.copy(email = "")
    }

    private fun clearPassword() {
        viewStates = viewStates.copy(password = "")
    }

    private fun updateEmail(email: String) {
        viewStates = if (!email.isEmail()) {
            viewStates.copy(email = email, isEmailError = true, emailLabel = "请输入正确的邮箱")
        } else {
            viewStates.copy(email = email, isEmailError = false, emailLabel = "邮箱")
        }
    }

    private fun updatePassword(password: String) {
        val label = if (loginMethod == LoginMethod.Email) "密码" else "令牌"
        viewStates = viewStates.copy(password = password, isPassWordError = false, passwordLabel = label)
    }

    private fun togglePasswordVisibility() {
        viewStates = viewStates.copy(isPasswordVisibility = !viewStates.isPasswordVisibility)
    }

    private fun toggleLoginHelpDialogShow(isShow: Boolean) {
        viewStates = viewStates.copy(isShowLoginHelpDialog = isShow)
    }

    private fun checkLoginState() {
        val exception = CoroutineExceptionHandler { _, throwable ->
            viewStates = viewStates.copy(isLogging = false)
            viewModelScope.launch {
                _viewEvents.send(LoginViewEvent.ShowMessage("请求失败：${throwable.message}"))
            }
        }

        viewModelScope.launch(exception) {
            withContext(Dispatchers.IO) {
                val loginMethodString = DataStoreUtils.readStringData(DataKey.LOGIN_METHOD)
                if (loginMethodString.isBlank()) {
                    // 从未登录过
                    viewStates = viewStates.copy(isLogging = false)
                }
                else {
                    // 本地保存了登录信息
                    try {
                        loginMethod = LoginMethod.valueOf(loginMethodString)
                    } catch (e: IllegalArgumentException) {
                        _viewEvents.send(LoginViewEvent.ShowMessage("程序内部错误，请重新登录"))
                        viewStates = viewStates.copy(isLogging = false)
                    }

                    when (loginMethod) {
                        LoginMethod.Email, LoginMethod.OAuth2 -> {
                            val expireTime = DataStoreUtils.getSyncData(DataKey.LOGIN_TOKEN_EXPIRE_TIME, 0)

                            Log.i(TAG, "checkLoginState: Token过期有效期判断：expireTime = $expireTime, currentTime = ${System.currentTimeMillis() / 1000 - 1800}")

                            if (expireTime < System.currentTimeMillis() / 1000 - 1800) {  // 提前半小时刷新
                                // 已过期，需要刷新
                                Log.i(TAG, "checkLoginState: Token已过期，尝试刷新！")
                                val refreshToken = DataStoreUtils.getSyncData(DataKey.LOGIN_REFRESH_TOKEN, "")
                                val response = oAuthApi.refreshToken(
                                    refreshToken = refreshToken
                                )
                                resolveTokenResponse(response, loginMethod)
                            }
                            else {
                                // 未过期，开始检查
                                Log.i(TAG, "checkLoginState: Token未过期！")
                                viewStates = viewStates.copy(
                                    password = DataStoreUtils.readStringData(DataKey.LOGIN_ACCESS_TOKEN)
                                )
                                loginByAccess(true, loginMethod)
                            }
                        }
                        LoginMethod.AccessToken -> {
                            Log.i(TAG, "checkLoginState: 使用 AccessToken 登录！")
                            viewStates = viewStates.copy(
                                password = DataStoreUtils.readStringData(DataKey.LOGIN_ACCESS_TOKEN)
                            )
                            loginByAccess()
                        }
                    }

                    // 检测完毕后就恢复成默认模式
                    loginMethod = LoginMethod.Email
                }
            }
        }
    }

    private suspend fun resolveTokenResponse(response: Response<Token>, fromLoginMethod: LoginMethod = LoginMethod.Email) {
        if (response.isSuccessful) {
            val tokenBody = response.body()
            if (tokenBody == null) {
                _viewEvents.send(LoginViewEvent.ShowMessage("登录失败：返回数据为空！"))
            }
            else {
                val currentTime = (System.currentTimeMillis() / 1000).toInt()
                DataStoreUtils.saveSyncStringData(DataKey.LOGIN_ACCESS_TOKEN, tokenBody.accessToken)
                DataStoreUtils.saveSyncStringData(DataKey.LOGIN_METHOD, fromLoginMethod.name)
                DataStoreUtils.saveSyncStringData(DataKey.LOGIN_REFRESH_TOKEN, tokenBody.refreshToken)
                DataStoreUtils.saveSyncIntData(DataKey.LOGIN_TOKEN_EXPIRE_TIME, tokenBody.expiresIn + currentTime)
                DataStoreUtils.saveSyncIntData(DataKey.LOGIN_TOKEN_REFRESH_TIME, currentTime)

                navToHome()
            }
        }
        else {
            viewStates = viewStates.copy(isLogging = false)
            val result = kotlin.runCatching {
                _viewEvents.send(LoginViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
            }
            if (result.isFailure) {
                _viewEvents.send(LoginViewEvent.ShowMessage("登录失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
            }
        }
    }
}

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val emailLabel: String = "邮箱",
    val passwordLabel: String = "密码",
    val accessLoginTitle: String = "私人令牌登录",
    val isPasswordVisibility: Boolean = false,
    val isEmailError: Boolean = false,
    val isPassWordError: Boolean = false,
    val isShowEmailEdit: Boolean = true,
    val isShowLoginHelpDialog: Boolean = false,
    val isLogging: Boolean = true,
)

sealed class LoginViewEvent {
    data class NavTo(val route: String) : LoginViewEvent()
    data class ShowMessage(val message: String) : LoginViewEvent()
}

sealed class LoginViewAction {
    data object Login : LoginViewAction()
    data object ClearEmail : LoginViewAction()
    data object ClearPassword : LoginViewAction()
    data object TogglePasswordVisibility : LoginViewAction()
    data object SwitchToOAuth2: LoginViewAction()
    data object SwitchToAccessToken: LoginViewAction()
    data object ShowLoginHelp: LoginViewAction()
    data object CheckLoginState: LoginViewAction()
    data class Register(val context: Context): LoginViewAction()
    data class UpdateEmail(val email: String) : LoginViewAction()
    data class UpdatePassword(val password: String) : LoginViewAction()
    data class ToggleLoginHelpDialogShow(val isShow: Boolean): LoginViewAction()
}

enum class LoginMethod {
    Email,
    OAuth2,
    AccessToken
}