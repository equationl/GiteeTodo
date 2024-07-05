package com.equationl.giteetodo.ui.page

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.BottomAppBarScrollBehavior
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
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

@Suppress("unused")
private const val TAG = "el, TodoHome"

@OptIn(ExperimentalMaterial3Api::class)
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

    var bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    var topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // 通过使用不同的 scrollBehavior 来确保状态恢复
    if (viewState.currentPage != CurrentPager.HOME_TODO) {
        bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
        topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    }

    val isBottomBarCollapsed by remember {
        derivedStateOf {
            bottomAppBarScrollBehavior.state.collapsedFraction == 0f
        }
    }

    LaunchedEffect(key1 = isBottomBarCollapsed) {
        viewModel.dispatch(TodoHomeViewAction.ChangeSystemBarShowState(isBottomBarCollapsed))
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoHomeViewEvent.ShowMessage) {
                coroutineScope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            } else if (it is TodoHomeViewEvent.Goto) {
                if (it.isClrStack) {
                    navController.navigate(it.route) {
                        popUpTo(0)
                    }
                } else {
                    navController.navigate(it.route)
                }
            }
        }
    }

    BackHandler { // 在主页点击返回按键一律按退出程序处理
        activity?.finish()
    }

    Scaffold(
        topBar = {
            TopBar(
                title = viewState.title,
                currentPager = viewState.currentPage,
                scrollBehavior = topAppBarScrollBehavior,
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
                viewState = viewState,
                scrollBehavior = bottomAppBarScrollBehavior,
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
            HomeContent(
                pagerState = pagerState,
                repoPath = repoPath,
                scaffoldState = scaffoldState,
                topAppBarScrollBehavior = topAppBarScrollBehavior,
                bottomBarScrollBehavior = bottomAppBarScrollBehavior
            )
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
    onClickAdd: () -> Unit
) {
    if (currentPager == CurrentPager.HOME_TODO) {
        HomeFloatAction(
            isShowSystemBar = isShowSystemBar,
            onAddClick = onClickAdd
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    viewState: TodoHomeViewState,
    scrollBehavior: BottomAppBarScrollBehavior,
    onScrollToHome: () -> Unit,
    onScrollToMe: () -> Unit,
    onAddATodo: () -> Unit
) {
    HomeBottomBar(
        viewState,
        scrollBehavior,
        onScrollToHome,
        onScrollToMe,
        onAddATodo
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    currentPager: CurrentPager,
    scrollBehavior: TopAppBarScrollBehavior,
    onChangeRepo: () -> Unit,
    onLogOut: () -> Unit,
    onExit: () -> Unit
) {
    HomeTopBar(
        title = title,
        navigationIcon = Icons.Outlined.Close,
        currentPager = currentPager,
        scrollBehavior = scrollBehavior,
        actions = {
            HomeTopBarAction(currentPager, onChangeRepo, onLogOut)
        },
        onBack = onExit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    pagerState: PagerState,
    repoPath: String,
    scaffoldState: BottomSheetScaffoldState,
    topAppBarScrollBehavior: TopAppBarScrollBehavior,
    bottomBarScrollBehavior: BottomAppBarScrollBehavior,
) {
    HorizontalPager(
        state = pagerState
    ) { page ->
        when (page) {
            0 -> TodoListScreen(
                repoPath,
                scaffoldState,
                topAppBarScrollBehavior = topAppBarScrollBehavior,
                bottomBarScrollBehavior = bottomBarScrollBehavior,
            )

            1 -> ProfileScreen(scaffoldState, repoPath)
        }
    }
}

@Composable
private fun HomeFloatAction(
    isShowSystemBar: Boolean,
    onAddClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val offsetValue by animateIntAsState(
        targetValue = if (isShowSystemBar) 0 else screenWidth / 2 - 32 - 16,
        animationSpec = spring(),
        label = "homeFabOffset"
    )

    FloatingActionButton(
        onClick = onAddClick,
        modifier = Modifier.offset { IntOffset(offsetValue.dp.roundToPx(), 0) }
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
            Icon(Icons.AutoMirrored.Outlined.Logout, "注销")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeBottomBar(
    viewState: TodoHomeViewState,
    scrollBehavior: BottomAppBarScrollBehavior,
    onScrollToHome: () -> Unit,
    onScrollToMe: () -> Unit,
    onAddATodo: () -> Unit
) {
    BottomAppBar(
        scrollBehavior = scrollBehavior,
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