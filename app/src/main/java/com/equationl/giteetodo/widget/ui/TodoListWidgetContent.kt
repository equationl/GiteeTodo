package com.equationl.giteetodo.widget.ui

import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CheckBox
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.equationl.giteetodo.MainActivity
import com.equationl.giteetodo.widget.callback.TodoListWidgetCallback
import com.equationl.giteetodo.widget.dataBean.TodoListWidgetShowData
import com.equationl.giteetodo.widget.receive.TodoListWidgetReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

private const val TAG = "el, TodoListWidgetContent"

val actionKey = ActionParameters.Key<String>(TodoListWidgetCallback.ACTION_NAME)
val issueNumKey = ActionParameters.Key<String>(TodoListWidgetCallback.ISSUE_NUM_NAME)
val refreshActionPar = actionParametersOf(actionKey to TodoListWidgetCallback.REFRESH_ACTION)

@Composable
fun TodoListWidgetContent(todoList: String?, loadStatus: Int?) {
    Log.i(TAG, "TodoListWidgetContent: todoList=$todoList")
    Log.i(TAG, "TodoListWidgetContent: loadState=$loadStatus")

    val showTodoList = remember(todoList) { mutableStateListOf<TodoListWidgetShowData>() }
    showTodoList.addAll(resolveData(todoList))

    Log.i(TAG, "TodoListWidgetContent: showTodoList=${showTodoList.toList()}")


    Column(modifier = GlanceModifier
        .fillMaxSize()
        .cornerRadius(10.dp)
        .padding(8.dp)
        .background(MaterialTheme.colorScheme.background)
        //.clickable(actionStartActivity<MainActivity>())
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<TodoListWidgetCallback>(refreshActionPar))) {
            Box(modifier = GlanceModifier.fillMaxWidth()) {
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "待办列表：",
                        style = TextStyle(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = ColorProvider(MaterialTheme.colorScheme.secondary)
                        ),
                        modifier = GlanceModifier.padding(start = 8.dp)
                    )
                }
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Text(text = "刷新",
                        style = TextStyle(color = ColorProvider(MaterialTheme.colorScheme.primary)))
                }
            }
        }

        if (loadStatus == TodoListWidgetReceiver.LOAD_SUCCESS) {
            if (showTodoList.isEmpty()) {
                WidgetEmptyContent()
            }
            else {
                LazyColumn {
                    itemsIndexed(showTodoList) { index, item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CheckBox(
                                checked = item.isChecked,
                                onCheckedChange = actionRunCallback<TodoListWidgetCallback>(
                                    actionParametersOf(
                                        actionKey to TodoListWidgetCallback.CHECK_ISSUE_ACTION,
                                        issueNumKey to item.issueNum
                                    )
                                )
                            )

                            Text(
                                text = "${index+1}: ${item.title}",
                                style = TextStyle(color = ColorProvider(MaterialTheme.colorScheme.primary)),
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(start = 2.dp, bottom = 2.dp)
                                    .clickable(actionStartActivity<MainActivity>(
                                        actionParametersOf(issueNumKey to item.issueNum))
                                    )
                            )
                        }
                    }
                }
            }
        }
        else {
            WidgetEmptyContent("加载失败：$loadStatus， 点击进入主页处理", actionStartActivity<MainActivity>())
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
        Text(text = text, style = TextStyle(color = ColorProvider(MaterialTheme.colorScheme.primary)))
    }
}

private fun resolveData(jsonString: String?): List<TodoListWidgetShowData> {
    try {
        val listType: Type = object : TypeToken<List<TodoListWidgetShowData?>?>() {}.type
        return Gson().fromJson(jsonString, listType)
    } catch (tr: Throwable) {
        Log.w(TAG, "resolveData: ", tr)
    }

    return listOf()
}