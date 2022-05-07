package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.model.request.CreateLabel
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LabelMgViewModel : ViewModel() {
    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _viewEvents.send(LabelMgViewEvent.ShowMessage("请求失败：${throwable.message}"))
        }
    }

    private val repoApi = RetrofitManger.getReposApi()

    var viewStates by mutableStateOf(LabelMgViewState())
        private set

    private val _viewEvents = Channel<LabelMgViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: LabelMgViewAction) {
        when (action) {
            is LabelMgViewAction.ClickAddLabel -> clickAddLabel()
            is LabelMgViewAction.LoadLabel -> loadLabel(action.repoPath)
            is LabelMgViewAction.ClickEditLabel -> clickEditLabel(action.pos)
            is LabelMgViewAction.DeleteLabel -> deleteLabel(action.label, action.repoPath)
            is LabelMgViewAction.InitEdit -> initEdit(action.label)
            is LabelMgViewAction.OnEditColorChange -> onEditColorChange(action.value)
            is LabelMgViewAction.OnEditNameChange -> onEditNameChange(action.value)
            is LabelMgViewAction.ClickSave -> clickSave(action.pos, action.repoPath, action.label)
        }
    }

    private fun clickSave(pos: Int, repoPath: String, label: Label?) {
        viewModelScope.launch(exception) {
            val accessToken = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val owner = repoPath.split("/")[0]
            val repo = repoPath.split("/")[1]
            val response = if (pos == Int.MAX_VALUE) {  // 新建
                repoApi.createLabel(
                    owner,
                    repo,
                    CreateLabel(accessToken, viewStates.editName, viewStates.editColor)
                )
            }
            else {  // 更新
                repoApi.updateLabel(
                    owner,
                    repo,
                    label?.name ?: "",
                    CreateLabel(accessToken, viewStates.editName, viewStates.editColor)
                )
            }

            if (response.isSuccessful) {
                loadLabel(repoPath)
            }
            else {
                val result = kotlin.runCatching {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("更新标签失败"+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("更新标签失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }

    private fun initEdit(label: Label?) {
        if (viewStates.editName.isBlank()) {
            viewStates = viewStates.copy(editName = label?.name ?: "")
        }
        if (viewStates.editColor.isBlank()) {
            viewStates = viewStates.copy(editColor = label?.color ?: "", )
        }
    }

    private fun onEditNameChange(value: String) {
        viewStates = viewStates.copy(editName = value)
    }

    private fun onEditColorChange(value: String) {
        viewStates = viewStates.copy(editColor = value)
    }

    private fun deleteLabel(label: Label, repoPath: String) {
        val newLabelList = mutableListOf<Label>()
        newLabelList.addAll(viewStates.labelList)
        newLabelList.remove(label)
        viewStates = viewStates.copy(labelList = newLabelList)

        viewModelScope.launch(exception) {
            val accessToken = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val response = repoApi.deleteLabel(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                label.name,
                accessToken
            )

            if (!response.isSuccessful) {
                val result = kotlin.runCatching {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("删除标签失败"+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("删除标签失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                }
                // 重新加载标签
                loadLabel(repoPath)
            }
        }
    }

    private fun clickAddLabel() {
        viewStates = viewStates.copy(editPos = Int.MAX_VALUE, editName = "", editColor = "")
    }

    private fun clickEditLabel(pos: Int) {
        viewStates = if (viewStates.editPos == pos) {
            viewStates.copy(editPos = -1, editColor = "", editName = "")
        } else {
            viewStates.copy(editPos = pos)
        }
    }

    private fun loadLabel(repoPath: String) {
        viewModelScope.launch(exception) {
            val accessToken = DataStoreUtils.getSyncData(DataKey.LoginAccessToken, "")
            val response = repoApi.getExistLabels(
                repoPath.split("/")[0],
                repoPath.split("/")[1],
                accessToken
            )

            if (response.isSuccessful) {
                val labels = response.body() ?: listOf()
                viewStates = viewStates.copy(labelList = labels as MutableList<Label>, editPos = -1, editColor = "", editName = "")
            }
            else {
                val result = kotlin.runCatching {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("获取标签失败"+response.errorBody()?.string()))
                }
                if (result.isFailure) {
                    _viewEvents.send(LabelMgViewEvent.ShowMessage("获取标签失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                }
            }
        }
    }
}

data class LabelMgViewState(
    val editPos: Int = -1,
    val labelList: MutableList<Label> = mutableListOf(),
    val editName: String = "",
    val editColor: String = ""
)

sealed class LabelMgViewEvent {
    data class Goto(val route: String):LabelMgViewEvent()
    data class ShowMessage(val message: String) :LabelMgViewEvent()
}

sealed class LabelMgViewAction {
    object ClickAddLabel: LabelMgViewAction()
    data class ClickEditLabel(val pos: Int): LabelMgViewAction()
    data class LoadLabel(val repoPath: String): LabelMgViewAction()
    data class DeleteLabel(val label: Label, val repoPath: String): LabelMgViewAction()
    data class InitEdit(val label: Label?): LabelMgViewAction()
    data class OnEditNameChange(val value: String): LabelMgViewAction()
    data class OnEditColorChange(val value: String): LabelMgViewAction()
    data class ClickSave(val pos: Int, val repoPath: String, val label: Label?): LabelMgViewAction()
}