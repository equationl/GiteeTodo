package com.equationl.giteetodo.viewmodel

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.constants.DefaultText
import com.equationl.giteetodo.data.repos.RepoApi
import com.equationl.giteetodo.data.repos.model.request.UpdateContent
import com.equationl.giteetodo.data.user.model.request.UserRepos
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class RepoDetailViewModel @Inject constructor(
    private val repoApi: RepoApi
) : ViewModel() {

    var viewStates by mutableStateOf(RepoDetailViewState())
        private set

    private val _viewEvents = Channel<RepoDetailViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _viewEvents.send(RepoDetailViewEvent.ShowMessage("错误："+throwable.message))
        }
    }

    fun dispatch(action: RepoDetailViewAction) {
        when (action) {
            is RepoDetailViewAction.Create -> create()
            is RepoDetailViewAction.ChangeDescribe -> changeDescribe(action.text)
            is RepoDetailViewAction.ChangeIsInit -> changeIsInit(action.checked)
            is RepoDetailViewAction.ChangeIsPrivate -> changeIsPrivate(action.checked)
            is RepoDetailViewAction.ChangeName -> changeName(action.text)
            is RepoDetailViewAction.ChangePath -> changePath(action.text)
            is RepoDetailViewAction.ChangeReadmeContent -> changeReadmeContent(action.text)
        }
    }

    private fun create() {
        viewStates = viewStates.copy(isUnderCreation = true)

        viewModelScope.launch(exception) {
            if (viewStates.repoName.isBlank()) {
                _viewEvents.send(RepoDetailViewEvent.ShowMessage("至少需要输入仓库名称！"))
                viewStates = viewStates.copy(isUnderCreation = false)
                return@launch
            }

            val token = DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, "")

            val response = repoApi.createRepos(UserRepos(
                accessToken = token,
                name = viewStates.repoName,
                description = viewStates.repoDescribe,
                path = viewStates.repoPath,
                autoInit = viewStates.isInitRepo,
                private = viewStates.isPrivateRepo
            ))

            if (response.isSuccessful) {
                val repos = response.body()
                if (repos == null) {
                    _viewEvents.send(RepoDetailViewEvent.ShowMessage("返回数据为空！"))
                    viewStates = viewStates.copy(isUnderCreation = false)
                    return@launch
                }
                DataStoreUtils.putSyncData(DataKey.USING_REPO, repos.fullName)

                if (viewStates.isInitRepo) {
                    customInitRepo(token, repos.fullName)
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

    private suspend fun customInitRepo(token: String, repoPath: String) {
        val contentMsg = DefaultText.README_CONTENT.trimIndent()
        val contentResponse = repoApi.getContent(
            owner = repoPath.split("/")[0],
            repo = repoPath.split("/")[1],
            path =  DefaultText.README_FILE_NAME,
            accessToken = token
        )
        if (!contentResponse.isSuccessful) {
            kotlin.runCatching {
                Log.w(TAG, "customInitRepo: 获取 ${DefaultText.README_FILE_NAME} 文件内容失败！${contentResponse.errorBody()?.string()}")
            }.fold({}, {
                Log.w(TAG, "customInitRepo: 获取 ${DefaultText.README_FILE_NAME}文件内容失败！", it)
            })

            return
        }

        val content = String(
            Base64.encode((viewStates.readmeContent + contentMsg).toByteArray(), Base64.DEFAULT),
            StandardCharsets.UTF_8)
            .replace("\n", "")
            .replace("\\n", "")

        val updateResponse = repoApi.updateContent(
            owner = repoPath.split("/")[0],
            repo = repoPath.split("/")[1],
            path = DefaultText.README_FILE_NAME,
            UpdateContent(
                accessToken = token,
                content = content,
                sha = contentResponse.body()?.sha ?: "",
                message = "init by giteeTodo"
            )
        )

        if (!updateResponse.isSuccessful) {
            kotlin.runCatching {
                Log.w(TAG, "customInitRepo: 更新 ${DefaultText.README_FILE_NAME} 文件内容失败！${updateResponse.errorBody()?.string()}")
            }.fold({}, {
                Log.w(TAG, "customInitRepo: 更新 ${DefaultText.README_FILE_NAME} 文件内容失败！", it)
            })

            return
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
    private fun changeReadmeContent(text: String) {
        viewStates = viewStates.copy(readmeContent = text)
    }

    companion object {
        private const val TAG = "el, RepoDetailViewModel"
    }

}

data class RepoDetailViewState(
    val repoName: String = "",
    val repoDescribe: String = "",
    val repoPath: String = "",
    val readmeContent: String = "",
    val isPrivateRepo: Boolean = true,
    val isInitRepo: Boolean = true,
    val isUnderCreation: Boolean = false
)

sealed class RepoDetailViewEvent {
    data class Goto(val route: String): RepoDetailViewEvent()
    data class ShowMessage(val message: String) : RepoDetailViewEvent()
}

sealed class RepoDetailViewAction {
    data object Create : RepoDetailViewAction()
    data class ChangeName(val text: String): RepoDetailViewAction()
    data class ChangeDescribe(val text: String): RepoDetailViewAction()
    data class ChangePath(val text: String): RepoDetailViewAction()
    data class ChangeReadmeContent(val text: String): RepoDetailViewAction()
    data class ChangeIsPrivate(val checked: Boolean): RepoDetailViewAction()
    data class ChangeIsInit(val checked: Boolean): RepoDetailViewAction()
}