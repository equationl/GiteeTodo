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

        composable(Route.TODO_LIST) {
            TodoListScreen(navController)
        }

        composable("${Route.TODO_DETAIL}/{${RouteParams.PAR_ISSUE_NUM}}",
        arguments = listOf(
            navArgument(RouteParams.PAR_ISSUE_NUM) { type = NavType.StringType }
        )) {
            val argument = requireNotNull(it.arguments)
            val issueNum = argument.getString(RouteParams.PAR_ISSUE_NUM) ?: "null"
            TodoDetailScreen(navController, issueNum)
        }

        composable(Route.REPO_DETAIL) {
            RepoDetailScreen(navController)
        }
    }
}