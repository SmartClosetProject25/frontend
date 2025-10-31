package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcloset_frontend.ui.ClothesDetailScreen
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.SuggestionHistoryScreen
import com.example.smartcloset_frontend.ui.SuggestionScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("coordinate") { SuggestionScreen(navController) }
        composable("clothes_detail") { 
            ClothesDetailScreen(navController, clothesId = null)
        }
        composable("suggestion_history") { 
            SuggestionHistoryScreen(navController)
        }
//        composable("register") { RegisterScreen(navController) }
//        composable("favorite") { FavoriteScreen(navController) }
//        composable("settings") { SettingsScreen(navController) }
    }
}