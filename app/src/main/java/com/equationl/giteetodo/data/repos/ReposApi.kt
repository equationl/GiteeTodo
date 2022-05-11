package com.equationl.giteetodo.data.repos

import com.equationl.giteetodo.data.repos.model.request.*
import com.equationl.giteetodo.data.repos.model.response.*
import com.equationl.giteetodo.data.user.model.request.UserRepos
import com.equationl.giteetodo.data.user.model.response.Repos
import retrofit2.Response
import retrofit2.http.*

interface ReposApi {
    /**
     * 创建一个仓库
     * */
    @POST("user/repos")
    suspend fun createRepos(@Body createRepos: UserRepos): Response<Repos>

    /**
     * 获取所有仓库
     * */
    @GET("user/repos")
    suspend fun getRepos(
        @Query("access_token") accessToken: String,
        @Query("visibility") visibility: String? = null,
        @Query("affiliation") affiliation: String? = null,
        @Query("type") type: String = "owner",
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc",
        @Query("q") q: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page")perPage: Int = 100
    ): Response<List<Repos>>

    /**
     * 删除一个 仓库
     * */
    @DELETE("repos/{owner}/{repo}")
    suspend fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 创建一个 issue
     * */
    @POST("repos/{owner}/issues")
    suspend fun createIssues(@Path("owner") owner: String, @Body createIssues: CreateIssues): Response<Issues>


    /**
     * 获取所有 issue
     * */
    @GET("repos/{owner}/{repo}/issues")
    suspend fun getAllIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("access_token") accessToken: String,
        @Query("state") state: String? = null,
        @Query("sort") sort: String = "created",
        @Query("direction") direction: String? = null,
        @Query("created_at") createdAt: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
        @Query("labels") labels: String? = null,
        @QueryMap filters: Map<String, String> = mapOf()): Response<List<Issues>>


    /**
     * 获取指定 issue
     * */
    @GET("repos/{owner}/{repo}/issues/{number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String
    ): Response<Issues>

    /**
     * 更新指定 issue
     * */
    @PATCH("repos/{owner}/issues/{number}")
    suspend fun updateIssues(
        @Path("owner") owner: String,
        @Path("number") number: String,
        @Body updateIssue: UpdateIssue
    ): Response<Issues>

    /**
     * 获取指定 label
     * */
    @GET("repos/{owner}/{repo}/labels/{name}")
    suspend fun getLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("name") name: String,
        @Query("access_token") accessToken: String
    ): Response<Label>

    /**
     * 获取指定仓库的所有可用标签
     * */
    @GET("repos/{owner}/{repo}/labels")
    suspend fun getExistLabels(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("access_token") accessToken: String
    ): Response<List<Label>>

    /**
     * 创建 issue label
     * */
    @POST("repos/{owner}/{repo}/issues/{number}/labels")
    suspend fun createIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Body list: List<String>
    ): Response<List<Label>>

    /**
     * 替换 issue 所有 label
     * */
    @PUT("repos/{owner}/{repo}/issues/{number}/labels")
    suspend fun replaceIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Body list: List<String>
    ): Response<List<Label>>

    /**
     * 删除 issue 所有 label
     * */
    @DELETE("repos/{owner}/{repo}/issues/{number}/labels")
    suspend fun deleteAllIssueLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    @POST("repos/{owner}/{repo}/labels")
    suspend fun createLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body createLabel: CreateLabel
    ): Response<Label>

    @PATCH("repos/{owner}/{repo}/labels/{original_name}")
    suspend fun updateLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("original_name") originalName: String,
        @Body createLabel: CreateLabel
    ): Response<Label>

    /**
     * 删除指定标签
     * */
    @DELETE("repos/{owner}/{repo}/labels/{name}")
    suspend fun deleteLabel(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("name") name: String,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 删除 issue 的指定 label
     * */
    @DELETE("repos/{owner}/{repo}/issues/{number}/labels/{name}")
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
    @GET("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun getAllComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Query("access_token") accessToken: String,
        @Query("since") since: String? = null,
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 100,
        @Query("order") sort: String? = null,
    ): Response<List<Comment>>

    /**
     * 创建某个 issue 的评论
     * */
    @POST("repos/{owner}/{repo}/issues/{number}/comments")
    suspend fun createComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("number") number: String,
        @Body createComment: CreateComment
    ): Response<Comment>

    /**
     * 获取仓库的某条评论
     * */
    @GET("repos/{owner}/{repo}/issues/comments/{id}")
    suspend fun getComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Query("access_token") accessToken: String
    ): Response<Comment>

    /**
     * 更新 issue 某条评论
     * */
    @PATCH("repos/{owner}/{repo}/issues/comments/{id}")
    suspend fun updateComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Body createComment: CreateComment
    ): Response<Comment>

    /**
     * 删除 issue 某条评论
     * */
    @DELETE("repos/{owner}/{repo}/issues/comments/{id}")
    suspend fun deleteComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("id") id: Int,
        @Query("access_token") accessToken: String
    ): Response<Any?>

    /**
     * 获取仓库具体路径下的内容
     */
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("access_token") accessToken: String,
        @Query("ref") ref: String? = null
    ): Response<Contents>

    /**
    * 更新仓库中的指定文件内容
    * */
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun updateContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body updateContent: UpdateContent
    ): Response<UpdateContentResponse>
}