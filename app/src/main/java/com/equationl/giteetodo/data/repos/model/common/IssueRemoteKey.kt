package com.equationl.giteetodo.data.repos.model.common

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "issue_remote_key")
data class IssueRemoteKey(
    @PrimaryKey
    val issueId: Int,
    val prevKey: Int?,
    val nextKey: Int?
)
