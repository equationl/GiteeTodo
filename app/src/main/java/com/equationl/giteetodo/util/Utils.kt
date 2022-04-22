package com.equationl.giteetodo.util

import android.util.Log
import com.equationl.giteetodo.data.user.model.response.Repos
import com.equationl.giteetodo.viewmodel.RepoItemData

object Utils {
    private const val TAG = "Utils"

    fun String.isEmail(): Boolean {
        val emailAddressRegex = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )


        if (this.matches(emailAddressRegex)){
            return true
        }

        return false
    }

    fun resolveRepos(body: List<Repos>?): List<RepoItemData> {
        val result = mutableListOf<RepoItemData>()
        if (body == null) return result

        for (repo in body) {
            if (repo.namespace.type == "personal") {  // 仅加载类型为个人的仓库
                result.add(
                    RepoItemData(
                        repo.fullName,
                        repo.openIssuesCount,
                        repo.name,
                        repo.createdAt  // TODO 格式化日期
                    )
                )
            }
        }
        return result
    }

    fun getIssueState(state: String): IssueState {
        return try {
            IssueState.valueOf(state.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "getIssueState: get Issue state fail", e)
            IssueState.OPEN
        }
    }

    enum class IssueState(val des: String) {
        OPEN("open"),
        PROGRESSING("progressing"),
        CLOSED("closed"),
        REJECTED("rejected")
    }
}