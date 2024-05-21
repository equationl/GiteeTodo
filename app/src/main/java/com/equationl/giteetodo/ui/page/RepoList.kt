package com.equationl.giteetodo.ui.page

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.user.model.response.Repo
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.BlurImage
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.RepoListViewAction
import com.equationl.giteetodo.viewmodel.RepoListViewEvent
import com.equationl.giteetodo.viewmodel.RepoListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "el, RepoList"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    viewModel: RepoListViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val activity = (LocalContext.current as? Activity)
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            when (it) {
                is RepoListViewEvent.ShowMessage -> {
                    coroutineState.launch {
                        scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                    }
                }

                is RepoListViewEvent.Goto -> {
                    navController.navigate(it.route)
                }

                is RepoListViewEvent.ShowDeleteRepoMsg -> {
                    coroutineState.launch {
                        when (scaffoldState.snackbarHostState.showSnackbar(
                            message = it.message,
                            actionLabel = "前往删除"
                        )) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                uriHandler.openUri(it.deleteUrl)
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar("仓库列表", actions = {
                IconButton(onClick = {
                    navController.navigate(Route.REPO_DETAIL)
                }) {
                    Icon(Icons.Outlined.LibraryAdd, "添加仓库")
                }
            }) {

                // fixme need confirm this code
                val backStackList = navController.currentBackStack.value
                val lastQueue = backStackList[backStackList.size - 2]
                if (lastQueue.destination.route?.contains(Route.HOME) == true) { // 只有从首页跳转过来的才返回，否则直接退出
                    navController.popBackStack()
                } else {
                    activity?.finish()
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        })
    {
        RepoListContent(viewState.repoFlow, viewModel, it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListContent(
    repoFlow: Flow<PagingData<Repo>>,
    viewModel: RepoListViewModel,
    paddingValues: PaddingValues
) {
    val repoList = repoFlow.collectAsLazyPagingItems()

    Log.i(TAG, "RepoListContent: loadState=${repoList.loadState}")

    if (repoList.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(RepoListViewAction.SendMsg("加载错误：" + (repoList.loadState.refresh as LoadState.Error).error.message))
    }

    if (repoList.itemCount < 1) {
        if (repoList.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载仓库列表中…")
        } else {
            ListEmptyContent("没有找到仓库数据，点击刷新或点击右上角创建一个仓库") {
                repoList.refresh()
            }
        }
    } else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn {
                items(count = repoList.itemCount, key = repoList.itemKey { it.fullName }) {index ->
                    val item = repoList[index]
                    if (item != null && item.namespace.type == "personal") {  // 仅加载类型为个人的仓库
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.dispatch(RepoListViewAction.DeleteRepo(item.fullName))
                                }
                                false
                            },
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Card(
                                    modifier = Modifier.padding(32.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                            .background(MaterialTheme.colorScheme.error),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "继续滑动删除",
                                            color = MaterialTheme.colorScheme.background
                                        )
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "删除",
                                            tint = MaterialTheme.colorScheme.background
                                        )
                                    }
                                }
                            },
                            content = {
                                RepoItem(item) {
                                    viewModel.dispatch(RepoListViewAction.ChoiceARepo(it))
                                }
                            },
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                        )
                    }
                }

                item {
                    when (repoList.loadState.append) {
                        is LoadState.NotLoading -> {}
                        LoadState.Loading -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("加载中")
                            }
                        }

                        is LoadState.Error -> {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                LinkText("加载失败，点击重试") {
                                    repoList.retry()
                                }
                                viewModel.dispatch(RepoListViewAction.SendMsg("加载出错：" + (repoList.loadState.append as LoadState.Error).error.toString()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoItem(itemData: Repo, onClick: (path: String) -> Unit) {
    Card(
        onClick = { onClick.invoke(itemData.fullName) },
        modifier = Modifier.padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                BlurImage(
                    paint = painterResource(R.drawable.bg2),  // 如果使用随机的背景而非固定背景会造成卡顿与闪退（猜测是内存溢出）
                    contentDescription = "background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp), horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = itemData.path,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text =
                            if (itemData.openIssuesCount > 0) "${itemData.openIssuesCount}项未完成" else "已全部完成"
                        Text(text = text, color = Color.White)
                    }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = itemData.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = Utils.getDateTimeString(itemData.createdAt), fontSize = 10.sp)
                }
            }

        }
    }
}