package com.snoffee.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.snoffee.app.presentation.home.HomeScreen
import com.snoffee.app.presentation.caffeine.CaffeineInputScreen

object Routes {
    // 변수명: UPPER_SNAKE_CASE, 값: snake_case (예: const val SLEEP_MAIN = "sleep_main")

    // TODO :: 온보딩

    // 메인 화면
    const val HOME = "home"

    // TODO :: 카페인
    const val CAFFEINE_INPUT = "caffeine_input"

    // TODO :: 수면

    // TODO :: 리포트

    // TODO :: 개인 설정
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToCaffeineInput = {
                    navController.navigate(Routes.CAFFEINE_INPUT)
                }
            )
        }

        composable(Routes.CAFFEINE_INPUT) {
            CaffeineInputScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: 온보딩 화면 추가
        // TODO: 수면 화면 추가
        // TODO: 리포트 화면 추가
        // TODO: 개인 설정 화면 추가
    }
}