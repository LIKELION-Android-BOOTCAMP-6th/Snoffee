package com.snoffee.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.snoffee.app.core.navigation.AppNavHost
import com.snoffee.app.core.navigation.Screen
import com.snoffee.app.core.navigation.SnoffeeBottomBar
import com.snoffee.app.core.ui.component.SnoffeeAppBar
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnoffeeTheme {
                val navController = rememberNavController()

                //현재 경로 확인 (온보딩 등 특정 화면에서 앱바/탭바를 숨기기 위함)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                //앱 전체 레이아웃
                Scaffold(
                    topBar = {
                        // 온보딩 화면이 아닐 때만 상단 앱바 표시
                        if (currentRoute != Screen.Onboarding.route) {
                            SnoffeeAppBar(
                                onNotificationClick = {
                                    // 알림 아이콘 클릭 시 로직 (알림 화면 이동 등)
                                }
                            )
                        }
                    },
                    bottomBar = {
                        // 온보딩 화면이 아닐 때만 하단 탭바 표시
                        if (currentRoute != Screen.Onboarding.route) {
                            SnoffeeBottomBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    // 4. 중앙 콘텐츠 영역 (NavHost)
                    // innerPadding을 통해 콘텐츠가 앱바나 탭바에 가려지지 않도록 설정
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        // 초기 화면 설정 (실제 프로젝트에서는 데이터스토어 등을 통해 온보딩 완료 여부 확인 후 결정)
                        startDestination = Screen.Home.route
                    )
                }
            }
        }
    }
}