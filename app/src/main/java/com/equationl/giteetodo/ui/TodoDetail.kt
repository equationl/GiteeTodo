package com.equationl.giteetodo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equationl.giteetodo.ui.theme.Shapes
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.util.Utils.isEmail
import com.equationl.giteetodo.viewmodel.MainViewModel

@Composable
fun TodoDetailScreen() {
    val viewModel: MainViewModel = viewModel()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("TODO DETAIL", actions = {
                    if (!viewModel.isEdit) {
                        IconButton(onClick = {
                            // TODO 点击编辑
                            viewModel.isEdit = true
                        }) {
                            Icon(Icons.Outlined.EditNote, contentDescription = "编辑")
                        }
                    }
                }) {
                    // TODO 点击返回
                }
            })
        {
            TodoDetailContent()
        }
    }
}

@Composable
fun TodoDetailContent() {
    val viewModel: MainViewModel = viewModel()

    Column(modifier = Modifier
        .fillMaxSize()
        .background(baseBackground)) {
        OutlinedTextField(value = "我是标题", onValueChange = {}, readOnly = !viewModel.isEdit, modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .background(Color.White))
        Column(
            Modifier
                .fillMaxWidth()
                .padding(end = 2.dp), horizontalAlignment = Alignment.End) {
            Text(text = "创建于 2022.04.18 12:00", fontSize = 12.sp)
            Text(text = "修改于 2022.04.18 13:00", fontSize = 12.sp)
        }
        OutlinedTextField(value = "我是内容，我是内容，\n我是内容，我是内容，我是内容，我是内容，我是内容，我是内容，我是内容，\n我是内容，我是内容，我是内容，我是内容，\n我是内容，结束",
            onValueChange = {},
            readOnly = !viewModel.isEdit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .padding(top = 32.dp)
                .background(Color.White))

        TodoDetailCardItem(title = "状态：", content = "未完成") {

        }

        TodoDetailCardItem(title = "优先级：", content = "主要") {

        }

        TodoDetailCardItem(title = "标签：", content = "bug, feature") {

        }

        TodoDetailCardItem(title = "开始时间：", content = "2022.04.18 18:00") {

        }

        TodoDetailCardItem(title = "结束时间：", content = "2022.04.20 18:00") {

        }

        if (viewModel.isEdit) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                              /*TODO*/
                              viewModel.isEdit = !viewModel.isEdit
                              },
                    shape = Shapes.large) {
                    Text(text = "保存", fontSize = 20.sp, modifier = Modifier.padding(start = 82.dp, end = 82.dp, top = 4.dp, bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
fun TodoDetailCardItem(title: String, content: String, onclick: () -> Unit) {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .padding(top = 24.dp)
            .background(Color.White)) {
        Row(
            Modifier
                .padding(8.dp)
                .clickable(onClick = onclick), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = title)
            Text(text = content)
        }
    }
}



@Preview
@Composable
fun TodoDetailPreview() {
    TodoDetailScreen()
}