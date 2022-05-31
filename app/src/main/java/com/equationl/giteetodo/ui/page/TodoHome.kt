package com.equationl.giteetodo.ui.page

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.theme.systemBar
import com.equationl.giteetodo.ui.widgets.HomeTopBar
import com.equationl.giteetodo.viewmodel.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

private const val TAG = "el, TodoHome"

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    repoPath: String,
    viewModel: TodoHomeViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val activity = (LocalContext.current as? Activity)
    val pagerState = rememberPagerState()
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()
    val systemUiCtrl = rememberSystemUiController()
    val systemBarColor = MaterialTheme.colors.systemBar

    DisposableEffect(Unit) {
        onDispose {
            systemUiCtrl.setSystemBarsColor(systemBarColor)
            systemUiCtrl.systemBarsDarkContentEnabled = false
        }
    }

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

    BackHandler { // 在主页点击返回按键一律按退出程序处理
        activity?.finish()
    }

    if (viewState.isShowSystemBar) {
        systemUiCtrl.setSystemBarsColor(systemBarColor)
        systemUiCtrl.systemBarsDarkContentEnabled = false
    }
    else {
        systemUiCtrl.setSystemBarsColor(Color.Transparent)
        systemUiCtrl.systemBarsDarkContentEnabled = true
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = viewState.isShowSystemBar,
                    exit = slideOutVertically(),
                    enter = slideInVertically()
                ) {
                    Column(modifier = if (viewState.isShowSystemBar) Modifier.statusBarsPadding() else Modifier) {
                        HomeTopBar(viewState.title,
                            navigationIcon = Icons.Outlined.Close,
                            currentPager = viewState.currentPage,
                            actions = {
                                HomeTopBarAction(viewState.currentPage, viewModel)
                            }) {
                            // 点击退出
                            activity?.finish()
                        }
                    }
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = viewState.isShowSystemBar,
                    exit = slideOutVertically(targetOffsetY = { it / 2}),
                    enter = slideInVertically(initialOffsetY = { it / 2})
                ) {
                    Column(modifier = if (viewState.isShowSystemBar) Modifier.navigationBarsPadding() else Modifier) {
                        HomeBottomBar(viewState, pagerState)
                    }
                }
            },
            floatingActionButton = {
                if (viewState.currentPage == CurrentPager.HOME_TODO) {
                    Column(modifier =
                    if (viewState.isShowSystemBar) Modifier else Modifier.navigationBarsPadding()
                    ) {
                        HomeFloatActionBar(
                            viewState.isShowSystemBar,
                        ) { viewModel.dispatch(TodoHomeViewAction.AddATodo) }
                    }
                }
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
                HomeContent(pagerState, navController, repoPath, scaffoldState, viewModel)
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
fun HomeContent(
    pagerState: PagerState,
    navController: NavHostController,
    repoPath: String,
    scaffoldState: ScaffoldState,
    viewModel: TodoHomeViewModel
) {
    HorizontalPager(count = 2,
        state = pagerState
    ) { page ->
        when (page) {
            0 -> TodoListScreen(navController, repoPath, scaffoldState) {
                viewModel.dispatch(TodoHomeViewAction.ChangeSystemBarShowState(it))
            }
            1 -> ProfileScreen(navController, scaffoldState, repoPath)
        }
    }
}

@Composable
fun HomeFloatActionBar(
    isShowSystemBar: Boolean,
    onAddClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val offsetValue by animateIntAsState(
        targetValue = if (isShowSystemBar) 0 else screenWidth / 2 - 32 - 16,
        animationSpec = spring(0.3f)
    )

    FloatingActionButton(
        onClick = onAddClick,
        modifier = Modifier.offset(offsetValue.dp, 0.dp)
    ) {
        Icon(Icons.Outlined.Add, "Add")
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
            Icon(viewState.homeIcon, "首页", tint = viewState.homeTextColor)
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
            Icon(viewState.meIcon, "我的", tint = viewState.meTextColor)
            Text("我的", color = viewState.meTextColor)
        }
    }
}