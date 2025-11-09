package com.example.regen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.regen.components.BudgetScreen
import com.example.regen.components.HomeScreen
import com.example.regen.components.SettingsScreen
import com.example.regen.components.WalletScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onWalletClick = { navController.navigate("wallet") },
                onBudgetClick = { navController.navigate("budget") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("wallet") {
            WalletScreen(onBackClick = { navController.popBackStack() })
        }
        composable("budget") {
            BudgetScreen(onBackClick = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}