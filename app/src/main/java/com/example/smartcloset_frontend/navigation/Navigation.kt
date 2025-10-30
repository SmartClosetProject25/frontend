package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.LoginScreen

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
                    // 新規登録画面へ遷移（後で実装）
                },
                onForgotPasswordClick = {
                    // パスワード再設定画面へ遷移（後で実装）
                }
            )
        }
        composable("home") { HomeScreen(navController) }
//        composable("register") { RegisterScreen(navController) }
//        composable("coordinate") { CoordinateScreen(navController) }
//        composable("favorite") { FavoriteScreen(navController) }
//        composable("settings") { SettingsScreen(navController) }
        composable("register") { /* TODO: RegisterScreen(navController) */ }
        composable("forgot") { /* TODO: ForgotPasswordScreen(navController) */ }
    }
}