package com.example.smartcloset_frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.navigation.BottomNavBar
import com.example.smartcloset_frontend.navigation.NavGraph
import com.example.smartcloset_frontend.ui.theme.SmartClosetTheme
import com.example.smartcloset_frontend.data.PreferencesManager
import com.example.smartcloset_frontend.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartClosetTheme {
                val context = LocalContext.current
                val preferencesManager = remember { PreferencesManager(context) }
                val loginViewModel: LoginViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute != null && currentRoute !in listOf("login", "register", "forgot", "signup", "signup_complete", "forgot_reset", "forgot_complete")
                
                // 自動ログイン可能かどうかを同期的にチェック
                val initialDestination = remember {
                    if (preferencesManager.shouldAutoLogin()) {
                        val savedEmail = preferencesManager.getSavedEmail()
                        val savedPassword = preferencesManager.getSavedPassword()
                        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                            "home"  // 自動ログイン可能な場合はホーム画面から開始
                        } else {
                            "login"
                        }
                    } else {
                        if (preferencesManager.isLoginExpired()) {
                            preferencesManager.clearLoginInfo()
                        }
                        "login"
                    }
                }
                
                // NavGraphが設定された後に自動ログインを試行
                LaunchedEffect(Unit) {
                    if (initialDestination == "home") {
                        // NavGraphが設定されるまで少し待つ
                        delay(100)
                        
                        val savedEmail = preferencesManager.getSavedEmail()
                        val savedPassword = preferencesManager.getSavedPassword()
                        
                        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                            // 自動ログインを試行
                            val success = loginViewModel.autoLogin(savedEmail, savedPassword)
                            if (!success) {
                                // 自動ログイン失敗時は情報をクリアしてログイン画面に遷移
                                preferencesManager.clearLoginInfo()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph(
                            navController = navController,
                            startDestination = initialDestination
                        )
                    }
                }
            }
        }
    }
}