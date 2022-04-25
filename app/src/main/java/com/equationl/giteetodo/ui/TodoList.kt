package com.equationl.giteetodo.ui

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
import com.equationl.giteetodo.ui.common.Route
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
        viewModel.dispatch(TodoListViewAction.LoadIssues(repoPath))
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

    if (viewState.isLoading) {
        LoadDataContent("加载数据中…")
    }
    else {
        TodoListContent(viewState, viewModel, repoPath, navController)
    }
}

@Composable
fun TodoListContent(viewState: TodoListViewState, viewModel: TodoListViewModel, repoPath: String, navController: NavHostController) {
    // 筛选类型（只显示未完成、筛选排序规则等）
    TodoFilterContent(viewState, viewModel, repoPath)

    if (viewState.todoList.isEmpty()) {
        ListEmptyContent("还没有数据哦，点击立即刷新\n或点击下方 ”+“ 添加数据；也可以点击右上角切换仓库哦") {
            viewModel.dispatch(TodoListViewAction.LoadIssues(repoPath))
        }
    }
    else {
        LazyColumn {
            viewState.todoList.forEach{
                item(it.createDate) {
                    TodoCardScreen(it, navController, viewModel, repoPath)
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
            IssueState.REJECTED -> {
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
fun TodoFilterContent(viewState: TodoListViewState, viewModel: TodoListViewModel, repoPath: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(end = 8.dp)) {
        Checkbox(checked = viewState.isFitterOnlyOpen,
            onCheckedChange = {
                viewModel.dispatch(TodoListViewAction.CheckFitterOnlyOpen(it))
                viewModel.dispatch(TodoListViewAction.LoadIssues(repoPath))
            })
        Text(text = "仅显示未完成")
    }
}