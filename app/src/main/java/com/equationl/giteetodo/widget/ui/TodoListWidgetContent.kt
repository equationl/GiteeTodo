package com.equationl.giteetodo.widget.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.equationl.giteetodo.MainActivity
import com.equationl.giteetodo.ui.theme.baseBackground
import com.equationl.giteetodo.util.fromJson
import com.equationl.giteetodo.widget.callback.TodoListWidgetCallback

val actionKey = ActionParameters.Key<String>(TodoListWidgetCallback.ACTION_NAME)
val refreshActionPar = actionParametersOf(actionKey to TodoListWidgetCallback.UPDATE_ACTION)

@Composable
fun TodoListWidgetContent(todoList: String?) {
    val showTodoList = remember { mutableStateListOf<String>() }
    showTodoList.addAll(resolveData(todoList))


    Column(modifier = GlanceModifier
        .fillMaxSize()
        .cornerRadius(10.dp)
        .padding(8.dp)
        .background(MaterialTheme.colors.baseBackground)
        .clickable(actionStartActivity<MainActivity>())
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<TodoListWidgetCallback>(refreshActionPar))) {
            Box(modifier = GlanceModifier.fillMaxWidth()) {
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "当前未完成：",
                        style = TextStyle(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = ColorProvider(MaterialTheme.colors.secondary)
                        ),
                        modifier = GlanceModifier.padding(start = 8.dp)
                    )
                }
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(text = "刷新",
                        style = TextStyle(color = ColorProvider(MaterialTheme.colors.primary)))
                }
            }
        }

        if (showTodoList.isEmpty()) {
            WidgetEmptyContent()
        }
        else {
            LazyColumn {
                itemsIndexed(showTodoList) { index, item ->
                    Text(
                        text = "${index+1}: $item",
                        style = TextStyle(color = ColorProvider(MaterialTheme.colors.primary)),
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 2.dp).clickable(actionStartActivity<MainActivity>())
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetEmptyContent(text: String = "无数据， 点击刷新", action: Action = actionRunCallback<TodoListWidgetCallback>(refreshActionPar)) {
    Column(
        modifier = GlanceModifier.fillMaxSize().clickable(action),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, style = TextStyle(color = ColorProvider(MaterialTheme.colors.primary)))
    }
}

private fun resolveData(jsonString: String?): List<String> {
    return jsonString?.fromJson<List<String>>() ?: listOf()
}