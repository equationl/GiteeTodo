package com.equationl.giteetodo.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.constants.ClientInfo
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.datastore.DataKey
import com.equationl.giteetodo.datastore.DataStoreUtils
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.Utils.isEmail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LoginViewModel: ViewModel() {
    private var loginMethod: LoginMethod = LoginMethod.Email
    private val oAuthApi by lazy { RetrofitManger.getOAuthApi() }
    private val userApi by lazy { RetrofitManger.getUserApi() }

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
        viewStates = viewStates.copy(isLogging = true)

        when (loginMethod) {
            LoginMethod.Email -> loginByEmail()
            LoginMethod.OAuth2 -> throw UnsupportedOperationException("暂不支持 OAuth2 授权登录")
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


        viewModelScope.launch {
            val response = oAuthApi.getTokenByPsw(
                viewStates.email,
                viewStates.password,
                ClientInfo.ClientId,
                ClientInfo.ClientSecret)

            println("response = $response")

            if (response.isSuccessful) {
                DataStoreUtils.saveSyncStringData(DataKey.LoginAccess, response.body()?.accessToken ?: "")
                DataStoreUtils.saveSyncStringData(DataKey.LoginEmail, viewStates.email)
                DataStoreUtils.saveSyncStringData(DataKey.LoginPassword, viewStates.password)
                DataStoreUtils.saveSyncStringData(DataKey.LoginMethod, LoginMethod.Email.name)

                navToHome()
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

    private fun loginByAccess() {
        if (viewStates.password.isBlank()) {
            viewStates = viewStates.copy(isPassWordError = true, passwordLabel = "请输入令牌")
            return
        }

        viewModelScope.launch {
            val response = userApi.getUser(viewStates.password)

            println("response = $response")

            if (response.isSuccessful) {
                DataStoreUtils.saveSyncStringData(DataKey.LoginAccess, viewStates.password)
                DataStoreUtils.saveSyncStringData(DataKey.LoginEmail, "")
                DataStoreUtils.saveSyncStringData(DataKey.LoginPassword, "")
                DataStoreUtils.saveSyncStringData(DataKey.LoginMethod, LoginMethod.AccessToken.name)

                navToHome()
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

    private suspend fun navToHome() {
        val usingRepo = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
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
            _viewEvents.send(LoginViewEvent.ShowMessage("暂不支持该登录方式"))
        }
    }

    private fun switchToAccessToken() {
        viewStates = if (loginMethod != LoginMethod.AccessToken) {
            viewStates.copy(
                password = "",
                passwordLabel = "令牌",
                isShowEmailEdit = false,
                accessLoginTitle = "账号密码登录")
        } else {
            viewStates.copy(
                password = "",
                passwordLabel = "密码",
                isShowEmailEdit = true,
                accessLoginTitle = "私人令牌登录")
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
        viewModelScope.launch {
            val loginMethodString = DataStoreUtils.readStringData(DataKey.LoginMethod)
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
                    LoginMethod.Email -> {
                        viewStates = viewStates.copy(
                            email = DataStoreUtils.readStringData(DataKey.LoginEmail),
                            password = DataStoreUtils.readStringData(DataKey.LoginPassword)
                        )
                        loginByEmail()
                    }
                    LoginMethod.OAuth2 -> throw UnsupportedOperationException()
                    LoginMethod.AccessToken -> {
                        viewStates = viewStates.copy(
                            password = DataStoreUtils.readStringData(DataKey.LoginAccess)
                        )
                        loginByAccess()
                    }
                }
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
    object Login : LoginViewAction()
    object ClearEmail : LoginViewAction()
    object ClearPassword : LoginViewAction()
    object TogglePasswordVisibility : LoginViewAction()
    object SwitchToOAuth2: LoginViewAction()
    object SwitchToAccessToken: LoginViewAction()
    object ShowLoginHelp: LoginViewAction()
    object CheckLoginState: LoginViewAction()
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