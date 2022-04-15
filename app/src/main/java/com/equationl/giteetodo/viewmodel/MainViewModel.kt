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

    val oAuthApi by lazy { RetrofitManger.getOAuthApi() }

    var issues by mutableStateOf<Issues?>(null)

    var apiError: MutableLiveData<Throwable> = MutableLiveData()

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