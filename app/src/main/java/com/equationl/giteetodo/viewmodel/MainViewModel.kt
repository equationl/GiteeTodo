package com.equationl.giteetodo.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equationl.giteetodo.data.RetrofitManger
import com.equationl.giteetodo.data.repos.model.request.CreateIssues
import com.equationl.giteetodo.data.repos.model.response.Issues
import com.equationl.giteetodo.ui.TodoCardData
import com.equationl.giteetodo.ui.TodoCardItemData
import com.equationl.giteetodo.util.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val reposApi by lazy { RetrofitManger.getReposApi() }

    val oAuthApi by lazy { RetrofitManger.getOAuthApi() }

    var issues by mutableStateOf<Issues?>(null)

    var apiError: MutableLiveData<Throwable> = MutableLiveData()

    var isEdit by mutableStateOf(true)

    fun getIssue(): List<TodoCardData> {
        // TODO
        return listOf(
            TodoCardData("2022年04月19日",
                listOf(
                    TodoCardItemData("Open", Utils.IssueState.OPEN, "0001"),
                    TodoCardItemData("Closed", Utils.IssueState.CLOSED, "0002"),
                    TodoCardItemData("Open", Utils.IssueState.PROGRESSING, "0003"),
                    TodoCardItemData("Open", Utils.IssueState.REJECTED, "0004"),
                    TodoCardItemData("超级长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长的字符串", Utils.IssueState.REJECTED, "0005")
                    )
            ),
            TodoCardData("2022年04月18日",
                listOf(
                    TodoCardItemData("Open", Utils.IssueState.OPEN, "0001"),
                )
            ),
            TodoCardData("2022年04月17日",
                listOf(
                    TodoCardItemData("超级长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长长的字符串", Utils.IssueState.REJECTED, "0005")
                )
            ),
            TodoCardData("2022年04月16日",
                listOf(
                    TodoCardItemData("Open", Utils.IssueState.OPEN, "0001"),
                    TodoCardItemData("Closed", Utils.IssueState.CLOSED, "0002"),
                    TodoCardItemData("Open", Utils.IssueState.PROGRESSING, "0003"),
                )
            )
        )
    }

    fun pswLogin(email: String, psw: String) {
        val exception = CoroutineExceptionHandler { coroutineContext, throwable ->
            apiError.postValue(throwable)
            Log.w("CoroutinesViewModel",throwable.message!!)
        }

        viewModelScope.launch(exception) {
            val respose = oAuthApi.getTokenByPsw(
                email,
                psw,
                "bfcca943cb10013bcaef4d1b779c2af9229b2a6dc4aee36488e203649a9c5789",
                "342dfa49d229a2cf0a6f4d81425796ca22d558e18060c5b25f49ed2d01a46471")

            println("response = ${respose.body()}")
        }
    }

    fun createIssues(owner: String, createIssues: CreateIssues) {

        val exception = CoroutineExceptionHandler { coroutineContext, throwable ->
            apiError.postValue(throwable)
            Log.w("CoroutinesViewModel",throwable.message!!)
        }

        viewModelScope.launch(exception) {
            val respose = reposApi.createIssues(owner, createIssues)
            issues = respose.body()
        }
    }
}