package com.example.smartcloset_frontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartcloset_frontend.data.repository.UserSessionRepository

import com.example.smartcloset_frontend.ui.TestScreen
import com.example.smartcloset_frontend.ui.HomeScreen
import com.example.smartcloset_frontend.ui.SettingsScreen
import com.example.smartcloset_frontend.ui.LoginScreen
import com.example.smartcloset_frontend.ui.SignupScreen
import com.example.smartcloset_frontend.ui.SignupCompleteScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordRequestScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordEmailSentScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordResetScreen
import com.example.smartcloset_frontend.ui.ForgotPasswordCompleteScreen
import com.example.smartcloset_frontend.ui.ProfileEditScreen
import com.example.smartcloset_frontend.ui.ProfileScreen
import com.example.smartcloset_frontend.ui.SuggestionHistoryScreen
import com.example.smartcloset_frontend.ui.SuggestionScreen
import com.example.smartcloset_frontend.ui.ClothesDetailScreen
import com.example.smartcloset_frontend.ui.ItemConfirmationScreen
import com.example.smartcloset_frontend.ui.ItemRegistrationScreen
import com.example.smartcloset_frontend.ui.GeneratedResultScreen
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    val context = LocalContext.current.applicationContext
    val userSessionRepository = remember { UserSessionRepository(context) }
    val userSessionViewModel: UserSessionViewModel = viewModel(
        factory = UserSessionViewModelFactory(userSessionRepository)
    )
    val itemViewModel: ItemViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val sharedVM: AddItemViewModel = viewModel()
    NavHost(navController, startDestination = startDestination) {
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
                },
                //debug用に直接homeへ飛ぶボタンを追加
                navController = navController,
                userSessionViewModel = userSessionViewModel
            )
        }
        composable("home") { backStackEntry ->
            val viewModel: ItemViewModel = viewModel(backStackEntry)
            HomeScreen(navController, itemViewModel ,userSessionViewModel = userSessionViewModel)
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }
        composable("test") { TestScreen(navController) }
        composable("coordinate") { SuggestionScreen(navController) }

        composable("suggestion_history") { 
            SuggestionHistoryScreen(navController)
        }
        composable("generate") { GeneratedResultScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("profile_edit") { ProfileEditScreen(navController) }
//        composable("clothes_detail") { ClothesDetailScreen(navController) }
        composable("register") { ItemRegistrationScreen(navController,sharedVM) }
        composable("item_confirm") { ItemConfirmationScreen(navController,sharedVM) }
        // 詳細画面への遷移時に itemId を渡す
        composable(
            "detail/{itemId}",
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("itemId")!!
            ClothesDetailScreen(navController, itemId = id)
        }

//        composable("favorite") { FavoriteScreen(navController) }

//        composable("settings") { SettingsScreen(navController) }

        // 認証関連画面
        composable("signup") { SignupScreen(navController) }
        composable("signup_complete") { SignupCompleteScreen(navController) }
        composable("forgot") { ForgotPasswordRequestScreen(navController) }
        composable("forgot_email_sent") { ForgotPasswordEmailSentScreen(navController) }
        composable(
            route = "forgot_reset?token={token}",
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")
            ForgotPasswordResetScreen(navController, token)
        }
        composable("forgot_complete") { ForgotPasswordCompleteScreen(navController) }
    }
}