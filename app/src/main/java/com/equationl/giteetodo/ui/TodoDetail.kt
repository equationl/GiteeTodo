package com.equationl.giteetodo.ui

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.viewmodel.*
import kotlinx.coroutines.launch

private const val TAG = "el, TodoDetailScreen"

@Composable
fun TodoDetailScreen(navController: NavHostController, issueNum: String) {
    val viewModel: TodoDetailViewModel = viewModel()
    val viewState = viewModel.viewStates
    val scaffoldState = rememberScaffoldState()
    val coroutineState = rememberCoroutineScope()


    DisposableEffect(Unit) {
        if (issueNum == "null") {
            viewModel.dispatch(TodoDetailViewAction.EnterEditModel)
        }
        else {
            viewModel.dispatch(TodoDetailViewAction.LoadIssue(issueNum))
        }

        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoDetailViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar(viewState.title, actions = {
                    if (!viewState.isEditAble) {
                        IconButton(onClick = {
                            viewModel.dispatch(TodoDetailViewAction.EnterEditModel)
                        }) {
                            Icon(Icons.Outlined.EditNote, contentDescription = "编辑")
                        }
                    }
                }) {
                    navController.popBackStack()
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                    Snackbar(snackbarData = snackBarData)
                }})
        {
            if (viewState.isLoading) {
                TodoDetailLoad()
            }
            else {
                TodoDetailContent(it.calculateTopPadding(), viewModel, viewState, issueNum)
            }
        }
    }
}

@Composable
fun TodoDetailLoad() {
    LoadDataContent(text = "加载详情中…")
}

@Composable
fun TodoDetailContent(topPadding: Dp, viewModel: TodoDetailViewModel, viewState: TodoDetailViewState, issueNum: String) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = topPadding)
        .background(baseBackground)
        .verticalScroll(rememberScrollState())) {
        OutlinedTextField(
            value = viewState.title,
            onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnTitleChange(it)) },
            readOnly = !viewState.isEditAble,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(Color.White)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .padding(end = 2.dp), horizontalAlignment = Alignment.End) {
            Text(text = "创建于 ${viewState.createdDateTime}", fontSize = 12.sp)
            Text(text = "更新于 ${viewState.updateDateTime}", fontSize = 12.sp)
        }
        OutlinedTextField(
            value = viewState.content,
            onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnContentChange(it)) },
            readOnly = !viewState.isEditAble,
            label = {
                Text("描述")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .padding(top = 32.dp)
                .background(Color.White)
        )

        TodoDetailMultipleChoiceItem(viewModel, viewState, "状态：", viewState.state.humanName) {
            viewModel.dispatch(TodoDetailViewAction.StateDropMenuShowState(true))
        }

        TodoDetailMultipleChoiceItem(viewModel, viewState, "优先级：", viewState.priority.getPriorityString()) {
            // OpenApi 中没有修改该值的接口
        }

        TodoDetailMultipleChoiceItem(viewModel, viewState, "标签：", viewState.labels) {
            viewModel.dispatch(TodoDetailViewAction.LabelsDropMenuShowState(true))
        }

        TodoDetailMultipleChoiceItem(viewModel, viewState, "开始时间：", viewState.startDateTime) {
            // OpenApi 中没有修改该值的接口
        }

        TodoDetailMultipleChoiceItem(viewModel, viewState, "结束时间：", viewState.stopDateTime) {
            // OpenApi 中没有修改该值的接口
        }

        if (viewState.isEditAble) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { viewModel.dispatch(TodoDetailViewAction.ClickSave(issueNum)) },
                    shape = Shapes.large) {
                    Text(text = "保存", fontSize = 20.sp, modifier = Modifier.padding(start = 82.dp, end = 82.dp, top = 4.dp, bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun TodoDetailMultipleChoiceItem(viewModel: TodoDetailViewModel, viewState: TodoDetailViewState, title: String, content: String, onclick: () -> Unit) {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 8.dp)
            .background(Color.White)) {
        Row(
            Modifier
                .padding(8.dp)
                .clickable(onClick = onclick, enabled = viewState.isEditAble),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title)
            Text(text = content)
        }

        LabelsDropMenu(viewModel.viewStates.availableLabels, viewModel, viewState)

        StateDropMenu(viewModel, viewState.isShowStateDropMenu)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LabelsDropMenu(options: MutableMap<String, Boolean>, viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    // FIXME 该 composable 会被重复重组导致弹出多个 dropMenu

    ExposedDropdownMenuBox(
        expanded = viewState.isShowLabelsDropMenu,
        onExpandedChange = {

        }
    ) {
        ExposedDropdownMenu(
            expanded = viewState.isShowLabelsDropMenu,
            onDismissRequest = {
                viewModel.dispatch(TodoDetailViewAction.UpdateLabels(options))
            }
        ) {
            options.forEach { (name, checked) ->
                var isChecked by remember { mutableStateOf(checked) }
                DropdownMenuItem(
                    onClick = {

                    },
                ) {
                    Checkbox(checked = isChecked, onCheckedChange = {
                        options["name"] = it
                        isChecked = it
                    })
                    Text(text = name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StateDropMenu(viewModel: TodoDetailViewModel, isShow: Boolean) {
    // FIXME 该 composable 会被重复重组导致弹出多个 dropMenu

    Log.i(TAG, "StateDropMenu: call StateDropMenu, isShow=$isShow")
    val options = listOf(IssueState.OPEN, IssueState.CLOSED, IssueState.PROGRESSING)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        viewModel.dispatch(TodoDetailViewAction.StateDropMenuShowState(false))
    }) {
        Log.i(TAG, "StateDropMenu: recompose, isShow=$isShow")
        options.forEach { state ->
            DropdownMenuItem(
                onClick = {
                    viewModel.dispatch(TodoDetailViewAction.UpdateState(state))
                },
            ) {
                Text(text = state.humanName)
            }
        }
    }
}



@Preview
@Composable
fun TodoDetailPreview() {
    TodoDetailScreen(rememberNavController(), "null")
}