package com.equationl.giteetodo.data.repos.model.common

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.equationl.giteetodo.data.repos.model.response.Label
import com.equationl.giteetodo.ui.common.IssueState

@Entity(tableName = "issues_show_data")
data class TodoShowData(
    @PrimaryKey
    val id: Int,
    val title: String,
    val number: String,
    val state: IssueState,
    val updateAt: String,
    val createdAt: String,
    val labels: List<Label>,
    val headerTitle: String
)