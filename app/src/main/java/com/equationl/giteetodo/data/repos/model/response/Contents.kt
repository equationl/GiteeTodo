package com.equationl.giteetodo.data.repos.model.response
import com.google.gson.annotations.SerializedName


data class Contents(
    @SerializedName("content")
    val content: String?,
    @SerializedName("download_url")
    val downloadUrl: String,
    @SerializedName("encoding")
    val encoding: String?,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("_links")
    val links: Links,
    @SerializedName("name")
    val name: String,
    @SerializedName("path")
    val path: String,
    @SerializedName("sha")
    val sha: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("url")
    val url: String
)

data class Links(
    @SerializedName("html")
    val html: String,
    @SerializedName("self")
    val self: String
)