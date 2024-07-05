package com.equationl.giteetodo.data.repos.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.equationl.giteetodo.data.repos.model.common.IssueRemoteKey
import com.equationl.giteetodo.data.repos.model.common.TodoShowData

@Database(
    entities = [TodoShowData::class, IssueRemoteKey::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(IssueConverters::class)
abstract class IssueDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean = false): IssueDb {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, IssueDb::class.java)
            } else {
                Room.databaseBuilder(context, IssueDb::class.java, "issue_show_data.db")
            }
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun issue(): IssueDao
    abstract fun issueRemoteKey(): IssueRemoteKeyDao
}