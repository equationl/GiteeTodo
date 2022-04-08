package com.equationl.giteetodo.data.user

import com.equationl.giteetodo.data.user.model.request.UserRepos
import com.equationl.giteetodo.data.user.model.response.Repos
import com.equationl.giteetodo.data.user.model.response.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserApi {

    /**
     * 获取用户信息
     * */
    @GET
    suspend fun getUser(@Query("access_token") accessToken: String): Response<User>

    /**
     * 创建一个仓库
     * */
    @POST("repos")
    suspend fun createRepos(@Body createRepos: UserRepos): Response<Repos>
}