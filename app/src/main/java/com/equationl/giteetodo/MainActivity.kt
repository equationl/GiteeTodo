package com.equationl.giteetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.equationl.giteetodo.constants.IntentDataKey
import com.equationl.giteetodo.ui.HomeNavHost
import com.equationl.giteetodo.ui.page.TodoDetailScreen
import com.equationl.giteetodo.ui.theme.GiteeTodoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            // for issue: https://gitee.com/equation/GiteeTodo/issues/I56D8N
            // see this: https://v2ex.com/t/851784
            delay(100)
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
}