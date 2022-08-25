package com.equationl.giteetodo.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            if (viewStates.isShowSystemBar != isShow) {  // 只有当前显示状态和请求状态不同时才继续，避免重复调用
                if (isAnimationFinish) {
                    // 只有在所有动画都完成后才继续，
                    // 因为动画正在进行时会导致尺寸计算不可预测使得一直重复调用，最终导致动画也跟着“反复横跳”
                    isAnimationFinish = false
                    viewStates = viewStates.copy(isShowSystemBar = isShow)
                }
            }
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
            homeIcon = Icons.Outlined.Home,
            homeTextColor = Color.Gray,
            meIcon = Icons.Filled.Person,
            meTextColor = Color.White,
            isShowSystemBar = true
        )
    }

    private fun goToTodo(repoPath: String) {
        viewStates = viewStates.copy(
            currentPage = CurrentPager.HOME_TODO,
            title = repoPath.split("/")[1],
            homeIcon = Icons.Filled.Home,
            homeTextColor = Color.White,
            meIcon = Icons.Outlined.Person,
            meTextColor = Color.Gray
        )
    }

    private fun addATodo() {
        viewModelScope.launch {
            _viewEvents.send(TodoHomeViewEvent.Goto("${Route.TODO_DETAIL}/null"))
        }
    }

    private fun logout() {
        viewModelScope.launch {
            Utils.clearCookies()
            DataStoreUtils.clear()
            dataBase.issue().clearAll()
            dataBase.issueRemoteKey().clearAll()
            _viewEvents.send(TodoHomeViewEvent.Goto(Route.LOGIN))
        }
    }
}

data class TodoHomeViewState(
    val title: String = "TODO LIST",
    val currentPage: CurrentPager = CurrentPager.HOME_TODO,
    val currentRepo: String = "",
    val homeIcon: ImageVector = Icons.Filled.Home,
    val homeTextColor: Color = Color.White,
    val meIcon: ImageVector = Icons.Outlined.Person,
    val meTextColor: Color = Color.Gray,
    val isShowSystemBar: Boolean = true
)

sealed class TodoHomeViewEvent {
    data class Goto(val route: String): TodoHomeViewEvent()
    data class ShowMessage(val message: String) : TodoHomeViewEvent()
}

sealed class TodoHomeViewAction {
    object ChangeRepo : TodoHomeViewAction()
    data class GoToTodo(val repoPath: String): TodoHomeViewAction()
    data class GoToMe(val repoPath: String): TodoHomeViewAction()
    data class ChangeSystemBarShowState(val isShow: Boolean): TodoHomeViewAction()
    object AddATodo: TodoHomeViewAction()
    object Logout: TodoHomeViewAction()
    object OnAnimateFinish : TodoHomeViewAction()
}

enum class CurrentPager {
    HOME_TODO,
    HOME_ME
}