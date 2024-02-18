package com.equationl.giteetodo.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.common.RouteParams
import com.equationl.giteetodo.ui.page.AboutScreen
import com.equationl.giteetodo.ui.page.HomeScreen
import com.equationl.giteetodo.ui.page.LabelManagerScreen
import com.equationl.giteetodo.ui.page.LoginScreen
import com.equationl.giteetodo.ui.page.OAuthLoginScreen
import com.equationl.giteetodo.ui.page.RepoDetailScreen
import com.equationl.giteetodo.ui.page.RepoListScreen
import com.equationl.giteetodo.ui.page.SettingScreen
import com.equationl.giteetodo.ui.page.TodoDetailScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNavHost() {
    val navController = rememberNavController()
    NavHost(navController, Route.LOGIN) {
        composable(Route.LOGIN) {
            Column(Modifier.systemBarsPadding()) {
                LoginScreen(navController)
            }
        }

        composable(Route.OAuthLogin) {
            Column(Modifier.systemBarsPadding()) {
                OAuthLoginScreen(navController)
            }
        }

        composable(Route.ABOUT,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))
            }
        ) {
            Column(Modifier.systemBarsPadding()) {
                AboutScreen(navController)
            }
        }

        composable("${Route.HOME}/{${RouteParams.PAR_REPO_PATH}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH) ?: "null/null"
            HomeScreen(navController, repoPath)
        }

        composable("${Route.TODO_DETAIL}/{${RouteParams.PAR_ISSUE_NUM}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_ISSUE_NUM) {
                    type = NavType.StringType
                    nullable = true}
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(700))
            }
        ) {
            val argument = requireNotNull(it.arguments)
            val issueNum = argument.getString(RouteParams.PAR_ISSUE_NUM) ?: "null"
            Column(Modifier.systemBarsPadding()) {
                TodoDetailScreen(navController, issueNum)
            }
        }

        composable(Route.REPO_DETAIL) {
            Column(Modifier.systemBarsPadding()) {
                RepoDetailScreen(navController)
            }
        }

        composable(Route.REPO_LIST,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(700))
            }
        ) {
            Column(Modifier.systemBarsPadding()) {
                RepoListScreen(navController)
            }
        }

        composable("${Route.LABEL_MG}/{${RouteParams.PAR_REPO_PATH}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false}
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))
            }
        ) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH) ?: "null/null"
            Column(Modifier.systemBarsPadding()) {
                LabelManagerScreen(repoPath = repoPath, navController = navController)
            }
        }

        composable(Route.SETTING,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(700))
            }) {
            Column(Modifier.systemBarsPadding()) {
                SettingScreen(navController)
            }
        }

    }
}