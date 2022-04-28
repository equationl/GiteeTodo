package com.equationl.giteetodo.data.repos.model.request

data class UpdateContent(
    val access_token: String,
    val content: String,
    val sha: String,
    val message: String,
    val branch: String? = null
)
