package com.example.smartcloset_frontend

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.navigation.BottomNavBar
import com.example.smartcloset_frontend.navigation.NavGraph
import com.example.smartcloset_frontend.ui.theme.SmartClosetTheme

class MainActivity : ComponentActivity() {
    // Deep Link URIを保持するための状態変数
    private var deepLinkUri: Uri? by mutableStateOf(null)
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // onCreate時のIntentを初期値として設定
        deepLinkUri = intent.data
        
        setContent {
            SmartClosetTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                // ルートのベース部分を取得（クエリパラメータを除く）
                val routeBase = currentRoute?.substringBefore("?") ?: currentRoute
                val hideBottomBarRoutes = listOf("login", "register", "forgot", "signup", "signup_complete", "forgot_email_sent", "forgot_reset", "forgot_complete")
                val showBottomBar = currentRoute != null && routeBase !in hideBottomBarRoutes

                // Deep Link処理 - deepLinkUriを監視
                LaunchedEffect(deepLinkUri) {
                    deepLinkUri?.let { uri ->
                        android.util.Log.d("MainActivity", "=== Deep Link Processing ===")
                        android.util.Log.d("MainActivity", "URI: $uri")
                        android.util.Log.d("MainActivity", "Scheme: ${uri.scheme}")
                        android.util.Log.d("MainActivity", "Host: ${uri.host}")
                        
                        if (uri.scheme == "smartcloset" && uri.host == "reset-password") {
                            val token = uri.getQueryParameter("token")
                            android.util.Log.d("MainActivity", "Token extracted: $token")
                            
                            if (token != null && token.isNotEmpty()) {
                                android.util.Log.d("MainActivity", "Navigating to forgot_reset with token: $token")
                                navController.navigate("forgot_reset?token=$token") {
                                    popUpTo("login") { inclusive = false }
                                    launchSingleTop = true
                                }
                            } else {
                                android.util.Log.w("MainActivity", "Token is null or empty")
                            }
                        } else {
                            android.util.Log.d("MainActivity", "URI does not match deep link pattern")
                        }
                    } ?: run {
                        android.util.Log.d("MainActivity", "No URI to process")
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
                        NavGraph(navController)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        android.util.Log.d("MainActivity", "=== onNewIntent called ===")
        android.util.Log.d("MainActivity", "Intent URI: ${intent.data}")
        setIntent(intent)
        // 状態変数を更新することで、LaunchedEffectが再実行される
        deepLinkUri = intent.data
    }
}