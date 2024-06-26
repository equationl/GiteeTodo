package com.equationl.giteetodo.ui.page

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EditOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.equationl.giteetodo.R
import com.equationl.giteetodo.constants.ShareElementKey
import com.equationl.giteetodo.data.repos.model.response.Comment
import com.equationl.giteetodo.ui.LocalShareAnimatedContentScope
import com.equationl.giteetodo.ui.LocalSharedTransitionScope
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.widgets.CommonMarkDown
import com.equationl.giteetodo.ui.widgets.LinkText
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.ui.widgets.placeholder.PlaceholderHighlight
import com.equationl.giteetodo.ui.widgets.placeholder.material3.fade
import com.equationl.giteetodo.ui.widgets.placeholder.material3.placeholder
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.TodoDetailViewAction
import com.equationl.giteetodo.viewmodel.TodoDetailViewEvent
import com.equationl.giteetodo.viewmodel.TodoDetailViewModel
import com.equationl.giteetodo.viewmodel.TodoDetailViewState
import com.equationl.giteetodo.viewmodel.getPriorityString
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    navController: NavHostController?,
    issueNum: String,
    issueTitle: String?,
    viewModel: TodoDetailViewModel = hiltViewModel()
) {
    val activity = (LocalContext.current as? Activity)
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()


    DisposableEffect(Unit) {
        if (issueNum == "null") {
            viewModel.dispatch(TodoDetailViewAction.ToggleEditModel(true))
        }
        else {
            if (!issueTitle.isNullOrBlank()) {
                viewModel.dispatch(TodoDetailViewAction.OnTitleChange(issueTitle))
            }
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
                if (navController == null) {
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
        TodoDetailContent(it, viewModel, viewState, issueNum)
    }
}

@Composable
fun TodoDetailContent(
    padding: PaddingValues,
    viewModel: TodoDetailViewModel,
    viewState: TodoDetailViewState,
    issueNum: String,
    ) {
    Box(
        modifier = Modifier.imePadding()
    ) {
        val listState = rememberLazyListState()
        LazyColumn(state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
            //.imePadding()
            //.imeNestedScroll()
        ) {
            item {
                TodoDetailMainContent(
                    viewState = viewState,
                    viewModel = viewModel,
                    issueNum = issueNum,
                )
            }

            item {
                if (!viewState.isEditAble && issueNum != "null") {
                    TodoCommentContent(viewState.commentList, viewModel, viewState)
                }
            }
        }

        AnimatedVisibility(
            visible = viewState.isShowCommentEdit,
            exit = slideOutVertically(targetOffsetY = { it / 2}),
            enter = slideInVertically(initialOffsetY = { it / 2})
        ) {
            TodoCreateCommentEdit(viewModel = viewModel, viewState = viewState, issueNum = issueNum)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TodoDetailMainContent(
    viewState: TodoDetailViewState,
    viewModel: TodoDetailViewModel,
    issueNum: String,
    ) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalShareAnimatedContentScope.current

    with(sharedTransitionScope) {
        OutlinedTextField(
            value = viewState.title,
            onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnTitleChange(it)) },
            readOnly = !viewState.isEditAble,
            label = { Text("标题")},
            singleLine = true,
            modifier = Modifier
                .sharedElement(
                    sharedTransitionScope.rememberSharedContentState(key = "${ShareElementKey.TODO_ITEM_TITLE}_${issueNum}"),
                    animatedVisibilityScope = animatedContentScope
                )
                .fillMaxWidth()
                .padding(2.dp)
                .background(MaterialTheme.colorScheme.background)
                // .placeholder(visible = viewState.isLoading, highlight = PlaceholderHighlight.fade())
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(end = 2.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(text = "创建于 ${viewState.createdDateTime}",
                fontSize = 12.sp,
                modifier = Modifier
                    .placeholder(visible = viewState.isLoading, highlight = PlaceholderHighlight.fade())
            )

            Text(text = "更新于 ${viewState.updateDateTime}",
                fontSize = 12.sp,
                modifier = Modifier
                    .placeholder(visible = viewState.isLoading, highlight = PlaceholderHighlight.fade())
            )
        }

        TodoDetailBodyItem(viewModel, viewState)

        TodoDetailSateItem(viewModel, viewState)

        TodoDetailLabelsItem(viewModel, viewState)

        // OpenApi 中没有修改下面这个三个值的接口
        TodoDetailCommonItem("优先级：", viewState.priority.getPriorityString(), viewState.isLoading)
        TodoDetailCommonItem("开始时间：", viewState.startDateTime, viewState.isLoading)
        TodoDetailCommonItem("结束时间：", viewState.stopDateTime, viewState.isLoading)

        if (viewState.isEditAble) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
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
            readOnly = false,
            label = {
                Text("描述（支持 Markdown）")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(2.dp)
                .padding(top = 32.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
    else {
        if (viewState.content.isNotBlank()) {
            Text("描述：",
                modifier = Modifier
                    .padding(top = 32.dp, start = 2.dp)
                    .placeholder(
                        visible = viewState.isLoading,
                        highlight = PlaceholderHighlight.fade()
                    )
            )
            Card(
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .placeholder(
                        visible = viewState.isLoading,
                        highlight = PlaceholderHighlight.fade()
                    )
            ) {
                Material3RichText(modifier = Modifier.padding(4.dp)) {
                    CommonMarkDown(viewState.content)
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
            .background(MaterialTheme.colorScheme.background)
            .placeholder(visible = viewState.isLoading, highlight = PlaceholderHighlight.fade())
    ) {
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
            .background(MaterialTheme.colorScheme.background)
            .placeholder(visible = viewState.isLoading, highlight = PlaceholderHighlight.fade())
    ) {
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
fun TodoDetailCommonItem(title: String, content: String, isLoading: Boolean) {
    Card(
        border = BorderStroke(0.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 8.dp)
            .background(MaterialTheme.colorScheme.background)
            .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
    ) {
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
                text = {
                    Text(text = name)
                },
                leadingIcon = {
                    Checkbox(checked = isChecked, onCheckedChange = {
                        options[name] = it
                        isChecked = it
                    })
                },
                onClick = {
                    isChecked = !isChecked
                    options[name] = isChecked
                },
            )
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
                text = {
                    Text(text = state.humanName)
                },
                onClick = {
                    viewModel.dispatch(TodoDetailViewAction.UpdateState(state))
                },
            )
        }
    }
}

@Composable
fun TodoCommentContent(
    commentList: List<Comment>,
    viewModel: TodoDetailViewModel,
    viewState: TodoDetailViewState
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("评论（${commentList.size}）",
                Modifier
                    .padding(start = 8.dp)
                    .placeholder(
                        visible = viewState.isLoading,
                        highlight = PlaceholderHighlight.fade()
                    )
            )

            LinkText(
                text = "新建",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .placeholder(
                        visible = viewState.isLoading,
                        highlight = PlaceholderHighlight.fade()
                    )
            ) {
                viewModel.dispatch(TodoDetailViewAction.ToggleCreateComment(true))
            }
        }

        Card(border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column {
                if (commentList.isEmpty()) {
                    Text(text = "暂无评论",
                        Modifier
                            .padding(8.dp)
                            .placeholder(
                                visible = viewState.isLoading,
                                highlight = PlaceholderHighlight.fade()
                            ))
                }
                else {
                    LazyColumn(modifier = Modifier.heightIn(0.dp, 1000.dp)) {
                        itemsIndexed(commentList) { index: Int, item: Comment ->
                            TodoCommentItem(item, viewModel, index != commentList.lastIndex, viewState.isLoading)
                        }
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
    hasDivider: Boolean = true,
    isLoading: Boolean
) {
    Column(
        Modifier
            .padding(4.dp)
            .placeholder(visible = isLoading, highlight = PlaceholderHighlight.fade())
    ) {
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
                            .clickable {
                                viewModel.dispatch(
                                    TodoDetailViewAction.ClickDeleteComment(
                                        comment.id
                                    )
                                )
                            })
                }
                //Text(text = "更新于 ${Utils.getDateTimeString(comment.updatedAt, "M月dd日 hh:mm:ss")}", fontSize = 8.sp)
            }
        }
        Material3RichText(Modifier.padding(bottom = 8.dp, start = 4.dp)) {
            CommonMarkDown(content = comment.body)
        }
        if (hasDivider) {
            HorizontalDivider()
        }
    }
}

@Composable
fun TodoCreateCommentEdit(viewModel: TodoDetailViewModel, viewState: TodoDetailViewState, issueNum: String) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(value = viewState.newComment,
                onValueChange = { viewModel.dispatch(TodoDetailViewAction.OnNewCommentChange(it)) },
                label = { Text(viewState.editCommentLabel) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp)
                    .weight(8f)
                    .padding(2.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                LinkText(
                    text = viewState.editCommentSaveBtn,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    viewModel.dispatch(TodoDetailViewAction.ClickSaveComment(issueNum))
                }

                LinkText(text = "取消") {
                    viewModel.dispatch(TodoDetailViewAction.ToggleCreateComment(false))
                }
            }
        }
    }
}