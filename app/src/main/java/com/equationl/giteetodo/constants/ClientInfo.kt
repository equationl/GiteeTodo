package com.equationl.giteetodo.constants

import com.equationl.giteetodo.BuildConfig

object ClientInfo {
    const val ClientId = BuildConfig.CLIENT_ID
    const val ClientSecret = BuildConfig.CLIENT_SECRET

    const val PermissionScope = "projects user_info issues notes"
    const val AuthUri = "giteetodoapp://authed"
}