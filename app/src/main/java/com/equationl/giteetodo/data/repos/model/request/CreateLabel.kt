package com.equationl.giteetodo.data.repos.model.request

data class CreateLabel(
    val access_token: String,
    val name: String,
    val color: String
)
