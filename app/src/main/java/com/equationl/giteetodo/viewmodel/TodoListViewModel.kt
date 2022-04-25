package com.equationl.giteetodo.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.model.request.UpdateIssue
import com.equationl.giteetodo.datastore.DataKey
import com.equationl.giteetodo.datastore.DataStoreUtils
import com.equationl.giteetodo.util.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

private const val TAG = "el, TodoListViewModel"

class TodoListViewModel: ViewModel() {
    private val reposApi = RetrofitManger.getReposApi()

    var viewStates by mutableStateOf(TodoListViewState())
        private set

    private val _viewEvents = Channel<TodoListViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: TodoListViewAction) {
        when (action) {
            is TodoListViewAction.LoadIssues -> loadIssues(action.repoPath)
            is TodoListViewAction.CheckFitterOnlyOpen -> checkFitterOnlyOpen(action.checked)
            is TodoListViewAction.UpdateIssueState -> updateIssueState(action.issueNum, action.isClose, action.repoPath)
        }
    }

    private fun loadIssues(repoPath: String) {
        viewStates = viewStates.copy(isLoading = true)

        viewModelScope.launch {
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccess, "")
            val response = reposApi.getAllIssues(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                token,
                state = if (viewStates.isFitterOnlyOpen) "open" else "all",
                page = 1  // TODO 加载下一页（数量和页码信息在 header 中）
            )

            if (response.isSuccessful) {
                val issueList = response.body()
                if (issueList.isNullOrEmpty()) {
                    viewStates = viewStates.copy(isLoading = false, todoList = listOf())
                    Log.i(TAG, "loadIssues: 内容为空！")
                    return@launch
                }
                val todoCardDataList = arrayListOf<TodoCardData>()
                var currentDate = Utils.getDateTimeString(issueList[0].createdAt)
                val currentItem = arrayListOf<TodoCardItemData>()

                for (issue in issueList) {
                    val issueDate = Utils.getDateTimeString(issue.createdAt)
                    Log.i(TAG, "loadIssues: issue=${issue.title}, date=${issue.createdAt}, currentDate=$currentDate, issueDate=$issueDate")
                    if (issueDate != currentDate) {
                        val tempItem: ArrayList<TodoCardItemData> = arrayListOf()
                        tempItem.addAll(currentItem)
                        todoCardDataList.add(
                            TodoCardData(currentDate, tempItem)
                        )
                        Log.i(TAG, "loadIssues: 添加：$currentDate, $tempItem, $currentItem")
                        currentDate = issueDate
                        currentItem.clear()

                        val state = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
                        currentItem.add(
                            TodoCardItemData(issue.title, state, issue.number)
                        )
                    }
                    else {
                        val state = try { IssueState.valueOf(issue.state.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
                        currentItem.add(
                            TodoCardItemData(issue.title, state, issue.number)
                        )
                    }
                }

                val tempItem: ArrayList<TodoCardItemData> = arrayListOf()
                tempItem.addAll(currentItem)
                todoCardDataList.add(
                    TodoCardData(currentDate, tempItem)
                )

                viewStates = viewStates.copy(isLoading = false, todoList = todoCardDataList)
                Log.i(TAG, "loadIssues: cardList=$todoCardDataList")
            }
            else {
                viewStates = viewStates.copy(isLoading = false, todoList = listOf())

                val result = kotlin.runCatching {
                    _viewEvents.send(TodoListViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
                }
                if (result.isFailure) {
                    _viewEvents.send(TodoListViewEvent.ShowMessage("加载失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }

        }
    }

    private fun checkFitterOnlyOpen(checked: Boolean) {
        viewStates = viewStates.copy(isFitterOnlyOpen = checked)
        DataStoreUtils.putSyncData(DataKey.FilterOnlyOpenIssue, checked)
    }

    private fun updateIssueState(issueNum: String, isClose: Boolean, repoPath: String) {
        viewModelScope.launch {
            val response = reposApi.updateIssues(
                repoPath.split("/")[0],
                issueNum,
                UpdateIssue(
                    DataStoreUtils.getSyncData(DataKey.LoginAccess, ""),
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
    val isLoading: Boolean = true,
    val isFitterOnlyOpen: Boolean = DataStoreUtils.getSyncData(DataKey.FilterOnlyOpenIssue, true),
    val todoList: List<TodoCardData> = listOf(),
)

sealed class TodoListViewEvent {
    data class ShowMessage(val message: String) : TodoListViewEvent()
}

sealed class TodoListViewAction {
    data class CheckFitterOnlyOpen(val checked: Boolean) : TodoListViewAction()
    data class LoadIssues(val repoPath: String): TodoListViewAction()
    data class UpdateIssueState(val issueNum: String, val isClose: Boolean, val repoPath: String): TodoListViewAction()
}


data class TodoCardData(
    val createDate: String,
    val itemArray: List<TodoCardItemData>
)

data class TodoCardItemData(
    val title: String,
    val state: IssueState,
    val number: String
)

enum class IssueState(val des: String) {
    OPEN("open"),
    PROGRESSING("progressing"),
    CLOSED("closed"),
    REJECTED("rejected")
}