package com.equationl.giteetodo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.viewmodel.MainViewModel

@Composable
fun TodoListScreen() {
    val viewModel: MainViewModel = viewModel()

    MaterialTheme {
        Scaffold(
            topBar = {
                TopBar("TODO",
                    navigationIcon = Icons.Outlined.Close
                    ,actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Outlined.LibraryAdd, "添加仓库")
                    }
                }) {
                    // TODO 点击退出
                }
            },
            bottomBar = {
                BottomAppBar {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { /*TODO*/ }
                            .fillMaxWidth()
                            .weight(1f)) {
                        Icon(Icons.Filled.Home, "Home")
                        Text("首页")
                    }
                    Spacer(Modifier.weight(1f, true))
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable { /*TODO*/ }
                            .fillMaxWidth()
                            .weight(1f)) {
                        Icon(Icons.Outlined.Person, "Me")
                        Text("我的", color = Color.Gray)
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Outlined.Add, "Add")
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        )
        {
            Column(
                Modifier
                    .background(baseBackground)
                    .fillMaxSize()) {
                val cardData = viewModel.getIssue()

                TabRow(selectedTabIndex = 0) {
                    Tab(selected = true, onClick = { /*TODO*/ }, text = {
                        Text(text = "仓库1")
                    })
                    Tab(selected = true, onClick = { /*TODO*/ }, text = {
                        Text(text = "仓库2")
                    })
                    Tab(selected = true, onClick = { /*TODO*/ }, text = {
                        Text(text = "仓库3")
                    })

                    // TODO 如果显示的仓库过多应该隐藏掉，改成 “更多” 菜单，或者支持滑动，而不是现在这样直接堆叠
                }

                LazyColumn {
                    cardData.forEach{
                        item(it.date) {
                            TodoCardScreen(it)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoCardScreen(data: TodoCardData) {
    Card(modifier = Modifier
        .heightIn(20.dp, 500.dp)
        .padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
        Column(Modifier.padding(8.dp)) {
            Text(text = data.date, Modifier.padding(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                data.itemArray.forEach {
                    item(key = it.number) {
                        TodoItem(title = it.title, state = it.state)
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(title: String, state: Utils.IssueState) {
    var checked by remember { mutableStateOf(false) }
    checked = when (state) {
        Utils.IssueState.OPEN,
        Utils.IssueState.PROGRESSING,
        Utils.IssueState.REJECTED -> {
            false
        }
        Utils.IssueState.CLOSED -> {
            true
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
        Checkbox(checked = checked,
            enabled = state != Utils.IssueState.REJECTED,
            onCheckedChange = {
            // TODO 更新状态
            checked = it
        })
        Text(
            text = title,
            textDecoration = if (state == Utils.IssueState.REJECTED) TextDecoration.LineThrough else null,
        modifier = Modifier.clickable { /*TODO 点击文字*/ })
    }
}

data class TodoCardData(
    val date: String,
    val itemArray: List<TodoCardItemData>
)

data class TodoCardItemData(
    val title: String,
    val state: Utils.IssueState,
    val number: String
)

@Preview
@Composable
fun TodoListPreview() {
    TodoListScreen()
}