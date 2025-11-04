package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
//        composable("register") { RegisterScreen(navController) }
//        composable("coordinate") { CoordinateScreen(navController) }
//        composable("favorite") { FavoriteScreen(navController) }

    }
}