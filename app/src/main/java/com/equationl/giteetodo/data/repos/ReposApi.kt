package com.equationl.giteetodo.data.repos

import com.equationl.giteetodo.data.repos.model.request.CreateComments
import com.equationl.giteetodo.data.repos.model.request.CreateIssues
import com.equationl.giteetodo.data.repos.model.response.Comment
import com.equationl.giteetodo.data.repos.model.response.Issues
import com.equationl.giteetodo.data.repos.model.response.Labels
import com.equationl.giteetodo.data.user.model.response.Repos
import retrofit2.Response
import retrofit2.http.*

interface ReposApi {
    /**
     * 创建一个 仓库
     * */
    suspend fun createRepo(): Response<Repos> {
        throw UnsupportedOperationException("Please call UserApi.createRepos()")
    }

    /**
     * 删除一个 仓库
     * */
    @DELETE("{owner}/{repo}")
    suspend fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 创建一个 issue
     * */
    @POST("{owner}/issues")
    suspend fun createIssues(@Path("owner") owner: String, @Body createIssues: CreateIssues): Response<Issues>


    /**
     * 获取所有 issue
     * */
    @GET("{owner}/{repo}/issues")
    suspend fun getAllIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("access_token") accessToken: String,
        @QueryMap filters: Map<String, String>? = null): Response<List<Issues>>


    /**
     * 获取指定 issue
     * */
    @GET("{owner}/{repo}/issues/{number}")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String
    ): Response<Issues>

    /**
     * 更新指定 issue
     * */
    @PATCH("{owner}/issues/{number}")
    suspend fun updateIssues(
        @Path("owner") owner: String,
        @Path("number") number: String,
        @Body createIssues: CreateIssues
    ): Response<Issues>

    /**
     * 获取指定 label
     * */
    @GET("{owner}/{repo}/labels/{name}")
    suspend fun getLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("name") name: String,
        @Query("access_token") accessToken: String
    ): Response<Labels>

    /**
     * 创建 issue label
     * */
    @POST("{owner}/{repo}/issues/{number}/labels")
    suspend fun createIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Body list: List<String>
    ): Response<List<Labels>>

    /**
     * 替换 issue 所有 label
     * */
    @PUT("{owner}/{repo}/issues/{number}/labels")
    suspend fun replaceIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Body list: List<String>
    ): Response<List<Labels>>

    /**
     * 删除 issue 所有 label
     * */
    @DELETE("{owner}/{repo}/issues/{number}/labels")
    suspend fun deleteAllIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 删除 issue 的指定 label
     * */
    @DELETE("{owner}/{repo}/issues/{number}/labels/{name}")
    suspend fun deleteIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Path("name") name: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 获取某个 issue 的所有评论
     * */
    @GET("{owner}/{repo}/issues/{number}/comments")
    suspend fun getAllComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Query("since") since: String? = null,
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("order") sort: String? = null,
    ): List<Comment>

    /**
     * 创建某个 issue 的评论
     * */
    @POST("{owner}/{repo}/issues/{number}/comments")
    suspend fun createComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Body createComments: CreateComments
    ): Comment

    /**
     * 获取仓库的某条评论
     * */
    @GET("{owner}/{repo}/issues/comments/{id}")
    suspend fun getComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Query("access_token") accessToken: String
    ): Comment

    /**
     * 更新 issue 某条评论
     * */
    @PATCH("{owner}/{repo}/issues/comments/{id}")
    suspend fun updateComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Body createComments: CreateComments
    ): Comment

    /**
     * 删除 issue 某条评论
     * */
    @DELETE("{owner}/{repo}/issues/comments/{id}")
    suspend fun deleteComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Query("access_token") accessToken: String
    )
}