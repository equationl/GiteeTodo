package com.equationl.giteetodo.data.repos.model.response
import com.google.gson.annotations.SerializedName


data class Labels(
    @SerializedName("color")
    val color: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("repository_id")
    val repositoryId: Int,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("url")
    val url: String
)