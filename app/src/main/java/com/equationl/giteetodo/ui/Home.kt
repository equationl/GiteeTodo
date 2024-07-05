@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.equationl.giteetodo.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.common.RouteParams
import com.equationl.giteetodo.ui.page.AboutScreen
import com.equationl.giteetodo.ui.page.HomeScreen
import com.equationl.giteetodo.ui.page.ImageScreen
import com.equationl.giteetodo.ui.page.LabelManagerScreen
import com.equationl.giteetodo.ui.page.LoginScreen
import com.equationl.giteetodo.ui.page.OAuthLoginScreen
import com.equationl.giteetodo.ui.page.RepoDetailScreen
import com.equationl.giteetodo.ui.page.RepoListScreen
import com.equationl.giteetodo.ui.page.SettingScreen
import com.equationl.giteetodo.ui.page.TodoDetailScreen

val LocalNavController =
    staticCompositionLocalOf<NavHostController> { error("No NavController provided") }
val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope> { error("No SharedTransitionScope provided") }
val LocalShareAnimatedContentScope =
    staticCompositionLocalOf<AnimatedContentScope> { error("No AnimatedContentScope provided") }

@Composable
fun HomeNavHost() {
    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalNavController provides rememberNavController(),
            LocalSharedTransitionScope provides this@SharedTransitionLayout
        ) {
            NavHost(LocalNavController.current, Route.LOGIN) {
                composable(Route.LOGIN) {
                    Column(Modifier.systemBarsPadding()) {
                        LoginScreen()
                    }
                }

                composable(Route.OAUTH_LOGIN) {
                    Column(Modifier.systemBarsPadding()) {
                        OAuthLoginScreen()
                    }
                }

                composable(Route.ABOUT,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )
                    }
                ) {
                    Column(Modifier.systemBarsPadding()) {
                        AboutScreen()
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

                    CompositionLocalProvider(LocalShareAnimatedContentScope provides this@composable) {
                        HomeScreen(
                            repoPath,
                        )
                    }
                }

                composable(
                    "${Route.TODO_DETAIL}/{${RouteParams.PAR_ISSUE_NUM}}/{${RouteParams.PAR_ISSUE_TITLE}}",
                    arguments = listOf(
                        navArgument(RouteParams.PAR_ISSUE_NUM) {
                            type = NavType.StringType
                            nullable = true
                        },
                        navArgument(RouteParams.PAR_ISSUE_TITLE) {
                            type = NavType.StringType
                            nullable = true
                        }
                    ),
//                enterTransition = {
//                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Down, animationSpec = tween(700))
//                },
//                exitTransition = {
//                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Up, animationSpec = tween(700))
//                }
                ) {
                    val argument = requireNotNull(it.arguments)
                    val issueNum = argument.getString(RouteParams.PAR_ISSUE_NUM) ?: "null"
                    val issueTitle = argument.getString(RouteParams.PAR_ISSUE_TITLE)
                    CompositionLocalProvider(LocalShareAnimatedContentScope provides this@composable) {
                        Column(Modifier.systemBarsPadding()) {
                            TodoDetailScreen(
                                LocalNavController.current,
                                issueNum,
                                issueTitle,
                            )
                        }
                    }
                }

                composable(Route.REPO_DETAIL) {
                    Column(Modifier.systemBarsPadding()) {
                        RepoDetailScreen()
                    }
                }

                composable(Route.REPO_LIST,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(700)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(700)
                        )
                    }
                ) {
                    Column(Modifier.systemBarsPadding()) {
                        RepoListScreen()
                    }
                }

                composable("${Route.LABEL_MG}/{${RouteParams.PAR_REPO_PATH}}",
                    arguments = listOf(
                        navArgument(RouteParams.PAR_REPO_PATH) {
                            type = NavType.StringType
                            nullable = false
                        }
                    ),
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )
                    }
                ) {
                    val argument = requireNotNull(it.arguments)
                    val repoPath = argument.getString(RouteParams.PAR_REPO_PATH) ?: "null/null"
                    Column(Modifier.systemBarsPadding()) {
                        LabelManagerScreen(repoPath = repoPath)
                    }
                }

                composable(Route.SETTING,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )
                    }) {
                    Column(Modifier.systemBarsPadding()) {
                        SettingScreen()
                    }
                }

                composable("${Route.IMAGE_PREVIEW}/{${RouteParams.PAR_IMAGE_URL}}",
                    arguments = listOf(
                        navArgument(RouteParams.PAR_IMAGE_URL) {
                            type = NavType.StringType
                            nullable = false
                        }
                    )) {
                    val argument = requireNotNull(it.arguments)
                    val imageUrl = argument.getString(RouteParams.PAR_IMAGE_URL)

                    ImageScreen(image = imageUrl ?: "")
                }

            }
        }
    }
}