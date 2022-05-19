package com.equationl.giteetodo.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.common.RouteParams
import com.equationl.giteetodo.ui.page.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNavHost() {
    val navController = rememberAnimatedNavController()
    AnimatedNavHost(navController, Route.LOGIN) {
        composable(Route.LOGIN) {
            LoginScreen(navController)
        }

        composable(Route.OAuthLogin) {
            OAuthLoginScreen(navController)
        }

        composable(Route.ABOUT,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            }
        ) {
            AboutScreen(navController)
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
                slideIntoContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(700))
            }
        ) {
            val argument = requireNotNull(it.arguments)
            val issueNum = argument.getString(RouteParams.PAR_ISSUE_NUM) ?: "null"
            TodoDetailScreen(navController, issueNum)
        }

        composable(Route.REPO_DETAIL) {
            RepoDetailScreen(navController)
        }

        composable(Route.REPO_LIST,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Up, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Down, animationSpec = tween(700))
            }
        ) {
            RepoListScreen(navController)
        }

        composable("${Route.LABEL_MG}/{${RouteParams.PAR_REPO_PATH}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = false}
            ),
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            }
        ) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH) ?: "null/null"
            LabelManagerScreen(repoPath = repoPath, navController = navController)
        }

        composable(Route.SETTING,
            enterTransition = {
                slideIntoContainer(AnimatedContentScope.SlideDirection.Left, animationSpec = tween(700))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentScope.SlideDirection.Right, animationSpec = tween(700))
            }) {
            SettingScreen(navController)
        }

    }
}