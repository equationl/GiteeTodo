package com.equationl.giteetodo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equationl.giteetodo.ui.common.Route
import com.equationl.giteetodo.ui.common.RouteParams

@Composable
fun HomeNavHost() {
    val navController = rememberNavController()
    NavHost(navController, Route.LOGIN) {
        composable(Route.LOGIN) {
            LoginScreen(navController)
        }

        composable("${Route.TODO_LIST}/{${RouteParams.PAR_REPO_PATH}}",
            arguments = listOf(
                navArgument(RouteParams.PAR_REPO_PATH) {
                    type = NavType.StringType
                    nullable = true
                }
            )) {
            val argument = requireNotNull(it.arguments)
            val repoPath = argument.getString(RouteParams.PAR_REPO_PATH) ?: "null"
            TodoListScreen(navController, repoPath)
        }

        composable("${Route.TODO_DETAIL}/{${RouteParams.PAR_ISSUE_NUM}}",
        arguments = listOf(
            navArgument(RouteParams.PAR_ISSUE_NUM) {
                type = NavType.StringType
                nullable = true}
        )) {
            val argument = requireNotNull(it.arguments)
            val issueNum = argument.getString(RouteParams.PAR_ISSUE_NUM) ?: "null"
            TodoDetailScreen(navController, issueNum)
        }

        composable(Route.REPO_DETAIL) {
            RepoDetailScreen(navController)
        }

        composable("${Route.REPO_LIST}?${RouteParams.PAR_NEED_LOAD_REPO_LIST}={${RouteParams.PAR_NEED_LOAD_REPO_LIST}}",
        arguments = listOf(
            navArgument(RouteParams.PAR_NEED_LOAD_REPO_LIST) {
                defaultValue = true
                type = NavType.BoolType
            }
        )) {
            val argument = requireNotNull(it.arguments)
            val isNeedLoad = argument.getBoolean(RouteParams.PAR_NEED_LOAD_REPO_LIST)
            RepoListScreen(navController, isNeedLoad)
        }
    }
}