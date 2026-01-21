package com.example.smartcloset_frontend

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.navigation.BottomNavBar
import com.example.smartcloset_frontend.navigation.NavGraph
import com.example.smartcloset_frontend.ui.theme.SmartClosetTheme
import com.example.smartcloset_frontend.data.PreferencesManager
import com.example.smartcloset_frontend.data.repository.UserSessionRepository
import com.example.smartcloset_frontend.viewmodel.LoginViewModel
import com.example.smartcloset_frontend.viewmodel.SuggestionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModel
import com.example.smartcloset_frontend.viewmodel.UserSessionViewModelFactory
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
                val userSessionRepository = remember { UserSessionRepository(context) }
                val userSessionViewModel: UserSessionViewModel = viewModel(
                    factory = UserSessionViewModelFactory(userSessionRepository)
                )
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // 生成完了を監視して自動遷移
                val sharedPreferences = remember {
                    context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                }
                var imageGenerationComplete by remember { 
                    mutableStateOf(sharedPreferences.getBoolean("image_generation_complete", false))
                }
                
                // SharedPreferencesの変更を監視
                DisposableEffect(Unit) {
                    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                        if (key == "image_generation_complete") {
                            val isComplete = sharedPreferences.getBoolean(key, false)
                            if (isComplete && !imageGenerationComplete) {
                                imageGenerationComplete = true
                            }
                        }
                    }
                    sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                    onDispose {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                    }
                }
                
                // 生成完了時に自動遷移（どの画面にいても）
                LaunchedEffect(imageGenerationComplete) {
                    if (imageGenerationComplete) {
                        val imageUrl = sharedPreferences.getString("generated_image_url", null)
                        if (imageUrl != null) {
                            // フラグをリセット
                            sharedPreferences.edit().apply {
                                putBoolean("image_generation_complete", false)
                                putBoolean("has_new_generated_image", false)
                                apply()
                            }
                            // 自動的に生成結果画面へ遷移
                            navController.navigate("generate?imageUrl=${android.net.Uri.encode(imageUrl)}") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            imageGenerationComplete = false
                        }
                    }
                }
                
                // Deep Link処理
                var deepLinkUri by rememberSaveable { mutableStateOf<Uri?>(null) }
                
                // IntentからDeep Linkを取得（初期起動時とonNewIntent時）
                DisposableEffect(intent) {
                    val currentIntent = this@MainActivity.intent
                    if (currentIntent?.action == Intent.ACTION_VIEW && currentIntent.data != null) {
                        deepLinkUri = currentIntent.data
                    }
                    onDispose { }
                }
                
                // 初期起動時のIntentからDeep Linkをチェック
                val initialIntentUri = remember { 
                    intent?.takeIf { it.action == Intent.ACTION_VIEW }?.data 
                }
                
                val showBottomBar = currentRoute != null && currentRoute !in listOf(
                    "login", 
//                    "register",
                    "forgot", 
                    "forgot_email_sent",
                    "signup", 
                    "signup_complete", 
                    "forgot_complete"
                ) && !currentRoute.startsWith("forgot_reset")
                
                // 自動ログイン可能かどうかを同期的にチェック（Deep Link優先）
                val initialDestination = remember(initialIntentUri) {
                    // Deep Linkが来ている場合は、適切な画面を初期画面に
                    initialIntentUri?.let { uri ->
                        if (uri.scheme == "smartcloset") {
                            when (uri.host) {
                                "reset-password" -> {
                                    val token = uri.getQueryParameter("token")
                                    if (!token.isNullOrEmpty()) {
                                        return@remember "forgot_reset?token=$token"
                                    }
                                }
                            }
                        }
                    }
                    
                    // Deep Linkがない場合のみ、通常のロジック
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
                
                // Deep Link処理（onNewIntentで来た場合の処理）
                LaunchedEffect(deepLinkUri) {
                    deepLinkUri?.let { uri ->
                        delay(200)  // NavGraphが設定されるまで待つ
                        
                        if (uri.scheme == "smartcloset") {
                            when (uri.host) {
                                "reset-password" -> {
                                    // initialDestinationが既にforgot_resetの場合は処理不要
                                    if (initialDestination.startsWith("forgot_reset")) {
                                        deepLinkUri = null
                                        return@LaunchedEffect
                                    }
                                    
                                    val token = uri.getQueryParameter("token")
                                    if (!token.isNullOrEmpty()) {
                                        navController.navigate("forgot_reset?token=$token") {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        }
                        deepLinkUri = null  // 処理後はクリア
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
                            val userId = loginViewModel.autoLogin(savedEmail, savedPassword)
                            if (userId != null) {
                                // 自動ログイン成功時はuserIdを保存
                                userSessionViewModel.setUserId(userId)
                            } else {
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
                            BottomNavBar(navController, addItemViewModel = viewModel())
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
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // onNewIntentで来たDeep Linkを処理
        // Composeの再コンポジションでDeep Linkが処理される
    }
    
    override fun onResume() {
        super.onResume()
        // Activityが前面に来た時に、Intentを再チェック
        // Deep Linkの処理はCompose内で行われる
    }
}
