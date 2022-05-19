package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.toJson
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class SettingViewModel : ViewModel() {
    private val exception = CoroutineExceptionHandler { _, throwable ->
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
            is SettingViewAction.InitSetting -> initSetting()
            is SettingViewAction.ChoiceANewNum -> choiceANewNum(action.num)
            is SettingViewAction.ChoiceANewState -> choiceANewState(action.state)
            is SettingViewAction.ChoiceANewLabel -> choiceANewLabel(action.label, action.isChecked)
        }
    }

    private fun choiceANewLabel(label: Label, isChecked: Boolean) {
        val newLabel = mutableListOf<Label>()
        newLabel.addAll(viewStates.currentChoiceLabel)

        if (isChecked) {
            // 只有当前标签未加入筛选列表才继续加入
            if (viewStates.currentChoiceLabel.indexOfFirst { it.id == label.id } == -1) {
                newLabel.add(label)
                DataStoreUtils.putSyncData(DataKey.WidgetFilterLabels, newLabel.toJson())
            }
        }
        else {
            newLabel.removeIf { it.id == label.id }
            DataStoreUtils.putSyncData(DataKey.WidgetFilterLabels, newLabel.toJson())
        }
        viewStates = viewStates.copy(currentChoiceLabel = newLabel)
    }

    private fun choiceANewNum(num: Int) {
        if (num == viewStates.currentShowNum) {
            return
        }
        viewModelScope.launch(exception) {
            DataStoreUtils.putSyncData(DataKey.WidgetShowNum, num)
            viewStates = viewStates.copy(currentShowNum = num)
        }
    }

    private fun choiceANewState(state: String) {
        if (state == viewStates.currentState) {
            return
        }
        viewModelScope.launch(exception) {
            DataStoreUtils.putSyncData(DataKey.WidgetFilterState, state)
            viewStates = viewStates.copy(currentState = state)
        }
    }

    private fun initSetting() {
        viewModelScope.launch(exception) {
            val existLabels = Utils.getExistLabel()
            val currentShowNum = DataStoreUtils.getSyncData(DataKey.WidgetShowNum, 10)

            val currentChoiceLabelString = DataStoreUtils.getSyncData(DataKey.WidgetFilterLabels, "")
            val currentState = DataStoreUtils.getSyncData(DataKey.WidgetFilterState, "")

            val listType: Type = object : TypeToken<List<Label?>?>() {}.type
            val currentChoiceLabel: List<Label> =
                if (currentChoiceLabelString.isBlank()) { listOf() }
                else { Gson().fromJson(currentChoiceLabelString, listType) }

            viewStates = viewStates.copy(
                existLabels = existLabels,
                currentChoiceLabel = currentChoiceLabel,
                currentShowNum = currentShowNum,
                currentState = currentState.ifBlank { IssueState.OPEN.des }
            )
        }
    }
}

data class SettingViewState(
    val currentShowNum: Int = 10,
    val currentChoiceLabel: List<Label> = listOf(),
    val currentState: String = IssueState.OPEN.des,
    val existLabels: List<Label> = listOf()
)

sealed class SettingViewEvent {
    data class Goto(val route: String): SettingViewEvent()
    data class ShowMessage(val message: String): SettingViewEvent()
}

sealed class SettingViewAction {
    object InitSetting : SettingViewAction()
    data class ChoiceANewNum(val num: Int): SettingViewAction()
    data class ChoiceANewState(val state: String): SettingViewAction()
    data class ChoiceANewLabel(val label: Label, val isChecked: Boolean): SettingViewAction()
}

object SettingOption {
    val MaxShowNum = listOf(5, 10, 15, 20)

    val availableState = listOf(
        IssueState.OPEN,
        IssueState.PROGRESSING,
        IssueState.CLOSED,
        IssueState.REJECTED,
        IssueState.ALL
    )
}