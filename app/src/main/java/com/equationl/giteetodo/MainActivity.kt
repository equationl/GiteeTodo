package com.equationl.giteetodo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.equationl.giteetodo.ui.HomeNavHost
import com.equationl.giteetodo.ui.theme.GiteeTodoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        HomeNavHost()
                    }
                }
            }
        }
    }
}