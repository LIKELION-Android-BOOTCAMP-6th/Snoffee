package com.snoffee.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.snoffee.app.presentation.caffeine.input.search.CaffeineSearchScreen
import com.snoffee.app.presentation.caffeine.main.CaffeineMainScreen
import com.snoffee.app.presentation.home.HomeScreen
import com.snoffee.app.presentation.report.ReportScreen
import com.snoffee.app.presentation.setting.SettingScreen
import com.snoffee.app.presentation.sleep.SleepScreen

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
            HomeScreen( // presentation.home.HomeScreen
                onAddCaffeineClick = {
                    //카페인 추가 버튼 -> 카페인 검색 화면으로 이동
                    navController.navigate(Screen.CaffeineSearch.route) {
                        // 기본 시작 화면(보통 Home)의 상위 스택을 모두 비워 탭 간 이동 시 화면이 쌓이지 않게 합니다.
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // 같은 화면이 연속으로 쌓이는 것을 방지
                        launchSingleTop = true
                        // 다른 탭으로 이동했다가 돌아왔을 때 이전 상태를 복원
                        restoreState = true
                    }
                },
                onViewAllClick = {
                    //최근 기록 우측 '전체보기' -> 카페인 탭 메인
                    navController.navigate(Screen.Caffeine.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        //카페인 입력/목록 화면
        composable(Screen.Caffeine.route) {
            CaffeineMainScreen {
                navController.navigate(Screen.CaffeineSearch.route)
            }
        }

        // 카페인 검색/추가 화면 [A-4-4]
        composable(Screen.CaffeineSearch.route) {
            CaffeineSearchScreen(
                onBack = { navController.popBackStack() },
                onConfirmSuccess = { navController.popBackStack() }, // 추가
                onNavigateToDirectInput = {}
            )
        }
        //수면 화면
        composable(Screen.Sleep.route) {
            SleepScreen() // presentation.sleep.SleepScreen
        }

        //리포트 화면
        composable(Screen.Report.route) {
            ReportScreen() // presentation.report.ReportScreen
        }

        //설정 화면
        composable(Screen.Setting.route) {
            SettingScreen() // presentation.mySetting.SettingScreen
        }

        //온보딩
        composable(Screen.Onboarding.route) {
//            OnboardingScreen()
        }
    }
}