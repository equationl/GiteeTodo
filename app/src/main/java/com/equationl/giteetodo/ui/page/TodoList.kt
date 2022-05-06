package com.equationl.giteetodo.ui.page

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.viewmodel.*
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch

private const val TAG = "el, TodoListScreen"

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
    val todoList = viewState.todoFlow.collectAsLazyPagingItems()

    Log.i(TAG, "TodoListContent: loadState=${todoList.loadState}")

    if (todoList.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(TodoListViewAction.SendMsg("加载错误："+ (todoList.loadState.refresh as LoadState.Error).error.message))
    }

    if (todoList.itemCount < 1) {
        if (todoList.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载中…")
        }
        else {
            // 筛选类型
            TodoFilterContent(viewState, viewModel)
            ListEmptyContent("还没有数据哦，点击立即刷新\n或点击下方 ”+“ 添加数据；也可以点击右上角切换仓库哦") {
                todoList.refresh()
            }
        }
    }
    else {
        LazyColumn {
            item(key = "headerFilter") {
                // 筛选类型
                TodoFilterContent(viewState, viewModel)
            }

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
            IssueState.CLOSED -> {
                true
            }
            else -> {
                false
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
    // TODO 增加已选中筛选的视觉效果和展开 DropMenu 后的视觉效果

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .padding(end = 32.dp, start = 32.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            viewModel.dispatch(TodoListViewAction.ChangeLabelsDropMenuShowState(true))
        }) {
            Text("标签")
            Icon(Icons.Filled.ArrowDropUp, contentDescription = "标签")
            TodoListLabelDropMenu(viewState.availableLabels, viewModel, viewState)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            viewModel.dispatch(TodoListViewAction.ChangeStateDropMenuShowState(true))
        }) {
            Text("状态")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "状态")
            TodoListStateDropMenu(viewModel = viewModel, isShow = viewState.isShowStateDropMenu)
        }

        val dialogState = rememberMaterialDialogState()
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            dialogState.show()
        }) {
            Text("时间")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "时间")
            TodoListDateTimePicker(dialogState, viewModel)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
            viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(true))
        }) {
            Text("排序")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "排序")
            TodoListDirecDropMenu(viewModel = viewModel, isShow = viewState.isShowDirectionDropMenu)
        }
    }
}

@Composable
fun TodoListLabelDropMenu(options: MutableMap<String, Boolean>, viewModel: TodoListViewModel, viewState: TodoListViewState) {
    DropdownMenu(expanded = viewState.isShowLabelsDropMenu, onDismissRequest = {
        viewModel.dispatch(TodoListViewAction.FilterLabels(options))
    }) {
        options.forEach { (name, checked) ->
            var isChecked by remember { mutableStateOf(checked) }
            DropdownMenuItem(
                onClick = {

                },
            ) {
                Checkbox(checked = isChecked, onCheckedChange = {
                    options[name] = it
                    isChecked = it
                })
                Text(text = name)
            }
        }
    }
}

@Composable
fun TodoListStateDropMenu(viewModel: TodoListViewModel, isShow: Boolean) {
    val options = listOf(IssueState.OPEN, IssueState.CLOSED, IssueState.PROGRESSING, IssueState.REJECTED, IssueState.ALL)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        viewModel.dispatch(TodoListViewAction.ChangeStateDropMenuShowState(false))
    }) {
        options.forEach { state ->
            DropdownMenuItem(
                onClick = {
                    viewModel.dispatch(TodoListViewAction.FilterState(state))
                },
            ) {
                Text(text = state.humanName)
            }
        }
    }
}

@Composable
fun TodoListDirecDropMenu(viewModel: TodoListViewModel, isShow: Boolean) {
    val options = listOf(Direction.DESC, Direction.ASC)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(false))
    }) {
        options.forEach { direction ->
            DropdownMenuItem(
                onClick = {
                    viewModel.dispatch(TodoListViewAction.FilterDirection(direction))
                },
            ) {
                Text(text = direction.humanName)
            }
        }
    }
}

@Composable
fun TodoListDateTimePicker(showState: MaterialDialogState, viewModel: TodoListViewModel) {
    var isStartDate by remember { mutableStateOf(true) }
    val tipText = if (isStartDate) "请选择起始日期" else "请选择结束日期"
    MaterialDialog(
        dialogState = showState,
        buttons = {
            positiveButton("确定") {
                if (isStartDate) {
                    isStartDate = false
                    showState.show()
                }
                else {
                    isStartDate = true
                }
            }
            negativeButton("取消")
        }
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(tipText)
        }
        datepicker { date ->
            viewModel.dispatch(TodoListViewAction.FilterDate(date, isStartDate))
        }
    }
}