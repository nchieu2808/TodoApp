package com.nch.todoapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.ui.create.CreateTodoScreen
import com.nch.todoapp.ui.create.CreateTodoViewModel
import com.nch.todoapp.ui.details.DetailsScreen
import com.nch.todoapp.ui.details.DetailsViewModel
import com.nch.todoapp.ui.list.ListTodoScreen
import com.nch.todoapp.ui.list.ListTodoViewModel

@Composable
fun AppNavigation(todoManager: TodoManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ListTodoScreen(
                viewModel = viewModel { ListTodoViewModel(todoManager) },
                onNavigateToCreate = { navController.navigate("create") },
                onNavigateToDetails = { id -> navController.navigate("details/$id") }
            )
        }

        composable("create") {
            CreateTodoScreen(
                viewModel = viewModel { CreateTodoViewModel(todoManager) },
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("details/{todoId}") { backStackEntry ->
            val todoId = backStackEntry.arguments?.getString("todoId") ?: ""
            DetailsScreen(
                id = todoId,
                viewModel = viewModel { DetailsViewModel(todoManager) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}