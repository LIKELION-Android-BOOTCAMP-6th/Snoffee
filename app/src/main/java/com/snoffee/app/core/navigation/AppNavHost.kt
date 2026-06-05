package com.snoffee.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.snoffee.app.presentation.caffeine.input.search.CaffeineSearchScreen
import com.snoffee.app.presentation.caffeine.main.CaffeineMainScreen
import com.snoffee.app.presentation.home.HomeScreen
import com.snoffee.app.presentation.onboarding.OnboardingScreen
import com.snoffee.app.presentation.report.ReportScreen
import com.snoffee.app.presentation.setting.SettingScreen
import com.snoffee.app.presentation.sleep.SleepScreen
import java.time.LocalDate

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Onboarding.route // 실제로는 온보딩 여부에 따라 결정
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 온보딩 화면
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinishOnboarding = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        // 홈 화면
        composable(Screen.Home.route) {
            HomeScreen( // presentation.home.HomeScreen
                onAddCaffeineClick = {
                    //카페인 추가 버튼 -> 카페인 검색 화면으로 이동
                    navController.navigate(Screen.CaffeineSearch.createRoute(LocalDate.now())) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
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
            CaffeineMainScreen(
                onRecordClick = { selectedDate ->
                    navController.navigate(Screen.CaffeineSearch.createRoute(selectedDate))
                }
            )
        }

        // 카페인 검색/추가 화면 [A-4-4]
        composable(
            Screen.CaffeineSearch.route,
            arguments = listOf(
                navArgument("selectedDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString("selectedDate")
            val selectedDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

            CaffeineSearchScreen(
                selectedDate = selectedDate,
                onBack = { navController.popBackStack() },
                onConfirmSuccess = { navController.popBackStack() },
                onNavigateToDirectInput = {}
            )
        }

        //수면 화면
        composable(Screen.Sleep.route) {
            SleepScreen() // presentation.sleep.SleepScreen
        }

        //리포트 화면
        composable(Screen.Report.route) {
            ReportScreen( // presentation.report.ReportScreen
                onAddCaffeineClick = {
                    navController.navigate(Screen.CaffeineSearch.createRoute(LocalDate.now())) {
                        launchSingleTop = true
                    }
                }
            )
        }

        //설정 화면
//        composable(Screen.Setting.route) {
//            SettingScreen() // presentation.mySetting.SettingScreen
//        }

        // 온보딩 테스트를 위한 처리
        /*
        * todo :: 온보딩 작업이 정말 다 했다면 이 코드 지우고 위에 주석 해제할 것
        *  온보딩 추가 작업(헬스커넥트 연결방법 안내)이 끝나면 그때 제가 이 부분 코드 정리하겠습니다.
        *   - 제이 -
        */
        composable(Screen.Setting.route) {
            SettingScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route)
                }
            )
        }
    }
}