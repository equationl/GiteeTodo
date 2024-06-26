package com.equationl.giteetodo.ui.page

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.equationl.giteetodo.R
import com.equationl.giteetodo.constants.ShareElementKey
import com.equationl.giteetodo.data.repos.model.common.TodoShowData
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.LocalShareAnimatedContentScope
import com.equationl.giteetodo.ui.LocalSharedTransitionScope
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.isScrollingUp
import com.equationl.giteetodo.ui.widgets.noRippleClickable
import com.equationl.giteetodo.ui.widgets.placeholder.PlaceholderHighlight
import com.equationl.giteetodo.ui.widgets.placeholder.material3.fade
import com.equationl.giteetodo.ui.widgets.placeholder.material3.placeholder
import com.equationl.giteetodo.viewmodel.FilteredOption
import com.equationl.giteetodo.viewmodel.TodoListViewAction
import com.equationl.giteetodo.viewmodel.TodoListViewEvent
import com.equationl.giteetodo.viewmodel.TodoListViewModel
import com.equationl.giteetodo.viewmodel.TodoListViewState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    repoPath: String,
    scaffoldState: BottomSheetScaffoldState,
    isShowSystemBar: (isShow: Boolean) -> Unit,
    viewModel: TodoListViewModel = hiltViewModel(),
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
            todoPagingItems,
            coroutineState,
            isShowSystemBar
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListContent(
    viewState: TodoListViewState,
    viewModel: TodoListViewModel,
    repoPath: String,
    todoPagingItems: LazyPagingItems<TodoShowData>,
    coroutineState: CoroutineScope,
    isShowSystemBar: (isShow: Boolean) -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }

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
        isRefreshing = (todoPagingItems.loadState.refresh is LoadState.Loading)

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                todoPagingItems.refresh()
            },
            modifier = Modifier.fillMaxSize()
        ) {
            TodoListLazyColumn(
                viewState,
                viewModel,
                todoPagingItems,
                repoPath,
                isRefreshing,
                coroutineState,
                isShowSystemBar,
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
                        itemData = it,
                        viewModel = viewModel,
                        repoPath = repoPath,
                        isLoading = isLoading,
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
            .background(MaterialTheme.colorScheme.background)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TodoItem(
    itemData: TodoShowData,
    viewModel: TodoListViewModel,
    repoPath: String,
    isLoading: Boolean,
) {

    val navController = LocalNavController.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalShareAnimatedContentScope.current

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
            .background(MaterialTheme.colorScheme.background)
            .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade()),
        shape = RoundedCornerShape(4.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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
                with(sharedTransitionScope) {
                    Text(
                        text = itemData.title,
                        textDecoration = if (itemData.state == IssueState.REJECTED) TextDecoration.LineThrough else null,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "${ShareElementKey.TODO_ITEM_TITLE}_${itemData.number}"),
                                animatedVisibilityScope = animatedContentScope
                            )
                            .noRippleClickable {
                                navController.navigate("${Route.TODO_DETAIL}/${itemData.number}/${itemData.title}")
                            }
                    )
                }
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
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowLabelsDropMenu) -180f else 0f,
                label = "arrowRotateDegrees"
            )
            Text("标签", color = if (viewState.filteredOptionList.contains(FilteredOption.Labels)) MaterialTheme.colorScheme.primary else Color.Unspecified)
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
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowStateDropMenu) -180f else 0f,
                label = "arrowRotateDegrees"
            )
            Text("状态", color = if (viewState.filteredOptionList.contains(FilteredOption.States)) MaterialTheme.colorScheme.primary else Color.Unspecified)
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
            Text("时间", color = if (viewState.filteredOptionList.contains(FilteredOption.DateTime)) MaterialTheme.colorScheme.primary else Color.Unspecified)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "时间")
            TodoListDateTimePicker(
                dialogState
            ) { start, end ->
                if (start == null || end == null) {
                    viewModel.dispatch(TodoListViewAction.SendMsg("请选择一个日期范围"))
                }
                else {
                    viewModel.dispatch(TodoListViewAction.FilterDate(start, end))
                    onRefresh()
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.noRippleClickable {
            viewModel.dispatch(TodoListViewAction.ChangeDirectionDropMenuShowState(true))
        }) {
            val arrowRotateDegrees: Float by animateFloatAsState(if (viewState.isShowDirectionDropMenu) -180f else 0f, label = "arrowRotateDegrees")
            Text("排序", color = if (viewState.filteredOptionList.contains(FilteredOption.Direction)) MaterialTheme.colorScheme.primary else Color.Unspecified)
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
                text = {
                    Text(text = name)
                },
                onClick = {
                    isChecked = !isChecked
                    options[name] = isChecked
                },
                leadingIcon = {
                    Checkbox(checked = isChecked, onCheckedChange = {
                        options[name] = it
                        isChecked = it
                    })
                }
            )
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
                text = {
                    Text(text = state.humanName)
                },
                onClick = {
                    onFilterState(state)
                },
            )
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
                text = {
                    Text(text = direction.humanName)
                },
                onClick = {
                    onFilterDirec(direction)
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListDateTimePicker(
    showState: MaterialDialogState,
    onFilterDate: (startDate: Long?, endDate: Long?) -> Unit
) {
    val state = rememberDateRangePickerState()

    MaterialDialog(
        dialogState = showState,
        buttons = {
            positiveButton("确定") {
                onFilterDate(state.selectedStartDateMillis, state.selectedEndDateMillis)
            }
            negativeButton("取消")
        },
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        DateRangePicker(
            state = state,
            headline = null,
            title = null
        )
    }
}