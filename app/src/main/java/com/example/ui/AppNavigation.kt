package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.viewmodel.OrderListViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: OrderListViewModel
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { user ->
                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGenerateOrder = { navController.navigate("generate_order") },
                onNavigateToOrderDetails = { orderId -> navController.navigate("order_details/$orderId") },
                onNavigateToChat = { userId -> navController.navigate("chat/$userId") },
                onLogout = { 
                    viewModel.setCurrentUser(null)
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }

        composable("generate_order") {
            GenerateOrderScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderCreated = { navController.popBackStack() },
                onNavigateToChat = { userId -> navController.navigate("chat/$userId") }
            )
        }

        composable(
            route = "order_details/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailsScreen(
                viewModel = viewModel,
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { userId -> navController.navigate("chat/$userId") }
            )
        }

        composable(
            route = "chat/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            ChatScreen(
                viewModel = viewModel,
                otherUserId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
