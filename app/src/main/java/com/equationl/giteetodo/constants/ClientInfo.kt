package com.equationl.giteetodo.constants

import com.equationl.giteetodo.BuildConfig

object ClientInfo {
    const val CLIENT_ID = BuildConfig.CLIENT_ID
    const val CLIENT_SECRET = BuildConfig.CLIENT_SECRET

    const val PERMISSION_SCOPE = "projects user_info issues notes"
    const val AUTH_URI = "giteetodoapp://authed"
}