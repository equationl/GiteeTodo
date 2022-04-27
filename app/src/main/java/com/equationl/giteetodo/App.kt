package com.equationl.giteetodo

import android.app.Application
import com.equationl.giteetodo.util.datastore.DataStoreUtils

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DataStoreUtils.init(this)
    }
}