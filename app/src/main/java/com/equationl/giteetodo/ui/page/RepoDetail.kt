package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.RepoDetailViewAction
import com.equationl.giteetodo.viewmodel.RepoDetailViewEvent
import com.equationl.giteetodo.viewmodel.RepoDetailViewModel
import com.equationl.giteetodo.viewmodel.RepoDetailViewState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    viewModel: RepoDetailViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val viewState = viewModel.viewStates
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineState = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.viewEvents.collect {
            if (it is RepoDetailViewEvent.ShowMessage) {
                println("收到错误消息：${it.message}")
                coroutineState.launch {
                    scaffoldState.snackbarHostState.showSnackbar(message = it.message)
                }
            }
            else if (it is RepoDetailViewEvent.Goto) {
                println("Goto route=${it.route}")
                navController.navigate(it.route)
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar("新建仓库") {
                navController.popBackStack()
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = scaffoldState.snackbarHostState) { snackBarData ->
                Snackbar(snackbarData = snackBarData)
            }})
    {
        RepoDetailContent(viewModel, viewState, it)
    }
}

@Composable
fun RepoDetailContent(
    viewModel: RepoDetailViewModel,
    viewState: RepoDetailViewState,
    paddingValues: PaddingValues
) {
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        //.imePadding()
        //.imeNestedScroll()
    ) {
        item(key = "仓库名称") {
            OutlinedTextField(
                value = viewState.repoName,
                onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeName(it)) },
                label = { Text("仓库名称")},
                singleLine = true,
                //placeholder = { Text("仓库名只允许包含中文、字母、数字或者下划线(_)、中划线(-)、英文句号(.)、加号(+)，必须以字母或数字开头，不能以下划线/中划线结尾，且长度为2~191个字符")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.background))
        }

        item(key = "仓库描述") {
            OutlinedTextField(
                value = viewState.repoDescribe,
                onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeDescribe(it)) },
                label = { Text("仓库描述")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .padding(top = 32.dp)
                    .background(MaterialTheme.colorScheme.background))
        }

        item(key = "仓库路径") {
            OutlinedTextField(
                value = viewState.repoPath,
                onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangePath(it)) },
                label = { Text("仓库路径")},
                singleLine = true,
                //placeholder = { Text("路径只允许包含字母、数字或者下划线(_)、中划线(-)、英文句号(.)，必须以字母开头，且长度为2~191个字符")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.background))
        }

        item(key = "私有仓库") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = viewState.isPrivateRepo, onCheckedChange = { viewModel.dispatch(RepoDetailViewAction.ChangeIsPrivate(it)) })
                Text("私有仓库")
            }
        }

        item(key = "初始化") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = viewState.isInitRepo, onCheckedChange = { viewModel.dispatch(RepoDetailViewAction.ChangeIsInit(it)) })
                Text("初始化")
            }
        }

        item(key = "markdown") {
            if (viewState.isInitRepo) {
                OutlinedTextField(
                    value = viewState.readmeContent,
                    onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeReadmeContent(it)) },
                    label = { Text("仓库详细介绍（README）， 支持 markdown")},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(2.dp)
                        .background(MaterialTheme.colorScheme.background))
            }
        }

        item(key = "创建") {
            if (viewState.isUnderCreation) {
                Row(modifier = Modifier.heightIn(0.dp, 100.dp), horizontalArrangement = Arrangement.Center) {
                    LoadDataContent("正在创建中…")
                }
            }
            else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp), horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = {
                            viewModel.dispatch(RepoDetailViewAction.Create)
                        },
                        shape = Shapes.large) {
                        Text(text = "创建", fontSize = 20.sp, modifier = Modifier.padding(start = 82.dp, end = 82.dp, top = 4.dp, bottom = 4.dp))
                    }
                }
            }
        }
    }
}