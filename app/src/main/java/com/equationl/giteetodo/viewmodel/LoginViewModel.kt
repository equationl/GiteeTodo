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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

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
            viewStates = viewStates.copy(isEmailError = true, emailLabel = "???????????????")
            return
        }
        if (viewStates.password.isBlank()) {
            viewStates = viewStates.copy(isPassWordError = true, passwordLabel = "???????????????")
            return
        }
        if (!viewStates.email.isEmail()) {
            viewStates = viewStates.copy(isEmailError = true, emailLabel = "????????????????????????")
            return
        }

        viewStates = viewStates.copy(isLogging = true)

        val exception = CoroutineExceptionHandler { _, throwable ->
            viewStates = viewStates.copy(isLogging = false)
            viewModelScope.launch {
                _viewEvents.send(LoginViewEvent.ShowMessage("???????????????${throwable.message}"))
            }
        }

        viewModelScope.launch(exception) {
            val response = oAuthApi.getTokenByPsw(
                viewStates.email,
                viewStates.password,
                ClientInfo.ClientId,
                ClientInfo.ClientSecret)

            resolveTokenResponse(response)
        }
    }

    private fun loginByAccess(isClr: Boolean = false) {
        if (viewStates.password.isBlank()) {
            viewStates = viewStates.copy(isPassWordError = true, passwordLabel = "???????????????")
            return
        }

        viewStates = viewStates.copy(isLogging = true)

        viewModelScope.launch {
            val response: Response<User>
            try {
                response = userApi.getUser(viewStates.password)
            } catch (tr: Throwable) {
                viewStates = viewStates.copy(isLogging = false, password = if (isClr) "" else viewStates.password)
                _viewEvents.send(LoginViewEvent.ShowMessage("???????????????${tr.message}"))
                return@launch
            }

            if (response.isSuccessful) {
                DataStoreUtils.putSyncData(DataKey.UserInfo, response.body()?.toJson() ?: "")
                DataStoreUtils.saveSyncStringData(DataKey.LoginAccessToken, viewStates.password)
                DataStoreUtils.saveSyncStringData(DataKey.LoginMethod, LoginMethod.AccessToken.name)

                navToHome()
            }
            else {
                viewStates = viewStates.copy(isLogging = false, password = if (isClr) "" else viewStates.password)
                val result = kotlin.runCatching {
                    _viewEvents.send(LoginViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
                }
                if (result.isFailure) {
                    _viewEvents.send(LoginViewEvent.ShowMessage("??????????????????????????????????????????${result.exceptionOrNull()?.message ?: ""}"))
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
                    _viewEvents.send(LoginViewEvent.ShowMessage("???????????????????????????????????????${it.message}"))
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
            _viewEvents.send(LoginViewEvent.NavTo(Route.OAuthLogin))
        }
    }

    private fun switchToAccessToken() {
        if (loginMethod != LoginMethod.AccessToken) {
            viewStates = viewStates.copy(
                password = "",
                passwordLabel = "??????",
                isShowEmailEdit = false,
                accessLoginTitle = "??????????????????")
            loginMethod = LoginMethod.AccessToken
        } else {
            viewStates = viewStates.copy(
                password = "",
                passwordLabel = "??????",
                isShowEmailEdit = true,
                accessLoginTitle = "??????????????????")
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
            viewStates.copy(email = email, isEmailError = true, emailLabel = "????????????????????????")
        } else {
            viewStates.copy(email = email, isEmailError = false, emailLabel = "??????")
        }
    }

    private fun updatePassword(password: String) {
        val label = if (loginMethod == LoginMethod.Email) "??????" else "??????"
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
                _viewEvents.send(LoginViewEvent.ShowMessage("???????????????${throwable.message}"))
            }
        }

        viewModelScope.launch(exception) {
            val loginMethodString = DataStoreUtils.readStringData(DataKey.LoginMethod)
            if (loginMethodString.isBlank()) {
                // ???????????????
                viewStates = viewStates.copy(isLogging = false)
            }
            else {
                // ???????????????????????????
                try {
                    loginMethod = LoginMethod.valueOf(loginMethodString)
                } catch (e: IllegalArgumentException) {
                    _viewEvents.send(LoginViewEvent.ShowMessage("????????????????????????????????????"))
                    viewStates = viewStates.copy(isLogging = false)
                }

                when (loginMethod) {
                    LoginMethod.Email, LoginMethod.OAuth2 -> {
                        val expireTime = DataStoreUtils.getSyncData(DataKey.LoginTokenExpireTime, 0)
                        if (expireTime < System.currentTimeMillis() / 1000 - 1800) {  // ?????????????????????
                            // ????????????????????????
                            val refreshToken = DataStoreUtils.getSyncData(DataKey.LoginRefreshToken, "")
                            val response = oAuthApi.refreshToken(
                                refreshToken = refreshToken
                            )
                            resolveTokenResponse(response)
                        }
                        else {
                            // ????????????????????????
                            viewStates = viewStates.copy(
                                password = DataStoreUtils.readStringData(DataKey.LoginAccessToken)
                            )
                            loginByAccess(true)
                        }
                    }
                    LoginMethod.AccessToken -> {
                        viewStates = viewStates.copy(
                            password = DataStoreUtils.readStringData(DataKey.LoginAccessToken)
                        )
                        loginByAccess()
                    }
                }

                // ???????????????????????????????????????
                loginMethod = LoginMethod.Email
            }
        }
    }

    private suspend fun resolveTokenResponse(response: Response<Token>) {
        if (response.isSuccessful) {
            val tokenBody = response.body()
            if (tokenBody == null) {
                _viewEvents.send(LoginViewEvent.ShowMessage("????????????????????????????????????"))
            }
            else {
                val currentTime = (System.currentTimeMillis() / 1000).toInt()
                DataStoreUtils.saveSyncStringData(DataKey.LoginAccessToken, tokenBody.accessToken)
                DataStoreUtils.saveSyncStringData(DataKey.LoginMethod, LoginMethod.Email.name)
                DataStoreUtils.saveSyncStringData(DataKey.LoginRefreshToken, tokenBody.refreshToken)
                DataStoreUtils.saveSyncIntData(DataKey.LoginTokenExpireTime, tokenBody.expiresIn + currentTime)
                DataStoreUtils.saveSyncIntData(DataKey.LoginTokenRefreshTime, currentTime)

                navToHome()
            }
        }
        else {
            viewStates = viewStates.copy(isLogging = false)
            val result = kotlin.runCatching {
                _viewEvents.send(LoginViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
            }
            if (result.isFailure) {
                _viewEvents.send(LoginViewEvent.ShowMessage("??????????????????????????????????????????${result.exceptionOrNull()?.message ?: ""}"))
            }
        }
    }
}

data class LoginViewState(
    val email: String = "",
    val password: String = "",
    val emailLabel: String = "??????",
    val passwordLabel: String = "??????",
    val accessLoginTitle: String = "??????????????????",
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