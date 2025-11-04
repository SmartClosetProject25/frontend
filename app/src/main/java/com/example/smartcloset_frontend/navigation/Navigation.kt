package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.example.smartcloset_frontend.ui.TestScreen
//import com.example.smartcloset_frontend.ui.ClothesDetailScreen
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.LoginScreen
import com.example.smartcloset_frontend.ui.SignupScreen
import com.example.smartcloset_frontend.ui.SignupCompleteScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordRequestScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordResetScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordCompleteScreen
import com.example.smartcloset_frontend.ui.ProfileEditScreen
import com.example.smartcloset_frontend.ui.ProfileScreen
import com.example.smartcloset_frontend.ui.SuggestionHistoryScreen
import com.example.smartcloset_frontend.ui.SuggestionScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") { 
            LoginScreen(
                onLoginClick = { email, password ->
                    // 認証成功時にホーム画面へ遷移（バックスタックからlogin等を除去）
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate("signup")
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot")
                }
            )
        }
        composable("home") { HomeScreen(navController) }
        composable("test") { TestScreen(navController) }
        composable("coordinate") { SuggestionScreen(navController) }
//        composable("clothes_detail") {
//            ClothesDetailScreen(navController, clothesId = null)
//        }
        composable("suggestion_history") { 
            SuggestionHistoryScreen(navController)
        }
        composable("profile") { ProfileScreen(navController) }
        composable("profile_edit") { ProfileEditScreen(navController) }
//        composable("register") { RegisterScreen(navController) }
//        composable("favorite") { FavoriteScreen(navController) }
//        composable("settings") { SettingsScreen(navController) }
        // 既存ナビ用の register は別用途で使用する想定
        // composable("register") { RegisterScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("signup_complete") { SignupCompleteScreen(navController) }
        composable("forgot") { ForgotPasswordRequestScreen(navController) }
        composable("forgot_reset") { ForgotPasswordResetScreen(navController) }
        composable("forgot_complete") { ForgotPasswordCompleteScreen(navController) }
    }
}