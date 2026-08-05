package com.nch.todoapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nch.todoapp.ui.create.CreateTodoScreen
import com.nch.todoapp.ui.create.CreateTodoViewModel
import com.nch.todoapp.ui.details.DetailsScreen
import com.nch.todoapp.ui.details.DetailsViewModel
import com.nch.todoapp.ui.list.ListTodoScreen
import com.nch.todoapp.ui.list.ListTodoViewModel
import com.nch.todoapp.ui.login.LoginScreen
import com.nch.todoapp.ui.login.LoginViewModel

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val user by loginViewModel.currentUser.collectAsState()
    val navController = rememberNavController()
    val startDestination = remember {
        if (loginViewModel.currentUser.value != null) Routes.LIST else Routes.LOGIN
    }

    LaunchedEffect(user) {
        val target = if (user == null) Routes.LOGIN else Routes.LIST
        val current = navController.currentDestination?.route
        if (current == target) return@LaunchedEffect
        navController.navigate(target) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(viewModel = loginViewModel)
        }

        composable(Routes.LIST) {
            ListTodoScreen(
                viewModel = hiltViewModel<ListTodoViewModel>(),
                onNavigateToCreate = { navController.navigate(Routes.CREATE) },
                onNavigateToDetails = { id -> navController.navigate(Routes.details(id)) },
                onSignOut = { loginViewModel.signOut() }
            )
        }

        composable(Routes.CREATE) {
            CreateTodoScreen(
                viewModel = hiltViewModel<CreateTodoViewModel>(),
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DETAILS) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getString("todoId") ?: ""
            DetailsScreen(
                id = todoId,
                viewModel = hiltViewModel<DetailsViewModel>(),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
