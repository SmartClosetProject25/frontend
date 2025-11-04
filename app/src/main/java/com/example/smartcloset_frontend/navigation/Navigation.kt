package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.LoginScreen
import com.example.smartcloset_frontend.ui.SignupScreen
import com.example.smartcloset_frontend.ui.SignupCompleteScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordRequestScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordResetScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordCompleteScreen
import com.example.smartcloset_frontend.ui.ItemRegistrationScreen
import com.example.smartcloset_frontend.ui.ItemConfirmationScreen
import com.example.smartcloset_frontend.ui.ProfileEditScreen
import com.example.smartcloset_frontend.ui.ProfileScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "register") {
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
        composable("profile") { ProfileScreen(navController) }
        composable("profile_edit") { ProfileEditScreen(navController) }
        // --- アイテム登録画面 ---
        composable("register") { ItemRegistrationScreen(navController) }

        // 💡 --- アイテム確認画面 (JSON引数を受け取るように修正) ---
        // ItemRegistrationScreenから "confirmation/{encodedJson}" の形式で遷移します
        composable(
            route = "confirmation/{itemJson}",
            arguments = listOf(
                navArgument("itemJson") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val itemJson = backStackEntry.arguments?.getString("itemJson")

            if (itemJson != null) {
                ItemConfirmationScreen(
                    navController = navController,
                    itemJson = itemJson // 取得したJSON文字列を渡す
                )
            } else {
                // 引数がない場合は前の画面に戻る
                navController.popBackStack()
            }
        }
//        composable("coordinate") { CoordinateScreen(navController) }
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