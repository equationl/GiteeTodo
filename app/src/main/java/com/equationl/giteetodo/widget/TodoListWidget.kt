package com.equationl.giteetodo.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import com.equationl.giteetodo.widget.receive.TodoListWidgetReceiver
import com.equationl.giteetodo.widget.ui.TodoListWidgetContent

class TodoListWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val todoList = prefs[TodoListWidgetReceiver.TodoListKey]
            val loadStatus = prefs[TodoListWidgetReceiver.LoadStateKey]


            TodoListWidgetContent(todoList, loadStatus)
        }
    }
}