package com.equationl.giteetodo.widget.callback

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.ToggleableStateKey
import com.equationl.giteetodo.constants.IntentDataKey
import com.equationl.giteetodo.widget.receive.TodoListWidgetReceiver

// 其实可以不用 callback 来中转，
// 可以直接使用 actionSendBroadcast 发送广播的，
// 但是这里为了方便以后扩展，所以还是统一都使用 callback 转一下
class TodoListWidgetCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val actionKey = ActionParameters.Key<String>(ACTION_NAME)
        val actionName = parameters[actionKey]

        if (actionName == UPDATE_ACTION) {
            Log.i(TAG, "onRun: UPDATE_ACTION")
            val intent = Intent(context, TodoListWidgetReceiver::class.java).apply {
                action = UPDATE_ACTION
            }
            context.sendBroadcast(intent)
        }
        else if (actionName == CHECK_ISSUE_ACTION) {
            Log.i(TAG, "onRun: CHECK_ISSUE_ACTION")

            val isChecked = parameters[ToggleableStateKey]
            val issueNum = parameters[ActionParameters.Key<String>(ISSUE_NUM_NAME)]
            val intent = Intent(context, TodoListWidgetReceiver::class.java).apply {
                action = CHECK_ISSUE_ACTION
                putExtra(ISSUE_NUM_NAME, issueNum)
                putExtra(ToggleableStateKey.name, isChecked)
            }
            context.sendBroadcast(intent)
        }
    }

    companion object {
        private const val TAG = "el, TodoListWidgetCallback"

        const val ACTION_NAME = "actionName"
        const val ISSUE_NUM_NAME = IntentDataKey.IssueNumber

        const val UPDATE_ACTION = "updateAction"
        const val CHECK_ISSUE_ACTION = "checkIssueAction"
    }
}