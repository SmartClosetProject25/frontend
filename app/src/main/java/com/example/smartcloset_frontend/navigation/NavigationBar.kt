package com.example.smartcloset_frontend.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        val items = listOf("home", "coordinate", "register", "favorite", "settings")
        items.forEach { route ->
            NavigationBarItem(
                selected = false,
                onClick = {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                label = { Text(route.replaceFirstChar { it.uppercase() }) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) }
            )
        }
    }
}
