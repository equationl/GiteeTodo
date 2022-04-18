package com.equationl.giteetodo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                TopBar("TODO") {
                    // TODO 点击返回
                }
            })
        {
            Column(Modifier.background(baseBackground).fillMaxSize()) {
                val cardData = viewModel.getIssue()
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
    Card(modifier = Modifier.heightIn(20.dp, 500.dp).padding(32.dp), shape = RoundedCornerShape(16.dp), elevation = 5.dp) {
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