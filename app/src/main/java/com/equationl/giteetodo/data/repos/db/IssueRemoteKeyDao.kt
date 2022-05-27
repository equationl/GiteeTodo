package com.equationl.giteetodo.data.repos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equationl.giteetodo.data.repos.model.common.IssueRemoteKey


@Dao
interface IssueRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(remoteKey: List<IssueRemoteKey>)

    @Query("SELECT * FROM issue_remote_key WHERE issueId = :issueId")
    fun remoteKeysByNewsId(issueId: Int): IssueRemoteKey?

    @Query("DELETE FROM issue_remote_key")
    fun clearAll()
}