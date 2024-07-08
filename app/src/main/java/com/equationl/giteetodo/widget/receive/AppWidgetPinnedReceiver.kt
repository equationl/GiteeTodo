package com.equationl.giteetodo.widget.receive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * 小组件添加成功监听广播
 * */
class AppWidgetPinnedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(
            context,
            "小组件已添加，请返回桌面查看",
            Toast.LENGTH_SHORT
        ).show()
    }
}