package com.snoffee.app.core.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.unit.dp
import com.snoffee.app.core.ui.theme.*
import androidx.compose.ui.res.painterResource
import com.snoffee.app.R

@Composable
fun SnoffeeBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(Screen.Home.route, "홈", R.drawable.main_bottombar_home),
        Triple(Screen.Caffeine.route, "카페인", R.drawable.main_bottombar_cafe),
        Triple(Screen.Sleep.route, "수면", R.drawable.main_bottombar_sleep),
        Triple(Screen.Report.route, "리포트", R.drawable.main_bottombar_report),
        Triple(Screen.MySetting.route, "설정", R.drawable.main_bottombar_setting)
    )

    NavigationBar(
        containerColor = SnoffeeNavBar,
        tonalElevation = 0.dp
    ) {
        items.forEach { (route, title, iconRes) ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title
                    )
                },
                label = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(route) {
                        // 스택이 쌓이는 것을 방지하기 위해 시작 화면으로 팝업
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SnoffeePrimaryDark,
                    selectedTextColor = SnoffeePrimaryDark,
                    indicatorColor = SnoffeePrimarySubtle, // 선택 시 나타나는 연한 배경색
                    unselectedIconColor = SnoffeeTextDisabled,
                    unselectedTextColor = SnoffeeTextDisabled
                )
            )
        }
    }
}
