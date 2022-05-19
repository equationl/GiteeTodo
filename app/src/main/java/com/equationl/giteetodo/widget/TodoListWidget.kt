package com.equationl.giteetodo.widget

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.currentState
import com.equationl.giteetodo.widget.receive.TodoListWidgetReceiver
import com.equationl.giteetodo.widget.ui.TodoListWidgetContent

class TodoListWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val todoList = prefs[TodoListWidgetReceiver.TodoListKey]
        val loadStatus = prefs[TodoListWidgetReceiver.LoadStateKey]


        TodoListWidgetContent(todoList, loadStatus)
    }
}