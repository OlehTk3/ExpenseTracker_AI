package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapp.ui.AddExpenseScreen
import com.example.myapp.ui.ExpenseViewModel
import com.example.myapp.ui.ExpenseViewModelFactory
import com.example.myapp.ui.HomeScreen
import com.example.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                val app = application as MyAppApplication
                val viewModel: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModelFactory(app.repository)
                )

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(viewModel, navController)
                    }
                    composable(
                        "add_expense/{expenseId}",
                        arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
                        AddExpenseScreen(viewModel, navController, expenseId)
                    }
                }
            }
        }
    }
}