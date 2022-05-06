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
import com.equationl.giteetodo.data.repos.model.pagingSource.ReposPagingSource
import com.equationl.giteetodo.data.user.model.response.Repos
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "el, RepoListViewModel"

class RepoListViewModel: ViewModel() {
    private val repoApi = RetrofitManger.getReposApi()

    private val reposData by lazy {
        Pager(
            PagingConfig(pageSize = 6, initialLoadSize = 6)
        ) {
            ReposPagingSource(repoApi, DataStoreUtils.getSyncData(DataKey.LoginAccessToken, ""))
        }.flow.cachedIn(viewModelScope)
    }

    var viewStates by mutableStateOf(RepoListViewState(repoFlow = reposData))
        private set

    private val _viewEvents = Channel<RepoListViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoListViewAction) {
        when (action) {
            is RepoListViewAction.SendMsg -> sendMsg(action.msg)
            is RepoListViewAction.ChoiceARepo -> choiceARepo(action.repoPath)
        }
    }

    private fun sendMsg(msg: String) {
        viewModelScope.launch {
            _viewEvents.send(RepoListViewEvent.ShowMessage(msg))
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
            Log.w(TAG, "choiceARepo: ", result.exceptionOrNull())
        }
    }
}

data class RepoListViewState(
    val repoFlow: Flow<PagingData<Repos>>
)

sealed class RepoListViewEvent {
    data class Goto(val route: String): RepoListViewEvent()
    data class ShowMessage(val message: String) : RepoListViewEvent()
}

sealed class RepoListViewAction {
    data class SendMsg(val msg: String): RepoListViewAction()
    data class ChoiceARepo(val repoPath: String): RepoListViewAction()
}