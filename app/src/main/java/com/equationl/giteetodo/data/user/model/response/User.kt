package com.equationl.giteetodo.data.user.model.response
import com.google.gson.annotations.SerializedName


data class User(
    @SerializedName("avatar_url")
    val avatarUrl: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("blog")
    val blog: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("events_url")
    val eventsUrl: String,
    @SerializedName("followers")
    val followers: Int,
    @SerializedName("followers_url")
    val followersUrl: String,
    @SerializedName("following")
    val following: Int,
    @SerializedName("following_url")
    val followingUrl: String,
    @SerializedName("gists_url")
    val gistsUrl: String,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("organizations_url")
    val organizationsUrl: String,
    @SerializedName("public_gists")
    val publicGists: Int,
    @SerializedName("public_repos")
    val publicRepos: Int,
    @SerializedName("received_events_url")
    val receivedEventsUrl: String,
    @SerializedName("remark")
    val remark: String,
    @SerializedName("repos_url")
    val reposUrl: String,
    @SerializedName("stared")
    val stared: Int,
    @SerializedName("starred_url")
    val starredUrl: String,
    @SerializedName("subscriptions_url")
    val subscriptionsUrl: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("watched")
    val watched: Int,
    @SerializedName("weibo")
    val weibo: String
)