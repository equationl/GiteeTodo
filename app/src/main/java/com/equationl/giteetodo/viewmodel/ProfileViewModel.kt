package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.user.UserApi
import com.equationl.giteetodo.data.user.model.response.User
import com.equationl.giteetodo.util.datastore.DataKey
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import com.equationl.giteetodo.util.fromJson
import com.equationl.giteetodo.util.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userApi: UserApi
) : ViewModel() {

    var viewStates by mutableStateOf(ProfileViewState())
        private set

    private val _viewEvents = Channel<ProfileViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    private val exception = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _viewEvents.send(ProfileViewEvent.ShowMessage("错误："+throwable.message))
        }
    }

    fun dispatch(action: ProfileViewAction) {
        when (action) {
            is ProfileViewAction.ReadUserInfo -> readUserInfo()
            is ProfileViewAction.ShowMessage -> showMessage(action.msg)
        }
    }

    private fun showMessage(msg: String) {
        viewModelScope.launch {
            _viewEvents.send(ProfileViewEvent.ShowMessage(msg))
        }
    }

    private fun readUserInfo() {
        viewModelScope.launch(exception) {
            var user: User? = DataStoreUtils.readStringData(DataKey.USER_INFO).fromJson()
            if (user == null) {
                val accessToken = DataStoreUtils.getSyncData(DataKey.LOGIN_ACCESS_TOKEN, "")
                val response = userApi.getUser(accessToken)

                if (response.isSuccessful && response.body() != null) {
                    user = response.body()
                    DataStoreUtils.putSyncData(DataKey.USER_INFO, user?.toJson() ?: "")
                }
                else {
                    val result = kotlin.runCatching {
                        _viewEvents.send(ProfileViewEvent.ShowMessage("获取信息失败："+response.errorBody()?.string()))
                    }
                    if (result.isFailure) {
                        _viewEvents.send(ProfileViewEvent.ShowMessage("获取信息失败失败，获取失败信息失败：${result.exceptionOrNull()?.message ?: ""}"))
                    }
                    return@launch
                }
            }

            viewStates = viewStates.copy(user = user)
        }
    }
}

data class ProfileViewState(
    val user: User? = null,
)

sealed class ProfileViewEvent {
    data class Goto(val route: String): ProfileViewEvent()
    data class ShowMessage(val message: String) : ProfileViewEvent()
}

sealed class ProfileViewAction {
    data object ReadUserInfo: ProfileViewAction()
    data class ShowMessage(val msg: String): ProfileViewAction()
}