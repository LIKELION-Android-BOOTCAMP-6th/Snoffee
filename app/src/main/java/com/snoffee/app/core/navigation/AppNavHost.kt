package com.snoffee.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.snoffee.app.presentation.caffeine.input.search.CaffeineInputScreen
import com.snoffee.app.presentation.caffeine.main.CaffeineMainScreen
import com.snoffee.app.presentation.home.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route // 실제로는 온보딩 여부에 따라 결정
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 홈 화면
        composable(Screen.Home.route) {
            HomeScreen() // presentation.home.HomeScreen
        }

        //카페인 입력/목록 화면
        composable(Screen.Caffeine.route) {
            CaffeineMainScreen {
                navController.navigate(Screen.CaffeineInput.route)
            }
        }

        // 카페인 검색/추가 화면 [A-4-4]
        composable(Screen.CaffeineInput.route) {
            CaffeineInputScreen(
                onBack = { navController.popBackStack() },
                onConfirmSuccess = { navController.popBackStack() }, // 추가
                onNavigateToDirectInput = {}
            )
        }

        //리포트 화면
        composable(Screen.Report.route) {
//            ReportScreen() // presentation.report.ReportScreen
        }

        //설정 화면
        composable(Screen.MySetting.route) {
//            MySettingScreen() // presentation.mySetting.MySettingScreen
        }

        //온보딩
        composable(Screen.Onboarding.route) {
//            OnboardingScreen()
        }
    }
}