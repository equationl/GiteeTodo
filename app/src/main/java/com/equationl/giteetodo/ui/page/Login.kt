package com.equationl.giteetodo.ui.page

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.BaseMsgDialog
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.LoginViewAction
import com.equationl.giteetodo.viewmodel.LoginViewEvent
import com.equationl.giteetodo.viewmodel.LoginViewModel
import com.equationl.giteetodo.viewmodel.LoginViewState
import kotlinx.coroutines.launch

private const val TAG = "el, LoginScreen"

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val activity = (LocalContext.current as? Activity)
    val viewState = viewModel.viewStates
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.dispatch(LoginViewAction.CheckLoginState)
        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is LoginViewEvent.NavTo) {
                navController.navigate(it.route) {
                    popUpTo(Route.LOGIN) {
                        inclusive = true
                    }
                }
            }
            else if (it is LoginViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("登录", navigationIcon = Icons.Outlined.Close) {
                    // 点击退出
                    activity?.finish()
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }}
        )
        {
            Log.i(TAG, "LoginScreen: padding=$it")
            if (viewState.isLogging) {
                LoadDataContent(text = "正在登录中…")
            }
            else {
                LoginContent()
            }

            LoginHelpDialog(loginViewModel = viewModel, viewState)
        }
    }
}

@Composable
fun LoginContent() {
    val loginViewModel: LoginViewModel = viewModel()
    val viewState = loginViewModel.viewStates
    val context = LocalContext.current

    Column(Modifier.background(MaterialTheme.colors.baseBackground)) {

        Column(Modifier.weight(9f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                horizontalArrangement = Arrangement.Center) {

                Text(text = "登录", fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 10.sp)
            }

            if (viewState.isShowEmailEdit) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center) {
                    EmailEditWidget(loginViewModel, viewState)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center) {
                PasswordEditWidget(loginViewModel, viewState)
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 4.dp)) {
                LinkText("注册账号") {
                    loginViewModel.dispatch(LoginViewAction.Register(context = context))
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick =
                    {
                        loginViewModel.dispatch(LoginViewAction.Login)
                    },
                    shape = Shapes.large) {
                    Text(text = "登录", fontSize = 20.sp, modifier = Modifier.padding(start = 82.dp, end = 82.dp, top = 4.dp, bottom = 4.dp))
                }
            }
        }

        Column(Modifier.weight(1.5f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
                Row(horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)) {
                    Text(text = "其他登录方式")
                    IconButton(onClick = { loginViewModel.dispatch(LoginViewAction.ShowLoginHelp) }) {
                        Icon(Icons.Outlined.HelpOutline, contentDescription = "疑问", tint = MaterialTheme.colors.primary)
                    }
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(end = 8.dp, start = 8.dp)
                        .weight(1f)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp, 0.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                LinkText("OAuth2授权登录") {
                    loginViewModel.dispatch(LoginViewAction.SwitchToOAuth2)
                }
                LinkText(viewState.accessLoginTitle) {
                    loginViewModel.dispatch(LoginViewAction.SwitchToAccessToken)
                }
            }
        }
    }
}

@Composable
fun EmailEditWidget(loginViewModel: LoginViewModel, viewState: LoginViewState) {
    OutlinedTextField(
        value = viewState.email,
        onValueChange = {
            loginViewModel.dispatch(LoginViewAction.UpdateEmail(it))
        },
        label = {
            Text(text = viewState.emailLabel)
        },
        isError = viewState.isEmailError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        trailingIcon = {
            if (viewState.email.isNotEmpty()) {
                IconButton(onClick = { loginViewModel.dispatch(LoginViewAction.ClearEmail) }) {
                    Icon(Icons.Outlined.Clear,
                        contentDescription = "清除"
                    )
                }
            }
        },
        leadingIcon = {
            Icon(Icons.Outlined.Email,
                contentDescription = "邮箱"
            )
        },
        shape = Shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
fun PasswordEditWidget(loginViewModel: LoginViewModel, viewState: LoginViewState) {
    val keyboardService = LocalTextInputService.current

    OutlinedTextField(
        value = viewState.password,
        onValueChange = {
            loginViewModel.dispatch(LoginViewAction.UpdatePassword(it))
        },
        label = {
            Text(text = viewState.passwordLabel)
        },
        isError = viewState.isPassWordError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions { keyboardService?.hideSoftwareKeyboard()  },
        visualTransformation = if (viewState.isPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            if (viewState.password.isNotEmpty()) {
                IconButton(onClick = {
                    loginViewModel.dispatch(LoginViewAction.TogglePasswordVisibility)
                }) {
                    if (viewState.isPasswordVisibility) {
                        Icon(Icons.Outlined.VisibilityOff,
                            contentDescription = "隐藏密码"
                        )
                    }
                    else {
                        Icon(Icons.Outlined.Visibility,
                            contentDescription = "显示密码"
                        )
                    }
                }
            }
        },
        leadingIcon = {
            Icon(Icons.Outlined.Password,
                contentDescription = "密码"
            )
        },
        shape = Shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    )
}

@Composable
fun LoginHelpDialog(loginViewModel: LoginViewModel, viewState: LoginViewState) {
    val message = """
        ### 登录方式
        本程序支持以下三种登录方式：
        
        1. 账号密码登录
        2. 私人令牌登录
        3. OAuth2 授权登录
                
        我们推荐使用第 2 或 第 3 种方式登录，不建议使用第 1 种方式登录。
        
        #### 账号密码登录
        直接使用码云账号密码登录，我们不会储存你的账号密码，该账号密码仅用于当前获取 token 这一过程，使用完毕会立即销毁。  
        
        **我们不推荐使用该登录方式**

        #### 私人令牌登录
        
        登录码云（必须是桌面版）后，依次点击 右上角头像 - 设置 - 安全设置 - 私人令牌 - 生成新令牌 - 勾选所需要的权限（`user_info projects issues notes`） - 提交即可。
           
        *使用私人令牌的优势： 不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权。*

        #### OAuth2 授权登录
        
        使用码云官方 OAuth2 认证授权。
           
        *使用 OAuth2 的优势：不会暴露账号密码、权限可以自己控制、随时可以修改或删除授权、token有效期只有一天。*
        
        **注意**：无论使用什么方式登录，我们都会储存你的授权 Token ， 便于下次直接登录。
        
        该 Token 你随时可以在 Gitee（码云）个人设置中查看并取消授权或删除、修改。
        
        本程序绝对不会滥用用户授权的权限，仅使用本程序提到的功能，也不会读取或修改用户的其他任何信息。

        如果不放心，欢迎查看源码或自行使用源码编译使用。
    """.trimIndent()
    if (viewState.isShowLoginHelpDialog) {
        BaseMsgDialog(message = message, confirmText = "知道了") {
            loginViewModel.dispatch(LoginViewAction.ToggleLoginHelpDialogShow(false))
        }
    }
}

@Preview
@Composable
fun PreviewLoginView() {
    LoginContent()
}