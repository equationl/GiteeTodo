package com.equationl.giteetodo.widget.receive

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ToggleableStateKey
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.model.request.UpdateIssue
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.toJson
import com.equationl.giteetodo.widget.TodoListWidget
import com.equationl.giteetodo.widget.callback.TodoListWidgetCallback
import com.equationl.giteetodo.widget.dataBean.TodoListWidgetShowData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class TodoListWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var repoApi: RepoApi

    private val coroutineScope = MainScope()

    override val glanceAppWidget: GlanceAppWidget = TodoListWidget()

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
        else if (intent.action == TodoListWidgetCallback.CHECK_ISSUE_ACTION) {
            val issueNum = intent.getStringExtra(TodoListWidgetCallback.ISSUE_NUM_NAME)
            val isChecked = intent.getBooleanExtra(ToggleableStateKey.name, true)
            if (issueNum != null) {
                updateIssueState(context, issueNum, isChecked)
            }
            else {
                Log.w(TAG, "onReceive: issue num is null!")
            }
        }
    }

    private fun refreshData(context: Context) {
        coroutineScope.launch {
            var loadState = LoadSuccess
            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "null/null")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val maxNum = DataStoreUtils.getSyncData(DataKey.WidgetShowNum, 10)
            val filterState = DataStoreUtils.getSyncData(DataKey.WidgetFilterState, "")
            val filterLabelsString = DataStoreUtils.getSyncData(DataKey.WidgetFilterLabels, "")

            val listType: Type = object : TypeToken<List<Label?>?>() {}.type
            val filterLabelList: List<Label> =
                if (filterLabelsString.isBlank()) { listOf() }
                else { Gson().fromJson(filterLabelsString, listType) }

            var filterLabels = ""
            filterLabelList.forEach { label ->
                filterLabels += "${label.name},"
            }

            filterLabels = if (filterLabels.isBlank()) {
                ""
            } else {
                filterLabels.substring(0, filterLabels.length-1)
            }

            val todoTitleList: MutableList<TodoListWidgetShowData> = mutableListOf()

            if (repoPath == "null/null" || token.isBlank()) {
                loadState = LoadFailByOther
            }

            else {
                val todoListResponse = repoApi.getAllIssues(
                    repoPath.split("/")[0],
                    repoPath.split("/")[1],
                    token,
                    state = filterState.ifBlank { null },
                    labels = filterLabels.ifBlank { null },
                    page = 1,
                    perPage = maxNum,
                )

                val todoList = if (todoListResponse.isSuccessful) {
                    todoListResponse.body() ?: listOf()
                }
                else {
                    loadState = todoListResponse.code()
                    listOf()
                }

                for (todo in todoList) {
                    val isChecked = todo.state != IssueState.OPEN.des
                    todoTitleList.add(TodoListWidgetShowData(todo.title, todo.number, isChecked))
                }
            }

            // 所有 widget 都更新成同一个内容
            val glanceIdList = GlanceAppWidgetManager(context).getGlanceIds(TodoListWidget::class.java)
            for (glanceId in glanceIdList) {
                glanceId.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            this[TodoListKey] = todoTitleList.toJson()
                            this[LoadStateKey] = loadState
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }

    private fun updateIssueState(context: Context, issueNum: String, isChecked: Boolean) {
        coroutineScope.launch {
            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "null/null")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val response = repoApi.updateIssues(
                repoPath.split("/")[0],
                issueNum,
                UpdateIssue(
                    token,
                    repo = repoPath.split("/")[1],
                    state = if (isChecked) IssueState.CLOSED.des else IssueState.OPEN.des
                )
            )
            if (response.isSuccessful) {
                Log.i(TAG, "updateIssueState: change #$issueNum state to $isChecked success!")
                // refreshData(context)  // 更新组件数据
            }
            else {
                val result = kotlin.runCatching {
                    Log.w(TAG, "updateIssueState: 更新失败：${response.errorBody()?.string()}")
                }
                if (result.isFailure) {
                    Log.w(TAG, "更新失败，获取失败信息错误：${result.exceptionOrNull()?.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "el, TodoListWidgetReceiver"


        val TodoListKey = stringPreferencesKey("todo_List")
        /**
         * 加载状态，成功：[LoadSuccess]；由于网络请求失败：失败的 Http 状态码；其他错误：[LoadFailByOther]
         * */
        val LoadStateKey = intPreferencesKey("list_load_State")

        /**
         * 加载成功
         * */
        const val LoadSuccess = 0
        /**
         * 加载失败，其他错误
         * */
        const val LoadFailByOther = -1
    }
}