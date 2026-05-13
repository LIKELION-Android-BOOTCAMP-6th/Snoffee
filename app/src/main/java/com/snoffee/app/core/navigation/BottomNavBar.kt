package com.snoffee.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.unit.dp

@Composable
fun SnoffeeBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            BottomNavItem.Dashboard,
            BottomNavItem.Log,
            BottomNavItem.Analysis,
            BottomNavItem.Settings
        )

        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // 스택이 쌓이는 것을 방지하기 위해 시작 화면으로 팝업
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF4E342E),
                    selectedTextColor = Color(0xFF4E342E),
                    indicatorColor = Color(0xFFF5EBE0), // 선택 시 나타나는 연한 배경색
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

// 네비게이션 아이템 정의
sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem(Screen.Home.route, "Dashboard", Icons.Default.GridView)
    object Log : BottomNavItem(Screen.Caffeine.route, "Log", Icons.Default.AddCircleOutline)
    object Analysis : BottomNavItem(Screen.Report.route, "Analysis", Icons.Default.BarChart)
    object Settings : BottomNavItem(Screen.MySetting.route, "Settings", Icons.Default.Settings)
}