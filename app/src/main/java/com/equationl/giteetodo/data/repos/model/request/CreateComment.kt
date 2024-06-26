package com.equationl.giteetodo.data.repos.model.request

import com.google.gson.annotations.SerializedName

data class CreateComment(
    @SerializedName("access_token")
    val accessToken: String,
    val body: String
)