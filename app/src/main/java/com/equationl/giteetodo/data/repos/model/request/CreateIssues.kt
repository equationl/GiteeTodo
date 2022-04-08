package com.equationl.giteetodo.data.repos.model.request

data class CreateIssues(
    val access_token: String,
    val repo: String? = null,
    val title: String,
    val issue_type: String? = null,
    val body: String? = null,
    val assignee: String? = null,
    val collaborators: String? = null,
    val milestone: Int? = null,
    val labels: String? = null,
    val program: String? = null,
    val security_hole: Boolean? = null
)