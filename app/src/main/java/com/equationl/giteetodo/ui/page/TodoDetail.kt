package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.giteetodo.R
import com.equationl.giteetodo.data.repos.model.response.Comment
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.*
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.RichText
import com.halilibo.richtext.ui.material.MaterialRichText
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
            viewModel.dispatch(TodoDetailViewAction.ToggleEditModel(true))
        }
        else {
            viewModel.dispatch(TodoDetailViewAction.LoadIssue(issueNum))
            viewModel.dispatch(TodoDetailViewAction.LoadComment(issueNum))
        }

        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is TodoDetailViewEvent.ShowMessage) {
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar(viewState.title.ifEmpty { "新建" }, actions = {
                    if (issueNum != "null") {
                        val icon = if (viewState.isEditAble) Icons.Outlined.EditOff else Icons.Outlined.EditNote
                        IconButton(onClick = {
                            viewModel.dispatch(TodoDetailViewAction.ToggleEditModel(!viewState.isEditAble))
                        }) {
                            Icon(icon, contentDescription = "编辑")
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
    val listState = rememberLazyListState()
    LazyColumn(state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding)
            .background(MaterialTheme.colors.baseBackground)
    ) {
        item {
            OutlinedTextField(
                value = viewState.title,
                onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnTitleChange(it)) },
                readOnly = !viewState.isEditAble,
                label = { Text("标题")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(MaterialTheme.colors.background)
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

        item {
            if (!viewState.isEditAble && issueNum != "null") {
                TodoCommentContent(viewState.commentList, issueNum, viewModel, viewState)
            }
            if (viewState.editCommentId != -1) {
                val scope = rememberCoroutineScope()
                scope.launch {
                    listState.animateScrollToItem(1)
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
                .background(MaterialTheme.colors.background)
        )
    }
    else {
        if (viewState.content.isNotBlank()) {
            Text("描述：", modifier = Modifier.padding(top = 32.dp, start = 2.dp))
            Card(
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(MaterialTheme.colors.background)) {
                MaterialRichText(modifier = Modifier.padding(4.dp)) {
                    Markdown(
                        viewState.content
                    )
                }
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
            .background(MaterialTheme.colors.background)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "状态：")
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
            .background(MaterialTheme.colors.background)) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "标签：")
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
            .background(MaterialTheme.colors.background)) {
        Row(
            Modifier
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title)
            Text(text = content)
        }
    }
}


@Composable
fun LabelsDropMenu(options: MutableMap<String, Boolean>, viewModel: TodoDetailViewModel, viewState: TodoDetailViewState) {
    DropdownMenu(expanded = viewState.isShowLabelsDropMenu, onDismissRequest = {
        viewModel.dispatch(TodoDetailViewAction.UpdateLabels(options))
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

@Composable
fun TodoCommentContent(
    commentList: List<Comment>,
    issueNum: String,
    viewModel: TodoDetailViewModel,
    viewState: TodoDetailViewState
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp)) {
        Text("评论（${commentList.size}）", Modifier.padding(start = 8.dp, end = 8.dp))

        Card(border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(MaterialTheme.colors.background)
        ) {
            Column {  // 不知道为什么，这里如果再嵌套一个 LazyColumn 的话会闪退

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

                    OutlinedTextField(value = viewState.newComment,
                        onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnNewCommentChange(it)) },
                        label = { Text(viewState.editCommentLabel) },
                        modifier = Modifier
                            .weight(8f)
                            .padding(end = 4.dp)
                            .scale(1f, 0.8f)
                    )

                    Button(onClick = { viewModel.dispatch(TodoDetailViewAction.ClickSaveComment(issueNum)) }, modifier = Modifier.weight(2f)) {
                        Text(text = viewState.editCommentSaveBtn, fontSize = 12.sp)
                    }
                }

                Divider(Modifier.padding(bottom = 8.dp))

                if (commentList.isEmpty()) {
                    Text(text = "暂无评论", Modifier.padding(8.dp))
                }
                else {
                    commentList.forEachIndexed { index, comment ->
                        TodoCommentItem(comment, viewModel, index != commentList.lastIndex)
                    }
                }
            }
        }
    }
}

@Composable
fun TodoCommentItem(
    comment: Comment,
    viewModel: TodoDetailViewModel,
    hasDivider: Boolean = true
) {
    Column(Modifier.padding(4.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comment.user.avatarUrl)
                        .placeholder(R.drawable.ic_unknown_user)
                        .build(),
                    contentDescription = "头像",
                    modifier = Modifier
                        .width(25.dp)
                        .height(25.dp)
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.padding(start = 2.dp)) {
                    Text(text = comment.user.name, fontSize = 14.sp)
                    Text(text = Utils.getDateTimeString(comment.createdAt, "M月dd日 hh:mm:ss"), fontSize = 10.sp)
                }
            }
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.End) {
                    Icon(Icons.Outlined.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                viewModel.dispatch(TodoDetailViewAction.ClickEditComment(comment))
                            })

                    Icon(Icons.Outlined.Delete,
                        contentDescription = "删除",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(16.dp)
                            .clickable { viewModel.dispatch(TodoDetailViewAction.ClickDeleteComment(comment.id)) })
                }
                //Text(text = "更新于 ${Utils.getDateTimeString(comment.updatedAt, "M月dd日 hh:mm:ss")}", fontSize = 8.sp)
            }
        }
        RichText(Modifier.padding(bottom = 8.dp, start = 4.dp)) {
            Markdown(content = comment.body)
        }
        if (hasDivider) {
            Divider()
        }
    }
}