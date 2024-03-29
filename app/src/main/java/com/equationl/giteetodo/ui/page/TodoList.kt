package com.equationl.giteetodo.ui.page

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.repos.model.common.TodoShowData
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.theme.baseBackground
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    val todoPagingItems = viewState.todoFlow.collectAsLazyPagingItems()

    DisposableEffect(Unit) {
        viewModel.dispatch(TodoListViewAction.Init(repoPath))

        onDispose {
            viewModel.dispatch(TodoListViewAction.OnExit)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoListViewEvent.ShowMessage) {
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        TodoListContent(
            viewState,
            viewModel,
            repoPath,
            navController,
            todoPagingItems,
            coroutineState,
            isShowSystemBar
        )
    }
}

@Composable
fun TodoListContent(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    repoPath: String,
    navController: NavHostController,
    todoPagingItems: LazyPagingItems<TodoShowData>,
    coroutineState: CoroutineScope,
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
    val rememberSwipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    if (todoPagingItems.loadState.refresh is LoadState.Error) {
        viewModel.dispatch(TodoListViewAction.SendMsg("加载错误："+ (todoPagingItems.loadState.refresh as LoadState.Error).error.message))
    }

    if (todoPagingItems.itemCount < 1) {
        @Suppress("ControlFlowWithEmptyBody")
        if (todoPagingItems.loadState.refresh == LoadState.Loading) {
            LoadDataContent("正在加载中…")
        }
        else if (todoPagingItems.loadState.mediator == null ||
            (todoPagingItems.loadState.mediator != null &&
                    todoPagingItems.loadState.source.refresh  == LoadState.Loading)
        ) {

        }
        else {
            if (viewState.isAutoRefresh) {
                // 筛选类型
                TodoFilterContent(viewState, viewModel) {
                    coroutineState.launch(Dispatchers.IO) {
                        delay(100)
                        todoPagingItems.refresh()
                    }
                }
                ListEmptyContent("还没有数据哦，点击立即刷新", "TIPS： 点击下方 “+” 可以添加你的第一条数据哦\n也可以尝试更换仓库或筛选条件再试试") {
                    todoPagingItems.refresh()
                }
            }
            else {
                viewModel.dispatch(TodoListViewAction.AutoRefreshFinish)
                todoPagingItems.refresh() // 如果数据为空则先尝试刷新一下
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
            TodoListLazyColumn(
                viewState,
                viewModel,
                todoPagingItems,
                navController,
                repoPath,
                rememberSwipeRefreshState.isRefreshing,
                coroutineState,
                isShowSystemBar
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoListLazyColumn(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    todoPagingItems: LazyPagingItems<TodoShowData>,
    navController: NavHostController,
    repoPath: String,
    isLoading: Boolean,
    coroutineState: CoroutineScope,
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
    val listState = rememberLazyListState()

    isShowSystemBar(listState.isScrollingUp())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 2.dp),
        state = listState
    ) {
        item(key = "headerFilter") {
            // 筛选类型
            TodoFilterContent(viewState, viewModel) {
                coroutineState.launch(Dispatchers.IO) {
                    // 需要延迟一段时间等待新的对象创建完成才能成功刷新
                    delay(100)
                    todoPagingItems.refresh()
                }
            }
        }

        var lastTitle = ""

        todoPagingItems.itemSnapshotList.forEach {
            if (it != null) {
                if (it.headerTitle != lastTitle) {
                    stickyHeader {
                        TodoListGroupHeader(text = it.headerTitle, isLoading)
                    }
                    lastTitle = it.headerTitle
                }

                item(key = it.toString()) {
                    TodoItem(
                        navController = navController,
                        itemData = it,
                        viewModel = viewModel,
                        repoPath = repoPath,
                        isLoading
                    )
                }
            }
        }

        item(key = "loading") {
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
fun TodoListGroupHeader(text: String, isLoading: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.baseBackground)
            .padding(start = 12.dp)
            .padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
        )
    }
}

@Composable
fun TodoItem(
    navController: NavHostController,
    itemData: TodoShowData,
    viewModel: TodoListViewModel,
    repoPath: String,
    isLoading: Boolean
) {
    var checked by remember {
        mutableStateOf(
            when (itemData.state) {
                IssueState.CLOSED -> true
                else -> false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 2.dp)
            .background(MaterialTheme.colors.baseBackground)
            .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()),
        shape = RoundedCornerShape(4.dp),
        elevation = 2.dp
    ) {
        Column(Modifier.padding(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Checkbox(checked = checked,
                    enabled = itemData.state != IssueState.REJECTED,
                    onCheckedChange = {
                        checked = it
                        viewModel.dispatch(
                            TodoListViewAction.UpdateIssueState(
                                itemData.number,
                                it,
                                repoPath
                            )
                        )
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
    }
}

@Composable
fun TodoFilterContent(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    onRefresh: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
        .fillMaxWidth()
        .padding(end = 32.dp, start = 32.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeLabelsDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowLabelsDropMenu) -180f else 0f)
            Text("标签", color = if (viewState.filteredOptionList.contains(FilteredOption.Labels)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "标签", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListLabelDropMenu(
                options = viewState.availableLabels,
                isShowDropMenu = viewState.isShowLabelsDropMenu,
                onFilterLabels = {
                    viewModel.dispatch(TodoListViewAction.FilterLabels(it))
                    onRefresh()
                }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeStateDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowStateDropMenu) -180f else 0f)
            Text("状态", color = if (viewState.filteredOptionList.contains(FilteredOption.States)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "状态", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListStateDropMenu(
                isShow = viewState.isShowStateDropMenu,
                changeShowState = {viewModel.dispatch(TodoListViewAction.ChangeStateDropMenuShowState(false))},
                onFilterState = {
                    viewModel.dispatch(TodoListViewAction.FilterState(it))
                    onRefresh()
                }
            )
        }

        val dialogState = rememberMaterialDialogState()
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            dialogState.show()
        }) {
            Text("时间", color = if (viewState.filteredOptionList.contains(FilteredOption.DateTime)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "时间")
            TodoListDateTimePicker(
                dialogState
            ) { date, isStartDate ->
                viewModel.dispatch(TodoListViewAction.FilterDate(date, isStartDate))
                if (!isStartDate) {
                    onRefresh()
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowDirectionDropMenu) -180f else 0f)
            Text("排序", color = if (viewState.filteredOptionList.contains(FilteredOption.Direction)) MaterialTheme.colors.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "排序", modifier = Modifier.rotate(arrowRotateDegrees))
            TodoListDirecDropMenu(
                isShow = viewState.isShowDirectionDropMenu,
                changeShowState = { viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(false)) },
                onFilterDirec = {
                    viewModel.dispatch(TodoListViewAction.FilterDirection(it))
                }
            )
        }

        if (viewState.filteredOptionList.isNotEmpty()) {
            Icon(painter = painterResource(id = R.drawable.filter_alt_off) , contentDescription = "清除",
                modifier = Modifier.noRippleClickable {
                    viewModel.dispatch(TodoListViewAction.ClearFilter)
                    onRefresh()
                }
            )
        }
    }
}

@Composable
fun TodoListLabelDropMenu(
    options: MutableMap<String, Boolean>,
    isShowDropMenu: Boolean,
    onFilterLabels: (options: MutableMap<String, Boolean>) -> Unit
) {
    DropdownMenu(expanded = isShowDropMenu, onDismissRequest = {
        onFilterLabels(options)
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
fun TodoListStateDropMenu(
    isShow: Boolean,
    changeShowState: (isShow: Boolean) -> Unit,
    onFilterState: (state: IssueState) -> Unit
) {
    val options = listOf(IssueState.OPEN, IssueState.CLOSED, IssueState.PROGRESSING, IssueState.REJECTED, IssueState.ALL)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        changeShowState(false)
    }) {
        options.forEach { state ->
            DropdownMenuItem(
                onClick = {
                    onFilterState(state)
                },
            ) {
                Text(text = state.humanName)
            }
        }
    }
}

@Composable
fun TodoListDirecDropMenu(
    isShow: Boolean,
    changeShowState: (isShow: Boolean) -> Unit,
    onFilterDirec: (Direction) -> Unit
) {
    val options = listOf(Direction.DESC, Direction.ASC)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        changeShowState(false)
    }) {
        options.forEach { direction ->
            DropdownMenuItem(
                onClick = {
                    onFilterDirec(direction)
                },
            ) {
                Text(text = direction.humanName)
            }
        }
    }
}

@Composable
fun TodoListDateTimePicker(
    showState: MaterialDialogState,
    onFilterDate: (date: LocalDate, isStartDate: Boolean) -> Unit
) {
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
            onFilterDate(date, isStartDate)
        }
    }
}