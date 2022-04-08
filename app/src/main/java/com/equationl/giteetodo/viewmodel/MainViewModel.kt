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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val reposApi by lazy { RetrofitManger.getReposApi() }

    var issues by mutableStateOf<Issues?>(null)

    var apiError: MutableLiveData<Throwable> = MutableLiveData()

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