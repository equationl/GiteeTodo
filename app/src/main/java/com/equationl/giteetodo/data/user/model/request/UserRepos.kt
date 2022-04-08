package com.equationl.giteetodo.data.user.model.request

data class UserRepos(
    val access_token: String,
    val name: String,
    val description: String? = null,
    val homepage: String? = null,
    val has_issues: Boolean? = null,
    val has_wiki: Boolean? = null,
    val can_comment: Boolean? = null,
    val auto_init: Boolean? = null,
    val gitignore_template: String? = null,
    val license_template: String? = null,
    val path: String? = null,
    val private: Boolean? = null
)
