package com.equationl.giteetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.equationl.giteetodo.constants.IntentDataKey
import com.equationl.giteetodo.ui.HomeNavHost
import com.equationl.giteetodo.ui.page.TodoDetailScreen
import com.equationl.giteetodo.ui.theme.GiteeTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GiteeTodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val issueNumber = intent.getStringExtra(IntentDataKey.IssueNumber)
                    if (issueNumber.isNullOrBlank()) {
                        HomeNavHost()
                    }
                    else {
                        TodoDetailScreen(null, issueNumber)
                    }
                }
            }
        }

    }
}