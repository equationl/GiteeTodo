package com.equationl.giteetodo.data

import android.content.Context
import com.equationl.giteetodo.data.repos.db.IssueDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//这里使用了SingletonComponent，因此 NetworkModule 绑定到 Application 的整个生命周期
@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideIssueDataBase(@ApplicationContext app: Context) = run {
        IssueDb.create(app)
    }
}