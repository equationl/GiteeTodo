package com.equationl.giteetodo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.repos.db.IssueDb
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.Utils
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoHomeViewModel @Inject constructor(
    private val dataBase: IssueDb
) : ViewModel() {
    /**
     * 用于标记进入全屏动画是否完成
     * */
    private var isAnimationFinish = true

    var viewStates by mutableStateOf(TodoHomeViewState())
        private set

    private val _viewEvents = Channel<TodoHomeViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: TodoHomeViewAction) {
        when (action) {
            is TodoHomeViewAction.ChangeRepo -> changeRepo()
            is TodoHomeViewAction.GoToMe -> goToMe(action.repoPath)
            is TodoHomeViewAction.GoToTodo -> goToTodo(action.repoPath)
            is TodoHomeViewAction.AddATodo -> addATodo()
            is TodoHomeViewAction.Logout -> logout()
            is TodoHomeViewAction.ChangeSystemBarShowState -> changeSystemBarShowState(action.isShow)
            is TodoHomeViewAction.OnAnimateFinish -> onAnimateFinish()
        }
    }

    private fun onAnimateFinish() {
        isAnimationFinish = true
    }

    private fun changeSystemBarShowState(isShow: Boolean) {
        if (viewStates.currentPage == CurrentPager.HOME_TODO) {
            viewStates = viewStates.copy(isShowSystemBar = isShow)
        }
    }

    private fun changeRepo() {
        viewModelScope.launch {
            _viewEvents.send(TodoHomeViewEvent.Goto(Route.REPO_LIST))
        }
    }

    private fun goToMe(repoPath: String) {
        viewStates = viewStates.copy(
            currentPage = CurrentPager.HOME_ME,
            title = repoPath.split("/")[0],
            isShowSystemBar = true
        )
    }

    private fun goToTodo(repoPath: String) {
        viewStates = viewStates.copy(
            currentPage = CurrentPager.HOME_TODO,
            title = repoPath.split("/")[1],
        )
    }

    private fun addATodo() {
        viewModelScope.launch {
            _viewEvents.send(TodoHomeViewEvent.Goto("${Route.TODO_DETAIL}/null/null"))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            Utils.clearCookies()
            DataStoreUtils.clear()
            dataBase.issue().clearAll()
            dataBase.issueRemoteKey().clearAll()
            _viewEvents.send(TodoHomeViewEvent.Goto(Route.LOGIN, isClrStack = true))
        }
    }
}

data class TodoHomeViewState(
    val title: String = "TODO LIST",
    val currentPage: CurrentPager = CurrentPager.HOME_TODO,
    val currentRepo: String = "",
    val isShowSystemBar: Boolean = true
)

sealed class TodoHomeViewEvent {
    data class Goto(val route: String, val isClrStack: Boolean = false) : TodoHomeViewEvent()
    data class ShowMessage(val message: String) : TodoHomeViewEvent()
}

sealed class TodoHomeViewAction {
    data object ChangeRepo : TodoHomeViewAction()
    data class GoToTodo(val repoPath: String) : TodoHomeViewAction()
    data class GoToMe(val repoPath: String) : TodoHomeViewAction()
    data class ChangeSystemBarShowState(val isShow: Boolean) : TodoHomeViewAction()
    data object AddATodo : TodoHomeViewAction()
    data object Logout : TodoHomeViewAction()
    data object OnAnimateFinish : TodoHomeViewAction()
}

enum class CurrentPager {
    HOME_TODO,
    HOME_ME
}