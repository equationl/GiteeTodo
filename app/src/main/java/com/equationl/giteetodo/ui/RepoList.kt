package com.equationl.giteetodo.ui

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
import com.equationl.giteetodo.R
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.BlurImage
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.viewmodel.*
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
            LoadRepoListContent(viewModel = viewModel, viewState = viewState)
        }
    }
}

@Composable
fun LoadRepoListContent(viewModel: RepoListViewModel, viewState: RepoListViewState) {
    if (viewState.isLoading) {
        LoadDataContent("正在加载仓库列表中…")
        viewModel.dispatch(RepoListViewAction.LoadRepos)
    }
    else {
        RepoListContent(viewState.repoList, viewModel)  // 读取数据
    }
}

@Composable
fun RepoListContent(repoList: List<RepoItemData>, viewModel: RepoListViewModel) {
    if (repoList.isEmpty()) {
        ListEmptyContent {
            viewModel.dispatch(RepoListViewAction.LoadRepos)
        }
    }
    else {
        Column(
            Modifier
                .fillMaxSize()
                .background(baseBackground)) {
            LazyColumn {
                for (item in repoList) {
                    item(key = item.path) {
                        RepoItem(viewModel, item)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepoItem(viewModel: RepoListViewModel, itemData: RepoItemData) {
    Card(onClick = { viewModel.dispatch(RepoListViewAction.ChoiceARepo(Route.TODO_LIST, itemData.path)) },
        modifier = Modifier.padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)) {
                BlurImage(
                    paint = painterResource(id = R.drawable.bg2),
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
                        val text = if (itemData.notClosedCount > 0) "${itemData.notClosedCount}项未完成" else "已全部完成"
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
                    Text(text = itemData.createDate, fontSize = 10.sp)
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