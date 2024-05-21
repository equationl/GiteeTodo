package com.equationl.giteetodo.data.user.model.request

import com.google.gson.annotations.SerializedName

data class UserRepos(
    @SerializedName("access_token")
    val accessToken: String,
    val name: String,
    val description: String? = null,
    val homepage: String? = null,
    @SerializedName("has_issues")
    val hasIssues: Boolean? = null,
    @SerializedName("has_wiki")
    val hasWiki: Boolean? = null,
    @SerializedName("can_comment")
    val canComment: Boolean? = null,
    @SerializedName("auto_init")
    val autoInit: Boolean? = null,
    @SerializedName("gitignore_template")
    val gitignoreTemplate: String? = null,
    @SerializedName("license_template")
    val licenseTemplate: String? = null,
    val path: String? = null,
    val private: Boolean? = null
)
