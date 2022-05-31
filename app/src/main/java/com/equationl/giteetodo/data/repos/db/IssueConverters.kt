package com.equationl.giteetodo.data.repos.db

import androidx.room.TypeConverter
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IssueConverters {

    @TypeConverter
    fun fromLabelList(value: List<Label>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Label>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toLabelList(value: String): List<Label> {
        val gson = Gson()
        val type = object : TypeToken<List<Label>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIssueState(value: IssueState): String {
        return value.des
    }

    @TypeConverter
    fun toIssueState(value: String): IssueState {
        return try { IssueState.valueOf(value.uppercase()) } catch (e: IllegalArgumentException) { IssueState.OPEN }
    }

}