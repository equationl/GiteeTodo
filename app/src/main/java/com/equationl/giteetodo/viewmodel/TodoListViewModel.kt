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
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "el, TodoListViewModel"

class TodoListViewModel: ViewModel() {
    private val reposApi = RetrofitManger.getReposApi()
    private val queryFlow = MutableStateFlow(QueryParameter())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val issueData = queryFlow.flatMapLatest {
        Log.i(TAG, "更新: $it")
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

    fun dispatch(action: TodoListViewAction) {
        when (action) {
            is TodoListViewAction.SetRepoPath -> setRepoPath(action.repoPath)
            is TodoListViewAction.CheckFitterOnlyOpen -> checkFitterOnlyOpen(action.checked)
            is TodoListViewAction.UpdateIssueState -> updateIssueState(action.issueNum, action.isClose, action.repoPath)
            is TodoListViewAction.SendMsg -> sendMsg(action.msg)
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

    private fun checkFitterOnlyOpen(checked: Boolean) {
        viewStates = viewStates.copy(isFitterOnlyOpen = checked)
        DataStoreUtils.putSyncData(DataKey.FilterOnlyOpenIssue, checked)
        val state = if (checked) "open" else "all"
        viewModelScope.launch {
            queryFlow.emit(queryFlow.value.copy(state = state))
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
    val isFitterOnlyOpen: Boolean = DataStoreUtils.getSyncData(DataKey.FilterOnlyOpenIssue, true),
)

sealed class TodoListViewEvent {
    data class ShowMessage(val message: String) : TodoListViewEvent()
}

sealed class TodoListViewAction {
    data class CheckFitterOnlyOpen(val checked: Boolean) : TodoListViewAction()
    data class SetRepoPath(val repoPath: String): TodoListViewAction()
    data class UpdateIssueState(val issueNum: String, val isClose: Boolean, val repoPath: String): TodoListViewAction()
    data class SendMsg(val msg: String): TodoListViewAction()
}


data class TodoCardData(
    val createDate: String,
    val itemArray: ArrayList<TodoCardItemData>
)

data class TodoCardItemData(
    val title: String,
    val state: IssueState,
    val number: String
)

data class QueryParameter(
    val repoPath: String = "null/null",
    val state: String = if (DataStoreUtils.getSyncData(DataKey.FilterOnlyOpenIssue, true)) "open" else "all",
    val accessToken: String = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
)