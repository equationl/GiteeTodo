package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.datastore.DataKey
import com.equationl.giteetodo.datastore.DataStoreUtils
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
            is RepoListViewAction.CheckRepo -> checkRepo()
            is RepoListViewAction.ChoiceARepo -> choiceARepo(action.route, action.repoPath)
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

    private fun choiceARepo(route: String, repoPath: String) {
        viewModelScope.launch {
            DataStoreUtils.saveSyncStringData(DataKey.UsingRepo, repoPath)
            val result = kotlin.runCatching {
                println("repoPath=$repoPath")
                val encodeRepoPath = URLEncoder.encode(repoPath, StandardCharsets.UTF_8.toString())
                val fullRoute = "$route/$encodeRepoPath"
                _viewEvents.send(RepoListViewEvent.Goto(fullRoute))
            }
            println("null=${result.exceptionOrNull()?.stackTraceToString()}")
        }
    }

    private fun checkRepo() {
        val usingRepo = DataStoreUtils.getSyncData(DataKey.UsingRepo, "")
        viewStates = if (usingRepo.isBlank()) {
            viewStates.copy(isSelectedRepo = false, isCheckingRepo = false)
        } else {
            viewStates.copy(isSelectedRepo = true, isCheckingRepo = false, selectedRepo = usingRepo)
        }
    }
}

data class RepoListViewState(
    val repoList: List<RepoItemData> = listOf(),
    val selectedRepo: String = "",
    val isLoading: Boolean = true,
    // fixme me 如果将该状态也放入统一的状态管理，那么会由于其他状态的变化导致重复多次重组，并且热启动会死循环重组
    val isSelectedRepo: Boolean = false,
    val isCheckingRepo: Boolean = true
)

sealed class RepoListViewEvent {
    data class Goto(val route: String): RepoListViewEvent()
    data class ShowMessage(val message: String) : RepoListViewEvent()
}

sealed class RepoListViewAction {
    object CheckRepo: RepoListViewAction()
    object LoadRepos : RepoListViewAction()
    data class ChoiceARepo(val route: String, val repoPath: String): RepoListViewAction()
}

data class RepoItemData(
    val path: String,
    val notClosedCount: Int,
    val name: String,
    val createDate: String
)