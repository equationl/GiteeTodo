package com.equationl.giteetodo.data.repos.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equationl.giteetodo.data.repos.model.common.IssueRemoteKey


@Dao
interface IssueRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<IssueRemoteKey>)

    @Query("SELECT * FROM issue_remote_key WHERE issueId = :issueId")
    fun remoteKeysByNewsId(issueId: Int): IssueRemoteKey?

    @Query("SELECT * FROM issue_remote_key ORDER BY id DESC")
    suspend fun getLastItem(): IssueRemoteKey?

    @Query("DELETE FROM issue_remote_key")
    suspend fun clearAll()
}