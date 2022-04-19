package com.equationl.giteetodo.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equationl.giteetodo.util.RouteConfig
import com.equationl.giteetodo.util.RouteTodoParams

@Composable
fun HomeNavHost() {
    val navController = rememberNavController()
    NavHost(navController, RouteConfig.ROUTE_LOGIN) {
        composable(RouteConfig.ROUTE_LOGIN) {
            LoginScreen(navController)
        }

        composable(RouteConfig.ROUTE_TODO_LIST) {
            TodoListScreen(navController)
        }

        composable("${RouteConfig.ROUTE_TODO_DETAIL}/{${RouteTodoParams.PAR_ISSUE_NUM}}",
        arguments = listOf(
            navArgument(RouteTodoParams.PAR_ISSUE_NUM) { type = NavType.StringType }
        )) {
            val argument = requireNotNull(it.arguments)
            val issueNum = argument.getString(RouteTodoParams.PAR_ISSUE_NUM) ?: "null"
            TodoDetailScreen(navController, issueNum)
        }

        composable(RouteConfig.ROUTE_REPO_DETAIL) {
            RepoDetailScreen(navController)
        }
    }
}