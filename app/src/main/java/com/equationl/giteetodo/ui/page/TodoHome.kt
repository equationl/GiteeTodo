package com.equationl.giteetodo.ui.page

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChangeCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.widgets.HomeTopBar
import com.equationl.giteetodo.viewmodel.CurrentPager
import com.equationl.giteetodo.viewmodel.TodoHomeViewAction
import com.equationl.giteetodo.viewmodel.TodoHomeViewEvent
import com.equationl.giteetodo.viewmodel.TodoHomeViewModel
import com.equationl.giteetodo.viewmodel.TodoHomeViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "el, TodoHome"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    repoPath: String,
    viewModel: TodoHomeViewModel = hiltViewModel(),
) {
    val viewState = viewModel.viewStates
    val activity = (LocalContext.current as? Activity)
    val navController = LocalNavController.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoHomeViewEvent.ShowMessage) {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            } else if (it is TodoHomeViewEvent.Goto) {
                navController.navigate(it.route)
            }
        }
    }

    BackHandler { // 在主页点击返回按键一律按退出程序处理
        activity?.finish()
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
                },
                onAnimationFinish = {
                    viewModel.dispatch(TodoHomeViewAction.OnAnimateFinish)
                },
                onAddATodo = {
                    viewModel.dispatch(TodoHomeViewAction.AddATodo)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        },
        floatingActionButton = {
            if (!viewState.isShowSystemBar) {
                Fab(
                    currentPager = viewState.currentPage,
                    isShowSystemBar = false,
                    onAnimationFinish = {
                        viewModel.dispatch(TodoHomeViewAction.OnAnimateFinish)
                    },
                    onClickAdd = {
                        viewModel.dispatch(TodoHomeViewAction.AddATodo)
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(it)
        ) {
            HomeContent(pagerState, repoPath, scaffoldState) { isShow ->
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
    onAnimationFinish: () -> Unit,
    onAddATodo: () -> Unit
) {
    AnimatedVisibility(
        visible = isShowSystemBar,
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 50),
            targetOffsetY = { it / 2 }
        ),
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 50),
            initialOffsetY = { it / 2 }
        )
    ) {
        Column(modifier = if (isShowSystemBar) Modifier.navigationBarsPadding() else Modifier) {
            HomeBottomBar(
                viewState,
                onScrollToHome,
                onScrollToMe,
                onAnimationFinish,
                onAddATodo
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    pagerState: PagerState,
    repoPath: String,
    scaffoldState: BottomSheetScaffoldState,
    onChangeSystemBar: (isShow: Boolean) -> Unit,
) {
    HorizontalPager(
        state = pagerState
    ) { page ->
        when (page) {
            0 -> TodoListScreen(
                repoPath,
                scaffoldState,
                isShowSystemBar = onChangeSystemBar
            )

            1 -> ProfileScreen(scaffoldState, repoPath)
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
            Log.i(TAG, "HomeFloatActionBar: animation finish ")
            // 应该使用进入或退出全屏时耗时最长动画的完成事件作为当前动画已全部完成的标志
            // 目前来说，耗时最长的就是这个悬浮按钮的位移动画，所以这里使用它来标记动画完成
            onAnimationFinish()
        },
        label = "homeFabOffset"
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
    } else if (currentPager == CurrentPager.HOME_ME) {
        IconButton(onClick = onLogOut) {
            // fixme 注销后应该清空返回栈，不然在登录页面按返回按键又会返回到主页
            Icon(Icons.AutoMirrored.Outlined.Logout, "注销")
        }
    }
}

@Composable
private fun HomeBottomBar(
    viewState: TodoHomeViewState,
    onScrollToHome: () -> Unit,
    onScrollToMe: () -> Unit,
    onAnimationFinish: () -> Unit,
    onAddATodo: () -> Unit
) {
    BottomAppBar(
        actions = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = onScrollToHome)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Icon(
                    if (viewState.currentPage == CurrentPager.HOME_TODO) Icons.Filled.Home else Icons.Outlined.Home,
                    "首页",
                    tint = if (viewState.currentPage == CurrentPager.HOME_TODO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "首页",
                    color = if (viewState.currentPage == CurrentPager.HOME_TODO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.weight(1f, true))

            if (viewState.isShowSystemBar) {
                Fab(
                    currentPager = viewState.currentPage,
                    isShowSystemBar = true,
                    onAnimationFinish = onAnimationFinish,
                    onClickAdd = onAddATodo
                )
            }

            Spacer(Modifier.weight(1f, true))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable(onClick = onScrollToMe)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Icon(
                    if (viewState.currentPage == CurrentPager.HOME_ME) Icons.Filled.Person else Icons.Outlined.Person,
                    "我的",
                    tint = if (viewState.currentPage == CurrentPager.HOME_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "我的",
                    color = if (viewState.currentPage == CurrentPager.HOME_ME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}