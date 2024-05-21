package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.db.IssueDb
import com.equationl.giteetodo.data.repos.model.common.TodoShowData
import com.equationl.giteetodo.data.repos.model.request.UpdateIssue
import com.equationl.giteetodo.data.repos.paging.remoteMediator.IssueRemoteMediator
import com.equationl.giteetodo.ui.common.Direction
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.fromJson
import com.equationl.giteetodo.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val repoApi: RepoApi,
    private val dataBase: IssueDb
) : ViewModel() {
    private var filterDate = ""
    private val queryFlow = MutableStateFlow(QueryParameter())

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
    private val issueData = queryFlow.flatMapLatest {
        Pager(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50),
            remoteMediator = IssueRemoteMediator(it, dataBase, repoApi)
        ) {
            if (it.direction == Direction.ASC.des) {
                dataBase.issue().pagingSourceOrderByAsc()
            }
            else {
                dataBase.issue().pagingSourceOrderByDesc()
            }
        }
            .flow
            /*.map { pagingData ->
                pagingData.map { issue ->
                    issue.convertToShowData()
                }
            } */
            .cachedIn(viewModelScope)
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
            is TodoListViewAction.AutoRefreshFinish -> autoFreshFinish()
            is TodoListViewAction.Init -> init(action.repoPath)
            is TodoListViewAction.UpdateIssueState -> updateIssueState(action.issueNum, action.isClose, action.repoPath)
            is TodoListViewAction.SendMsg -> sendMsg(action.msg)
            is TodoListViewAction.FilterLabels -> filterLabels(action.labels)
            is TodoListViewAction.ChangeLabelsDropMenuShowState -> changeLabelsDropMenuShowState(action.isShow)
            is TodoListViewAction.ChangeStateDropMenuShowState -> changeStateDropMenuShowState(action.isShow)
            is TodoListViewAction.FilterState -> filterState(action.state)
            is TodoListViewAction.ChangeDirectionDropMenuShowState -> changeDirectionDropMenuShowState(action.isShow)
            is TodoListViewAction.FilterDirection -> filterDirection(action.direction)
            is TodoListViewAction.FilterDate -> filterDate(action.start, action.end)
            is TodoListViewAction.OnExit -> onExit()
        }
    }

    private fun autoFreshFinish() {
        viewStates = viewStates.copy(isAutoRefresh = true)
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

    private fun filterDate(start: Long, end: Long) {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.CHINA)

        val startDate: String = sdf.format(Date(start))
        val endDate: String = sdf.format(Date(end))
        filterDate += startDate
        filterDate += "T000000+8-"
        filterDate += endDate
        filterDate += "T235959+8"

        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(createdAt = filterDate))
        }
        if (!viewStates.filteredOptionList.contains(FilteredOption.DateTime)) {
            viewStates.filteredOptionList.add(FilteredOption.DateTime)
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
                val labelList = Utils.getExistLabel(repoApi)

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

                    viewStates = viewStates.copy(isShowLabelsDropMenu = true, availableLabels = showLabelMap)
                }
            }
        }
        else {
            viewStates = viewStates.copy(isShowLabelsDropMenu = false)
        }
    }

    private fun sendMsg(msg: String) {
        viewModelScope.launch {
            _viewEvents.send(TodoListViewEvent.ShowMessage(msg))
        }
    }

    private fun init(repoPath: String) {
        viewModelScope.launch {
            val saveFilter = DataStoreUtils.getSyncData(DataKey.FILTER_INFO, "")
            val newQuery: QueryParameter = if (saveFilter.isNotBlank()) {
                saveFilter.fromJson<QueryParameter>() ?: QueryParameter()
            } else {
                QueryParameter()
            }

            val filterList = arrayListOf<FilteredOption>()
            if (newQuery.direction != "desc") {
                filterList.add(FilteredOption.Direction)
            }
            if (newQuery.labels?.isNotBlank() == true) {
                filterList.add(FilteredOption.Labels)
            }
            if (newQuery.state?.isNotBlank() == true) {
                filterList.add(FilteredOption.States)
            }
            if (newQuery.createdAt?.isNotBlank() == true) {
                filterList.add(FilteredOption.DateTime)
            }

            if (filterList.isNotEmpty()) {
                viewStates = viewStates.copy(filteredOptionList = filterList)
            }

            queryFlow.emit(
                newQuery.copy(
                    repoPath = repoPath,
                    accessToken = DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, "")
                )
            )
        }
    }

    private fun onExit() {
        viewModelScope.launch(Dispatchers.IO) {
            val saveString = queryFlow.value.toJson()
            DataStoreUtils.saveSyncStringData(DataKey.FILTER_INFO, saveString)
        }
    }

    private fun updateIssueState(issueNum: String, isClose: Boolean, repoPath: String) {
        viewModelScope.launch {
            val response = repoApi.updateIssues(
                repoPath.split("/")[0],
                issueNum,
                UpdateIssue(
                    DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, ""),
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
    val todoFlow: Flow<PagingData<TodoShowData>>,
    val availableLabels: MutableMap<String, Boolean> = mutableMapOf(),
    val isShowLabelsDropMenu: Boolean = false,
    val isShowStateDropMenu: Boolean = false,
    val isShowDirectionDropMenu: Boolean = false,
    val filteredOptionList: ArrayList<FilteredOption> = arrayListOf(),
    val isAutoRefresh: Boolean = false
)

sealed class TodoListViewEvent {
    data class ShowMessage(val message: String) : TodoListViewEvent()
}

sealed class TodoListViewAction {
    data object ClearFilter: TodoListViewAction()
    data object AutoRefreshFinish: TodoListViewAction()
    data object OnExit: TodoListViewAction()
    data class Init(val repoPath: String): TodoListViewAction()
    data class UpdateIssueState(val issueNum: String, val isClose: Boolean, val repoPath: String): TodoListViewAction()
    data class SendMsg(val msg: String): TodoListViewAction()
    data class FilterLabels(val labels: MutableMap<String, Boolean>): TodoListViewAction()
    data class ChangeLabelsDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterState(val state: IssueState): TodoListViewAction()
    data class ChangeStateDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterDirection(val direction: Direction): TodoListViewAction()
    data class ChangeDirectionDropMenuShowState(val isShow: Boolean): TodoListViewAction()
    data class FilterDate(val start: Long, val end: Long): TodoListViewAction()
}

data class QueryParameter(
    val repoPath: String = "null/null",
    val state: String? = null,
    val accessToken: String = "",
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