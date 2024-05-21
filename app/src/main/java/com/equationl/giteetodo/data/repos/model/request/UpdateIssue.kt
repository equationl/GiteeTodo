package com.equationl.giteetodo.data.repos.model.request

import com.google.gson.annotations.SerializedName

data class UpdateIssue(
    @SerializedName("access_token")
    val accessToken: String,
    val repo: String? = null,
    val title: String? = null,
    val state: String? = null,
    val body: String? = null,
    val assignee: String? = null,
    val collaborators: String? = null,
    val milestone: Int? = null,
    val labels: String? = null,
    val program: String? = null,
    @SerializedName("security_hole")
    val securityHole: Boolean? = null
)
