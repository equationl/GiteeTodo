package com.equationl.giteetodo.ui.common

enum class IssueState(val des: String, val humanName: String) {
    OPEN("open", "待办的"),
    PROGRESSING("progressing", "进行中"),
    CLOSED("closed", "已关闭"),
    REJECTED("rejected", "已拒绝"),
    ALL("all", "全部"),
    UNKNOWN("unknown", "未知的")
}

fun getIssueState(state: String): IssueState {
    return try {
        IssueState.valueOf(state.uppercase())
    } catch (e: IllegalArgumentException) {
        IssueState.UNKNOWN
    }
}