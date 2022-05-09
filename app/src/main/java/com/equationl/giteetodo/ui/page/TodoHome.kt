package com.equationl.giteetodo.ui.page

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavHostController, repoPath: String) {
    val viewModel: TodoHomeViewModel = viewModel()
    val viewState = viewModel.viewStates
    val activity = (LocalContext.current as? Activity)
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoHomeViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is TodoHomeViewEvent.Goto) {
                println("Goto route=${it.route}")
                navController.navigate(it.route)
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar(viewState.title,
                    navigationIcon = Icons.Outlined.Close,
                    actions = {
                        HomeTopBarAction(viewState.currentPage, viewModel)
                    }) {
                    // 点击退出
                    activity?.finish()
                }
            },
            bottomBar = {
                HomeBottomBar(viewState, viewModel)
            },
            floatingActionButton = {
                HomeFloatActionBar(viewState.currentPage, viewModel)
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true,
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }}
        )
        {
            Column(
                Modifier
                    .background(MaterialTheme.colors.baseBackground)
                    .fillMaxSize()
                    .padding(bottom = it.calculateBottomPadding())) {
                HomeContent(viewState.currentPage, viewModel, navController, repoPath, scaffoldState)
            }
        }
    }
}

@Composable
fun HomeContent(currentPager: CurrentPager, viewModel: TodoHomeViewModel, navController: NavHostController, repoPath: String, scaffoldState: ScaffoldState) {
    if (currentPager == CurrentPager.HOME_TODO) {
        viewModel.dispatch(TodoHomeViewAction.ChangeTitle(repoPath.split("/")[1]))
        TodoListScreen(navController, repoPath, scaffoldState)
    }
    else if (currentPager == CurrentPager.HOME_ME) {
        viewModel.dispatch(TodoHomeViewAction.ChangeTitle(repoPath.split("/")[0]))
        ProfileScreen(navController, scaffoldState, repoPath)
    }
}

@Composable
fun HomeFloatActionBar(currentPager: CurrentPager, viewModel: TodoHomeViewModel) {
    if (currentPager == CurrentPager.HOME_TODO) {
        FloatingActionButton(onClick = {
            viewModel.dispatch(TodoHomeViewAction.AddATodo)
        }) {
            Icon(Icons.Outlined.Add, "Add")
        }
    }
}

@Composable
fun HomeTopBarAction(currentPager: CurrentPager, viewModel: TodoHomeViewModel) {
    if (currentPager == CurrentPager.HOME_TODO) {
        IconButton(onClick = {
            viewModel.dispatch(TodoHomeViewAction.ChangeRepo)
        }) {
            Icon(Icons.Outlined.ChangeCircle, "切换仓库")
        }
    }
    else if (currentPager == CurrentPager.HOME_ME) {
        IconButton(onClick = {
            viewModel.dispatch(TodoHomeViewAction.Logout)
        }) {
            Icon(Icons.Outlined.Logout, "注销")
        }
    }
}

@Composable
fun HomeBottomBar(viewState: TodoHomeViewState, viewModel: TodoHomeViewModel) {
    BottomAppBar {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable {
                    viewModel.dispatch(TodoHomeViewAction.GoToTodo)
                }
                .fillMaxWidth()
                .weight(1f)) {
            Icon(viewState.homeIcon, "Home")
            Text("首页", color = viewState.homeTextColor)
        }
        Spacer(Modifier.weight(1f, true))
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable {
                    viewModel.dispatch(TodoHomeViewAction.GoToMe)
                }
                .fillMaxWidth()
                .weight(1f)) {
            Icon(viewState.meIcon, "Me")
            Text("我的", color = viewState.meTextColor)
        }
    }
}

@Preview
@Composable
fun PreviewTodoListHome() {
    HomeScreen(navController = rememberNavController(), repoPath = "")
}