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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(navController: NavHostController, repoPath: String) {
    val viewModel: TodoHomeViewModel = viewModel()
    val viewState = viewModel.viewStates
    val activity = (LocalContext.current as? Activity)
    val pagerState = rememberPagerState()
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
                HomeBottomBar(viewState, pagerState)
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
                    .padding(bottom = it.calculateBottomPadding())
            ) {
                HomeContent(pagerState, navController, repoPath, scaffoldState)
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page == CurrentPager.HOME_TODO.ordinal) {
                viewModel.dispatch(TodoHomeViewAction.GoToTodo(repoPath))
            }

            if (page == CurrentPager.HOME_ME.ordinal) {
                viewModel.dispatch(TodoHomeViewAction.GoToMe(repoPath))
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeContent(pagerState: PagerState, navController: NavHostController, repoPath: String, scaffoldState: ScaffoldState) {
    HorizontalPager(count = 2,
        state = pagerState
    ) { page ->
        when (page) {
            0 -> TodoListScreen(navController, repoPath, scaffoldState)
            1 -> ProfileScreen(navController, scaffoldState, repoPath)
        }
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeBottomBar(viewState: TodoHomeViewState, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    BottomAppBar {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(CurrentPager.HOME_TODO.ordinal)
                    }
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
                    scope.launch {
                        pagerState.animateScrollToPage(CurrentPager.HOME_ME.ordinal)
                    }
                }
                .fillMaxWidth()
                .weight(1f)) {
            Icon(viewState.meIcon, "Me")
            Text("我的", color = viewState.meTextColor)
        }
    }
}