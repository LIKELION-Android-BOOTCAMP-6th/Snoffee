package com.snoffee.app.core.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeNavBar
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled

@Composable
fun SnoffeeBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(
            Screen.Home.route,
            stringResource(R.string.main_tabBar_homeTitle),
            R.drawable.ic_main_bottombar_home
        ),
        Triple(
            Screen.Caffeine.route,
            stringResource(R.string.main_tabBar_caffeineTitle),
            R.drawable.ic_outline_coffee
        ),
        Triple(
            Screen.Sleep.route,
            stringResource(R.string.main_tabBar_sleepTitle),
            R.drawable.ic_main_bottombar_sleep
        ),
        Triple(
            Screen.Report.route,
            stringResource(R.string.main_tabBar_reportTitle),
            R.drawable.ic_main_bottombar_report
        ),
        Triple(
            Screen.MySetting.route,
            stringResource(R.string.main_tabBar_settingTitle),
            R.drawable.ic_main_bottombar_setting
        )
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
