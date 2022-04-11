package com.equationl.giteetodo.data.repos.model.response
import com.google.gson.annotations.SerializedName


data class Comment(
    @SerializedName("body")
    val body: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("source")
    val source: Any?,
    @SerializedName("target")
    val target: Target,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("user")
    val user: SimpleUser
)

data class Target(
    @SerializedName("issue")
    val issue: Issue,
    @SerializedName("pull_request")
    val pullRequest: Any?
)

data class Issue(
    @SerializedName("id")
    val id: Int,
    @SerializedName("number")
    val number: String,
    @SerializedName("title")
    val title: String
)