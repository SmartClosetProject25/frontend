package com.example.smartcloset_frontend.navigation

import android.content.SharedPreferences
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.R

@Composable
fun BottomNavBar(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    }
    
    // 生成完了フラグを監視
    var hasNewGeneratedImage by remember { 
        mutableStateOf(sharedPreferences.getBoolean("has_new_generated_image", false))
    }
    
    // 生成中フラグを監視
    var isGeneratingImage by remember { 
        mutableStateOf(sharedPreferences.getBoolean("is_generating_image", false))
    }
    
    // SharedPreferencesの変更を監視
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "has_new_generated_image" -> {
                    hasNewGeneratedImage = sharedPreferences.getBoolean(key, false)
                }
                "is_generating_image" -> {
                    isGeneratingImage = sharedPreferences.getBoolean(key, false)
                }
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            "home" to R.drawable.wardrobe_line,
            "coordinate" to R.drawable.light_line,
            // register=登録
            "register" to R.drawable.add_square_line,
            "profile" to R.drawable.user_4_line,
            "settings" to R.drawable.settings_4_line,
            // 不要になったら消す(コメントアウトでも可)
//            "test" to R.drawable.bug_line
        )

        items.forEach { (route, iconRes) ->
            val showBadge = route == "coordinate" && hasNewGeneratedImage
            val isGenerating = route == "coordinate" && isGeneratingImage
            
            NavigationBarItem(
                selected = currentRoute == route,
                enabled = !isGenerating, // 生成中は無効化
                onClick = {
                    // 生成中は何もしない
                    if (isGenerating) return@NavigationBarItem
                    
                    // バッジが付いている場合は生成結果画面へ遷移
                    if (route == "coordinate" && hasNewGeneratedImage) {
                        // バッジを消す
                        sharedPreferences.edit().putBoolean("has_new_generated_image", false).apply()
                        hasNewGeneratedImage = false
                        
                        // 生成結果画面へ遷移
                        navController.navigate("generate") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        // 通常通り指定された画面へ遷移
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true   // 画面と ViewModel の状態を保持
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    }
                },
                icon = {
                    Box {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = if (currentRoute == route) Color(0xFF558DD8) else Color.Unspecified
                        )
                        // 生成中アニメーション
                        if (isGenerating) {
                            val infiniteTransition = rememberInfiniteTransition(label = "generating")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )
                            Badge(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x = 8.dp, y = (-8).dp),
                                containerColor = Color(0xFF558DD8).copy(alpha = alpha)
                            )
                        }
                        // 生成完了バッジ
                        else if (showBadge) {
                            Badge(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x = 8.dp, y = (-8).dp),
                                containerColor = Color(0xFFF44336)
                            )
                        }
                    }
                }
            )
        }
    }
}
