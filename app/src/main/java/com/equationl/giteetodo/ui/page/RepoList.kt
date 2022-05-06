package com.equationl.giteetodo.ui.page

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.user.model.response.Repos
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.*
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.RepoListViewAction
import com.equationl.giteetodo.viewmodel.RepoListViewEvent
import com.equationl.giteetodo.viewmodel.RepoListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "el, RepoList"

@Composable
fun RepoListScreen(navController: NavHostController) {
    val activity = (LocalContext.current as? Activity)
    val viewModel: RepoListViewModel = viewModel()
    val viewState = viewModel.viewStates
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is RepoListViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is RepoListViewEvent.Goto) {
                println("Goto route=${it.route}")
                navController.navigate(it.route)
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("仓库列表", actions = {
                    IconButton(onClick = {
                        navController.navigate(Route.REPO_DETAIL)
                    }) {
                        Icon(Icons.Outlined.LibraryAdd, "添加仓库")
                    }
                }) {
                    if (navController.backQueue.size <= 2) {
                        Log.i(TAG, "RepoListScreen: 退出1")
                        activity?.finish()
                    }
                    else {
                        navController.popBackStack()
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }})
        {
            RepoListContent(viewState.repoFlow, viewModel, it)
        }
    }
}

@Composable
fun RepoListContent(
    repoFlow: Flow<PagingData<Repos>>,
    viewModel: RepoListViewModel,
    paddingValues: PaddingValues
) {
    val repoList = repoFlow.collectAsLazyPagingItems()

    Log.i(TAG, "RepoListContent: loadState=${repoList.loadState}")

    if (repoList.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(RepoListViewAction.SendMsg("加载错误："+ (repoList.loadState.refresh as LoadState.Error).error.message))
    }

    if (repoList.itemCount < 1) {
        if (repoList.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载仓库列表中…")
        }
        else {
            ListEmptyContent("没有找到仓库数据，点击刷新或点击右上角创建一个仓库") {
                repoList.refresh()
            }
        }
    }
    else {
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(baseBackground)) {
            LazyColumn {
                itemsIndexed(repoList, key = {_, item -> item.fullName}) { _, item ->
                    if (item != null && item.namespace.type == "personal") {  // 仅加载类型为个人的仓库
                        RepoItem(item) {
                            viewModel.dispatch(RepoListViewAction.ChoiceARepo(it))
                        }
                    }
                }

                item {
                    when (repoList.loadState.append) {
                        is LoadState.NotLoading -> {}
                        LoadState.Loading -> {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                Text("加载中")
                            }
                        }
                        is LoadState.Error -> {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                LinkText("加载失败，点击重试") {
                                    repoList.retry()
                                }
                                viewModel.dispatch(RepoListViewAction.SendMsg("加载出错："+ (repoList.loadState.append as LoadState.Error).error.toString()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepoItem(itemData: Repos, onClick: (path: String) -> Unit) {
    Card(onClick = { onClick.invoke(itemData.fullName) },
        modifier = Modifier.padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)) {
                BlurImage(
                    paint = painterResource(R.drawable.bg2),  // 如果使用随机的背景而非固定背景会造成卡顿与闪退（猜测是内存溢出）
                    contentDescription = "background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(modifier = Modifier
                    .fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp), horizontalArrangement = Arrangement.Start) {
                        Text(text = itemData.path, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Row(
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text = if (itemData.openIssuesCount > 0) "${itemData.openIssuesCount}项未完成" else "已全部完成"
                        Text(text = text, color = Color.White)
                    }
                }
            }
            Row(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = itemData.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = Utils.getDateTimeString(itemData.createdAt), fontSize = 10.sp)
                }
            }

        }
    }
}

@Preview
@Composable
fun PreviewRepoList() {
    RepoListScreen(rememberNavController())
}