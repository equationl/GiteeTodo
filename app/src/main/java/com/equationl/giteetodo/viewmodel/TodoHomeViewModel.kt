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
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.util.datastore.DataStoreUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TodoHomeViewModel : ViewModel() {
    var viewStates by mutableStateOf(TodoHomeViewState())
        private set

    private val _viewEvents = Channel<TodoHomeViewEvent>(Channel.BUFFERED)
    val viewEvents = _viewEvents.receiveAsFlow()

    fun dispatch(action: TodoHomeViewAction) {
        when (action) {
            is TodoHomeViewAction.ChangeRepo -> changeRepo()
            is TodoHomeViewAction.GoToMe -> goToMe()
            is TodoHomeViewAction.GoToTodo -> goToTodo()
            is TodoHomeViewAction.AddATodo -> addATodo()
            is TodoHomeViewAction.Logout -> logout()
            is TodoHomeViewAction.ChangeTitle -> changeTitle(action.title)
        }
    }

    private fun changeRepo() {
        viewModelScope.launch {
            _viewEvents.send(TodoHomeViewEvent.Goto(Route.REPO_LIST))
        }
    }

    private fun goToMe() {
        if (viewStates.currentPage != CurrentPager.HOME_ME) {
            viewStates = viewStates.copy(
                currentPage = CurrentPager.HOME_ME,
                title = "我的",
                homeIcon = Icons.Outlined.Home,
                homeTextColor = Color.Gray,
                meIcon = Icons.Filled.Person,
                meTextColor = Color.White
            )
        }
    }

    private fun goToTodo() {
        if (viewStates.currentPage != CurrentPager.HOME_TODO) {
            viewStates = viewStates.copy(
                currentPage = CurrentPager.HOME_TODO,
                title = "TODO TITLE",
                homeIcon = Icons.Filled.Home,
                homeTextColor = Color.White,
                meIcon = Icons.Outlined.Person,
                meTextColor = Color.Gray)
        }
    }

    private fun addATodo() {
        viewModelScope.launch {
            _viewEvents.send(TodoHomeViewEvent.Goto("${Route.TODO_DETAIL}/null"))
        }
    }

    private fun changeTitle(title: String) {
        viewStates = viewStates.copy(title = title)
    }

    private fun logout() {
        viewModelScope.launch {
            DataStoreUtils.clear()
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
    val meTextColor: Color = Color.Gray
)

sealed class TodoHomeViewEvent {
    data class Goto(val route: String): TodoHomeViewEvent()
    data class ShowMessage(val message: String) : TodoHomeViewEvent()
}

sealed class TodoHomeViewAction {
    object ChangeRepo : TodoHomeViewAction()
    object GoToTodo: TodoHomeViewAction()
    object GoToMe: TodoHomeViewAction()
    object AddATodo: TodoHomeViewAction()
    object Logout: TodoHomeViewAction()
    data class ChangeTitle(val title: String): TodoHomeViewAction()
}

enum class CurrentPager {
    HOME_ME,
    HOME_TODO
}