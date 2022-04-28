package com.equationl.giteetodo.data.repos.model.response
import com.google.gson.annotations.SerializedName


data class UpdateContentResponse(
    @SerializedName("commit")
    val commit: Commit,
    @SerializedName("content")
    val content: Contents
)

data class Commit(
    @SerializedName("author")
    val author: Author,
    @SerializedName("committer")
    val committer: Committer,
    @SerializedName("message")
    val message: String,
    @SerializedName("parents")
    val parents: List<Parent>,
    @SerializedName("sha")
    val sha: String,
    @SerializedName("tree")
    val tree: Tree
)

data class Author(
    @SerializedName("date")
    val date: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String
)

data class Committer(
    @SerializedName("date")
    val date: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("name")
    val name: String
)

data class Parent(
    @SerializedName("sha")
    val sha: String,
    @SerializedName("url")
    val url: String
)

data class Tree(
    @SerializedName("sha")
    val sha: String,
    @SerializedName("url")
    val url: String
)