package com.equationl.giteetodo.util

import android.util.Log

object Utils {
    private const val TAG = "Utils"

    fun String.isEmail(): Boolean {
        val emailAddressRegex = Regex(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
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