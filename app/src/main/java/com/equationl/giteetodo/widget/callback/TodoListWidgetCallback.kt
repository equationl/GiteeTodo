package com.equationl.giteetodo.widget.callback

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.equationl.giteetodo.widget.receive.TodoListWidgetReceiver

class TodoListWidgetCallback : ActionCallback {
    override suspend fun onRun(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val actionKey = ActionParameters.Key<String>(ACTION_NAME)
        val actionName = parameters[actionKey]

        if (actionName == UPDATE_ACTION) {
            val intent = Intent(context, TodoListWidgetReceiver::class.java).apply {
                action = UPDATE_ACTION
            }
            context.sendBroadcast(intent)
        }
    }

    companion object {
        const val ACTION_NAME = "actionName"
        const val UPDATE_ACTION = "updateAction"
    }
}