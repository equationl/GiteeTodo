package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.widgets.ListEmptyContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.util.Utils.toColor
import com.equationl.giteetodo.util.Utils.toHexString
import com.equationl.giteetodo.viewmodel.LabelMgViewAction
import com.equationl.giteetodo.viewmodel.LabelMgViewEvent
import com.equationl.giteetodo.viewmodel.LabelMgViewModel
import com.equationl.giteetodo.viewmodel.LabelMgViewState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.color.ARGBPickerState
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch

private const val TAG = "el, LabelManagerScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelManagerScreen(
    repoPath: String,
    navController: NavHostController,
    viewModel: LabelMgViewModel = hiltViewModel()
) {
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()

    DisposableEffect(Unit) {
        viewModel.dispatch(LabelMgViewAction.LoadLabel(false))
        onDispose {  }
    }

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is LabelMgViewEvent.Goto) {
                navController.navigate(it.route)
            }
            else if (it is LabelMgViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar("标签管理",
                actions = {
                    IconButton(onClick = {
                        viewModel.dispatch(LabelMgViewAction.LoadLabel(forceRequest = true, isShowSuccessAlt = true)) }) {
                        Icon(Icons.Outlined.SyncAlt, "同步标签")
                    }
                    IconButton(onClick = { viewModel.dispatch(LabelMgViewAction.ClickAddLabel) }) {
                        Icon(Icons.Outlined.Add, "添加标签")
                    }
                }
            ) {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }
        }
    )
    {
        Column(
            modifier = Modifier.padding(it)
        ) {
            if (viewState.labelList.isEmpty()) {
                ListEmptyContent(title = "暂无标签，点击刷新或点击右上角新建") {
                    viewModel.dispatch(LabelMgViewAction.LoadLabel(true))
                }
            }
            else {
                LabelListContent(viewState, viewModel, repoPath)
            }
        }
    }
}

@Composable
fun LabelListContent(viewState: LabelMgViewState, viewModel: LabelMgViewModel, repoPath: String) {
    val labelList = viewState.labelList
    Column(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(top = 8.dp)) {

        LazyColumn {
            itemsIndexed(labelList, key = {_, item -> item.name}) { index, item ->
                LabelItem(item, index, repoPath, viewModel)
                if (viewState.editPos == index) {
                    viewModel.dispatch(LabelMgViewAction.InitEdit(item))
                    EditLabelContent(index, repoPath, viewModel, viewState, item)
                }
            }
        }

        if (viewState.editPos == Int.MAX_VALUE) {
            Divider(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp))
            EditLabelContent(Int.MAX_VALUE, repoPath, viewModel, viewState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelItem(label: Label, pos: Int, repoPath: String, viewModel: LabelMgViewModel) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp, 4.dp)
        .clickable {
            viewModel.dispatch(LabelMgViewAction.ClickEditLabel(pos))
        }
    ) {
        val dismissState = rememberDismissState(
            confirmValueChange = {
                if (it == DismissValue.DismissedToStart) {
                    viewModel.dispatch(LabelMgViewAction.DeleteLabel(label, repoPath))
                    true
                }
                else {
                    false
                }
            }
        )

        SwipeToDismiss(
            state = dismissState,
            background = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    Text("继续滑动删除", color = MaterialTheme.colorScheme.background)
                    Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.background)
                }
            },
            dismissContent = {
                Card(
                    border = BorderStroke(1.dp, Color.Gray),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(label.name, color = "#${label.color}".toColor, modifier = Modifier.padding(32.dp, 8.dp))
                        Text(label.id.toString(), modifier = Modifier.padding(32.dp, 8.dp))
                    }
                }
            },
            directions = setOf(DismissDirection.EndToStart)
        )
    }
}

@Composable
fun EditLabelContent(pos: Int, repoPath: String, viewModel: LabelMgViewModel, viewState: LabelMgViewState, label: Label? = null) {
    val dialogState = rememberMaterialDialogState()
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(value = viewState.editName, onValueChange = { viewModel.dispatch(LabelMgViewAction.OnEditNameChange(it)) }, label = { Text("标签名称")})
        Card(
            border = BorderStroke(1.dp, Color.Gray),
            modifier = Modifier
                .padding(top = 4.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(Modifier.padding(8.dp)) {
                val color = if (viewState.editColor.isBlank()) Color.Unspecified else "#${viewState.editColor}".toColor
                Text("颜色：")
                Text(text = viewState.editColor, color = color, modifier = Modifier.clickable {
                    dialogState.show()
                })
            }
        }
        Button(onClick = {
            viewModel.dispatch(LabelMgViewAction.ClickSave(pos, repoPath, label))
        }) {
            Text("确认")
        }
    }

    ColorPickerDialog(dialogState, viewModel)
}

@Composable
fun ColorPickerDialog(dialogState: MaterialDialogState, viewModel: LabelMgViewModel) {
    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton("确定")
            negativeButton("取消")
        }

    ) {
        colorChooser(colors = ColorPalette.Primary, argbPickerState = ARGBPickerState.WithoutAlphaSelector) {
            viewModel.dispatch(LabelMgViewAction.OnEditColorChange(it.toHexString))
        }
    }
}