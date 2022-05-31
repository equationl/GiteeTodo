package com.equationl.giteetodo.data.repos.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equationl.giteetodo.data.repos.model.common.TodoShowData

@Dao
interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<TodoShowData>)

    @Query("SELECT * FROM issues_show_data ORDER BY updateAt ASC")
    fun pagingSourceOrderByAsc(): PagingSource<Int, TodoShowData>

    @Query("SELECT * FROM issues_show_data ORDER BY updateAt DESC")
    fun pagingSourceOrderByDesc(): PagingSource<Int, TodoShowData>

    @Query("DELETE FROM issues_show_data")
    suspend fun clearAll()
}