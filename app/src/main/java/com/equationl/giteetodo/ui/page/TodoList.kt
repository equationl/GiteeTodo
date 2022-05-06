package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.viewmodel.*
import kotlinx.coroutines.launch

@Composable
fun TodoListScreen(navController: NavHostController, repoPath: String, scaffoldState: ScaffoldState) {
    val viewModel: TodoListViewModel = viewModel()
    val viewState = viewModel.viewStates
    val coroutineState = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.dispatch(TodoListViewAction.SetRepoPath(repoPath))
        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoListViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    TodoListContent(viewState, viewModel, repoPath, navController)
}

@Composable
fun TodoListContent(viewState: TodoListViewState, viewModel: TodoListViewModel, repoPath: String, navController: NavHostController) {
    // 筛选类型（只显示未完成、筛选排序规则等）
    TodoFilterContent(viewState, viewModel)

    val todoList = viewState.todoFlow.collectAsLazyPagingItems()

    if (todoList.itemCount < 1) {
        if (todoList.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载中…")
        }
        else {
            ListEmptyContent("还没有数据哦，点击立即刷新\n或点击下方 ”+“ 添加数据；也可以点击右上角切换仓库哦") {
                todoList.refresh()
            }
            if (todoList.loadState.refresh is LoadState.Error) {
                viewModel.dispatch(TodoListViewAction.SendMsg("加载错误："+ (todoList.loadState.refresh as LoadState.Error).error.message))
            }
        }
    }
    else {
        LazyColumn {
            itemsIndexed(todoList, key = {_, item -> item.itemArray.toString()}) { _, item ->
                if (item != null) {
                    TodoCardScreen(item, navController, viewModel, repoPath)
                }
            }

            item {
                when (todoList.loadState.append) {
                    is LoadState.NotLoading -> {}
                    LoadState.Loading -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Text("加载中")
                        }
                    }
                    is LoadState.Error -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            LinkText("加载失败，点击重试") {
                                todoList.retry()
                            }
                            viewModel.dispatch(TodoListViewAction.SendMsg("加载出错："+ (todoList.loadState.append as LoadState.Error).error.toString()))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoCardScreen(data: TodoCardData, navController: NavHostController, viewModel: TodoListViewModel, repoPath: String) {
    Card(modifier = Modifier
        .heightIn(20.dp, 800.dp)
        .padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column(Modifier.padding(8.dp)) {
            Text(text = data.createDate, Modifier.padding(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                data.itemArray.forEach {
                    item(key = it.number) {
                        TodoItem(navController, it, viewModel, repoPath)
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(navController: NavHostController, itemData: TodoCardItemData, viewModel: TodoListViewModel, repoPath: String) {
    var checked by remember { mutableStateOf(
        when (itemData.state) {
            IssueState.OPEN,
            IssueState.PROGRESSING,
            IssueState.REJECTED,
            IssueState.UNKNOWN -> {
                false
            }
            IssueState.CLOSED -> {
                true
            }
        }
    ) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
        Checkbox(checked = checked,
            enabled = itemData.state != IssueState.REJECTED,
            onCheckedChange = {
                checked = it
                viewModel.dispatch(TodoListViewAction.UpdateIssueState(itemData.number, it, repoPath))
        })
        Text(
            text = itemData.title,
            textDecoration = if (itemData.state == IssueState.REJECTED) TextDecoration.LineThrough else null,
        modifier = Modifier.clickable {
            navController.navigate("${Route.TODO_DETAIL}/${itemData.number}")
        })
    }
}

@Composable
fun TodoFilterContent(viewState: TodoListViewState, viewModel: TodoListViewModel) {
    // TODO 增加按标签筛选、按状态（已完成、已拒绝、进行中等）筛选、排序方式（正序、倒序）

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
        Checkbox(checked = viewState.isFitterOnlyOpen,
            onCheckedChange = {
                viewModel.dispatch(TodoListViewAction.CheckFitterOnlyOpen(it))
            })
        Text(text = "仅显示未完成")
    }
}