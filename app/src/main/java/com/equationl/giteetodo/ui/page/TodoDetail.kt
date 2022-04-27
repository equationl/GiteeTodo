package com.equationl.giteetodo.ui.page

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
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.*
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
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
            label = { Text("标题")},
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

        TodoDetailBodyItem(viewModel, viewState)

        TodoDetailSateItem(viewModel, viewState)

        TodoDetailLabelsItem(viewModel, viewState)

        // OpenApi 中没有修改下面这个三个值的接口
        TodoDetailCommonItem("优先级：", viewState.priority.getPriorityString())
        TodoDetailCommonItem("开始时间：", viewState.startDateTime)
        TodoDetailCommonItem("结束时间：", viewState.stopDateTime)

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
fun TodoDetailBodyItem(viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    if (viewState.isEditAble) {
        OutlinedTextField(
            value = viewState.content,
            onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnContentChange(it)) },
            readOnly = !viewState.isEditAble,
            label = {
                Text("描述（支持 Markdown）")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .padding(top = 32.dp)
                .background(Color.White)
        )
    }
    else {
        Text("描述：", modifier = Modifier.padding(top = 32.dp, start = 2.dp))
        Card(
            border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(Color.White)) {
            RichText(modifier = Modifier.padding(4.dp)) {
                Markdown(
                    viewState.content
                )
            }
        }
    }
}

@Composable
fun TodoDetailSateItem(viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 8.dp)
            .background(Color.White)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "状态")
            Box {
                Text(text = viewState.state.humanName, modifier = Modifier
                    .clickable(onClick = {
                        viewModel.dispatch(TodoDetailViewAction.StateDropMenuShowState(true)) },
                        enabled = viewState.isEditAble))
                StateDropMenu(viewModel, viewState.isShowStateDropMenu)
            }
        }
    }
}

@Composable
fun TodoDetailLabelsItem(viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 8.dp)
            .background(Color.White)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "标签")
            Box {
                Text(text = viewState.labels, modifier = Modifier
                    .clickable(onClick = {
                        viewModel.dispatch(TodoDetailViewAction.LabelsDropMenuShowState(true))
                    },
                        enabled = viewState.isEditAble))
                LabelsDropMenu(viewModel.viewStates.availableLabels, viewModel, viewState)
            }
        }
    }
}

@Composable
fun TodoDetailCommonItem(title: String, content: String) {
    Card(
        border = BorderStroke(0.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 8.dp)
            .background(Color.White)) {
        Row(
            Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title)
            Text(text = content)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LabelsDropMenu(options: MutableMap<String, Boolean>, viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    DropdownMenu(expanded = viewState.isShowLabelsDropMenu, onDismissRequest = {
        viewModel.dispatch(TodoDetailViewAction.UpdateLabels(options))
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StateDropMenu(viewModel: TodoDetailViewModel, isShow: Boolean) {
    val options = listOf(IssueState.OPEN, IssueState.CLOSED, IssueState.PROGRESSING)

    DropdownMenu(expanded = isShow, onDismissRequest = {
        viewModel.dispatch(TodoDetailViewAction.StateDropMenuShowState(false))
    }) {
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