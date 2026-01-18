package com.example.smartcloset_frontend.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartcloset_frontend.data.repository.UserSessionRepository

//import com.example.smartcloset_frontend.ui.TestScreen
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
import com.example.smartcloset_frontend.ui.suggestion.SuggestionScreen
import com.example.smartcloset_frontend.ui.suggestion_history.SuggestionHistoryScreen
import com.example.smartcloset_frontend.ui.generated_result.GeneratedResultScreen
import com.example.smartcloset_frontend.ui.ClothesDetailScreen
import com.example.smartcloset_frontend.ui.ItemConfirmationScreen
import com.example.smartcloset_frontend.ui.ItemRegistrationScreen
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionHistoryViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModelFactory
import com.example.smartcloset_frontend.viewmodel.GetWeatherViewModel

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
    val itemViewModel: ItemViewModel = viewModel()
    val sharedVM: AddItemViewModel = viewModel()
    val suggestionViewModel: SuggestionViewModel = viewModel()
    val GetWeatherViewModel: GetWeatherViewModel = viewModel()
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
                navController = navController,
                userSessionViewModel = userSessionViewModel
            )
        }
//        composable("test") { TestScreen(navController) }
        composable("coordinate") { 
            SuggestionScreen(
                navController,
                suggestionViewModel = suggestionViewModel,
                userSessionViewModel = userSessionViewModel, getWeatherViewModel = GetWeatherViewModel)
        }



        composable("suggestion_history") { 
            val historyViewModel: SuggestionHistoryViewModel = viewModel()
            SuggestionHistoryScreen(navController, historyViewModel, suggestionViewModel)
        }
        composable(
            route = "generate?imageUrl={imageUrl}",
            arguments = listOf(
                navArgument("imageUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl")
            val context = LocalContext.current
            
            // 画像URLが指定されている場合、ViewModelに設定
            imageUrl?.let { url ->
                LaunchedEffect(url) {
                    suggestionViewModel.setGeneratedImageFromNotification(url)
                }
            }
            // 生成結果画面に遷移したらバッジを消す
            LaunchedEffect(Unit) {
                val sharedPreferences = context.applicationContext.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                sharedPreferences.edit().putBoolean("has_new_generated_image", false).apply()
            }
            GeneratedResultScreen(navController, suggestionViewModel)
        }
        composable("profile") { ProfileScreen(navController, userSessionViewModel = userSessionViewModel) }
        composable("profile_edit") { ProfileEditScreen(navController, userSessionViewModel = userSessionViewModel) }
//        composable("clothes_detail") { ClothesDetailScreen(navController) }
        composable("register") { ItemRegistrationScreen(navController,sharedVM) }
        composable("item_confirm") { ItemConfirmationScreen(navController, sharedVM, userSessionViewModel) }
        
        // 編集画面のルート
        composable(
            "item_edit/{itemId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")!!
            
            LaunchedEffect(itemId) {
                sharedVM.loadItemForEdit(itemId)
            }
            
            ItemRegistrationScreen(
                navController = navController,
                viewModel = sharedVM,
                isEditMode = true,
                itemId = itemId
            )
        }
        
        composable("item_confirm_edit") { 
            ItemConfirmationScreen(
                navController = navController, 
                viewModel = sharedVM, 
                userSessionViewModel = userSessionViewModel,
                isEditMode = true
            ) 
        }
        
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
            ClothesDetailScreen(navController, clothesId = id.toString())
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