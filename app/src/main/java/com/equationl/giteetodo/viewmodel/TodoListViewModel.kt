package com.equationl.giteetodo.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.model.pagingSource.IssuesPagingSource
import com.equationl.giteetodo.data.repos.model.request.UpdateIssue
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "el, TodoListViewModel"

class TodoListViewModel: ViewModel() {
    private var filterDate = ""
    private val reposApi = RetrofitManger.getReposApi()
    private val queryFlow = MutableStateFlow(QueryParameter())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val issueData = queryFlow.flatMapLatest {
        Pager(
            PagingConfig(pageSize = 50, initialLoadSize = 50)
        ) {
            IssuesPagingSource(reposApi, it)
        }.flow.cachedIn(viewModelScope)
    }

    var viewStates by mutableStateOf(TodoListViewState(todoFlow = issueData))
        private set

    private val _viewEvents = Channel<TodoListViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _viewEvents.send(TodoListViewEvent.ShowMessage("错误："+throwable.message))
        }
    }

    fun dispatch(action: TodoListViewAction) {
        when (action) {
            is TodoListViewAction.ClearFilter -> clearFilter()
            is TodoListViewAction.SetRepoPath -> setRepoPath(action.repoPath)
            is TodoListViewAction.UpdateIssueState -> updateIssueState(action.issueNum, action.isClose, action.repoPath)
            is TodoListViewAction.SendMsg -> sendMsg(action.msg)
            is TodoListViewAction.FilterLabels -> filterLabels(action.labels)
            is TodoListViewAction.ChangeLabelsDropMenuShowState -> changeLabelsDropMenuShowState(action.isShow)
            is TodoListViewAction.ChangeStateDropMenuShowState -> changeStateDropMenuShowState(action.isShow)
            is TodoListViewAction.FilterState -> filterState(action.state)
            is TodoListViewAction.ChangeDirectionDropMenuShowState -> changeDirectionDropMenuShowState(action.isShow)
            is TodoListViewAction.FilterDirection -> filterDirection(action.direction)
            is TodoListViewAction.FilterDate -> filterDate(action.date, action.isStart)
        }
    }

    private fun clearFilter() {
        viewModelScope.launch {
            viewStates.filteredOptionList.clear()
            queryFlow.emit(queryFlow.value.copy(
                state = null,
                labels = null,
                direction = "desc",
                createdAt = null
            ))
        }
    }

    private fun filterDate(date: LocalDate, isStart: Boolean) {
        Log.i(TAG, "filterDate: date=$date, isStart=$isStart")
        if (isStart) {
            filterDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA))
            filterDate += "T000000+8-"
        }
        else {
            filterDate += date.format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA))
            filterDate += "T235959+8"

            viewModelScope.launch {
                queryFlow.emit(queryFlow.value.copy(createdAt = filterDate))
            }
            if (!viewStates.filteredOptionList.contains(FilteredOption.DateTime)) {
                viewStates.filteredOptionList.add(FilteredOption.DateTime)
            }
        }
    }

    private fun filterState(state: IssueState) {
        viewModelScope.launch {
            viewStates = viewStates.copy(isShowStateDropMenu = false)
            queryFlow.emit(queryFlow.value.copy(state = state.des))
            if (!viewStates.filteredOptionList.contains(FilteredOption.States)) {
                viewStates.filteredOptionList.add(FilteredOption.States)
            }
        }
    }

    private fun filterDirection(state: Direction) {
        viewModelScope.launch {
            viewStates = viewStates.copy(isShowDirectionDropMenu = false)
            queryFlow.emit(queryFlow.value.copy(direction = state.des))
            if (!viewStates.filteredOptionList.contains(FilteredOption.Direction)) {
                viewStates.filteredOptionList.add(FilteredOption.Direction)
            }
        }
    }

    private fun filterLabels(labelMap: MutableMap<String, Boolean>) {
        viewStates = viewStates.copy(isShowLabelsDropMenu = false)

        var labels = ""
        labelMap.forEach { (name, checked) ->
            if (checked) labels += "$name,"
        }

        labels = if (labels.isBlank()) {
            ""
        } else {
            labels.substring(0, labels.length-1)
        }

        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(labels = labels.ifBlank { null }))
            if (labels.isBlank()) {
                viewStates.filteredOptionList.remove(FilteredOption.Labels)
            }
            else if (!viewStates.filteredOptionList.contains(FilteredOption.Labels)) {
                viewStates.filteredOptionList.add(FilteredOption.Labels)
            }
        }
    }

    private fun changeStateDropMenuShowState(isShow: Boolean) {
        viewStates = viewStates.copy(isShowStateDropMenu = isShow)
    }

    private fun changeDirectionDropMenuShowState(isShow: Boolean) {
        viewStates = viewStates.copy(isShowDirectionDropMenu = isShow)
    }

    private fun changeLabelsDropMenuShowState(isShow: Boolean) {
        if (isShow) {
            viewModelScope.launch(exception) {
                val labelList = Utils.getExistLabel()

                if (labelList.isEmpty()) {
                    viewStates = viewStates.copy(isShowLabelsDropMenu = false)
                    _viewEvents.send(TodoListViewEvent.ShowMessage("加载标签失败"))
                }
                else {
                    val selectedLabels = queryFlow.value.labels

                    val showLabelMap = mutableMapOf<String, Boolean>()
                    for (label in labelList) {
                        showLabelMap[label.name] = false
                    }

                    if (selectedLabels != null) {
                        val currentLabelList = selectedLabels.split(",")
                        for (currentLabel in currentLabelList) {
                            showLabelMap[currentLabel] = true
                        }
                    }

                    viewStates = viewStates.copy(isShowLabelsDropMenu = isShow, availableLabels = showLabelMap)
                }
            }
        }
        else {
            viewStates = viewStates.copy(isShowLabelsDropMenu = isShow)
        }
    }

    private fun sendMsg(msg: String) {
        viewModelScope.launch {
            _viewEvents.send(TodoListViewEvent.ShowMessage(msg))
        }
    }

    private fun setRepoPath(repoPath: String) {
        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(repoPath = repoPath))
        }
    }

    private fun updateIssueState(issueNum: String, isClose: Boolean, repoPath: String) {
        viewModelScope.launch {
            val response = reposApi.updateIssues(
                repoPath.split("/")[0],
                issueNum,
                UpdateIssue(
                    DataStoreUtils.getSyncData(DataKey.LoginAccessToken, ""),
                    repo = repoPath.split("/")[1],
                    state = if (isClose) IssueState.CLOSED.des else IssueState.OPEN.des
                )
            )
            if (response.isSuccessful) {
                _viewEvents.send(TodoListViewEvent.ShowMessage("标记成功"))
            }
            else {
                val result = kotlin.runCatching {
                    _viewEvents.send(TodoListViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
                }
                if (result.isFailure) {
                    _viewEvents.send(TodoListViewEvent.ShowMessage("更新失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }
}

data class TodoListViewState(
    val todoFlow: Flow<PagingData<TodoCardData>>,
    val availableLabels: MutableMap<String, Boolean> = mutableMapOf(),
    val isShowLabelsDropMenu: Boolean = false,
    val isShowStateDropMenu: Boolean = false,
    val isShowDirectionDropMenu: Boolean = false,
    val filteredOptionList: ArrayList<FilteredOption> = arrayListOf()
)

sealed class TodoListViewEvent {
    data class ShowMessage(val message: String) : TodoListViewEvent()
}

sealed class TodoListViewAction {
    object ClearFilter: TodoListViewAction()
    data class SetRepoPath(val repoPath: String): TodoListViewAction()
    data class UpdateIssueState(val issueNum: String, val isClose: Boolean, val repoPath: String): TodoListViewAction()
    data class SendMsg(val msg: String): TodoListViewAction()
    data class FilterLabels(val labels: MutableMap<String, Boolean>): TodoListViewAction()
    data class ChangeLabelsDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterState(val state: IssueState): TodoListViewAction()
    data class ChangeStateDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterDirection(val direction: Direction): TodoListViewAction()
    data class ChangeDirectionDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterDate(val date: LocalDate, val isStart: Boolean): TodoListViewAction()
}


data class TodoCardData(
    val cardTitle: String,
    val itemArray: ArrayList<TodoCardItemData>
)

data class TodoCardItemData(
    val title: String,
    val state: IssueState,
    val number: String
)

data class QueryParameter(
    val repoPath: String = "null/null",
    val state: String? = null,
    val accessToken: String = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, ""),
    val labels: String? = null,
    val direction: String = "desc",
    val createdAt: String? = null
)

enum class FilteredOption {
    Labels,
    States,
    DateTime,
    Direction
}