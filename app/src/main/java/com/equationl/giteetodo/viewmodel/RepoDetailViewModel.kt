package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.user.model.request.UserRepos
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RepoDetailViewModel: ViewModel() {
    private val repoApi = RetrofitManger.getReposApi()

    var viewStates by mutableStateOf(RepoDetailViewState())
        private set

    private val _viewEvents = Channel<RepoDetailViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoDetailViewAction) {
        when (action) {
            is RepoDetailViewAction.Create -> create()
            is RepoDetailViewAction.ChangeDescribe -> changeDescribe(action.text)
            is RepoDetailViewAction.ChangeIsInit -> changeIsInit(action.checked)
            is RepoDetailViewAction.ChangeIsPrivate -> changeIsPrivate(action.checked)
            is RepoDetailViewAction.ChangeName -> changeName(action.text)
            is RepoDetailViewAction.ChangePath -> changePath(action.text)
        }
    }

    private fun create() {
        viewStates = viewStates.copy(isUnderCreation = true)

        viewModelScope.launch {
            if (viewStates.repoName.isBlank()) {
                _viewEvents.send(RepoDetailViewEvent.ShowMessage("至少需要输入仓库名称！"))
                viewStates = viewStates.copy(isUnderCreation = false)
                return@launch
            }

            val token = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")

            val response = repoApi.createRepos(UserRepos(
                access_token = token,
                name = viewStates.repoName,
                description = viewStates.repoDescribe,
                path = viewStates.repoPath,
                auto_init = viewStates.isInitRepo,
                private = viewStates.isPrivateRepo
            ))

            if (response.isSuccessful) {
                val repos = response.body()
                if (repos == null) {
                    _viewEvents.send(RepoDetailViewEvent.ShowMessage("返回数据为空！"))
                    viewStates = viewStates.copy(isUnderCreation = false)
                    return@launch
                }
                DataStoreUtils.putSyncData(DataKey.UsingRepo, repos.fullName)

                if (viewStates.isInitRepo) {
                    // TODO 可以做一些初始化工作，比如把 README 文件改成仓库说明
                }

                kotlin.runCatching {
                    val encodeRepoPath = URLEncoder.encode(repos.fullName, StandardCharsets.UTF_8.toString())
                    val fullRoute = "${Route.HOME}/$encodeRepoPath"
                    _viewEvents.send(RepoDetailViewEvent.Goto(fullRoute))
                }
            }
            else {
                viewStates = viewStates.copy(isUnderCreation = false)

                val result = kotlin.runCatching {
                    _viewEvents.send(RepoDetailViewEvent.ShowMessage("创建失败："+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(RepoDetailViewEvent.ShowMessage("创建失败，获取失败信息错误：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private fun changeDescribe(text: String) {
        viewStates = viewStates.copy(repoDescribe = text)
    }
    private fun changeIsInit(checked: Boolean) {
        viewStates = viewStates.copy(isInitRepo = checked)
    }
    private fun changeIsPrivate(checked: Boolean) {
        viewStates = viewStates.copy(isPrivateRepo = checked)
    }
    private fun changeName(text: String) {
        viewStates = viewStates.copy(repoName = text)
    }
    private fun changePath(text: String) {
        viewStates = viewStates.copy(repoPath = text)
    }

}

data class RepoDetailViewState(
    val repoName: String = "",
    val repoDescribe: String = "",
    val repoPath: String = "",
    val isPrivateRepo: Boolean = true,
    val isInitRepo: Boolean = true,
    val isUnderCreation: Boolean = false
)

sealed class RepoDetailViewEvent {
    data class Goto(val route: String): RepoDetailViewEvent()
    data class ShowMessage(val message: String) : RepoDetailViewEvent()
}

sealed class RepoDetailViewAction {
    object Create : RepoDetailViewAction()
    data class ChangeName(val text: String): RepoDetailViewAction()
    data class ChangeDescribe(val text: String): RepoDetailViewAction()
    data class ChangePath(val text: String): RepoDetailViewAction()
    data class ChangeIsPrivate(val checked: Boolean): RepoDetailViewAction()
    data class ChangeIsInit(val checked: Boolean): RepoDetailViewAction()
}