package com.snoffee.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

// 각 스크린
import com.snoffee.app.presentation.home.HomeScreen
//import com.snoffee.app.presentation.caffeine.CaffeineMainScreen
//import com.snoffee.app.presentation.report.ReportScreen
//import com.snoffee.app.presentation.mySetting.MySettingScreen
//import com.snoffee.app.presentation.onboarding.OnboardingScreen

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
        //홈 화면
        composable(Screen.Home.route) {
            HomeScreen() // presentation.home.HomeScreen
        }

        //카페인 입력/목록 화면
//        composable(Screen.Caffeine.route) {
//            CaffeineMainScreen() // presentation.caffeine.CaffeineMainScreen
//        }
//
//        //리포트 화면
//        composable(Screen.Report.route) {
//            ReportScreen() // presentation.report.ReportScreen
//        }
//
//        //설정 화면
//        composable(Screen.MySetting.route) {
//            MySettingScreen() // presentation.mySetting.MySettingScreen
//        }
//
//        //온보딩
//        composable(Screen.Onboarding.route) {
//            OnboardingScreen()
//        }
    }
}