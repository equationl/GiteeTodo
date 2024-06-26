package com.equationl.giteetodo.data.repos.model.request

import com.google.gson.annotations.SerializedName

data class UpdateContent(
    @SerializedName("access_token")
    val accessToken: String,
    val content: String,
    val sha: String,
    val message: String,
    val branch: String? = null
)
