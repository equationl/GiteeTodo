package com.equationl.giteetodo.data.user

import com.equationl.giteetodo.data.user.model.response.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApi {

    /**
     * 获取用户信息
     * */
    @GET("user/")
    suspend fun getUser(@Query("access_token") accessToken: String): Response<User>
}