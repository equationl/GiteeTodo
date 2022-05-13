package com.equationl.giteetodo.util

import android.util.Log
import com.google.gson.Gson

fun Any.toJson(): String {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJson(): T? {
    return try {
        Gson().fromJson(this, T::class.java)
    } catch (e: Exception) {
        Log.w("el, JsonUtil", "fromJson: 转换json失败", e)
        null
    }
}