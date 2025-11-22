package com.example.smartcloset_frontend.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartcloset_frontend.R

@Composable
fun BottomNavBar(navController: NavHostController) {
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
            "test" to R.drawable.bug_line
        )

        items.forEach { (route, iconRes) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
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
        }
    }
}
