package com.example.smartcloset_frontend.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.R
import com.example.smartcloset_frontend.ui.ItemFormState
import com.example.smartcloset_frontend.viewmodel.AddItemViewModel

@Composable
fun BottomNavBar(
    navController: NavHostController,
    addItemViewModel: AddItemViewModel
) {
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

        var pendingRoute by remember { mutableStateOf<String?>(null) }
        var showLeaveDialog by remember { mutableStateOf(false) }

        val hasUnsaved = (currentRoute == "register") && (addItemViewModel.itemState != ItemFormState())


        items.forEach { (route, iconRes) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    if (route == "register") {
                        addItemViewModel.resetFormState()
                        navController.navigate("register") {
                            popUpTo("register") { inclusive = true }   // ★ここが効く
                            launchSingleTop = true
                            restoreState = false
                        }
                        return@NavigationBarItem
                    }
                    // アイテム登録画面に遷移する場合、フォームの状態をリセット
                    if (hasUnsaved) {
                        pendingRoute = route
                        showLeaveDialog = true
                        return@NavigationBarItem
                    }
                    // 通常通り指定された画面へ遷移
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true   // 画面と ViewModel の状態を保持
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = if (currentRoute == route) Color(0xFF558DD8) else Color.Unspecified
                    )
                }
            )
            if (showLeaveDialog) {
                AlertDialog(
                    onDismissRequest = { showLeaveDialog = false },
                    title = { Text("入力内容が破棄されます") },
                    text = { Text("このまま移動すると、入力中の内容は消えます。移動しますか？") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLeaveDialog = false
                            // 破棄して移動
                            addItemViewModel.resetFormState()
                            navController.navigate(pendingRoute ?: return@TextButton) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                                launchSingleTop = true
                                restoreState = false
                            }
                            pendingRoute = null
                        }) {
                            Text("移動する")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showLeaveDialog = false
                            pendingRoute = null
                        }) { Text("キャンセル") }
                    }
                )
            }
        }
    }
}
