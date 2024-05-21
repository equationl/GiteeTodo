package com.equationl.giteetodo.data.repos.model.request

import com.google.gson.annotations.SerializedName

data class CreateLabel(
    @SerializedName("access_token")
    val accessToken: String,
    val name: String,
    val color: String
)
