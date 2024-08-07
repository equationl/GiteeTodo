package com.equationl.giteetodo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.equationl.giteetodo.constants.IntentDataKey
import com.equationl.giteetodo.ui.HomeNavHost
import com.equationl.giteetodo.ui.LocalNavController
import com.equationl.giteetodo.ui.LocalShareAnimatedContentScope
import com.equationl.giteetodo.ui.LocalSharedTransitionScope
import com.equationl.giteetodo.ui.page.TodoDetailScreen
import com.equationl.giteetodo.ui.theme.GiteeTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            GiteeTodoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val issueNumber = intent.getStringExtra(IntentDataKey.ISSUE_NUMBER)
                    val repoPath = intent.getStringExtra(IntentDataKey.REPO_PATH)

                    Log.d("el, MainActivity", "onCreate: repoPath = $repoPath")

                    if (issueNumber.isNullOrBlank()) {
                        HomeNavHost()
                    }
                    else {
                        SharedTransitionLayout {
                            AnimatedContent(targetState = true, label = "testAnimatedContent") { show ->
                                // 这里是从小组件打开的 issue 详情，逻辑上并不会存在共享元素转换效果，但是为了保证数据不为空
                                // “强行” 造出了需要的参数，实际上这些参数并不会被使用
                                println("show = $show")
                                CompositionLocalProvider(
                                    LocalNavController provides rememberNavController(),
                                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                                    LocalShareAnimatedContentScope provides this@AnimatedContent,
                                ) {
                                    TodoDetailScreen(null, issueNumber, "Loading...", issueRepo = repoPath)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}