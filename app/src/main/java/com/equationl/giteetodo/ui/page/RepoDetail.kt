package com.equationl.giteetodo.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.ui.widgets.LoadDataContent
import com.equationl.giteetodo.ui.widgets.TopBar
import com.equationl.giteetodo.viewmodel.RepoDetailViewAction
import com.equationl.giteetodo.viewmodel.RepoDetailViewEvent
import com.equationl.giteetodo.viewmodel.RepoDetailViewModel
import com.equationl.giteetodo.viewmodel.RepoDetailViewState
import kotlinx.coroutines.launch

@Composable
fun RepoDetailScreen(navController: NavHostController) {
    val viewModel: RepoDetailViewModel = viewModel()
    val viewState = viewModel.viewStates
    val scaffoldState = rememberScaffoldState()
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

    MaterialTheme {
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
            RepoDetailContent(viewModel, viewState)
        }
    }
}

@Composable
fun RepoDetailContent(viewModel: RepoDetailViewModel, viewState: RepoDetailViewState) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(baseBackground)
        .verticalScroll(rememberScrollState())) {

        OutlinedTextField(
            value = viewState.repoName,
            onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeName(it)) },
            label = { Text("仓库名称")},
            placeholder = { Text("仓库名只允许包含中文、字母、数字或者下划线(_)、中划线(-)、英文句号(.)、加号(+)，必须以字母或数字开头，不能以下划线/中划线结尾，且长度为2~191个字符")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(Color.White))

        OutlinedTextField(
            value = viewState.repoDescribe,
            onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeDescribe(it)) },
            label = { Text("仓库描述")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .padding(top = 32.dp)
                .background(Color.White))


        OutlinedTextField(
            value = viewState.repoPath,
            onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangePath(it)) },
            label = { Text("仓库路径")},
            placeholder = { Text("路径只允许包含字母、数字或者下划线(_)、中划线(-)、英文句号(.)，必须以字母开头，且长度为2~191个字符")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(Color.White))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = viewState.isPrivateRepo, onCheckedChange = { viewModel.dispatch(RepoDetailViewAction.ChangeIsPrivate(it)) })
            Text("私有仓库")
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = viewState.isInitRepo, onCheckedChange = { viewModel.dispatch(RepoDetailViewAction.ChangeIsInit(it)) })
            Text("初始化")
        }

        if (viewState.isInitRepo) {
            OutlinedTextField(
                value = viewState.readmeContent,
                onValueChange = { viewModel.dispatch(RepoDetailViewAction.ChangeReadmeContent(it)) },
                label = { Text("仓库详细介绍（README）， 支持 markdown")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(Color.White))
        }


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

@Preview
@Composable
fun RepoDetailPreview() {
    RepoDetailScreen(rememberNavController())
}