package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.model.request.CreateComment
import com.equationl.giteetodo.data.repos.model.request.CreateIssues
import com.equationl.giteetodo.data.repos.model.request.UpdateIssue
import com.equationl.giteetodo.data.repos.model.response.Comment
import com.equationl.giteetodo.data.repos.model.response.Issues
import com.equationl.giteetodo.ui.common.IssueState
import com.equationl.giteetodo.ui.common.getIssueState
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TodoDetailViewModel: ViewModel() {
    private val repoApi = RetrofitManger.getReposApi()

    var viewStates by mutableStateOf(TodoDetailViewState())
        private set

    private val _viewEvents = Channel<TodoDetailViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _viewEvents.send(TodoDetailViewEvent.ShowMessage("错误："+throwable.message))
        }
    }

    fun dispatch(action: TodoDetailViewAction) {
        when (action) {
            is TodoDetailViewAction.ToggleEditModel -> toggleEditModel(action.isEditAble)
            is TodoDetailViewAction.LoadIssue -> loadIssue(action.issueNum)
            is TodoDetailViewAction.OnContentChange -> onContentChange(action.text)
            is TodoDetailViewAction.OnTitleChange -> onTitleChange(action.text)
            is TodoDetailViewAction.ClickSave -> clickSave(action.issueNum)
            is TodoDetailViewAction.LabelsDropMenuShowState -> labelsDropMenuShowState(action.isShow)
            is TodoDetailViewAction.StateDropMenuShowState -> stateDropMenuShowState(action.isShow)
            is TodoDetailViewAction.UpdateLabels -> updateLabels(action.labels)
            is TodoDetailViewAction.UpdateState -> updateState(action.state)
            is TodoDetailViewAction.LoadComment -> loadComment(action.issueNum)
            is TodoDetailViewAction.ClickSaveComment -> clickSaveComment(action.issueNum)
            is TodoDetailViewAction.OnNewCommentChange -> onNewCommentChange(action.value)
        }
    }

    private fun clickSaveComment(issueNum: String) {
        viewModelScope.launch(exception) {
            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")

            val response = repoApi.createComment(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                issueNum,
                CreateComment(token, viewStates.newComment)
            )

            if (response.isSuccessful) {
                val newComment = response.body()
                if (newComment != null) {
                    val newCommentList = arrayListOf<Comment>()
                    newCommentList.addAll(viewStates.commentList)
                    newCommentList.add(newComment)

                    viewStates = viewStates.copy(commentList = newCommentList, newComment = "")
                }
                else {
                    loadComment(issueNum)
                }
                _viewEvents.send(TodoDetailViewEvent.ShowMessage("创建评论成功"))
            }
            else {
                viewStates = viewStates.copy(isLoading = false)

                val result = kotlin.runCatching {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("创建评论失败："+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("创建评论失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private fun onNewCommentChange(value: String) {
        viewStates = viewStates.copy(newComment = value)
    }

    private fun loadComment(issueNum: String) {
        viewModelScope.launch(exception) {
            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")

            val response = repoApi.getAllComments(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                issueNum,
                token)

            if (response.isSuccessful) {
                viewStates = viewStates.copy(commentList = response.body() ?: listOf())
            }
            else {
                viewStates = viewStates.copy(isLoading = false)

                val result = kotlin.runCatching {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("获取评论失败："+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("加载失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private fun toggleEditModel(isEditAble: Boolean) {
        viewStates = viewStates.copy(isEditAble = isEditAble, isLoading = false)
    }

    private fun onTitleChange(text: String) {
        viewStates = viewStates.copy(title = text)
    }

    private fun onContentChange(text: String) {
        viewStates = viewStates.copy(content = text)
    }

    private fun stateDropMenuShowState(isShow: Boolean) {
        viewStates = viewStates.copy(isShowStateDropMenu = isShow)
    }

    private fun labelsDropMenuShowState(isShow: Boolean) {
        if (isShow) {
            viewModelScope.launch(exception) {
                val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
                val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
                val response = repoApi.getExistLabels(
                    repoPath.split("/")[0],
                    repoPath.split("/")[1],
                    token
                )
                if (response.isSuccessful) {
                    val showLabelMap = mutableMapOf<String, Boolean>()
                    for (label in response.body() ?: listOf()) {
                        showLabelMap[label.name] = false
                    }

                    if (viewStates.labels != "未设置") {
                        val currentLabelList = viewStates.labels.split(", ")
                        for (currentLabel in currentLabelList) {
                            showLabelMap[currentLabel] = true
                        }
                    }

                    viewStates = viewStates.copy(isShowLabelsDropMenu = isShow, availableLabels = showLabelMap)
                }
                else {
                    viewStates = viewStates.copy(isShowLabelsDropMenu = false)

                    val result = kotlin.runCatching {
                        _viewEvents.send(TodoDetailViewEvent.ShowMessage("加载标签失败："+response.errorBody()?.string()))
                    }
                    if (result.isFailure) {
                        _viewEvents.send(TodoDetailViewEvent.ShowMessage("加载标签失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                    }
                }
            }
        }
        else {
            viewStates = viewStates.copy(isShowLabelsDropMenu = isShow)
        }
    }

    private fun updateState(state: IssueState) {
        viewStates = viewStates.copy(isShowStateDropMenu = false, state = state)
    }

    private fun updateLabels(labelMap: MutableMap<String, Boolean>) {
        var labels = ""
        labelMap.forEach { (name, checked) ->
            if (checked) labels += "$name, "
        }

        labels = if (labels.isBlank()) {
            "未设置"
        } else {
            labels.substring(0, labels.length-2)
        }
        viewStates = viewStates.copy(isShowLabelsDropMenu = false, labels = labels)
    }

    private fun loadIssue(issueNum: String, issue: Issues? = null) {
        viewModelScope.launch(exception) {
            val issueDetail = issue ?: requestIssue(issueNum)

            if (issueDetail == null) {
                viewStates = viewStates.copy(isLoading = false, title = "加载失败", content = "加载失败", createdDateTime = "加载失败", updateDateTime = "加载失败")
            }
            else {
                var labels = ""
                for (label in issueDetail.labels) {
                    labels += label.name + ", "
                }
                labels = if (labels.isBlank()) {
                    "未设置"
                } else {
                    labels.substring(0, labels.length-2)
                }
                viewStates = viewStates.copy(
                    isEditAble = false,
                    isLoading = false,
                    title = issueDetail.title,
                    createdDateTime = Utils.getDateTimeString(issueDetail.createdAt, "yyyy年 M月dd日 HH:mm:ss"),
                    updateDateTime = Utils.getDateTimeString(issueDetail.updatedAt, "yyyy年 M月dd日 HH:mm:ss"),
                    content = issueDetail.body ?: "",
                    state = getIssueState(issueDetail.state),
                    priority = issueDetail.priority,
                    labels = labels.ifBlank { "未设置" },
                    startDateTime = if (issueDetail.planStartedAt == null) "未设置" else Utils.getDateTimeString(issueDetail.planStartedAt, "yyyy年 M月dd日 HH:mm:ss"),
                    stopDateTime = if (issueDetail.deadline == null ) "未设置" else Utils.getDateTimeString(issueDetail.deadline, "yyyy年 M月dd日 HH:mm:ss")
                )
            }
        }
    }

    private suspend fun requestIssue(issueNum: String): Issues? {
        val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
        val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")

        val response = repoApi.getIssue(
            repoPath.split("/")[0],
            repoPath.split("/")[1],
            issueNum,
            token)

        if (response.isSuccessful) {
            return response.body()
        }
        else {
            viewStates = viewStates.copy(isLoading = false)

            val result = kotlin.runCatching {
                _viewEvents.send(TodoDetailViewEvent.ShowMessage("获取Issue失败："+response.errorBody()?.string()))
            }
            if (result.isFailure) {
                _viewEvents.send(TodoDetailViewEvent.ShowMessage("加载失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
            }
        }

        return null
    }

    private fun clickSave(issueNum: String) {
        viewStates = viewStates.copy(isEditAble = false)
        viewModelScope.launch(exception) {
            if (viewStates.title.isBlank()) {
                _viewEvents.send(TodoDetailViewEvent.ShowMessage("请至少输入标题！"))
                return@launch
            }

            val state = if (viewStates.state == IssueState.UNKNOWN) IssueState.OPEN else viewStates.state
            val labels = if (viewStates.labels == "未设置") null else viewStates.labels
            val body = viewStates.content.ifBlank { null }

            val repoPath = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")

            val response = if (issueNum == "null") {
                repoApi.createIssues(
                    repoPath.split("/")[0],
                    CreateIssues(
                        access_token = token, repo = repoPath.split("/")[1],
                        title = viewStates.title, body = body,
                        issue_type = state.des, labels = labels
                    )
                )
            }
            else {
                repoApi.updateIssues(
                    repoPath.split("/")[0],
                    issueNum,
                    UpdateIssue(
                        access_token = token, repo = repoPath.split("/")[1],
                        title = viewStates.title, body = body,
                        state = state.des, labels = labels
                    )
                )
            }

            if (response.isSuccessful) {
                _viewEvents.send(TodoDetailViewEvent.ShowMessage("保存成功"))
                loadIssue(response.body()?.number ?: "null", response.body())
            }
            else {
                viewStates = viewStates.copy(isEditAble = true)

                val result = kotlin.runCatching {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("保存失败"+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(TodoDetailViewEvent.ShowMessage("保存失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }
}

fun Int.getPriorityString() =
    when (this) {
        0 -> "未指定"
        1 -> "不重要"
        2 -> "次要"
        3 -> "主要"
        4 -> "严重"
        else -> "未指定"
    }

data class TodoDetailViewState(
    val isEditAble: Boolean = false,
    val title: String = "",
    val createdDateTime: String = "",
    val updateDateTime: String = "",
    val content: String = "",
    val state: IssueState = IssueState.UNKNOWN,
    val priority: Int = 0,
    val labels: String = "未设置",
    val startDateTime: String = "",
    val stopDateTime: String = "",
    val newComment: String = "",
    val isLoading: Boolean = true,
    val isShowLabelsDropMenu: Boolean = false,
    val isShowStateDropMenu: Boolean = false,
    val availableLabels: MutableMap<String, Boolean> = mutableMapOf(),
    val commentList: List<Comment> = listOf()
)

sealed class TodoDetailViewEvent {
    data class ShowMessage(val message: String) : TodoDetailViewEvent()
}

sealed class TodoDetailViewAction {
    data class ToggleEditModel(val isEditAble: Boolean) : TodoDetailViewAction()
    data class StateDropMenuShowState(val isShow: Boolean): TodoDetailViewAction()
    data class LabelsDropMenuShowState(val isShow: Boolean): TodoDetailViewAction()
    data class ClickSave(val issueNum: String): TodoDetailViewAction()
    data class LoadIssue(val issueNum: String): TodoDetailViewAction()
    data class LoadComment(val issueNum: String): TodoDetailViewAction()
    data class OnTitleChange(val text: String): TodoDetailViewAction()
    data class OnContentChange(val text: String): TodoDetailViewAction()
    data class UpdateState(val state: IssueState): TodoDetailViewAction()
    data class UpdateLabels(val labels: MutableMap<String, Boolean>): TodoDetailViewAction()
    data class OnNewCommentChange(val value: String): TodoDetailViewAction()
    data class ClickSaveComment(val issueNum: String): TodoDetailViewAction()
}