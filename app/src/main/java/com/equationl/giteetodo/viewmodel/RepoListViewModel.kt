package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.datastore.DataKey
import com.equationl.giteetodo.datastore.DataStoreUtils
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RepoListViewModel: ViewModel() {
    private val repoApi = RetrofitManger.getReposApi()

    var viewStates by mutableStateOf(RepoListViewState())
        private set

    private val _viewEvents = Channel<RepoListViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoListViewAction) {
        when (action) {
            is RepoListViewAction.LoadRepos -> loadRepos()
            is RepoListViewAction.ChoiceARepo -> choiceARepo(action.repoPath)
        }
    }

    private fun loadRepos() {
        //viewStates = viewStates.copy(isLoading = true)

        viewModelScope.launch {
            val accessToken = DataStoreUtils.getSyncData(DataKey.LoginAccess, "")
            val response = repoApi.getRepos(accessToken)
            if (response.isSuccessful) {
                viewStates = viewStates.copy(isLoading = false, repoList = Utils.resolveRepos(response.body()))
            }
            else {
                viewStates = viewStates.copy(isLoading = false)
                val result = kotlin.runCatching {
                    _viewEvents.send(RepoListViewEvent.ShowMessage(response.errorBody()?.string() ?: ""))
                }
                if (result.isFailure) {
                    _viewEvents.send(RepoListViewEvent.ShowMessage("登录失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private fun choiceARepo(repoPath: String) {
        viewModelScope.launch {
            DataStoreUtils.saveSyncStringData(DataKey.UsingRepo, repoPath)
            val result = kotlin.runCatching {
                println("repoPath=$repoPath")
                val encodeRepoPath = URLEncoder.encode(repoPath, StandardCharsets.UTF_8.toString())
                val fullRoute = "${Route.HOME}/$encodeRepoPath"
                _viewEvents.send(RepoListViewEvent.Goto(fullRoute))
            }
            println("null=${result.exceptionOrNull()?.stackTraceToString()}")
        }
    }
}

data class RepoListViewState(
    val repoList: List<RepoItemData> = listOf(),
    val selectedRepo: String = "",
    val isLoading: Boolean = true
)

sealed class RepoListViewEvent {
    data class Goto(val route: String): RepoListViewEvent()
    data class ShowMessage(val message: String) : RepoListViewEvent()
}

sealed class RepoListViewAction {
    object LoadRepos : RepoListViewAction()
    data class ChoiceARepo(val repoPath: String): RepoListViewAction()
}

data class RepoItemData(
    val path: String,
    val notClosedCount: Int,
    val name: String,
    val createDate: String
)