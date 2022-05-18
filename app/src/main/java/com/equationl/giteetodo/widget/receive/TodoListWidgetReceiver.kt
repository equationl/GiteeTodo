package com.equationl.giteetodo.widget.receive

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.ReposApi
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.toJson
import com.equationl.giteetodo.widget.TodoListWidget
import com.equationl.giteetodo.widget.callback.TodoListWidgetCallback
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class TodoListWidgetReceiver : GlanceAppWidgetReceiver() {

    private val coroutineScope = MainScope()

    override val glanceAppWidget: GlanceAppWidget = TodoListWidget()

    private var reposApi: ReposApi = RetrofitManger.getReposApi()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        refreshData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == TodoListWidgetCallback.UPDATE_ACTION) {
            refreshData(context)
        }
    }

    private fun refreshData(context: Context) {
        coroutineScope.launch {
            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "null/null")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val todoListResponse = reposApi.getAllIssues(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                token,
                page = 1,
                perPage = 10
            )

            val todoList = if (todoListResponse.isSuccessful) {
                todoListResponse.body() ?: listOf()
            }
            else {
                listOf()
            }

            val todoTitleList = mutableListOf<String>()
            for (todo in todoList) {
                todoTitleList.add(todo.title)
            }

            // 所有 widget 都更新成同一个内容
            val glanceIdList = GlanceAppWidgetManager(context).getGlanceIds(TodoListWidget::class.java)
            for (glanceId in glanceIdList) {
                glanceId.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            this[todoListKey] = todoTitleList.toJson()
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }

    companion object {
        val todoListKey = stringPreferencesKey("todoList")
    }
}