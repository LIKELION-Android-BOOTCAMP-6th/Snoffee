package com.snoffee.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.snoffee.app.core.navigation.AppNavHost
import com.snoffee.app.core.navigation.Screen
import com.snoffee.app.core.navigation.SnoffeeBottomBar
import com.snoffee.app.core.ui.component.SnoffeeAppBar
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import com.snoffee.app.data.datasource.preference.OnboardingPreferenceDataSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var onboardingPreferenceDataSource: OnboardingPreferenceDataSource
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnoffeeTheme {
                val navController = rememberNavController()

                val isOnboardingCompleted by onboardingPreferenceDataSource
                    .isOnboardingCompleted
                    .collectAsState(initial = null)

                if (isOnboardingCompleted == null) {
                    return@SnoffeeTheme
                }

                val startDestination =
                    if (isOnboardingCompleted == true) {
                        Screen.Home.route
                    } else {
                        Screen.Onboarding.route
                    }

                //현재 경로 확인 (온보딩 등 특정 화면에서 앱바/탭바를 숨기기 위함)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val topBarTitle = when (currentRoute) {
                    Screen.Home.route -> "Snoffee"
                    Screen.Caffeine.route -> "카페인 리포트"
                    Screen.Sleep.route -> "수면 리포트"
                    Screen.Report.route -> "리포트"
                    Screen.Setting.route -> "설정"
                    else -> "Snoffee"
                }

                // 앱바·탭바를 숨길 화면 목록
                val fullScreenRoutes = setOf(
                    Screen.Onboarding.route,
                    Screen.CaffeineSearch.route,
                )

                //앱 전체 레이아웃
                val isFullScreenRoute = currentRoute in fullScreenRoutes

                if (isFullScreenRoute) {
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier,
                        startDestination = startDestination
                    )
                } else {
                    Scaffold(
                        topBar = {
                            SnoffeeAppBar(
                                title = topBarTitle,
                                onNotificationClick = {
                                    // 알림 아이콘 클릭 시 로직
                                }
                            )
                        },
                        bottomBar = {
                            SnoffeeBottomBar(navController = navController)
                        },
                        containerColor = SnoffeeBgBase
                    ) { innerPadding ->
                        AppNavHost(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding),
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}