package com.equationl.giteetodo.ui.page

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val coroutineScope = rememberCoroutineScope()
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
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is TodoHomeViewEvent.Goto) {
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

    Scaffold(
        topBar = {
             TopBar(
                 isShowSystemBar = viewState.isShowSystemBar,
                 title = viewState.title,
                 currentPager = viewState.currentPage,
                 onChangeRepo = {
                     viewModel.dispatch(TodoHomeViewAction.ChangeRepo)
                 },
                 onLogOut = {
                     viewModel.dispatch(TodoHomeViewAction.Logout)
                 },
                 onExit = {
                     activity?.finish()
                 }
             )
        },
        bottomBar = {
            BottomBar(
                viewState.isShowSystemBar,
                viewState,
                onScrollToHome = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(CurrentPager.HOME_TODO.ordinal)
                    }
                },
                onScrollToMe = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(CurrentPager.HOME_ME.ordinal)
                    }
                }
            )
        },
        floatingActionButton = {
            Fab(
                currentPager = viewState.currentPage,
                isShowSystemBar = viewState.isShowSystemBar,
                onAnimationFinish = {
                    viewModel.dispatch(TodoHomeViewAction.OnAnimateFinish)
                },
                onClickAdd = {
                    viewModel.dispatch(TodoHomeViewAction.AddATodo)
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }}
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colors.baseBackground)
                .fillMaxSize()
                .padding(bottom = it.calculateBottomPadding())
        ) {
            HomeContent(pagerState, navController, repoPath, scaffoldState) { isShow ->
                viewModel.dispatch(TodoHomeViewAction.ChangeSystemBarShowState(isShow))
            }
        }
    }

    LaunchedEffect(pagerState) {
        withContext(Dispatchers.IO) {
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
}

@Composable
fun Fab(
    currentPager: CurrentPager,
    isShowSystemBar: Boolean,
    onAnimationFinish: () -> Unit,
    onClickAdd: () -> Unit
) {
    if (currentPager == CurrentPager.HOME_TODO) {
        Column(modifier = if (isShowSystemBar) Modifier else Modifier.navigationBarsPadding()) {
            HomeFloatActionBar(
                isShowSystemBar = isShowSystemBar,
                onAnimationFinish = onAnimationFinish,
                onAddClick = onClickAdd
            )
        }
    }
}

@Composable
private fun BottomBar(
    isShowSystemBar: Boolean,
    viewState: TodoHomeViewState,
    onScrollToHome: () -> Unit,
    onScrollToMe: () -> Unit,
) {
    AnimatedVisibility(
        visible = isShowSystemBar,
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 50),
            targetOffsetY = { it / 2}
        ),
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 50),
            initialOffsetY = { it / 2}
        )
    ) {
        Column(modifier = if (isShowSystemBar) Modifier.navigationBarsPadding() else Modifier) {
            HomeBottomBar(
                viewState,
                onScrollToHome,
                onScrollToMe
            )
        }
    }
}

@Composable
private fun TopBar(
    isShowSystemBar: Boolean,
    title: String,
    currentPager: CurrentPager,
    onChangeRepo: () -> Unit,
    onLogOut: () -> Unit,
    onExit: () ->Unit
) {
    AnimatedVisibility(
        visible = isShowSystemBar,
        exit = slideOutVertically(animationSpec = tween(durationMillis = 50)),
        enter = slideInVertically(animationSpec = tween(durationMillis = 50))
    ) {
        Column(modifier = if (isShowSystemBar) Modifier.statusBarsPadding() else Modifier) {
            HomeTopBar(
                title = title,
                navigationIcon = Icons.Outlined.Close,
                currentPager = currentPager,
                actions = {
                    HomeTopBarAction(currentPager, onChangeRepo, onLogOut)
                },
                onBack = onExit
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun HomeContent(
    pagerState: PagerState,
    navController: NavHostController,
    repoPath: String,
    scaffoldState: ScaffoldState,
    onChangeSystemBar: (isShow: Boolean) -> Unit
) {
    HorizontalPager(count = 2,
        state = pagerState
    ) { page ->
        when (page) {
            0 -> TodoListScreen(navController, repoPath, scaffoldState, isShowSystemBar = onChangeSystemBar)
            1 -> ProfileScreen(navController, scaffoldState, repoPath)
        }
    }
}

@Composable
private fun HomeFloatActionBar(
    isShowSystemBar: Boolean,
    onAnimationFinish: () -> Unit,
    onAddClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val offsetValue by animateIntAsState(
        targetValue = if (isShowSystemBar) 0 else screenWidth / 2 - 32 - 16,
        animationSpec = spring(0.3f),
        finishedListener = {
            // 应该使用进入或退出全屏时耗时最长动画的完成事件作为当前动画已全部完成的标志
            // 目前来说，耗时最长的就是这个悬浮按钮的位移动画，所以这里使用它来标记动画完成
            onAnimationFinish()
        }
    )

    FloatingActionButton(
        onClick = onAddClick,
        modifier = Modifier.offset(offsetValue.dp, 0.dp)
    ) {
        Icon(Icons.Outlined.Add, "Add")
    }
}

@Composable
private fun HomeTopBarAction(
    currentPager: CurrentPager,
    onChangeRepo: () -> Unit,
    onLogOut: () -> Unit
) {
    if (currentPager == CurrentPager.HOME_TODO) {
        IconButton(onClick = onChangeRepo) {
            Icon(Icons.Outlined.ChangeCircle, "切换仓库")
        }
    }
    else if (currentPager == CurrentPager.HOME_ME) {
        IconButton(onClick = onLogOut) {
            // fixme 注销后应该清空返回栈，不然在登录页面按返回按键又会返回到主页
            Icon(Icons.Outlined.Logout, "注销")
        }
    }
}

@Composable
private fun HomeBottomBar(
    viewState: TodoHomeViewState,
    onScrollToHome: () -> Unit,
    onScrollToMe: () -> Unit,
) {
    BottomAppBar {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(onClick = onScrollToHome)
                .fillMaxWidth()
                .weight(1f)) {
            Icon(viewState.homeIcon, "首页", tint = viewState.homeTextColor)
            Text("首页", color = viewState.homeTextColor)
        }
        Spacer(Modifier.weight(1f, true))
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable(onClick = onScrollToMe)
                .fillMaxWidth()
                .weight(1f)) {
            Icon(viewState.meIcon, "我的", tint = viewState.meTextColor)
            Text("我的", color = viewState.meTextColor)
        }
    }
}