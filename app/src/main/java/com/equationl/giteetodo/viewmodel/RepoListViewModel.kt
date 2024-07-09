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
import com.equationl.giteetodo.constants.ChooseRepoType
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.db.IssueDb
import com.equationl.giteetodo.data.repos.paging.pagingSource.ReposPagingSource
import com.equationl.giteetodo.data.user.model.response.Repo
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.event.EventKey
import com.equationl.giteetodo.util.event.FlowBus
import com.equationl.giteetodo.util.event.MessageEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "el, RepoListViewModel"

@HiltViewModel
class RepoListViewModel @Inject constructor(
    private val repoApi: RepoApi,
    private val dataBase: IssueDb
) : ViewModel() {

    private val reposData by lazy {
        Pager(
            PagingConfig(pageSize = 6, initialLoadSize = 6)
        ) {
            ReposPagingSource(repoApi, DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, ""))
        }.flow.cachedIn(viewModelScope)
    }

    var viewStates by mutableStateOf(RepoListViewState(repoFlow = reposData))
        private set

    private val _viewEvents = Channel<RepoListViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: RepoListViewAction) {
        when (action) {
            is RepoListViewAction.SendMsg -> sendMsg(action.msg)
            is RepoListViewAction.ChoiceARepo -> choiceARepo(action.repoPath, action.repoName, action.chooseType)
            is RepoListViewAction.DeleteRepo -> deleteRepo(action.repoPath)
        }
    }

    private fun deleteRepo(repoPath: String) {
        viewModelScope.launch {
            _viewEvents.send(RepoListViewEvent.ShowDeleteRepoMsg("请自行前往 Gitee 官网删除", "https://gitee.com/$repoPath/settings#remove"))
        }
    }

    private fun sendMsg(msg: String) {
        viewModelScope.launch {
            _viewEvents.send(RepoListViewEvent.ShowMessage(msg))
        }
    }

    private fun choiceARepo(repoPath: String, repoName: String, chooseType: String?) {
        viewModelScope.launch {
            when (chooseType) {
                ChooseRepoType.WIDGET_SETTING -> {
                    Log.i(TAG, "choiceARepo: choice for widget: repoPath=$repoPath, repoName=$repoName")
                    FlowBus.sendEvents.tryEmit(MessageEvent(EventKey.WidgetChooseRepo, repoPath, repoName))
                    _viewEvents.trySend(RepoListViewEvent.Pop)
                }
                else -> {
                    // 切换仓库前先清空当前缓存的所有数据
                    DataStoreUtils.saveSyncStringData(DataKey.FILTER_INFO, "")
                    dataBase.issue().clearAll()
                    dataBase.issueRemoteKey().clearAll()

                    DataStoreUtils.saveSyncStringData(DataKey.USING_REPO, repoPath)
                    DataStoreUtils.saveSyncStringData(DataKey.USING_REPO_NAME, repoName)
                    val result = kotlin.runCatching {
                        val encodeRepoPath = URLEncoder.encode(repoPath, StandardCharsets.UTF_8.toString())
                        val fullRoute = "${Route.HOME}/$encodeRepoPath"
                        _viewEvents.send(RepoListViewEvent.Goto(fullRoute))
                    }
                    Log.w(TAG, "choiceARepo: ", result.exceptionOrNull())
                }
            }
        }
    }
}

data class RepoListViewState(
    val repoFlow: Flow<PagingData<Repo>>
)

sealed class RepoListViewEvent {
    data object Pop: RepoListViewEvent()
    data class Goto(val route: String): RepoListViewEvent()
    data class ShowMessage(val message: String) : RepoListViewEvent()
    data class ShowDeleteRepoMsg(val message: String, val deleteUrl: String): RepoListViewEvent()
}

sealed class RepoListViewAction {
    data class SendMsg(val msg: String): RepoListViewAction()
    data class ChoiceARepo(val repoPath: String, val repoName: String, val chooseType: String? = null): RepoListViewAction()
    data class DeleteRepo(val repoPath: String): RepoListViewAction()
}