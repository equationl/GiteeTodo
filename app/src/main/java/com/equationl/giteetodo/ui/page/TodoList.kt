package com.equationl.giteetodo.ui.page

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.equationl.giteetodo.R
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.*
import com.equationl.giteetodo.viewmodel.*
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch

private const val TAG = "el, TodoListScreen"

@Composable
fun TodoListScreen(
    navController: NavHostController,
    repoPath: String,
    scaffoldState: ScaffoldState,
    viewModel: TodoListViewModel = hiltViewModel(),
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
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

    Column(Modifier.fillMaxSize()) {
        TodoListContent(viewState, viewModel, repoPath, navController, isShowSystemBar)
    }
}

@Composable
fun TodoListContent(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    repoPath: String,
    navController: NavHostController,
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
    val todoPagingItems = viewState.todoFlow.collectAsLazyPagingItems()
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (todoPagingItems.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(TodoListViewAction.SendMsg("加载错误："+ (todoPagingItems.loadState.refresh as LoadState.Error).error.message))
    }

    if (todoPagingItems.itemCount < 1) {
        if (todoPagingItems.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载中…")
        }
        else {
            // 筛选类型
            TodoFilterContent(viewState, viewModel)
            ListEmptyContent("还没有数据哦，点击立即刷新\n或点击下方 ”+“ 添加数据；也可以点击右上角切换仓库哦") {
                todoPagingItems.refresh()
            }
        }
    }
    else {
        rememberSwipeRefreshState.isRefreshing = (todoPagingItems.loadState.refresh is LoadState.Loading)

        SwipeRefresh(
            state = rememberSwipeRefreshState,
            onRefresh = {
                todoPagingItems.refresh()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            TodoListLazyColumn(viewState, viewModel, todoPagingItems, navController, repoPath, rememberSwipeRefreshState.isRefreshing, isShowSystemBar)
        }
    }
}

@Composable
fun TodoListLazyColumn(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    todoPagingItems: LazyPagingItems<TodoCardData>,
    navController: NavHostController,
    repoPath: String,
    isLoading: Boolean,
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    // fixme 如果拉到底后往回拉一点再尝试拉到底会导致“反复横跳”
    isShowSystemBar(listState.isScrollingUp())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        item(key = "headerFilter") {
            // 筛选类型
            TodoFilterContent(viewState, viewModel)
        }

        /*todoPagingItems.itemSnapshotList.forEach {
            if (it != null) {
                stickyHeader {
                    Text(text = it.cardTitle)
                }

                item {
                    TodoCardScreen(it, navController, viewModel, repoPath, isLoading)
                }
            }
        }*/

        itemsIndexed(todoPagingItems, key = { _, item -> item.cardTitle+item.itemArray.toString()}) { _, item ->
            if (item != null) {
                TodoCardScreen(item, navController, viewModel, repoPath, isLoading)
            }
        }

        item {
            when (todoPagingItems.loadState.append) {
                is LoadState.NotLoading -> {}
                LoadState.Loading -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Text("加载中")
                    }
                }
                is LoadState.Error -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        LinkText("加载失败，点击重试") {
                            todoPagingItems.retry()
                        }
                        viewModel.dispatch(TodoListViewAction.SendMsg("加载出错："+ (todoPagingItems.loadState.append as LoadState.Error).error.toString()))
                    }
                }
            }
        }
    }
}

@Composable
fun TodoCardScreen(data: TodoCardData, navController: NavHostController, viewModel: TodoListViewModel, repoPath: String, isLoading: Boolean) {
    Card(modifier = Modifier
        .heightIn(20.dp, Int.MAX_VALUE.dp)
        .padding(32.dp)
        .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column(Modifier.padding(8.dp)) {
            Text(text = data.cardTitle, Modifier.padding(8.dp))

            Column(Modifier.fillMaxSize()) {
                data.itemArray.forEach {
                    TodoItem(navController, it, viewModel, repoPath)
                }
            }
            /*LazyColumn(modifier = Modifier.fillMaxWidth()) {
                data.itemArray.forEach {
                    item(key = it.number) {
                        TodoItem(navController, it, viewModel, repoPath)
                    }
                }
            }*/
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.noRippleClickable {
                navController.navigate("${Route.TODO_DETAIL}/${itemData.number}")
            }
        )
    }
}

@Composable
fun TodoFilterContent(viewState: TodoListViewState, viewModel: TodoListViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .padding(end = 32.dp, start = 32.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeLabelsDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowLabelsDropMenu) -180f else 0f)
            Text("标签", color = if (viewState.filteredOptionList.contains(FilteredOption.Labels)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "标签", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListLabelDropMenu(viewState.availableLabels, viewModel, viewState)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeStateDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowStateDropMenu) -180f else 0f)
            Text("状态", color = if (viewState.filteredOptionList.contains(FilteredOption.States)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "状态", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListStateDropMenu(viewModel = viewModel, isShow = viewState.isShowStateDropMenu)
        }

        val dialogState = rememberMaterialDialogState()
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            dialogState.show()
        }) {
            Text("时间", color = if (viewState.filteredOptionList.contains(FilteredOption.DateTime)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "时间")
            TodoListDateTimePicker(dialogState, viewModel)
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowDirectionDropMenu) -180f else 0f)
            Text("排序", color = if (viewState.filteredOptionList.contains(FilteredOption.Direction)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "排序", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListDirecDropMenu(viewModel = viewModel, isShow = viewState.isShowDirectionDropMenu)
        }

        if (viewState.filteredOptionList.isNotEmpty()) {
            Icon(painter = painterResource(id = R.drawable.filter_alt_off) , contentDescription = "清除",
                modifier = Modifier.noRippleClickable {
                    viewModel.dispatch(TodoListViewAction.ClearFilter)
                }
            )
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
                    isChecked = !isChecked
                    options[name] = isChecked
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
        datepicker(title = tipText) { date ->
            viewModel.dispatch(TodoListViewAction.FilterDate(date, isStartDate))
        }
    }
}