package com.equationl.giteetodo.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.constants.ChooseRepoType
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.event.EventKey
import com.equationl.giteetodo.util.event.FlowBus
import com.equationl.giteetodo.util.toJson
import com.equationl.giteetodo.widget.TodoListWidget
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val repoApi: RepoApi
) : ViewModel() {
    private val exception = CoroutineExceptionHandler { _, throwable ->
        Log.e("el", "global exception: ${throwable.stackTraceToString()}")
        viewModelScope.launch {
            _viewEvents.send(SettingViewEvent.ShowMessage("出错：${throwable.message}"))
        }
    }

    var viewStates by mutableStateOf(SettingViewState())
        private set

    private val _viewEvents = Channel<SettingViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: SettingViewAction) {
        when (action) {
            is SettingViewAction.InitSetting -> initSetting(action.context)
            is SettingViewAction.ChoiceANewNum -> choiceANewNum(action.appWidgetId, action.num)
            is SettingViewAction.ChoiceANewState -> choiceANewState(action.appWidgetId, action.state)
            is SettingViewAction.ChoiceANewLabel -> choiceANewLabel(action.appWidgetId, action.label, action.isChecked)
        }
    }

    private fun settingWidgetRepo(repoPath: String, repoName: String, appWidgetId: Int) {
        val widgetSettingModel = mutableMapOf<Int, WidgetSettingModel>()
        widgetSettingModel.putAll(viewStates.widgetSettingMap)
        var currentSetting = widgetSettingModel[appWidgetId]
        if (currentSetting == null) {
            viewModelScope.launch {
                _viewEvents.send(SettingViewEvent.ShowMessage("Error： GlanceId not exist!"))
            }
            return
        }

        if (repoPath == currentSetting.repoPath) {
            return
        }
        viewModelScope.launch(exception) {
            currentSetting = currentSetting!!.copy(repoPath = repoPath, repoName = repoName)
            widgetSettingModel[appWidgetId] = currentSetting!!
            DataStoreUtils.putSyncData(DataKey.WIDGET_SETTING_MAP, widgetSettingModel.toJson())
            viewStates = viewStates.copy(widgetSettingMap = widgetSettingModel)
        }
    }

    private fun choiceANewLabel(appWidgetId: Int, label: Label, isChecked: Boolean) {
        val widgetSettingModel = mutableMapOf<Int, WidgetSettingModel>()
        widgetSettingModel.putAll(viewStates.widgetSettingMap)
        var currentSetting = widgetSettingModel[appWidgetId]
        if (currentSetting == null) {
            viewModelScope.launch {
                _viewEvents.send(SettingViewEvent.ShowMessage("Error： GlanceId not exist!"))
            }
            return
        }

        val newLabel = mutableListOf<Label>()
        newLabel.addAll(currentSetting.filterLabels)

        if (isChecked) {
            // 只有当前标签未加入筛选列表才继续加入
            if (currentSetting.filterLabels.indexOfFirst { it.id == label.id } == -1) {
                newLabel.add(label)
                currentSetting = currentSetting.copy(filterLabels = newLabel)
                widgetSettingModel[appWidgetId] = currentSetting
                DataStoreUtils.putSyncData(DataKey.WIDGET_SETTING_MAP, widgetSettingModel.toJson())
            }
        }
        else {
            newLabel.removeIf { it.id == label.id }
            currentSetting = currentSetting.copy(filterLabels = newLabel)
            widgetSettingModel[appWidgetId] = currentSetting
            DataStoreUtils.putSyncData(DataKey.WIDGET_SETTING_MAP, widgetSettingModel.toJson())
        }

        viewStates = viewStates.copy(widgetSettingMap = widgetSettingModel)
    }

    private fun choiceANewNum(appWidgetId: Int, num: Int) {
        val widgetSettingModel = mutableMapOf<Int, WidgetSettingModel>()
        widgetSettingModel.putAll(viewStates.widgetSettingMap)
        var currentSetting = widgetSettingModel[appWidgetId]
        if (currentSetting == null) {
            viewModelScope.launch {
                _viewEvents.send(SettingViewEvent.ShowMessage("Error： GlanceId not exist!"))
            }
            return
        }

        if (num == currentSetting.showNum) {
            return
        }
        viewModelScope.launch(exception) {
            currentSetting = currentSetting!!.copy(showNum = num)
            widgetSettingModel[appWidgetId] = currentSetting!!
            DataStoreUtils.putSyncData(DataKey.WIDGET_SETTING_MAP, widgetSettingModel.toJson())
            viewStates = viewStates.copy(widgetSettingMap = widgetSettingModel)
        }
    }

    private fun choiceANewState(appWidgetId: Int, state: String) {
        val widgetSettingModel = mutableMapOf<Int, WidgetSettingModel>()
        widgetSettingModel.putAll(viewStates.widgetSettingMap)
        var currentSetting = widgetSettingModel[appWidgetId]
        if (currentSetting == null) {
            viewModelScope.launch {
                _viewEvents.send(SettingViewEvent.ShowMessage("Error： GlanceId not exist!"))
            }
            return
        }

        if (state == currentSetting.filterState) {
            return
        }
        viewModelScope.launch(exception) {
            currentSetting = currentSetting!!.copy(filterState = state)
            widgetSettingModel[appWidgetId] = currentSetting!!
            DataStoreUtils.putSyncData(DataKey.WIDGET_SETTING_MAP, widgetSettingModel.toJson())
            viewStates = viewStates.copy(widgetSettingMap = widgetSettingModel)
        }
    }

    private fun initSetting(context: Context) {
        viewModelScope.launch(exception) {
            launch(Dispatchers.IO) {
                initEvent()
            }

            val existLabels = Utils.getExistLabel(repoApi = repoApi)

            val glanceManager = GlanceAppWidgetManager(context)
            val glanceList = glanceManager.getGlanceIds(TodoListWidget::class.java)

            val mapType: Type = object : TypeToken<MutableMap<Int, WidgetSettingModel>>() {}.type
            val widgetSettingString = DataStoreUtils.getSyncData(DataKey.WIDGET_SETTING_MAP, "")
            val rawWidgetSettingMap: MutableMap<Int, WidgetSettingModel> = if (widgetSettingString.isBlank()) mutableMapOf() else Gson().fromJson(widgetSettingString, mapType)
            val widgetSettingMap: MutableMap<Int, WidgetSettingModel> = mutableMapOf()
            widgetSettingMap.putAll(rawWidgetSettingMap)

            // 先移除已经不存在的数据
            rawWidgetSettingMap.forEach { (appWidgetId) ->
                try {
                    // 如果 widget 已被移除的话，这个方法会抛出 IllegalArgumentException: Invalid AppWidget ID.
                    glanceManager.getGlanceIdBy(appWidgetId)
                } catch (e: IllegalArgumentException) {
                    widgetSettingMap.remove(appWidgetId)
                }
            }

            // 添加初始化数据
            glanceList.forEach {
                val appWidgetId = glanceManager.getAppWidgetId(it)
                if (!widgetSettingMap.contains(appWidgetId)) {
                    widgetSettingMap[appWidgetId] = WidgetSettingModel(
                        appWidgetId = appWidgetId,
                        repoPath = DataStoreUtils.getSyncData(DataKey.USING_REPO, ""),
                        repoName = DataStoreUtils.getSyncData(DataKey.USING_REPO_NAME, ""),
                    )
                }
            }

            Log.i("el", "initSetting: glanceList = $glanceList")
            Log.i("el", "initSetting: weightSettingMap = $widgetSettingMap")

            viewStates = viewStates.copy(
                existLabels = existLabels,
                widgetSettingMap = widgetSettingMap
            )
        }
    }

    private suspend fun initEvent() {
        FlowBus.events.collect {
            Log.d("el", "SettingScreen: rcv new event: $it")

            if (it.type == EventKey.WidgetChooseRepo) {
                val repoPath = it.params[0]
                val repoName = it.params[1]
                settingWidgetRepo(repoPath.toString(), repoName.toString(), ChooseRepoType.currentWidgetAppId)
            }
        }
    }
}

data class SettingViewState(
    val existLabels: List<Label> = listOf(),
    /**
     * key: appWidgetId
     * */
    val widgetSettingMap: MutableMap<Int, WidgetSettingModel> = mutableMapOf()
)

sealed class SettingViewEvent {
    data class Goto(val route: String): SettingViewEvent()
    data class ShowMessage(val message: String): SettingViewEvent()
}

sealed class SettingViewAction {
    data class InitSetting(val context: Context) : SettingViewAction()
    data class ChoiceANewNum(val appWidgetId: Int, val num: Int): SettingViewAction()
    data class ChoiceANewState(val appWidgetId: Int, val state: String): SettingViewAction()
    data class ChoiceANewLabel(val appWidgetId: Int, val label: Label, val isChecked: Boolean): SettingViewAction()
}

object SettingOption {
    val maxShowNum = listOf(5, 10, 15, 20)

    val availableState = listOf(
        IssueState.OPEN,
        IssueState.PROGRESSING,
        IssueState.CLOSED,
        IssueState.REJECTED,
        IssueState.ALL
    )
}

data class WidgetSettingModel(
    val appWidgetId: Int = -1,
    val showNum: Int = 10,
    val filterLabels: List<Label> = listOf(),
    val filterState: String = IssueState.OPEN.des,
    val repoPath: String = "",
    val repoName: String = ""
)