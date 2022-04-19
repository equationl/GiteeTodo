package com.equationl.giteetodo.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.util.RouteConfig
import com.equationl.giteetodo.util.Utils.isEmail
import com.equationl.giteetodo.viewmodel.MainViewModel

@Composable
fun LoginScreen(navController: NavHostController) {
    val activity = (LocalContext.current as? Activity)
    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("登录", navigationIcon = Icons.Outlined.Close) {
                    // 点击退出
                    activity?.finish()
                }
            })
        {
           LoginContent(navController)
        }
    }
}

@Composable
fun LoginContent(navController: NavHostController) {
    val mainViewModel: MainViewModel = viewModel()
    var email: String by remember { mutableStateOf("") }
    var psw: String by remember { mutableStateOf("") }
    var isShowPsw: Boolean by remember { mutableStateOf(false) }
    var isEmailError: Boolean by remember { mutableStateOf(false) }
    var isPswError: Boolean by remember { mutableStateOf(false) }
    var emailLabel: String by remember { mutableStateOf("邮箱") }
    var pswLabel: String by remember { mutableStateOf("密码") }

    Column(Modifier.background(baseBackground)) {

        Column(Modifier.weight(9f)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 100.dp),
                horizontalArrangement = Arrangement.Center) {

                Text(text = "登录", fontSize = 30.sp, fontWeight = FontWeight.Bold, letterSpacing = 10.sp)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailError = false
                        emailLabel = "邮箱"
                        if (!it.isEmail()) {
                            isEmailError = true
                            emailLabel = "请输入正确的邮箱"
                        }
                        else {
                            isEmailError = false
                            emailLabel = "邮箱"
                        }
                    },
                    label = {
                        Text(text = emailLabel)
                    },
                    isError = isEmailError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    trailingIcon = {
                        if (email.isNotEmpty()) {
                            IconButton(onClick = { email = "" }) {
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

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = psw,
                    onValueChange = {
                        isPswError = false
                        pswLabel = "密码"
                        psw = it
                    },
                    label = {
                        Text(text = pswLabel)
                    },
                    isError = isPswError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = if (isShowPsw) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        if (psw.isNotEmpty()) {
                            IconButton(onClick = {
                                isShowPsw = !isShowPsw
                            }) {
                                if (isShowPsw) {
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

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 4.dp)) {
                Text(text = "注册账号", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Black, modifier = Modifier.clickable { /*TODO*/ })
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick =
                    {
                        if (email.isBlank()) {
                            isEmailError = true
                            emailLabel = "请输入邮箱"
                            return@Button
                        }
                        if (psw.isBlank()) {
                            isPswError = true
                            pswLabel = "请输入密码"
                            return@Button
                        }
                        if (!email.isEmail()) {
                            isEmailError = true
                            emailLabel = "请输入正确的邮箱"
                            return@Button
                        }
                        clickLogin(navController, mainViewModel, email, psw)
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
                    IconButton(onClick = { /*TODO*/ }) {
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
                Text(text = "OAuth2授权登录", color = MaterialTheme.colors.primary, fontSize = 12.sp, modifier = Modifier.clickable { /*TODO*/ })
                Text(text = "私人令牌登录", color = MaterialTheme.colors.primary, fontSize = 12.sp, modifier = Modifier.clickable { /*TODO*/ })
            }
        }
    }
}

private fun clickLogin(
    navController: NavHostController,
    viewModel: MainViewModel,
    email: String,
    psw: String
) {
    // TODO 点击登录
    //viewModel.pswLogin(email, psw)

    navController.navigate(RouteConfig.ROUTE_TODO_LIST) {
        popUpTo(RouteConfig.ROUTE_LOGIN) {
            inclusive = true
        }
    }
}

@Preview
@Composable
fun PreviewLogin() {
    LoginScreen(rememberNavController())
}